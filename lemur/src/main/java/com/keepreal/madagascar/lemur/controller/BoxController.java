package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.BoxDTOFactory;
import com.keepreal.madagascar.lemur.service.BoxService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.BoxApi;
import swagger.model.DummyResponse;
import swagger.model.FullQuestionResponse;
import swagger.model.IslandBoxAccessResponse;
import swagger.model.IslandBoxResponse;
import swagger.model.PostAnswerRequest;
import swagger.model.PostQuestionRequest;
import swagger.model.PutIslandBoxAccessRequest;
import swagger.model.QuestionResponse;
import swagger.model.QuestionsResponse;
import swagger.model.WechatOrderResponse;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
public class BoxController implements BoxApi {

    private final BoxService boxService;
    private final BoxDTOFactory boxDTOFactory;

    public BoxController(BoxService boxService,
                         BoxDTOFactory boxDTOFactory) {
        this.boxService = boxService;
        this.boxDTOFactory = boxDTOFactory;
    }

    @Override
    public ResponseEntity<QuestionsResponse> apiV1BoxesAnswersGet(@Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer pageSize) {
        return null;
    }

    @Override
    public ResponseEntity<QuestionsResponse> apiV1BoxesQuestionsGet(@Valid Boolean answered, @Valid String membershipId, @Valid Boolean paid, @Valid Boolean _public, @Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer pageSize) {
        return null;
    }

    @Override
    public ResponseEntity<DummyResponse> apiV1BoxesQuestionsIdAnswerPost(String id, @Valid PostAnswerRequest postAnswerRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        this.boxService.answerQuestion(id, userId, postAnswerRequest.getText(),
                postAnswerRequest.getPublicVisible(), postAnswerRequest.getVisibleMembershipIds());
        return null;
    }

    @Override
    public ResponseEntity<FullQuestionResponse> apiV1BoxesQuestionsIdGet(String id) {
        return null;
    }

    @Override
    public ResponseEntity<DummyResponse> apiV1BoxesQuestionsIdIgnorePost(String id) {
        return null;
    }

    @Override
    public ResponseEntity<IslandBoxAccessResponse> apiV1IslandsIdBoxesAccessPut(String id, @Valid PutIslandBoxAccessRequest putIslandBoxAccessRequest) {
        Boolean enabled = putIslandBoxAccessRequest.getEnabled();
        List<String> membershipIds = putIslandBoxAccessRequest.getMembershipIds();

        this.boxService.createOrUpdateBoxInfo(id, enabled, membershipIds);

        IslandBoxAccessResponse response = new IslandBoxAccessResponse();
        response.setData(this.boxDTOFactory.boxAccessDTO(id));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<QuestionResponse> apiV1IslandsIdBoxesFreeQuestionsPost(String id, @Valid PostQuestionRequest postQuestionRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        this.createQuestion(id, userId, postQuestionRequest);
        return null;
    }

    @Override
    public ResponseEntity<IslandBoxResponse> apiV1IslandsIdBoxesGet(String id) {
        // get box info
        // isOpen, membershipIds


        IslandBoxResponse response = new IslandBoxResponse();
//        response.setData();
        return null;
    }

    @Override
    public ResponseEntity<QuestionResponse> apiV1IslandsIdBoxesPaidQuestionsIosPayPost(String id, @Valid PostQuestionRequest postQuestionRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        this.createQuestion(id, userId, postQuestionRequest);
        return null;
    }

    @Override
    public ResponseEntity<WechatOrderResponse> apiV1IslandsIdBoxesPaidQuestionsWechatPayPost(String id, @Valid PostQuestionRequest postQuestionRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        this.createQuestion(id, userId, postQuestionRequest);
        return null;
    }

    @Override
    public ResponseEntity<QuestionsResponse> apiV1IslandsIdBoxesQuestionsGet(String id, @Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer pageSize) {
        return null;
    }

    private void createQuestion(String islandId, String userId, PostQuestionRequest postQuestionRequest) {
        boxService.createQuestion(islandId, userId,
                postQuestionRequest.getText(),
                postQuestionRequest.getPriceInCents(),
                postQuestionRequest.getQuestionSkuId(),
                postQuestionRequest.getReceipt(),
                postQuestionRequest.getTransactionId());
    }
}
