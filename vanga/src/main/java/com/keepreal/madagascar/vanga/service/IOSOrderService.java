package com.keepreal.madagascar.vanga.service;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.config.IOSPayConfiguration;
import com.keepreal.madagascar.vanga.model.IosOrder;
import com.keepreal.madagascar.vanga.model.IosOrderState;
import com.keepreal.madagascar.vanga.repository.IosOrderRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the ios order service.
 */
@Service
public class IOSOrderService {

    private final IosOrderRepository iosOrderRepository;
    private final LongIdGenerator idGenerator;
    private final RestTemplate restTemplate;
    private final IOSPayConfiguration iosPayConfiguration;

    /**
     * Constructs the ios order service.
     *
     * @param restTemplate        {@link RestTemplate}.
     * @param iosPayConfiguration {@link IOSPayConfiguration}.
     */
    public IOSOrderService(RestTemplate restTemplate,
                           IOSPayConfiguration iosPayConfiguration,
                           LongIdGenerator idGenerator,
                           IosOrderRepository iosOrderRepository) {
        this.restTemplate = restTemplate;
        this.iosPayConfiguration = iosPayConfiguration;
        this.idGenerator = idGenerator;
        this.iosOrderRepository = iosOrderRepository;
    }

    /**
     * Verifies the receipt is valid.
     *
     * @param receipt       Receipt content.
     * @param appleSkuId    Apple sku id.
     * @param description   Description.
     * @param transactionId Transaction id.
     * @return Transaction id.
     */
    public IosOrder verify(String userId, String receipt, String description, String appleSkuId, String skuId, String transactionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("receipt-data", receipt);
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        IosOrder.IosOrderBuilder builder = IosOrder.builder().userId(userId).receiptHashcode(String.valueOf(receipt.hashCode()))
                .description(String.format("购买%s", description)).skuId(skuId);

        IosOrder iosOrder = this.createIosOrder(builder);

        ResponseEntity<String> response = this.restTemplate.postForEntity(this.iosPayConfiguration.getVerifyUrl(),
                request, String.class);

        if (response.getStatusCode().isError()) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_IOS_RECEIPT_VERIFY_ERROR, response.getStatusCode().name());
        }

        JSONObject responseData = JSONObject.parseObject(response.getBody());
        String status = responseData.getString("status");

        if (status.equals("21007") && this.iosPayConfiguration.getEnableSandbox()) {
            response = this.restTemplate.postForEntity(this.iosPayConfiguration.getVerifyUrlSandbox(),
                    request, String.class);
            if (response.getStatusCode().isError()) {
                throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_IOS_RECEIPT_VERIFY_ERROR, response.getStatusCode().name());
            }
            responseData = JSONObject.parseObject(response.getBody());
            status = responseData.getString("status");
        }

        iosOrder = this.updateIosOrderErrorMsgByStatus(iosOrder, status);

        if (!status.equals("0")) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_IOS_RECEIPT_VERIFY_ERROR, status);
        }

        List<HashMap> inApps = JSONObject.parseArray(
                JSONObject.parseObject(responseData.getString("receipt")).getString("in_app"), HashMap.class);

        if (!StringUtils.isEmpty(transactionId)) {
            HashMap dictionary = inApps.stream()
                    .filter(app -> transactionId.equals(app.get("transaction_id")) && appleSkuId.equals(app.get("product_id")))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(dictionary)) {
                iosOrder.setTransactionId(dictionary.get("transaction_id").toString());
                return iosOrder;
            }
        }

        Map<String, HashMap> dictionary = inApps.stream()
                .collect(Collectors.toMap(app -> app.get("product_id").toString(), Function.identity()));

        if (dictionary.containsKey(appleSkuId)) {
            iosOrder.setTransactionId(dictionary.get(appleSkuId).get("transaction_id").toString());
            return iosOrder;
        }

        throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_SHELL_IOS_RECEIPT_INVALID_ERROR);
    }

    private IosOrder createIosOrder(IosOrder.IosOrderBuilder builder) {
        IosOrder iosOrder = builder.id(String.valueOf(idGenerator.nextId())).build();
        return iosOrderRepository.save(iosOrder);
    }

    private IosOrder updateIosOrderErrorMsgByStatus(IosOrder iosOrder, String status) {
        switch (status) {
            case "21000":
                iosOrder.setState(IosOrderState.PAYERROR.getValue());
                iosOrder.setErrorMessage("App Store不能读取你提供的JSON对象");
                break;
            case "21002":
                iosOrder.setState(IosOrderState.PAYERROR.getValue());
                iosOrder.setErrorMessage("receipt-data属性中的数据格式错误或丢失");
                break;
            case "21003":
                iosOrder.setState(IosOrderState.PAYERROR.getValue());
                iosOrder.setErrorMessage("receipt无法通过验证");
                break;
            case "21004":
                iosOrder.setState(IosOrderState.PAYERROR.getValue());
                iosOrder.setErrorMessage("提供的共享密码与帐户的文件共享密码不匹配");
                break;
            case "21005":
                iosOrder.setState(IosOrderState.PAYERROR.getValue());
                iosOrder.setErrorMessage("receipt服务器当前不可用");
                break;
            case "21006":
                iosOrder.setState(IosOrderState.PAYERROR.getValue());
                iosOrder.setErrorMessage("该收据有效，但订阅已过期，当此状态代码返回到您的服务器时，收据数据也会被解码并作为响应的一部分返回，仅针对自动续订的iOS 6样式交易收据返回");
                break;
            case "21007":
                iosOrder.setState(IosOrderState.PAYERROR.getValue());
                iosOrder.setErrorMessage("receipt是Sandbox receipt，但却发送至生产系统的验证服务");
                break;
            case "21008":
                iosOrder.setState(IosOrderState.PAYERROR.getValue());
                iosOrder.setErrorMessage("receipt是生产receipt，但却发送至Sandbox环境的验证服务");
                break;
            case "21010":
                iosOrder.setState(IosOrderState.NOTPAY.getValue());
                iosOrder.setErrorMessage("此收据无法授权，就像从未进行过购买一样对待");
                break;
            case "0":
                iosOrder.setState(IosOrderState.SUCCESS.getValue());
                break;
            default:
                iosOrder.setState(IosOrderState.UNKNOWN.getValue());
        }
        return this.iosOrderRepository.save(iosOrder);
    }

}
