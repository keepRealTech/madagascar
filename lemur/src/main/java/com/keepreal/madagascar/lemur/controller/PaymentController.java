package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.WechatOrderMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.lemur.config.IOSClientConfiguration;
import com.keepreal.madagascar.lemur.dtoFactory.PaymentDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.WechatOrderDTOFactory;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.PaymentService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.vanga.RedirectResponse;
import com.keepreal.madagascar.vanga.UserPaymentMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.PaymentApi;
import swagger.model.ConfigurationDTO;
import swagger.model.DummyResponse;
import swagger.model.H5RedirectResponse;
import swagger.model.H5WechatOrderDTO;
import swagger.model.H5WechatOrderResponse;
import swagger.model.PostSupportRequest;
import swagger.model.H5RedirectDTO;
import swagger.model.SceneType;
import swagger.model.SubscribeMemberRequest;
import swagger.model.UserPaymentsResponse;
import swagger.model.WechatOrderResponse;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the payment controller.
 */
@RestController
public class PaymentController implements PaymentApi {

    private Set<String> auditUserIds = new HashSet<>(Collections.singleton("484"));
    private final Map<Integer, ConfigurationDTO> iOSConfigVersionMap = new HashMap<>();

    private final IOSClientConfiguration iosClientConfiguration;
    private final PaymentService paymentService;
    private final UserService userService;
    private final MembershipService membershipService;
    private final WechatOrderDTOFactory wechatOrderDTOFactory;
    private final PaymentDTOFactory paymentDTOFactory;
    private final IslandService islandService;
    private final FeedService feedService;

    /**
     * Constructs the payment controller.
     *
     * @param paymentService         {@link PaymentService}.
     * @param userService            {@link UserService}.
     * @param membershipService      {@link MembershipService}.
     * @param wechatOrderDTOFactory  {@link WechatOrderDTOFactory}.
     * @param paymentDTOFactory      {@link PaymentDTOFactory}.
     * @param iosClientConfiguration {@link IOSClientConfiguration}.
     * @param feedService            {@link FeedService}.
     */
    public PaymentController(PaymentService paymentService,
                             UserService userService,
                             MembershipService membershipService,
                             WechatOrderDTOFactory wechatOrderDTOFactory,
                             PaymentDTOFactory paymentDTOFactory,
                             IOSClientConfiguration iosClientConfiguration,
                             IslandService islandService,
                             FeedService feedService) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.membershipService = membershipService;
        this.wechatOrderDTOFactory = wechatOrderDTOFactory;
        this.paymentDTOFactory = paymentDTOFactory;
        this.islandService = islandService;
        this.iosClientConfiguration = iosClientConfiguration;
        this.feedService = feedService;
        this.iOSConfigVersionMap.putAll(iosClientConfiguration.getVersionInfoMap());
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
        String remoteAddress = HttpContextUtils.getRemoteIpFromContext();

        WechatOrderMessage wechatOrderMessage = this.paymentService.submitSubscribeMembershipWithWechatPay(userId,
                remoteAddress,
                subscribeMemberRequest.getMembershipSkuId());

