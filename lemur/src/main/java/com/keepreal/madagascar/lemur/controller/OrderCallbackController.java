package com.keepreal.madagascar.lemur.controller;

import com.google.gson.Gson;
import com.keepreal.madagascar.lemur.service.OrderService;
import com.keepreal.madagascar.lemur.util.WXPayConstants;
import com.keepreal.madagascar.lemur.util.WXPayUtil;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents the wechat pay order callback controller.
 */
@Controller
@Slf4j
public class OrderCallbackController {

    private final String successResponseXml;
    private final OrderService orderService;
    private final Gson gson = new Gson();

    /**
     * Constructs the wechat pay callback controller.
     *
     * @param orderService {@link OrderService}.
     */
    @SneakyThrows
    public OrderCallbackController(OrderService orderService) {
        this.orderService = orderService;
        Map<String, String> response = new HashMap<>();
        response.put("return_code", WXPayConstants.SUCCESS);
        response.put("return_msg", "OK");
        this.successResponseXml = WXPayUtil.mapToXml(response);
    }

    /**
     * Implements the wechat order callback api.
     *
     * @param request  {@link HttpServletRequest}.
     * @param response {@link HttpServletResponse}.
     */
    @RequestMapping(value = "/api/v1/orders/wechat/callback", method = RequestMethod.POST)
    public void apiV1OrdersWechatCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            String requestXml = IOUtils.toString(request.getInputStream(), Charset.forName(request.getCharacterEncoding()));

            log.warn(requestXml);

            this.orderService.wechatOrderCallback(requestXml);

            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            out.write(this.successResponseXml.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Implements the wechat refund order callback api.
     *
     * @param request  {@link HttpServletRequest}.
     * @param response {@link HttpServletResponse}.
     */
    @RequestMapping(value = "/api/v1/orders/wechat/refund/callback", method = RequestMethod.POST)
    public void apiV1OrdersWechatRefundCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            String requestXml = IOUtils.toString(request.getInputStream(), Charset.forName(request.getCharacterEncoding()));
            this.orderService.wechatOrderRefundCallback(requestXml);

            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            out.write(this.successResponseXml.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Implements the alipay order callback api.
     *
     * @param request {@link HttpServletRequest}.
     */
    @RequestMapping(value = "/api/v1/orders/alipay/callback", method = RequestMethod.POST)
    @ResponseBody
    public String apiV1OrdersAlipayCallback(HttpServletRequest request) {
        Map<String, String> retMap = new HashMap<>();
        Set<Map.Entry<String, String[]>> entrySet = request.getParameterMap().entrySet();

        for (Map.Entry<String, String[]> entry : entrySet) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            int valLen = values.length;

            if (valLen == 1) {
                retMap.put(name, values[0]);
            } else if (valLen > 1) {
                StringBuilder sb = new StringBuilder();
                for (String val : values) {
                    sb.append(",").append(val);
                }
                retMap.put(name, sb.toString().substring(1));
            } else {
                retMap.put(name, "");
            }
        }

        String payload = this.gson.toJson(retMap);

        log.warn(payload);

        this.orderService.alipayOrderCallback(payload);

        return "success";
    }

}
