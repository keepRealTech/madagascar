package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.CreateWithdrawRequest;
import com.keepreal.madagascar.vanga.IOSOrderBuyShellRequest;
import com.keepreal.madagascar.vanga.PaymentServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveWechatOrderByIdRequest;
import com.keepreal.madagascar.vanga.SubscribeMembershipRequest;
import com.keepreal.madagascar.vanga.WechatOrderBuyShellRequest;
import com.keepreal.madagascar.vanga.WechatOrderCallbackRequest;
import com.keepreal.madagascar.vanga.WechatOrderResponse;
import com.keepreal.madagascar.vanga.factory.BalanceMessageFactory;
import com.keepreal.madagascar.vanga.factory.WechatOrderMessageFactory;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.service.PaymentService;
import com.keepreal.madagascar.vanga.service.ShellService;
import com.keepreal.madagascar.vanga.service.SkuService;
import com.keepreal.madagascar.vanga.service.SubscribeMembershipService;
import com.keepreal.madagascar.vanga.service.WechatOrderService;
import com.keepreal.madagascar.vanga.service.WechatPayService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Represents the payment grpc controller.
 */
@GRpcService
@Slf4j
public class PaymentGRpcController extends PaymentServiceGrpc.PaymentServiceImplBase {

    private final PaymentService paymentService;
    private final WechatOrderService wechatOrderService;
    private final WechatPayService wechatPayService;
    private final BalanceMessageFactory balanceMessageFactory;
    private final SkuService skuService;
    private final ShellService shellService;
    private final WechatOrderMessageFactory wechatOrderMessageFactory;
    private final SubscribeMembershipService subscribeMembershipService;

    /**
     * Constructs the payment grpc controller.
     *
     * @param paymentService             {@link PaymentService}.
     * @param wechatOrderService         {@link WechatOrderService}.
     * @param wechatPayService           {@link WechatPayService}.
     * @param balanceMessageFactory      {@link BalanceMessageFactory}.
     * @param skuService                 {@link SkuService}.
     * @param shellService               {@link ShellService}.
     * @param wechatOrderMessageFactory  {@link WechatOrderMessageFactory}.
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     */
    public PaymentGRpcController(PaymentService paymentService,
                                 WechatOrderService wechatOrderService,
                                 WechatPayService wechatPayService,
                                 BalanceMessageFactory balanceMessageFactory,
                                 SkuService skuService,
                                 ShellService shellService,
                                 WechatOrderMessageFactory wechatOrderMessageFactory,
                                 SubscribeMembershipService subscribeMembershipService) {
        this.paymentService = paymentService;
        this.wechatOrderService = wechatOrderService;
        this.wechatPayService = wechatPayService;
        this.balanceMessageFactory = balanceMessageFactory;
        this.skuService = skuService;
        this.shellService = shellService;
        this.wechatOrderMessageFactory = wechatOrderMessageFactory;
        this.subscribeMembershipService = subscribeMembershipService;
    }

