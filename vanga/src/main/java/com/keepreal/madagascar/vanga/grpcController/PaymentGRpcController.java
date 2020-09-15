package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.ActivatePendingFeedPaymentRequest;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.CreatePaidFeedRequest;
import com.keepreal.madagascar.vanga.CreateWithdrawRequest;
import com.keepreal.madagascar.vanga.IOSOrderBuyShellRequest;
import com.keepreal.madagascar.vanga.IOSOrderSubscribeRequest;
import com.keepreal.madagascar.vanga.PaymentServiceGrpc;
import com.keepreal.madagascar.vanga.RedirectResponse;
import com.keepreal.madagascar.vanga.RefundWechatFeedRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportInfoRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportInfoResponse;
import com.keepreal.madagascar.vanga.RetrieveUserPaymentsRequest;
import com.keepreal.madagascar.vanga.RetrieveWechatOrderByIdRequest;
import com.keepreal.madagascar.vanga.SubscribeMembershipRequest;
import com.keepreal.madagascar.vanga.SupportMessage;
import com.keepreal.madagascar.vanga.SupportRequest;
import com.keepreal.madagascar.vanga.UserPaymentsResponse;
import com.keepreal.madagascar.vanga.WechatOrderBuyShellRequest;
import com.keepreal.madagascar.vanga.WechatOrderCallbackRequest;
import com.keepreal.madagascar.vanga.WechatOrderResponse;
import com.keepreal.madagascar.vanga.WithdrawPaymentsResponse;
import com.keepreal.madagascar.vanga.factory.BalanceMessageFactory;
import com.keepreal.madagascar.vanga.factory.PaymentMessageFactory;
import com.keepreal.madagascar.vanga.factory.WechatOrderMessageFactory;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import com.keepreal.madagascar.vanga.model.WechatOrderType;
import com.keepreal.madagascar.vanga.service.FeedService;
import com.keepreal.madagascar.vanga.service.MpWechatPayService;
import com.keepreal.madagascar.vanga.service.PaymentService;
import com.keepreal.madagascar.vanga.service.ShellService;
import com.keepreal.madagascar.vanga.service.SkuService;
import com.keepreal.madagascar.vanga.service.SubscribeMembershipService;
import com.keepreal.madagascar.vanga.service.SupportService;
import com.keepreal.madagascar.vanga.service.WechatOrderService;
import com.keepreal.madagascar.vanga.service.WechatPayService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import com.keepreal.madagascar.vanga.util.PaginationUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the payment grpc controller.
 */
@GRpcService
@Slf4j
public class PaymentGRpcController extends PaymentServiceGrpc.PaymentServiceImplBase {

    private final static String SUPPORT_TEXT = "夏日炎炎，请TA吃个椰子吧";
    private final static String MEMBERSHIP_TEMPLATE = "支持创作者 ¥%.2f（¥%.2f x %d个月）";
    private final static String SPONSOR_TEMPLATE = "支持一下创作者 ¥%.2f";

    private final FeedService feedService;
    private final PaymentService paymentService;
    private final WechatOrderService wechatOrderService;
    private final WechatPayService wechatPayService;
    private final MpWechatPayService mpWechatPayService;
    private final SkuService skuService;
    private final ShellService shellService;
    private final SubscribeMembershipService subscribeMembershipService;
    private final WechatOrderMessageFactory wechatOrderMessageFactory;
    private final BalanceMessageFactory balanceMessageFactory;
    private final PaymentMessageFactory paymentMessageFactory;
    private final SupportService supportService;

    /**
     * Constructs the payment grpc controller.
     *
     * @param feedService                {@link FeedService}.
     * @param paymentService             {@link PaymentService}.
     * @param wechatOrderService         {@link WechatOrderService}.
     * @param wechatPayService           {@link WechatPayService}.
     * @param mpWechatPayService         {@link MpWechatPayService}.
     * @param balanceMessageFactory      {@link BalanceMessageFactory}.
     * @param skuService                 {@link SkuService}.
     * @param shellService               {@link ShellService}.
     * @param wechatOrderMessageFactory  {@link WechatOrderMessageFactory}.
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     * @param paymentMessageFactory      {@link PaymentMessageFactory}.
     * @param supportService             {@link SupportService}.
     */
    public PaymentGRpcController(FeedService feedService,
                                 PaymentService paymentService,
                                 WechatOrderService wechatOrderService,
                                 WechatPayService wechatPayService,
                                 MpWechatPayService mpWechatPayService,
                                 BalanceMessageFactory balanceMessageFactory,
                                 SkuService skuService,
                                 ShellService shellService,
                                 WechatOrderMessageFactory wechatOrderMessageFactory,
                                 SubscribeMembershipService subscribeMembershipService,
                                 PaymentMessageFactory paymentMessageFactory,
                                 SupportService supportService) {
        this.feedService = feedService;
        this.paymentService = paymentService;
        this.wechatOrderService = wechatOrderService;
        this.wechatPayService = wechatPayService;
        this.mpWechatPayService = mpWechatPayService;
        this.balanceMessageFactory = balanceMessageFactory;
        this.skuService = skuService;
        this.shellService = shellService;
        this.wechatOrderMessageFactory = wechatOrderMessageFactory;
        this.subscribeMembershipService = subscribeMembershipService;
        this.paymentMessageFactory = paymentMessageFactory;
        this.supportService = supportService;
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
                WechatOrderType.PAYMEMBERSHIP,
                null,
                request.getIpAddress(),
                String.format(PaymentGRpcController.MEMBERSHIP_TEMPLATE,
                        sku.getPriceInCents().doubleValue() / 100,
                        sku.getPriceInCents().doubleValue() / sku.getTimeInMonths() / 100,
                        sku.getTimeInMonths()).replace(".00", ""));