        WechatOrderResponse response = new WechatOrderResponse();
        response.setData(this.wechatOrderDTOFactory.valueOf(wechatOrderMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the H5 wechat pay for membership subscription.
     *
     * @param id                     id (required) Island id.
     * @param sceneType              (required) Scene type.
     * @param subscribeMemberRequest (required) {@link SubscribeMemberRequest}.
     * @return {@link H5WechatOrderResponse}.
     */
    @CrossOrigin
    @Override
    public ResponseEntity<H5WechatOrderResponse> apiV1IslandsIdMemberSubscriptionWechatPayHtml5Post(String id,
                                                                                                    SceneType sceneType,
                                                                                                    SubscribeMemberRequest subscribeMemberRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        String[] remoteAddresses = HttpContextUtils.getRemoteIpFromContext().split(",");

        System.out.println(remoteAddresses[remoteAddresses.length - 1].trim());

        RedirectResponse redirectResponse = this.paymentService.submitSubscribeMembershipWithWechatPayH5(userId,
                remoteAddresses[remoteAddresses.length - 1].trim(),
                subscribeMemberRequest.getMembershipSkuId(),
                this.convertType(sceneType));
        H5WechatOrderDTO data = new H5WechatOrderDTO();
        data.setUrl(redirectResponse.getRedirectUrl());
        data.setOrderId(redirectResponse.getOrderId());

        H5WechatOrderResponse response = new H5WechatOrderResponse();
        response.setData(data);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get user payment history api.
     *
     * @param page     page number (optional, default to 0).
     * @param pageSize size of a page (optional, default to 10).
     * @return {@link UserPaymentsResponse}.
     */
    @CrossOrigin
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
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(userPaymentsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the membership subscription pay ios redirect url.
     *
     * @param id      id (required) Island id.
     * @param version (required) IOS client version.
     * @return {@link H5RedirectResponse}.
     */
    @Override
    public ResponseEntity<H5RedirectResponse> apiV1IslandsIdMemberSubscriptionIosPayGet(String id,
                                                                                        Integer version,
                                                                                        String membershipId) {
        String userId = HttpContextUtils.getUserIdFromContext();
        ConfigurationDTO configurationDTO = this.iOSConfigVersionMap.get(version);

        H5RedirectDTO data = new H5RedirectDTO();
        if (this.auditUserIds.contains(userId) || Objects.isNull(configurationDTO) || configurationDTO.getAudit()) {
            data.setUrl(String.format("%s/pay-ios?sid=%s&id=%s", this.iosClientConfiguration.getHtmlHostName(), membershipId, id));
        } else {
            data.setUrl(String.format("%s/pay?sid=%s&id=%s", this.iosClientConfiguration.getHtmlHostName(), membershipId, id));
        }

        H5RedirectResponse response = new H5RedirectResponse();
        response.setData(data);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the membership subscription pay ios redirect url.
     *
     * @param id      id (required) Island id.
     * @param version (required) IOS client version.
     * @return {@link H5RedirectResponse}.
     */
    @Override
    public ResponseEntity<H5RedirectResponse> apiV1IslandsIdSupportIosPayGet(String id,
                                                                             Integer version) {
        String userId = HttpContextUtils.getUserIdFromContext();
        ConfigurationDTO configurationDTO = this.iOSConfigVersionMap.get(version);

        H5RedirectDTO data = new H5RedirectDTO();
        if (this.auditUserIds.contains(userId) || Objects.isNull(configurationDTO) || configurationDTO.getAudit()) {
            data.setUrl(String.format("%s/pay-ta-ios?id=%s", this.iosClientConfiguration.getHtmlHostName(), id));
        } else {
            data.setUrl(String.format("%s/pay-ta?id=%s", this.iosClientConfiguration.getHtmlHostName(), id));
        }

        H5RedirectResponse response = new H5RedirectResponse();
        response.setData(data);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the support h5 wechat pay api.
     *
     * @param id id (required) Island id.
     * @param sceneType  (required) Scene type.
     * @param postSupportRequest  (required) {@link PostSupportRequest}.
     * @return {@link H5WechatOrderResponse}.
     */
    @CrossOrigin
    @Override
    public ResponseEntity<H5WechatOrderResponse> apiV1IslandsIdSupportWechatPayHtml5Post(String id,
                                                                                         SceneType sceneType,
                                                                                         PostSupportRequest postSupportRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        String remoteAddress = HttpContextUtils.getRemoteIpFromContext();

        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);

        RedirectResponse redirectResponse = this.paymentService.submitSupportWithWechatPayH5(userId,
                islandMessage.getHostId(),
                postSupportRequest.getSponsorSkuId(),
                postSupportRequest.getPriceInCents(),
                postSupportRequest.getPriceInShells(),
                remoteAddress,
                this.convertType(sceneType));

        H5WechatOrderDTO data = new H5WechatOrderDTO();
        data.setUrl(redirectResponse.getRedirectUrl());
        data.setOrderId(redirectResponse.getOrderId());

        H5WechatOrderResponse response = new H5WechatOrderResponse();
        response.setData(data);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<WechatOrderResponse> apiV1IslandsIdSupportWechatPayPost(String id, @Valid PostSupportRequest postSupportRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        String remoteAddress = HttpContextUtils.getRemoteIpFromContext();

        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);

        WechatOrderMessage wechatOrderMessage = this.paymentService.submitSupportWithWechatPay(userId,
                islandMessage.getHostId(),
                postSupportRequest.getSponsorSkuId(),
                postSupportRequest.getPriceInCents(),
                postSupportRequest.getPriceInShells(),
                remoteAddress);

        WechatOrderResponse response = new WechatOrderResponse();
        response.setData(this.wechatOrderDTOFactory.valueOf(wechatOrderMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<WechatOrderResponse> apiV1FeedsIdWechatPayPost(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();
        String ipAddress = HttpContextUtils.getRemoteIpFromContext();

        FeedMessage feedMessage = this.feedService.retrieveFeedById(id, userId);

        WechatOrderMessage wechatOrderMessage = this.paymentService.submitFeedWithWechatPay(userId, id, feedMessage.getPriceInCents(), feedMessage.getHostId(), ipAddress);

        WechatOrderResponse response = new WechatOrderResponse();
        response.setData(this.wechatOrderDTOFactory.valueOf(wechatOrderMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Converts the {@link SceneType} into {@link com.keepreal.madagascar.common.SceneType}.
     *
     * @param sceneType {@link SceneType}.
     * @return {@link com.keepreal.madagascar.common.SceneType}.
     */
    private com.keepreal.madagascar.common.SceneType convertType(SceneType sceneType) {
        switch (sceneType) {
            case IOS:
                return com.keepreal.madagascar.common.SceneType.SCENE_IOS;
            case ANDROID:
                return com.keepreal.madagascar.common.SceneType.SCENE_ANDROID;
            case WAP:
            default:
                return com.keepreal.madagascar.common.SceneType.SCENE_WAP;
        }
    }
}
