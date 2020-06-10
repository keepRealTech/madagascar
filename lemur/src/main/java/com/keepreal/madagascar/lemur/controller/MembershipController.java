package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.FeedMembershipMessage;
import com.keepreal.madagascar.coua.FeedMembershipResponse;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.lemur.dtoFactory.FeedDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.MembershipDTOFactory;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.MembershipApi;
import swagger.model.DummyResponse;
import swagger.model.FeedMembershipsResponse;
import swagger.model.MembershipResponse;
import swagger.model.MembershipsResponse;
import swagger.model.PostMembershipRequest;
import swagger.model.PutMembershipRequest;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the membership controller.
 */
@RestController
public class MembershipController implements MembershipApi {

    private final MembershipService membershipService;
    private final MembershipDTOFactory membershipDTOFactory;

    /**
     * Constructs the membership controller.
     *
     * @param membershipService     {@link MembershipService}.
     * @param membershipDTOFactory  {@link MembershipDTOFactory}.
     */
    public MembershipController(MembershipService membershipService,
                                MembershipDTOFactory membershipDTOFactory) {
        this.membershipService = membershipService;
        this.membershipDTOFactory = membershipDTOFactory;
    }

    /**
     * Implements the get feed memberships api.
     *
     * @param id island id.
     * @return   {@link FeedMembershipsResponse}.
     */
    @Override
    public ResponseEntity<FeedMembershipsResponse> apiV1IslandsIdFeedMembershipsGet(String id) {
        List<FeedMembershipMessage> feedMembershipMessages = membershipService.RetrieveFeedMembershipsByIslandId(id);

        FeedMembershipsResponse response = new FeedMembershipsResponse();
        response.data(feedMembershipMessages.stream().map(membershipDTOFactory::feedValueOf).collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get memberships api(with top flag and color type).
     *
     * @param id island id.
     * @return   {@link MembershipResponse}.
     */
    @Override
    public ResponseEntity<MembershipsResponse> apiV1IslandsIdMembershipsGet(String id) {
        List<MembershipMessage> membershipMessages = membershipService.RetrieveMembershipsByIslandId(id);

        MembershipsResponse response = new MembershipsResponse();
        response.data(membershipMessages.stream().map(membershipDTOFactory::valueOf).collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the membership api.
     *
     * @param id                    island id.
     * @param postMembershipRequest {@link PostMembershipRequest}.
     * @return                      {@link MembershipResponse}.
     */
    @Override
    public ResponseEntity<MembershipResponse> apiV1IslandsIdMembershipsPost(String id, @Valid PostMembershipRequest postMembershipRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        MembershipMessage membershipMessage = membershipService.createMembership(postMembershipRequest.getName(),
                postMembershipRequest.getChargePerMonth(),
                postMembershipRequest.getDescription(),
                id,
                userId);

        MembershipResponse response = new MembershipResponse();
        response.data(membershipDTOFactory.briefValueOf(membershipMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the deactivate membership api.
     *
     * @param id membership id.
     * @return   {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1MembershipsIdDeactivatePut(String id) {
        membershipService.deactivateMembershipById(id);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the delete membership api.
     *
     * @param id membership id.
     * @return   {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1MembershipsIdDelete(String id) {
        membershipService.deleteMembershipById(id);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements get membership api.
     *
     * @param id membership id.
     * @return   {@link MembershipResponse}.
     */
    @Override
    public ResponseEntity<MembershipResponse> apiV1MembershipsIdGet(String id) {
        MembershipMessage membershipMessage = membershipService.retrieveMembershipById(id);

        MembershipResponse response = new MembershipResponse();
        response.data(membershipDTOFactory.briefValueOf(membershipMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the update membership api.
     *
     * @param id                    membership id.
     * @param putMembershipRequest  {@link PutMembershipRequest}.
     * @return  {@link MembershipResponse}.
     */
    @Override
    public ResponseEntity<MembershipResponse> apiV1MembershipsIdPut(String id, @Valid PutMembershipRequest putMembershipRequest) {
        MembershipMessage membershipMessage = membershipService.updateMembershipById(id,
                putMembershipRequest.getName(),
                putMembershipRequest.getDescription(),
                putMembershipRequest.getChargePerMonth());

        MembershipResponse response = new MembershipResponse();
        response.data(membershipDTOFactory.briefValueOf(membershipMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the top membership api.
     *
     * @param id        membership id.
     * @param isRevoke  whether is revoking
     * @return  {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1MembershipsIdTopPost(String id, @Valid Boolean isRevoke) {
        membershipService.topMembershipById(id, isRevoke);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
