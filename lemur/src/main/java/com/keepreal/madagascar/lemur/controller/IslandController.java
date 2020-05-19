package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.stats_events.annotation.HttpStatsEventTrigger;
import com.keepreal.madagascar.coua.IslandSubscribersResponse;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.fossa.IslandRepostMessage;
import com.keepreal.madagascar.fossa.IslandRepostsResponse;
import com.keepreal.madagascar.lemur.dtoFactory.IslandDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.RepostDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.UserDTOFactory;
import com.keepreal.madagascar.lemur.service.ImageService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.RepostService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import swagger.api.IslandApi;
import swagger.model.BriefIslandResponse;
import swagger.model.BriefIslandsResponse;
import swagger.model.CheckIslandDTO;
import swagger.model.CheckIslandResponse;
import swagger.model.DummyResponse;
import swagger.model.IslandProfileResponse;
import swagger.model.IslandResponse;
import swagger.model.OfficialIslandsResponse;
import swagger.model.PostIslandPayload;
import swagger.model.PostRepostRequest;
import swagger.model.PutIslandPayload;
import swagger.model.RepostResponse;
import swagger.model.RepostsResponse;
import swagger.model.SubscribeIslandRequest;
import swagger.model.UsersResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the island controller.
 */
@RestController
public class IslandController implements IslandApi {

    private final List<String> officialIslandIdList;

    private final ImageService imageService;
    private final IslandService islandService;
    private final RepostService repostService;
    private final IslandDTOFactory islandDTOFactory;
    private final UserDTOFactory userDTOFactory;
    private final RepostDTOFactory repostDTOFactory;

    /**
     * Constructs the island controller.
     *
     * @param imageService     {@link ImageService}.
     * @param islandService    {@link IslandService}.
     * @param repostService    {@link RepostService}.
     * @param islandDTOFactory {@link IslandDTOFactory}.
     * @param userDTOFactory   {@link UserDTOFactory}.
     * @param repostDTOFactory {@link RepostDTOFactory}.
     */
    public IslandController(ImageService imageService,
                            IslandService islandService,
                            RepostService repostService,
                            IslandDTOFactory islandDTOFactory,
                            UserDTOFactory userDTOFactory,
                            RepostDTOFactory repostDTOFactory) {
        this.imageService = imageService;
        this.islandService = islandService;
        this.repostService = repostService;
        this.islandDTOFactory = islandDTOFactory;
        this.userDTOFactory = userDTOFactory;
        this.repostDTOFactory = repostDTOFactory;
        this.officialIslandIdList = Arrays.asList("1");
    }