    /**
     * Implements the create withdraw request.
     *
     * @param request          {@link CreateWithdrawRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void createWithdrawPayment(CreateWithdrawRequest request,
                                      StreamObserver<BalanceResponse> responseObserver) {
        BalanceResponse response = null;
        try {
            Balance balance = this.paymentService.createWithdrawPayment(request.getUserId(), request.getWithdrawAmountInCents());
            response = BalanceResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setBalance(this.balanceMessageFactory.valueOf(balance))
                    .build();
        } catch (KeepRealBusinessException exception) {
            response = BalanceResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(exception.getErrorCode()))
                    .build();
        } finally {
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * Implements the create wechat pay request.
     *
     * @param request          {@link SubscribeMembershipRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void submitSubscribeMembershipWithWechatPay(SubscribeMembershipRequest request,
                                                       StreamObserver<WechatOrderResponse> responseObserver) {
        MembershipSku sku = this.skuService.retrieveMembershipSkuById(request.getMembershipSkuId());
        if (Objects.isNull(sku)) {
            WechatOrderResponse response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        WechatOrder wechatOrder = this.wechatPayService.tryPlaceOrder(request.getUserId(),
                String.valueOf(sku.getPriceInCents()),
                sku.getId(),
                "",
                "APP",
                "");

        WechatOrderResponse response;
        if (Objects.nonNull(wechatOrder)) {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setWechatOrder(this.wechatOrderMessageFactory.valueOf(wechatOrder))
                    .build();
            this.paymentService.createNewWechatPayments(wechatOrder, sku);
        } else {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the wechat order check and retrieve.
     *
     * @param request          {@link RetrieveWechatOrderByIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveWechatOrderById(RetrieveWechatOrderByIdRequest request,
                                        StreamObserver<WechatOrderResponse> responseObserver) {
        WechatOrder wechatOrder = this.wechatOrderService.retrieveById(request.getId());

        wechatOrder = this.wechatPayService.tryUpdateOrder(wechatOrder);

        WechatOrderResponse response = WechatOrderResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setWechatOrder(this.wechatOrderMessageFactory.valueOf(wechatOrder))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        if (StringUtils.isEmpty(wechatOrder.getShellSkuId())) {
            this.subscribeMembershipService.subscribeMembershipWithWechatOrder(wechatOrder);
        } else {
            this.shellService.buyShellWithWechat(wechatOrder, this.skuService.retrieveShellSkuById(wechatOrder.getShellSkuId()));
        }
    }

    /**
     * Implements the wechat order pay callback api.
     *
     * @param request          {@link WechatOrderCallbackRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void wechatPayCallback(WechatOrderCallbackRequest request,
                                  StreamObserver<CommonStatus> responseObserver) {
        WechatOrder wechatOrder = this.wechatPayService.orderCallback(request.getPayload());

        if (StringUtils.isEmpty(wechatOrder.getShellSkuId())) {
            this.subscribeMembershipService.subscribeMembershipWithWechatOrder(wechatOrder);
        } else {
            this.shellService.buyShellWithWechat(wechatOrder, this.skuService.retrieveShellSkuById(wechatOrder.getShellSkuId()));
        }
    }

    /**
     * Implements the shell pay api.
     *
     * @param request          {@link SubscribeMembershipRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void subscribeMembershipWithShell(SubscribeMembershipRequest request,
                                             StreamObserver<CommonStatus> responseObserver) {
        MembershipSku sku = this.skuService.retrieveMembershipSkuById(request.getMembershipSkuId());
        CommonStatus response;
        if (Objects.isNull(sku)) {
            response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        try {
            this.subscribeMembershipService.subscribeMembershipWithShell(request.getUserId(), sku);
            response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        } catch (KeepRealBusinessException exception) {
            response = CommonStatusUtils.buildCommonStatus(exception.getErrorCode());
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the ios buy shell api.
     *
     * @param request          {@link IOSOrderBuyShellRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void iOSBuyShell(IOSOrderBuyShellRequest request,
                            StreamObserver<BalanceResponse> responseObserver) {
        ShellSku sku = this.skuService.retrieveShellSkuById(request.getShellSkuId());
        BalanceResponse response;
        if (Objects.isNull(sku)) {
            response = BalanceResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_IOS_ORDER_PLACE_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        try {
            Balance balance = this.shellService.buyShell(request.getUserId(), request.getAppleReceipt(), sku);
            response = BalanceResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setBalance(this.balanceMessageFactory.valueOf(balance))
                    .build();
        } catch (KeepRealBusinessException exception) {
            response = BalanceResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(exception.getErrorCode()))
                    .build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the wechat buy shell api.
     *
     * @param request           {@link WechatOrderBuyShellRequest}.
     * @param responseObserver  {@link StreamObserver}.
     */
    @Override
    public void wechatBuyShell(WechatOrderBuyShellRequest request,
                               StreamObserver<WechatOrderResponse> responseObserver) {
        ShellSku sku = this.skuService.retrieveShellSkuById(request.getShellSkuId());
        WechatOrderResponse response ;
        if (Objects.isNull(sku)) {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        WechatOrder wechatOrder = this.wechatPayService.tryPlaceOrder(request.getUserId(),
                String.valueOf(sku.getPriceInCents()),
                "",
                sku.getId(),
                "JSAPI",
                request.getOpenId());

        if (Objects.nonNull(wechatOrder)) {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setWechatOrder(this.wechatOrderMessageFactory.valueOf(wechatOrder))
                    .build();
            this.paymentService.createWechatBuyShellPayments(wechatOrder, sku);
        } else {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
