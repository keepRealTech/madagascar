package com.keepreal.madagascar.fossa.grpcController;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.fossa.AddFeedToCollectionRequest;
import com.keepreal.madagascar.fossa.CollectedFeedsResponse;
import com.keepreal.madagascar.fossa.FeedCollectionServiceGrpc;
import com.keepreal.madagascar.fossa.RemoveFeedToCollectionRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedsByUserIdRequest;
import com.keepreal.madagascar.fossa.model.FeedCollection;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.model.ReactionInfo;
import com.keepreal.madagascar.fossa.service.CommentService;
import com.keepreal.madagascar.fossa.service.FeedCollectionService;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.service.ReactionService;
import com.keepreal.madagascar.fossa.service.SubscribeMembershipService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@GRpcService
public class FeedCollectionGRpcController extends FeedCollectionServiceGrpc.FeedCollectionServiceImplBase {

    private final FeedCollectionService feedCollectionService;
    private final FeedInfoService feedInfoService;
    private final SubscribeMembershipService subscribeMembershipService;
    private final ReactionService reactionService;
    private final CommentService commentService;


    public FeedCollectionGRpcController(FeedCollectionService feedCollectionService,
                                        FeedInfoService feedInfoService,
                                        SubscribeMembershipService subscribeMembershipService,
                                        ReactionService reactionService,
                                        CommentService commentService) {
        this.feedCollectionService = feedCollectionService;
        this.feedInfoService = feedInfoService;
        this.subscribeMembershipService = subscribeMembershipService;
        this.reactionService = reactionService;
        this.commentService = commentService;
    }

    @Override
    public void retrieveFeedsByUserId(RetrieveFeedsByUserIdRequest request, StreamObserver<CollectedFeedsResponse> responseObserver) {
        String userId = request.getUserId();
        int pageSize = request.getPageSize();
        Page<FeedCollection> feedCollectionPage;
        if (request.hasTimestampAfter()) {
            long timestamp = request.getTimestampAfter().getValue();
            feedCollectionPage = this.feedCollectionService.findFeedCollectionsUpdatedTimeGE(userId, timestamp, pageSize);
        } else {
            long timestamp = request.hasTimestampBefore() ? request.getTimestampBefore().getValue() : System.currentTimeMillis();
            feedCollectionPage = this.feedCollectionService.findFeedCollectionsUpdatedTimeLE(userId, timestamp, pageSize);
        }

        List<String> feedIdList = feedCollectionPage.getContent().stream().map(FeedCollection::getFeedId).collect(Collectors.toList());
        List<FeedInfo> feedInfoList = this.feedInfoService.findByIds(feedIdList, false);
        List<String> myMembershipIds = this.subscribeMembershipService.retrieveMembershipIds(request.getUserId(), null);

        Set<String> likedFeedIds = this.reactionService.retrieveReactionsByFeedIdsAndUserId(feedIdList, request.getUserId()).stream()
                .map(ReactionInfo::getFeedId).collect(Collectors.toSet());

        Set<String> collectedFeedIds = this.feedCollectionService.findByFeedIdsAndUserId(feedIdList, request.getUserId()).stream()
                .map(FeedCollection::getFeedId).collect(Collectors.toSet());

        Map<String, List<CommentMessage>> commentMap = this.commentService.getLastCommentsByFeedIds(feedIdList, Constants.DEFAULT_FEED_LAST_COMMENT_COUNT);

        List<FeedMessage> feedMessageList = feedInfoList.stream()
                .map(info -> this.feedInfoService.getFeedMessage(info,
                        request.getUserId(),
                        myMembershipIds,
                        commentMap.getOrDefault(info.getId(), new ArrayList<>()),
                        likedFeedIds.contains(info.getId()),
                        collectedFeedIds.contains(info.getId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        responseObserver.onNext(CollectedFeedsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setPageResponse(PageRequestResponseUtils.buildPageResponse(feedCollectionPage))
                .addAllFeed(feedMessageList)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void addFeedToCollection(AddFeedToCollectionRequest request, StreamObserver<CommonStatus> responseObserver) {
        String userId = request.getUserId();
        String feedId = request.getFeedId();
        this.feedCollectionService.addFeedToCollection(userId, feedId);

        responseObserver.onNext(CommonStatusUtils.getSuccStatus());
        responseObserver.onCompleted();
    }

    @Override
    public void removeFeedToCollection(RemoveFeedToCollectionRequest request, StreamObserver<CommonStatus> responseObserver) {
        String userId = request.getUserId();
        String feedId = request.getFeedId();
        this.feedCollectionService.removeFeedToCollection(userId, feedId);

        responseObserver.onNext(CommonStatusUtils.getSuccStatus());
        responseObserver.onCompleted();
    }
}