    /**
     * Implements the check name api.
     *
     * @param name Name.
     * @return {@link CheckIslandResponse}.
     */
    @Override
    public ResponseEntity<CheckIslandResponse> apiV1IslandsCheckNameGet(String name) {
        boolean ifExists = this.islandService.checkName(name);

        CheckIslandDTO checkIslandDTO = new CheckIslandDTO();
        checkIslandDTO.setIsExisted(ifExists);

        CheckIslandResponse response = new CheckIslandResponse();
        response.setData(checkIslandDTO);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get islands api.
     *
     * @param name       Island name.
     * @param subscribed Whether subscribed by the context user.
     * @param page       Page index.
     * @param pageSize   Page size.
     * @return {@link BriefIslandsResponse}.
     */
    @Override
    @HttpStatsEventTrigger(
            category = StatsEventCategory.STATS_CAT_ISLAND,
            action = StatsEventAction.STATS_ACT_RETRIEVE,
            label = "islands hit number",
            value = "body.data.size()"
    )
    public ResponseEntity<swagger.model.IslandsResponse> apiV1IslandsGet(String name,
                                                                         Boolean subscribed,
                                                                         Integer page,
                                                                         Integer pageSize) {
        String subscriberId = (Objects.nonNull(subscribed) && subscribed) ? HttpContextUtils.getUserIdFromContext() : null;
        IslandsResponse islandsResponse = this.islandService.retrieveIslands(
                name, null, subscriberId, page, pageSize);

        swagger.model.IslandsResponse response = new swagger.model.IslandsResponse();
        response.setData(islandsResponse.getIslandsList()
                .stream()
                .map(this.islandDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(islandsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get island by id api.
     *
     * @param id Island id.
     * @return {@link IslandResponse}.
     */
    @Override
    public ResponseEntity<IslandResponse> apiV1IslandsIdGet(String id) {
        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);

        IslandResponse response = new IslandResponse();
        response.setData(this.islandDTOFactory.valueOf(islandMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get island profile by id api.
     *
     * @param id Island id.
     * @return {@link IslandProfileResponse}.
     */
    @Override
    public ResponseEntity<IslandProfileResponse> apiV1IslandsIdProfileGet(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        com.keepreal.madagascar.coua.IslandProfileResponse islandProfileResponse =
                this.islandService.retrieveIslandProfileById(id, userId);

        IslandProfileResponse response = new IslandProfileResponse();
        response.setData(this.islandDTOFactory.valueOf(islandProfileResponse, userId));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get my hosted islands api.
     *
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link BriefIslandsResponse}.
     */
    @Override
    public ResponseEntity<BriefIslandsResponse> apiV1IslandsMyIslandsGet(Integer page, Integer pageSize) {
        String hostId = HttpContextUtils.getUserIdFromContext();
        IslandsResponse islandsResponse = this.islandService.retrieveIslands(
                null, hostId, null, page, pageSize);

        return this.BuildBriefIslandsResponse(islandsResponse);
    }

    /**
     * Implements the get my hosted and subscribed islands api.
     *
     * @param page     page number (optional, default to 0)
     * @param pageSize size of a page (optional, default to 10)
     * @return {@link BriefIslandsResponse}.
     */
    @Override
    public ResponseEntity<BriefIslandsResponse> apiV1IslandsDefaultIslandsGet(Integer page, Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        IslandsResponse islandsResponse = islandService.retrieveDefaultIslands(userId, page, pageSize);
        return this.BuildBriefIslandsResponse(islandsResponse);
    }

    /**
     * Implements the official island id api.
     *
     * @return {@link OfficialIslandsResponse}.
     */
    @Override
    public ResponseEntity<OfficialIslandsResponse> apiV1IslandsOfficialIslandsGet() {
        OfficialIslandsResponse response = new OfficialIslandsResponse();
        response.setData(officialIslandIdList);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the create island api.
     *
     * @param payload       {@link PostIslandPayload}.
     * @param portraitImage Portrait image.
     * @return {@link BriefIslandResponse}.
     */
    @Override
    public ResponseEntity<BriefIslandResponse> apiV1IslandsPost(
            PostIslandPayload payload,
            @RequestPart(value = "portraitImage", required = false) MultipartFile portraitImage) {
        String userId = HttpContextUtils.getUserIdFromContext();

        if (Objects.isNull(payload)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(payload.getName())
                || StringUtils.isEmpty(payload.getSecret())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String portraitImageUri = null;
        if (Objects.nonNull(portraitImage) && portraitImage.getSize() > 0) {
            portraitImageUri = this.imageService.uploadSingleImageAsync(portraitImage);
        }

        IslandMessage islandMessage = this.islandService.createIsland(
                payload.getName(), portraitImageUri, payload.getSecret(), userId);

        BriefIslandResponse response = new BriefIslandResponse();
        response.setData(this.islandDTOFactory.briefValueOf(islandMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the update island by id api.
     *
     * @param id            Island id.
     * @param payload       {@link PutIslandPayload}
     * @param portraitImage Portrait image to update.
     * @return {@link BriefIslandResponse}.
     */
    @Override
    public ResponseEntity<BriefIslandResponse> apiV1IslandsIdPut(
            String id,
            PutIslandPayload payload,
            @RequestPart(value = "portraitImage", required = false) MultipartFile portraitImage) {
        String userId = HttpContextUtils.getUserIdFromContext();

        if (Objects.isNull(payload)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);
        if (!userId.equals(islandMessage.getHostId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String portraitImageUri = null;
        if (Objects.nonNull(portraitImage) && portraitImage.getSize() > 0) {
            portraitImageUri = this.imageService.uploadSingleImageAsync(portraitImage);
        }

        islandMessage = this.islandService.updateIslandById(
                id, payload.getName(), portraitImageUri, payload.getSecret(), payload.getDescription());

        BriefIslandResponse response = new BriefIslandResponse();
        response.setData(this.islandDTOFactory.briefValueOf(islandMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the subscribe island by id api.
     *
     * @param id                     Island id.
     * @param subscribeIslandRequest {@link SubscribeIslandRequest}.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1IslandsIdSubscribePost(String id,
                                                                     SubscribeIslandRequest subscribeIslandRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        this.islandService.subscribeIslandById(id, userId, subscribeIslandRequest.getSecret());

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the unsubscribe island by id api.
     *
     * @param id Island id.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1IslandsIdUnsubscribePost(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();
        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);
        if (userId.equals(islandMessage.getHostId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        this.islandService.unsubscribeIslandById(id, userId);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get subscribers by island id api.
     *
     * @param id       Island id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link UsersResponse}.
     */
    @Override
    public ResponseEntity<UsersResponse> apiV1IslandsIdSubscribersGet(String id,
                                                                      Integer page,
                                                                      Integer pageSize) {
        IslandSubscribersResponse islandSubscribersResponse =
                this.islandService.retrieveSubscriberByIslandId(id, page, pageSize);

        UsersResponse response = new UsersResponse();
        response.setData(islandSubscribersResponse.getUserList()
                .stream()
                .map(this.userDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(islandSubscribersResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get reposts by island id api.
     *
     * @param id       Island id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link RepostsResponse}.
     */
    @Override
    public ResponseEntity<RepostsResponse> apiV1IslandsIdRepostsGet(String id,
                                                                    Integer page,
                                                                    Integer pageSize) {
        IslandRepostsResponse islandRepostsResponse =
                this.repostService.retrieveRepostIslandById(id, page, pageSize);

        RepostsResponse response = new RepostsResponse();
        response.setData(islandRepostsResponse.getIslandRepostsList()
                .stream()
                .map(this.repostDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(islandRepostsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the repost island api.
     *
     * @param id                Island id.
     * @param postRepostRequest {@link PostRepostRequest}.
     * @return {@link RepostResponse}.
     */
    @Override
    public ResponseEntity<RepostResponse> apiV1IslandsIdRepostsPost(String id,
                                                                    PostRepostRequest postRepostRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        IslandRepostMessage repostMessage = this.repostService.createRepostIslandById(
                id, userId, postRepostRequest.getContent(), postRepostRequest.getIsSuccessful());

        RepostResponse response = new RepostResponse();
        response.setData(this.repostDTOFactory.valueOf(repostMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Builds the {@link BriefIslandsResponse} from {@link IslandsResponse}.
     *
     * @param islandsResponse {@link IslandsResponse}.
     * @return {@link BriefIslandsResponse}.
     */
    private ResponseEntity<BriefIslandsResponse> BuildBriefIslandsResponse(IslandsResponse islandsResponse) {
        BriefIslandsResponse response = new BriefIslandsResponse();
        response.setData(islandsResponse.getIslandsList()
                .stream()
                .map(this.islandDTOFactory::briefValueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(islandsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
