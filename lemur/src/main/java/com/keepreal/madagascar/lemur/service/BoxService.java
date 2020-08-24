package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.AnswerMessage;
import com.keepreal.madagascar.common.WechatOrderMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.AnswerQuestionRequest;
import com.keepreal.madagascar.fossa.BoxMessage;
import com.keepreal.madagascar.fossa.BoxServiceGrpc;
import com.keepreal.madagascar.fossa.CommonResponse;
import com.keepreal.madagascar.fossa.CreateOrUpdateBoxRequest;
import com.keepreal.madagascar.fossa.CreateOrUpdateBoxResponse;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.IgnoreQuestionRequest;
import com.keepreal.madagascar.fossa.NewFeedsRequestV2;
import com.keepreal.madagascar.fossa.NewFeedsResponse;
import com.keepreal.madagascar.fossa.NewWechatFeedsResponse;
import com.keepreal.madagascar.fossa.QuestionsResponse;
import com.keepreal.madagascar.fossa.RetrieveAnswerMeQuestionsRequest;
import com.keepreal.madagascar.fossa.RetrieveAnsweredAndVisibleQuestionsRequest;
import com.keepreal.madagascar.fossa.RetrieveAskMeQuestionsRequest;
import com.keepreal.madagascar.fossa.RetrieveBoxInfoRequest;
import com.keepreal.madagascar.fossa.RetrieveBoxInfoResponse;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
public class BoxService {

    private final Channel fossaChannel;
    private final IslandService islandService;

    public BoxService(@Qualifier("fossaChannel") Channel fossaChannel,
                      IslandService islandService) {
        this.fossaChannel = fossaChannel;
        this.islandService = islandService;
    }

    /**
     * Creates a free question.
     *
     * @param islandId  Island id.
     * @param userId    User id.
     * @param text      Question text.
     */
    public void createFreeQuestion(String islandId, String userId, String text) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        String hostId = this.islandService.retrieveIslandById(islandId).getHostId();

        NewFeedsRequestV2.Builder builder = NewFeedsRequestV2.newBuilder()
                .addAllIslandId(Collections.singletonList(islandId))
                .addAllHostId(Collections.singletonList(hostId))
                .setUserId(userId)
                .setType(MediaType.MEDIA_QUESTION)
                .setText(StringValue.of(text));

        NewFeedsResponse newFeedsResponse;
        try {
            newFeedsResponse = stub.createFeedsV2(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(newFeedsResponse)
                || !newFeedsResponse.hasStatus()) {
            log.error(Objects.isNull(newFeedsResponse) ? "Create free question returned null." : newFeedsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != newFeedsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(newFeedsResponse.getStatus());
        }
    }

    public WechatOrderMessage createWechatFeed(String islandId, String userId, String text, Long priceInCents) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        String hostId = this.islandService.retrieveIslandById(islandId).getHostId();

        NewFeedsRequestV2.Builder builder = NewFeedsRequestV2.newBuilder()
                .addAllIslandId(Collections.singletonList(islandId))
                .addAllHostId(Collections.singletonList(hostId))
                .setUserId(userId)
                .setType(MediaType.MEDIA_QUESTION)
                .setText(StringValue.of(text))
                .setPriceInCents(Int64Value.of(priceInCents));

        NewWechatFeedsResponse response;
        try {
            response = stub.createWechatFeedsV2(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Create wechat feed returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getMessage();
    }

    public void answerQuestion(String id, String userId, String answer, boolean publicVisible, List<String> visibleMembershipIds) {
        BoxServiceGrpc.BoxServiceBlockingStub stub = BoxServiceGrpc.newBlockingStub(this.fossaChannel);

        CommonResponse response;

        try {
            response = stub.answerQuestion(AnswerQuestionRequest.newBuilder()
                    .setId(id)
                    .setUserId(userId)
                    .setAnswer(answer)
                    .setPublicVisible(publicVisible)
                    .addAllVisibleMembershipIds(visibleMembershipIds == null ? Collections.emptyList() : visibleMembershipIds)
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Answer question returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }

    public BoxMessage createOrUpdateBoxInfo(String islandId, String userId, boolean enabled, List<String> membershipIds) {
        BoxServiceGrpc.BoxServiceBlockingStub stub = BoxServiceGrpc.newBlockingStub(this.fossaChannel);

        CreateOrUpdateBoxResponse response;

        try {
            response = stub.putBox(CreateOrUpdateBoxRequest.newBuilder()
                    .setIslandId(islandId)
                    .setEnabled(enabled)
                    .addAllMembershipIds(membershipIds)
                    .setUserId(userId)
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "update box info returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getMessage();
    }

    public BoxMessage retrieveBoxInfo(String islandId) {
        BoxServiceGrpc.BoxServiceBlockingStub stub = BoxServiceGrpc.newBlockingStub(this.fossaChannel);

        RetrieveBoxInfoResponse response;

        try {
            response = stub.retrieveBoxInfo(RetrieveBoxInfoRequest.newBuilder()
                    .setIslandId(islandId)
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "retrieve box info returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getMessage();
    }

    public QuestionsResponse retrieveAnsweredAndVisibleQuestions(String islandId, String userId, int page, int pageSize) {
        BoxServiceGrpc.BoxServiceBlockingStub stub = BoxServiceGrpc.newBlockingStub(this.fossaChannel);

        QuestionsResponse response;

        try {
            response = stub.retrieveAnsweredAndVisibleQuestion(RetrieveAnsweredAndVisibleQuestionsRequest.newBuilder()
                    .setIslandId(islandId)
                    .setUserId(userId)
                    .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "retrieve answer and visible questions returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    public QuestionsResponse retrieveAskMeQuestion(String userId, int page, int pageSize, Boolean answered, Boolean paid, String membershipId) {
        BoxServiceGrpc.BoxServiceBlockingStub stub = BoxServiceGrpc.newBlockingStub(this.fossaChannel);

        RetrieveAskMeQuestionsRequest.Builder builder = RetrieveAskMeQuestionsRequest
                .newBuilder()
                .setUserId(userId)
                .setAnswered(answered == null ? false : answered)
                .setPaid(paid == null ? false : paid)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .setMembershipId(membershipId == null ? "" : membershipId);

        QuestionsResponse response;
        try {
            response = stub.retrieveAskMeQuestion(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "retrieve ask me questions returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    public QuestionsResponse retrieveAnsweredMeQuestion(String userId, int page, int pageSize) {
        BoxServiceGrpc.BoxServiceBlockingStub stub = BoxServiceGrpc.newBlockingStub(this.fossaChannel);

        QuestionsResponse response;

        try {
            response = stub.retrieveAnswerMeQuestions(RetrieveAnswerMeQuestionsRequest.newBuilder()
                    .setUserId(userId)
                    .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "retrieve answer me questions returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    public void ignoreQuestion(String questionId) {
        BoxServiceGrpc.BoxServiceBlockingStub stub = BoxServiceGrpc.newBlockingStub(this.fossaChannel);

        CommonResponse response;

        try {
            response = stub.ignoreQuestion(IgnoreQuestionRequest.newBuilder()
                    .setQuestionId(questionId)
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "ignore question returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }
}
