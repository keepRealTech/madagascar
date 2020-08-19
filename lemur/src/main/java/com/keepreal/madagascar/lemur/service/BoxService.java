package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.QuestionMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.AnswerQuestionRequest;
import com.keepreal.madagascar.fossa.AnswerQuestionResponse;
import com.keepreal.madagascar.fossa.BoxServiceGrpc;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.NewFeedsRequestV2;
import com.keepreal.madagascar.fossa.NewFeedsResponse;
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

    public void createQuestion(String userId, String islandId, String text, Long priceInCents, String questionSkuId, String receipt, String transactionId) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        String hostId = this.islandService.retrieveIslandById(islandId).getHostId();


        QuestionMessage.Builder questionBuilder = QuestionMessage.newBuilder().setText(text);
        if (priceInCents != null && priceInCents > 0) {
            questionBuilder.setPriceInCents(Int64Value.of(priceInCents));
        }
        if (!StringUtils.isEmpty(questionSkuId)) {
            questionBuilder.setQuestionSkuId(StringValue.of(questionSkuId));
        }
        if (!StringUtils.isEmpty(receipt)) {
            questionBuilder.setReceipt(StringValue.of(receipt));
        }
        if (!StringUtils.isEmpty(transactionId)) {
            questionBuilder.setTransactionId(StringValue.of(transactionId));
        }

        NewFeedsRequestV2.Builder builder = NewFeedsRequestV2.newBuilder()
                .addAllIslandId(Collections.singletonList(islandId))
                .addAllHostId(Collections.singletonList(hostId))
                .setUserId(userId)
                .setType(MediaType.MEDIA_QUESTION)
                .setQuestion(questionBuilder.build());

        NewFeedsResponse newFeedsResponse;
        try {
            newFeedsResponse = stub.createFeedsV2(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(newFeedsResponse)
                || !newFeedsResponse.hasStatus()) {
            log.error(Objects.isNull(newFeedsResponse) ? "Create feed returned null." : newFeedsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != newFeedsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(newFeedsResponse.getStatus());
        }
    }

    public void answerQuestion(String id, String userId, String answer, boolean publicVisible, List<String> visibleMembershipIds) {
        BoxServiceGrpc.BoxServiceBlockingStub stub = BoxServiceGrpc.newBlockingStub(this.fossaChannel);

        AnswerQuestionResponse response;

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

    public void retrieveBoxInfo(String islandId) {

    }
}
