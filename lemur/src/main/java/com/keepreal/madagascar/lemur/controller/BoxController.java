package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.IslandAccessType;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.WechatOrderMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.FeedMembershipMessage;
import com.keepreal.madagascar.fossa.BoxMessage;
import com.keepreal.madagascar.lemur.dtoFactory.BoxDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.WechatOrderDTOFactory;
import com.keepreal.madagascar.lemur.service.BoxService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.BoxApi;
import swagger.model.BoxDTO;
import swagger.model.DummyResponse;
import swagger.model.FullQuestionResponse;
import swagger.model.IslandBoxAccessListResponse;
import swagger.model.IslandBoxAccessResponse;
import swagger.model.IslandBoxResponse;
import swagger.model.PostAnswerRequest;
import swagger.model.PostQuestionRequest;
import swagger.model.PutIslandBoxAccessRequest;
import swagger.model.QuestionResponse;
import swagger.model.QuestionsResponse;
import swagger.model.WechatOrderResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class BoxController implements BoxApi {

    private final BoxService boxService;
    private final FeedService feedService;
    private final IslandService islandService;
    private final BoxDTOFactory boxDTOFactory;
    private final WechatOrderDTOFactory wechatOrderDTOFactory;

    public BoxController(BoxService boxService,
                         FeedService feedService,
                         IslandService islandService,
                         BoxDTOFactory boxDTOFactory,
                         WechatOrderDTOFactory wechatOrderDTOFactory) {
        this.boxService = boxService;
        this.feedService = feedService;
        this.islandService = islandService;
        this.boxDTOFactory = boxDTOFactory;
        this.wechatOrderDTOFactory = wechatOrderDTOFactory;
    }

    /**
     * Implements the answered me question list api.
     *
     * @param page page number (optional, default to 0)
     * @param pageSize size of a page (optional, default to 10)
     * @return  {@link QuestionsResponse}.
     */
    @Override
    public ResponseEntity<QuestionsResponse> apiV1BoxesAnswersGet(Integer page,
                                                                  Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.QuestionsResponse questionsResponse = this.boxService.retrieveAnsweredMeQuestion(userId, page, pageSize);

        QuestionsResponse response = new QuestionsResponse();
        this.questionsResponse(response, questionsResponse, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the ask me question list by condition(answered, membershipId, paid).
     *
     * @param answered  (optional)
     * @param hasMembership  (optional)
     * @param paid  (optional)
     * @param page page number (optional, default to 0)
     * @param pageSize size of a page (optional, default to 10)
     * @return  {@link QuestionsResponse}.
     */
    @Override
    public ResponseEntity<QuestionsResponse> apiV1BoxesQuestionsGet(Boolean answered,
                                                                    Boolean hasMembership,
                                                                    Boolean paid,
                                                                    Integer page,
                                                                    Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.QuestionsResponse questionsResponse = this.boxService.retrieveAskMeQuestion(userId, page, pageSize, answered, paid, hasMembership);

        QuestionsResponse response = new QuestionsResponse();
        this.questionsResponse(response, questionsResponse, userId);
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
    public ResponseEntity<DummyResponse> apiV1BoxesQuestionsIdAnswerPost(String id,
                                                                         PostAnswerRequest postAnswerRequest) {
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
        response.setData(this.boxDTOFactory.fullValueOf(feedMessage, userId));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DummyResponse> apiV1BoxesQuestionsIdIgnorePost(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();
        this.boxService.ignoreQuestion(id, userId);
        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the update box access by island id api.
     *
     * @param id id (required)
     * @param putIslandBoxAccessRequest  (required)
     * @return  {@link IslandBoxResponse}.
     */
    @Override
    public ResponseEntity<IslandBoxAccessResponse> apiV1IslandsIdBoxesAccessPut(String id,
                                                                                PutIslandBoxAccessRequest putIslandBoxAccessRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        Boolean enabled = putIslandBoxAccessRequest.getEnabled();
        List<String> membershipIds = putIslandBoxAccessRequest.getMembershipIds();

        BoxMessage boxMessage = this.boxService.createOrUpdateBoxInfo(id, userId, enabled, membershipIds);

        IslandBoxAccessResponse response = new IslandBoxAccessResponse();
        response.setData(this.boxDTOFactory.valueOf(boxMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the create free question api.
     *
     * @param id id (required) Island id.
     * @param postQuestionRequest  (required) {@link PostQuestionRequest}.
     * @return  {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1IslandsIdBoxesFreeQuestionsPost(String id,
                                                                              PostQuestionRequest postQuestionRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        if (StringUtils.isEmpty(postQuestionRequest.getText().trim())) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        this.boxService.createFreeQuestion(id, userId, postQuestionRequest.getText());

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the retrieve box info api.
     *
     * @param id islandId (required)
     * @return  {@link IslandBoxResponse}.
     */
    @Override
    public ResponseEntity<IslandBoxResponse> apiV1IslandsIdBoxesGet(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();
        BoxMessage boxMessage = this.boxService.retrieveBoxInfo(id);

        BoxDTO dto = this.boxDTOFactory.valueOf(boxMessage, userId);
        List<FeedMessage> feedList = this.boxService.retrieveAnsweredAndVisibleQuestions(id, userId, 0, 2).getFeedList();
        dto.setRecentAnsweredQuestions(feedList.stream().map(feedMessage -> this.boxDTOFactory.valueOf(feedMessage, userId)).collect(Collectors.toList()));

        IslandBoxResponse response = new IslandBoxResponse();
        response.setData(dto);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<QuestionResponse> apiV1IslandsIdBoxesPaidQuestionsIosPayPost(String id,
                                                                                       PostQuestionRequest postQuestionRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        return null;
    }

    @Override
    public ResponseEntity<WechatOrderResponse> apiV1IslandsIdBoxesPaidQuestionsWechatPayPost(String id,
                                                                                             PostQuestionRequest postQuestionRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        if (StringUtils.isEmpty(postQuestionRequest.getText().trim())) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        WechatOrderMessage wechatOrderMessage = this.boxService.createWechatFeed(id,
                userId,
                postQuestionRequest.getText(),
                postQuestionRequest.getPriceInCents());

        WechatOrderResponse response = new WechatOrderResponse();

        response.setData(this.wechatOrderDTOFactory.valueOf(wechatOrderMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the retrieve answered and visible questions api.
     *
     * @param id islandId (required)
     * @param page page number (optional, default to 0)
     * @param pageSize size of a page (optional, default to 10)
     * @return  {@link QuestionsResponse}.
     */
    @Override
    public ResponseEntity<QuestionsResponse> apiV1IslandsIdBoxesQuestionsGet(String id,
                                                                             Integer page,
                                                                             Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);

        if (IslandAccessType.ISLAND_ACCESS_PRIVATE.equals(islandMessage.getIslandAccessType())
                && !this.islandService.checkIslandSubscription(id, userId)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_ISLAND_USER_NOT_SUBSCRIBED_ERROR);
        }


        com.keepreal.madagascar.fossa.QuestionsResponse questionsResponse = this.boxService.retrieveAnsweredAndVisibleQuestions(id, userId, page, pageSize);

        QuestionsResponse response = new QuestionsResponse();
        this.questionsResponse(response, questionsResponse, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<IslandBoxAccessListResponse> apiV1IslandsIdBoxesAccessGet(String id) {
        IslandBoxAccessListResponse response = new IslandBoxAccessListResponse();
        response.setData(this.boxDTOFactory.listValueOf(id));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void questionsResponse(QuestionsResponse response, com.keepreal.madagascar.fossa.QuestionsResponse questionsResponse, String userId) {
        response.setData(questionsResponse.getFeedList().stream().map(feedMessage -> this.boxDTOFactory.valueOf(feedMessage, userId)).collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(questionsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
    }

}