        WechatOrderResponse response;
        if (Objects.nonNull(wechatOrder)) {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setWechatOrder(this.wechatOrderMessageFactory.valueOf(wechatOrder))
                    .build();
            this.paymentService.createNewWechatMembershipPayments(wechatOrder, sku);
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

        if (Objects.isNull(wechatOrder)) {
            WechatOrderResponse response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WECHAT_ORDER_NOT_FOUND_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        switch (WechatOrderType.fromValue(wechatOrder.getType())) {
            case PAYMEMBERSHIP:
            case PAYMEMBERSHIPH5:
                wechatOrder = this.wechatPayService.tryUpdateOrder(wechatOrder);
                this.subscribeMembershipService.subscribeMembershipWithWechatOrder(wechatOrder);
                break;
            case PAYSHELL:
                wechatOrder = this.mpWechatPayService.tryUpdateOrder(wechatOrder);
                this.shellService.buyShellWithWechat(wechatOrder, this.skuService.retrieveShellSkuById(wechatOrder.getPropertyId()));
                break;
            case PAYQUESTION:
                wechatOrder = this.wechatPayService.tryUpdateOrder(wechatOrder);
                this.feedService.confirmQuestionPaid(wechatOrder);
                break;
            case PAYSUPPORT:
            case PAYSUPPORTH5: {
                wechatOrder = this.wechatPayService.tryUpdateOrder(wechatOrder);
                this.supportService.supportWithWechatOrder(wechatOrder);
                break;
            }
            default:
        }

        WechatOrderResponse response = WechatOrderResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setWechatOrder(this.wechatOrderMessageFactory.valueOf(wechatOrder))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
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

        if (Objects.isNull(wechatOrder) || WechatOrderState.SUCCESS.getValue() != wechatOrder.getState()) {
            responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC));
            responseObserver.onCompleted();
            return;
        }

        switch (WechatOrderType.fromValue(wechatOrder.getType())) {
            case PAYMEMBERSHIP:
            case PAYMEMBERSHIPH5:
                this.subscribeMembershipService.subscribeMembershipWithWechatOrder(wechatOrder);
                break;
            case PAYSHELL:
                this.shellService.buyShellWithWechat(wechatOrder, this.skuService.retrieveShellSkuById(wechatOrder.getPropertyId()));
                break;
            case PAYQUESTION:
                this.feedService.confirmQuestionPaid(wechatOrder);
                break;
            case PAYSUPPORT:
            case PAYSUPPORTH5: {
                this.supportService.supportWithWechatOrder(wechatOrder);
                break;
            }
            default:
        }

        responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC));
        responseObserver.onCompleted();
    }

    /**
     * Implements the wechat order refund callback api.
     *
     * @param request          {@link WechatOrderCallbackRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void wechatRefundCallback(WechatOrderCallbackRequest request,
                                     StreamObserver<CommonStatus> responseObserver) {
        WechatOrder wechatOrder = this.wechatPayService.refundCallback(request.getPayload());

        if (Objects.isNull(wechatOrder) || WechatOrderState.REFUNDED.getValue() != wechatOrder.getState()) {
            responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC));
            responseObserver.onCompleted();
            return;
        }

        switch (WechatOrderType.fromValue(wechatOrder.getType())) {
            case PAYQUESTION: {
                Payment payment = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId()).stream().findFirst().orElse(null);
                if (Objects.isNull(payment)) {
                    return;
                }

                payment.setState(PaymentState.REFUNDED.getValue());
                this.paymentService.updateAll(Collections.singletonList(payment));
                return;
            }
            case PAYSHELL:
            case PAYMEMBERSHIP:
            default:
        }

        responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC));
        responseObserver.onCompleted();
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
            Balance balance = this.shellService.buyShell(request.getUserId(), request.getAppleReceipt(), request.getTransactionId(), sku);
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
     * Implements the ios subscribe membership api.
     *
     * @param request          {@link IOSOrderSubscribeRequest}.
     * @param responseObserver {@link CommonStatus}.
     */
    @Override
    public void iOSSubscribeMembership(IOSOrderSubscribeRequest request,
                                       io.grpc.stub.StreamObserver<CommonStatus> responseObserver) {
        MembershipSku sku = this.skuService.retrieveMembershipSkuById(request.getMembershipSkuId());
        CommonStatus response;
        if (Objects.isNull(sku)) {
            response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_IOS_ORDER_PLACE_ERROR);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        try {
            this.subscribeMembershipService.subscibeMembershipWithIOSOrder(request.getUserId(), request.getAppleReceipt(), request.getTransactionId(), sku);
            response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        } catch (KeepRealBusinessException exception) {
            response = CommonStatusUtils.buildCommonStatus(exception.getErrorCode());
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the wechat buy shell api.
     *
     * @param request          {@link WechatOrderBuyShellRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void wechatBuyShell(WechatOrderBuyShellRequest request,
                               StreamObserver<WechatOrderResponse> responseObserver) {
        ShellSku sku = this.skuService.retrieveShellSkuById(request.getShellSkuId());
        WechatOrderResponse response;
        if (Objects.isNull(sku)) {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        WechatOrder wechatOrder = this.mpWechatPayService.tryPlaceOrder(request.getUserId(),
                String.valueOf(sku.getPriceInCents()),
                sku.getId(),
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

    /**
     * Implements the retrieve user payments rpc.
     *
     * @param request          {@link RetrieveUserPaymentsRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveUserPayments(RetrieveUserPaymentsRequest request,
                                     StreamObserver<UserPaymentsResponse> responseObserver) {
        Page<Payment> paymentPage = this.paymentService.retrievePaymentsByUserId(request.getUserId(), PaginationUtils.valueOf(request.getPageRequest(), "created_time"));

        List<String> membershipSkuIds = paymentPage.getContent().stream()
                .map(Payment::getMembershipSkuId)
                .filter(membershipId -> !StringUtils.isEmpty(membershipId))
                .collect(Collectors.toList());

        Map<String, MembershipSku> membershipSkuMap = this.skuService.retrieveMembershipSkusByIds(membershipSkuIds).stream()
                .collect(Collectors.toMap(MembershipSku::getId, Function.identity(), (m1, m2) -> m1, HashMap::new));

        UserPaymentsResponse response = UserPaymentsResponse.newBuilder()
                .addAllUserPayments(paymentPage.getContent().stream()
                        .map(payment -> this.paymentMessageFactory.valueOf(payment, membershipSkuMap.getOrDefault(payment.getMembershipSkuId(), null)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setPageResponse(PaginationUtils.valueOf(paymentPage, request.getPageRequest()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the user withdraw history.
     *
     * @param request           {@link RetrieveUserPaymentsRequest}.
     * @param responseObserver  {@link WithdrawPaymentsResponse}.
     */
    @Override
    public void retrieveUserWithdraws(RetrieveUserPaymentsRequest request,
                                      StreamObserver<WithdrawPaymentsResponse> responseObserver) {
        Page<Payment> paymentPage = this.paymentService.retrieveWithdrawsByUserId(request.getUserId(), PaginationUtils.valueOf(request.getPageRequest()));

        WithdrawPaymentsResponse response = WithdrawPaymentsResponse.newBuilder()
                .addAllUserWithdraws(paymentPage.getContent().stream()
                        .map(this.paymentMessageFactory::withdrawValueOf)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setPageResponse(PaginationUtils.valueOf(paymentPage, request.getPageRequest()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the feed creation with wechat pay.
     * Note: the creator pays with wechat in order to create successfully.
     *
     * @param request          {@link CreatePaidFeedRequest}.
     * @param responseObserver {@link WechatOrderResponse}.
     */
    @Override
    public void wechatCreateFeed(CreatePaidFeedRequest request,
                                 StreamObserver<WechatOrderResponse> responseObserver) {
        WechatOrder wechatOrder = this.wechatPayService.tryPlaceOrder(request.getUserId(),
                String.valueOf(request.getPriceInCents()),
                request.getFeedId(),
                WechatOrderType.PAYQUESTION,
                null,
                request.getIpAddress(),
                "");

        WechatOrderResponse response;
        if (Objects.nonNull(wechatOrder)) {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setWechatOrder(this.wechatOrderMessageFactory.valueOf(wechatOrder))
                    .build();
            this.paymentService.createNewWechatQuestionFeedPayment(wechatOrder, request.getHostId(), request.getPriceInCents());
        } else {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the feed question refund wechat pay.
     *
     * @param request          {@link CreatePaidFeedRequest}.
     * @param responseObserver {@link WechatOrderResponse}.
     */
    @Override
    public void refundWechatPaidFeed(RefundWechatFeedRequest request,
                                     StreamObserver<CommonStatus> responseObserver) {
        CommonStatus response;
        try {
            this.feedService.refundQuestionPaid(request.getFeedId(), request.getUserId());
            response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        } catch (KeepRealBusinessException exception) {
            response = CommonStatusUtils.buildCommonStatus(exception.getErrorCode());
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the feed question answered to flip payment state.
     *
     * @param request          {@link ActivatePendingFeedPaymentRequest}.
     * @param responseObserver {@link CommonStatus}.
     */
    @Override
    public void activateFeedPayment(ActivatePendingFeedPaymentRequest request,
                                    StreamObserver<CommonStatus> responseObserver) {
        CommonStatus response;
        try {
            this.feedService.activatePayment(request.getFeedId(), request.getUserId());
            response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        } catch (KeepRealBusinessException exception) {
            response = CommonStatusUtils.buildCommonStatus(exception.getErrorCode());
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the H5 payment api.
     *
     * @param request          {@link SubscribeMembershipRequest}.
     * @param responseObserver {@link RedirectResponse}.
     */
    @Override
    public void submitSubscribeMembershipWithWechatPayH5(SubscribeMembershipRequest request,
                                                         StreamObserver<RedirectResponse> responseObserver) {
        MembershipSku sku = this.skuService.retrieveMembershipSkuById(request.getMembershipSkuId());
        if (Objects.isNull(sku)) {
            RedirectResponse response = RedirectResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        WechatOrder wechatOrder = this.wechatPayService.tryPlaceOrder(request.getUserId(),
                String.valueOf(sku.getPriceInCents()),
                sku.getId(),
                WechatOrderType.PAYMEMBERSHIPH5,
                request.getSceneType(),
                request.getIpAddress(),
                String.format(PaymentGRpcController.MEMBERSHIP_TEMPLATE,
                        sku.getPriceInCents().doubleValue() / 100,
                        sku.getPriceInCents().doubleValue() / sku.getTimeInMonths() / 100,
                        sku.getTimeInMonths()).replace(".00", ""));

        RedirectResponse response;
        if (Objects.nonNull(wechatOrder)) {
            response = RedirectResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setRedirectUrl(wechatOrder.getMwebUrl())
                    .setOrderId(wechatOrder.getId())
                    .build();
            this.paymentService.createNewWechatMembershipPayments(wechatOrder, sku);
        } else {
            response = RedirectResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveSupportInfo(RetrieveSupportInfoRequest request, StreamObserver<RetrieveSupportInfoResponse> responseObserver) {
        String hostId = request.getHostId();
        int count = this.paymentService.supportCount(hostId);

        responseObserver.onNext(RetrieveSupportInfoResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setMessage(SupportMessage.newBuilder()
                        .setCount(count)
                        .setText(SUPPORT_TEXT)
                        .build())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void submitSupportWithWechatPay(SupportRequest request, StreamObserver<WechatOrderResponse> responseObserver) {
        WechatOrder wechatOrder = this.wechatPayService.tryPlaceOrder(request.getUserId(),
                String.valueOf(request.getPriceInCents()),
                request.getSponsorSkuId(),
                WechatOrderType.PAYSUPPORT,
                null,
                request.getIpAddress(),
                String.format(PaymentGRpcController.SPONSOR_TEMPLATE,
                        Long.valueOf(request.getPriceInCents()).doubleValue() / 100).replace(".00", ""));

        WechatOrderResponse response;
        if (Objects.nonNull(wechatOrder)) {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setWechatOrder(this.wechatOrderMessageFactory.valueOf(wechatOrder))
                    .build();
            this.paymentService.createNewWechatSupportPayment(wechatOrder, request.getPayeeId(), request.getPriceInCents());
        } else {
            response = WechatOrderResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void submitSupportWithWechatPayH5(SupportRequest request, StreamObserver<RedirectResponse> responseObserver) {
        WechatOrder wechatOrder = this.wechatPayService.tryPlaceOrder(request.getUserId(),
                String.valueOf(request.getPriceInCents()),
                request.getSponsorSkuId(),
                WechatOrderType.PAYSUPPORTH5,
                request.getSceneType(),
                request.getIpAddress(),
                String.format(PaymentGRpcController.SPONSOR_TEMPLATE,
                        Long.valueOf(request.getPriceInCents()).doubleValue() / 100).replace(".00", ""));

        RedirectResponse response;
        if (Objects.nonNull(wechatOrder)) {
            response = RedirectResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setRedirectUrl(wechatOrder.getMwebUrl())
                    .setOrderId(wechatOrder.getId())
                    .build();
            this.paymentService.createNewWechatSupportPayment(wechatOrder, request.getPayeeId(), request.getPriceInCents());
        } else {
            response = RedirectResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
