package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.CreateWithdrawRequest;
import com.keepreal.madagascar.vanga.PaymentServiceGrpc;
import com.keepreal.madagascar.vanga.SubscribeMembershipRequest;
import com.keepreal.madagascar.vanga.WechatOrderResponse;
import com.keepreal.madagascar.vanga.factory.BalanceMessageFactory;
import com.keepreal.madagascar.vanga.factory.WechatOrderMessageFactory;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.service.PaymentService;
import com.keepreal.madagascar.vanga.service.SkuService;
import com.keepreal.madagascar.vanga.service.WechatPayService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * Represents the payment grpc controller.
 */
@GRpcService
public class PaymentGRpcController extends PaymentServiceGrpc.PaymentServiceImplBase {

    private final PaymentService paymentService;
    private final WechatPayService wechatPayService;
    private final BalanceMessageFactory balanceMessageFactory;
    private final SkuService skuService;
    private final WechatOrderMessageFactory wechatOrderMessageFactory;

    /**
     * Constructs the payment grpc controller.
     *
     * @param paymentService            {@link PaymentService}.
     * @param wechatPayService          {@link WechatPayService}.
     * @param balanceMessageFactory     {@link BalanceMessageFactory}.
     * @param skuService                {@link SkuService}.
     * @param wechatOrderMessageFactory {@link WechatOrderMessageFactory}.
     */
    public PaymentGRpcController(PaymentService paymentService,
                                 WechatPayService wechatPayService,
                                 BalanceMessageFactory balanceMessageFactory,
                                 SkuService skuService,
                                 WechatOrderMessageFactory wechatOrderMessageFactory) {
        this.paymentService = paymentService;
        this.wechatPayService = wechatPayService;
        this.balanceMessageFactory = balanceMessageFactory;
        this.skuService = skuService;
        this.wechatOrderMessageFactory = wechatOrderMessageFactory;
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
        WechatOrder wechatOrder = this.wechatPayService.tryPlaceOrder(request.getUserId(),
                String.valueOf(sku.getPriceInCents()),
                String.format("购买会员%s", sku.getId()));
        WechatOrderResponse response = WechatOrderResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setWechatOrder(this.wechatOrderMessageFactory.valueOf(wechatOrder))
                .build();
        this.paymentService.createNewWechatPayments(wechatOrder, sku);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
