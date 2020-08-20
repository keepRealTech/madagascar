package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.fossa.BoxMessage;
import com.keepreal.madagascar.lemur.dtoFactory.BoxDTOFactory;
import com.keepreal.madagascar.lemur.service.BoxService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.BoxApi;
import swagger.model.BoxDTO;
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
import java.util.stream.Collectors;

@RestController
public class BoxController implements BoxApi {

    private final BoxService boxService;
    private final FeedService feedService;
    private final BoxDTOFactory boxDTOFactory;

    public BoxController(BoxService boxService,
                         FeedService feedService,
                         BoxDTOFactory boxDTOFactory) {
        this.boxService = boxService;
        this.feedService = feedService;
        this.boxDTOFactory = boxDTOFactory;
    }

    @Override
    public ResponseEntity<QuestionsResponse> apiV1BoxesAnswersGet(@Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.QuestionsResponse questionsResponse = this.boxService.retrieveAnsweredMeQuestion(userId, page, pageSize);

        QuestionsResponse response = new QuestionsResponse();
        response.setData(questionsResponse.getFeedList().stream().map(this.boxDTOFactory::questionDTO).collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(questionsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<QuestionsResponse> apiV1BoxesQuestionsGet(@Valid Boolean answered, @Valid String membershipId, @Valid Boolean paid, @Valid Boolean _public, @Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.QuestionsResponse questionsResponse = this.boxService.retrieveAskMeQuestion(userId, page, pageSize, answered, paid, membershipId);

        QuestionsResponse response = new QuestionsResponse();
        response.setData(questionsResponse.getFeedList().stream().map(this.boxDTOFactory::questionDTO).collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(questionsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the answer question by id api.
     *
     * @param id id (required)
     * @param postAnswerRequest  (required)
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1BoxesQuestionsIdAnswerPost(String id, @Valid PostAnswerRequest postAnswerRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        this.boxService.answerQuestion(id, userId, postAnswerRequest.getText(),
                postAnswerRequest.getPublicVisible(), postAnswerRequest.getVisibleMembershipIds());
        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the retrieve question by id api.
     *
     * @param id id (required)
     * @return  {@link FullQuestionResponse}.
     */
    @Override
    public ResponseEntity<FullQuestionResponse> apiV1BoxesQuestionsIdGet(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        FeedMessage feedMessage = this.feedService.retrieveFeedById(id, userId);

        FullQuestionResponse response = new FullQuestionResponse();
        response.setData(this.boxDTOFactory.fullQuestionDTO(feedMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DummyResponse> apiV1BoxesQuestionsIdIgnorePost(String id) {
        //todo...
        return null;
    }

    /**
     * Implements the update box access by island id api.
     *
     * @param id id (required)
     * @param putIslandBoxAccessRequest  (required)
     * @return  {@link IslandBoxResponse}.
     */
    @Override
    public ResponseEntity<IslandBoxAccessResponse> apiV1IslandsIdBoxesAccessPut(String id, @Valid PutIslandBoxAccessRequest putIslandBoxAccessRequest) {
        Boolean enabled = putIslandBoxAccessRequest.getEnabled();
        List<String> membershipIds = putIslandBoxAccessRequest.getMembershipIds();

        BoxMessage boxMessage = this.boxService.createOrUpdateBoxInfo(id, enabled, membershipIds);

        IslandBoxAccessResponse response = new IslandBoxAccessResponse();
        response.setData(this.boxDTOFactory.boxAccessDTO(boxMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the create free question api.
     *
     * @param id id (required)
     * @param postQuestionRequest  (required)
     * @return  {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1IslandsIdBoxesFreeQuestionsPost(String id, @Valid PostQuestionRequest postQuestionRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        this.createQuestion(id, userId, postQuestionRequest);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<IslandBoxResponse> apiV1IslandsIdBoxesGet(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();
        BoxMessage boxMessage = this.boxService.retrieveBoxInfo(id);

        BoxDTO dto = this.boxDTOFactory.boxDTO(boxMessage, userId);
        List<FeedMessage> feedList = this.boxService.retrieveAnsweredAndVisibleQuestions(id, userId, 0, 2).getFeedList();
        dto.setRecentAnsweredQuestions(feedList.stream().map(this.boxDTOFactory::questionDTO).collect(Collectors.toList()));

        IslandBoxResponse response = new IslandBoxResponse();
        response.setData(dto);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
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
        String userId = HttpContextUtils.getUserIdFromContext();

        com.keepreal.madagascar.fossa.QuestionsResponse questionsResponse = this.boxService.retrieveAnsweredAndVisibleQuestions(id, userId, page, pageSize);

        QuestionsResponse response = new QuestionsResponse();
        response.setData(questionsResponse.getFeedList().stream().map(this.boxDTOFactory::questionDTO).collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(questionsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
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
