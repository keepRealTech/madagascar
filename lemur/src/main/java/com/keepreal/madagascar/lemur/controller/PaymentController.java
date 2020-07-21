package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.lemur.dtoFactory.PaymentDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.WechatOrderDTOFactory;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.PaymentService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.vanga.MembershipSkuMessage;
import com.keepreal.madagascar.vanga.UserPaymentMessage;
import com.keepreal.madagascar.vanga.WechatOrderMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.PaymentApi;
import swagger.model.DummyResponse;
import swagger.model.SubscribeMemberRequest;
import swagger.model.UserPaymentsResponse;
import swagger.model.WechatOrderResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the payment controller.
 */
@RestController
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;
    private final UserService userService;
    private final MembershipService membershipService;
    private final WechatOrderDTOFactory wechatOrderDTOFactory;
    private final PaymentDTOFactory paymentDTOFactory;

    /**
     * Constructs the payment controller.
     * @param paymentService        {@link PaymentService}.
     * @param userService
     * @param membershipService
     * @param wechatOrderDTOFactory {@link WechatOrderDTOFactory}.
     * @param paymentDTOFactory
     */
    public PaymentController(PaymentService paymentService,
                             UserService userService,
                             MembershipService membershipService,
                             WechatOrderDTOFactory wechatOrderDTOFactory,
                             PaymentDTOFactory paymentDTOFactory) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.membershipService = membershipService;
        this.wechatOrderDTOFactory = wechatOrderDTOFactory;
        this.paymentDTOFactory = paymentDTOFactory;
    }

    /**
     * Implements the subscribe with shell balance post api.
     *
     * @param id                     id (required) Islands id.
     * @param subscribeMemberRequest (required) {@link SubscribeMemberRequest}.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1IslandsIdMemberSubscriptionShellPayPost(String id,
                                                                                      SubscribeMemberRequest subscribeMemberRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        this.paymentService.subscribeMembershipWithShell(userId, subscribeMemberRequest.getMembershipSkuId());

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the subscribe with wechat pay post api.
     *
     * @param id                     id (required) Islands id.
     * @param subscribeMemberRequest (required) {@link SubscribeMemberRequest}.
     * @return {@link WechatOrderResponse}.
     */
    @Override
    public ResponseEntity<WechatOrderResponse> apiV1IslandsIdMemberSubscriptionWechatPayPost(String id,
                                                                                             SubscribeMemberRequest subscribeMemberRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        WechatOrderMessage wechatOrderMessage = this.paymentService.submitSubscribeMembershipWithWechatPay(userId,
                subscribeMemberRequest.getMembershipSkuId());

        WechatOrderResponse response = new WechatOrderResponse();
        response.setData(this.wechatOrderDTOFactory.valueOf(wechatOrderMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get user payment history api.
     *
     * @param page page number (optional, default to 0).
     * @param pageSize size of a page (optional, default to 10).
     * @return {@link UserPaymentsResponse}.
     */
    @Override
    public ResponseEntity<UserPaymentsResponse> apiV1PaymentsGet(Integer page, Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();

        com.keepreal.madagascar.vanga.UserPaymentsResponse userPaymentsResponse =
                this.paymentService.retrieveUserPayments(userId, page, pageSize);

        if (userPaymentsResponse.getUserPaymentsList().isEmpty()) {
            UserPaymentsResponse response = new UserPaymentsResponse();
            response.setData(new ArrayList<>());
            response.setPageInfo(PaginationUtils.getPageInfo(userPaymentsResponse.getPageResponse()));
            response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
            response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Set<String> hostIds = userPaymentsResponse.getUserPaymentsList().stream()
                .map(UserPaymentMessage::getPayeeId)
                .collect(Collectors.toSet());
        Map<String, UserMessage> userMessageMap = this.userService.retrieveUsersByIds(hostIds).stream()
                .collect(Collectors.toMap(UserMessage::getId, Function.identity(), (u1, u2) -> u1, HashMap::new));

        Set<String> membershipIds = userPaymentsResponse.getUserPaymentsList().stream()
                .map(payment -> payment.getMembershipSku().getMembershipId())
                .collect(Collectors.toSet());
        Map<String, MembershipMessage> membershipMessageMap = this.membershipService.retrieveMembershipsByIds(membershipIds).stream()
                .collect(Collectors.toMap(MembershipMessage::getId, Function.identity(), (u1, u2) -> u1, HashMap::new));

        UserPaymentsResponse response = new UserPaymentsResponse();
        response.setData(userPaymentsResponse.getUserPaymentsList().stream()
                .map(userPaymentMessage -> this.paymentDTOFactory.valueOf(
                        userPaymentMessage,
                        userMessageMap.getOrDefault(userPaymentMessage.getPayeeId(), null),
                        userPaymentMessage.getMembershipSku(),
                        membershipMessageMap.getOrDefault(userPaymentMessage.getMembershipSku().getMembershipId(), null)
                ))
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(userPaymentsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
