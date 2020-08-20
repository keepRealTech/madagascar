package com.keepreal.madagascar.marty.service;

import com.google.common.collect.Lists;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenResponse;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensByUserIdListResponse;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensResponse;
import com.keepreal.madagascar.fossa.FeedResponse;
import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.mantella.FeedUpdateEvent;
import com.keepreal.madagascar.marty.model.PushType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PushService {

    private final UserService userService;
    private final IslandService islandService;
    private final ChatService chatService;
    private final UmengPushService umengPushService;
    private final JpushService jpushService;
    private final PushNotificationService pushNotificationService;
    private final FeedService feedService;

    /**
     * @param userService       {@link UserService}.
     * @param islandService     {@link IslandService}.
     * @param chatService       {@link ChatService}.
     * @param umengPushService  {@link UmengPushService}.
     * @param jpushService      {@link JpushService}.
     * @param pushNotificationService {@link PushNotificationService}
     * @param feedService {@link FeedService}
     */
    public PushService(UserService userService,
                       IslandService islandService,
                       ChatService chatService, UmengPushService umengPushService,
                       JpushService jpushService,
                       PushNotificationService pushNotificationService,
                       FeedService feedService) {
        this.userService = userService;
        this.islandService = islandService;
        this.chatService = chatService;
        this.umengPushService = umengPushService;
        this.jpushService = jpushService;
        this.pushNotificationService = pushNotificationService;
        this.feedService = feedService;
    }

    public void pushMessageByType(String userId, PushType pushType) {
        RetrieveDeviceTokenResponse response = userService.retrieveUserDeviceToken(userId);
        ProtocolStringList androidTokensList = response.getAndroidTokensList();
        ProtocolStringList iosTokensList = response.getIosTokensList();

        umengPushService.pushMessageByType(String.join(",", androidTokensList), pushType);

        jpushService.pushIOSMessageByType(pushType, iosTokensList.toArray(new String[0]));
    }

    public void pushNewFeed(FeedCreateEvent event, PushType pushType) {
        int page = 0;
        int pageSize = 500;
        RetrieveDeviceTokensResponse response;
        String islandId = event.getIslandId();
        do {
            PageRequest pageRequest = PageRequest.newBuilder()
                    .setPage(page++)
                    .setPageSize(pageSize)
                    .build();
            response = islandService.getDeviceTokenList(islandId, pageRequest);

            ProtocolStringList androidTokensList = response.getAndroidTokensList();
            ProtocolStringList iosTokensList = response.getIosTokensList();

            umengPushService.pushNewFeedByType(String.join(",", androidTokensList), islandId, pushType);
            jpushService.pushIOSNewFeedMessage(islandId, pushType, iosTokensList.toArray(new String[0]));
            if (event.getFromHost()) {
                pushNotificationService.jPushIosNewFeedNotification(event.getAuthorId(), event.getFeedId(), iosTokensList);
                pushNotificationService.umengPushAndroidNewFeedNotification(event.getAuthorId(), event.getFeedId(), androidTokensList);
            }
        } while (response.getPageResponse().getHasMore());
    }

    public void pushUpdateBulletinMessage(String chatGroupId, String userId, String bulletin, PushType pushType) {
        List<String> userIdList = chatService.retrieveChatgroupMemberIds(chatGroupId, userId);

        RetrieveDeviceTokensByUserIdListResponse response = userService.retrieveDeviceTokensByUserIdList(userIdList);

        ProtocolStringList androidTokensList = response.getAndroidTokensList();
        ProtocolStringList iosTokensList = response.getIosTokensList();

        List<List<String>> androidPartition = Lists.partition(androidTokensList, 500);
        androidPartition.forEach(list -> umengPushService.pushUpdateBulletin(String.join(",", list), chatGroupId, bulletin, pushType));

        List<List<String>> iOSPartition = Lists.partition(iosTokensList, 800);
        iOSPartition.forEach(list -> jpushService.pushIOSUpdateBulletinMessage(chatGroupId, bulletin, pushType, list.toArray(new String[0])));
    }

    /**
     * 有问题时 向被提问者(岛主)推送通知消息
     *
     * @param event    {@link FeedCreateEvent}
     * @param hostId   岛主 user id
     */
    public void pushNewQuestion(FeedCreateEvent event, String hostId) {

        RetrieveDeviceTokenResponse response = userService.retrieveUserDeviceToken(hostId);

        ProtocolStringList androidTokensList = response.getAndroidTokensList();
        ProtocolStringList iosTokensList = response.getIosTokensList();

        this.pushNotificationService.umengPushAndroidNewQuestionNotification(event.getAuthorId(), event.getFeedId(), androidTokensList);
        this.pushNotificationService.jPushIosNewQuestionNotificationNotification(event.getAuthorId(), event.getFeedId(), iosTokensList);
    }

    /**
     * 有回答时 向提问者推送通知消息 和 向全体岛民推送通知消息(当岛主允许查看回答时)
     *
     * @param event   {@link FeedUpdateEvent}
     */
    public void pushNewReply(FeedUpdateEvent event) {
        String feedId = event.getFeedId();
        String authorId = event.getAuthorId();

        FeedResponse feedResponse = this.feedService.retrieveFeedInfoById(feedId, authorId);

        boolean publicVisible = feedResponse.getFeed().getQuestion().getPublicVisible().getValue();
        if (publicVisible) {
            this.pushNewReplyToAllSubscriber(event);
        }
        RetrieveDeviceTokenResponse response = userService.retrieveUserDeviceToken(authorId);
        ProtocolStringList androidTokensList = response.getAndroidTokensList();
        ProtocolStringList iosTokensList = response.getIosTokensList();

        this.pushNotificationService.umengPushAndroidNewReplyNotification(event.getAuthorId(), event.getFeedId(),
                androidTokensList, false);
        this.pushNotificationService.jPushIosNewReplyNotificationNotification(event.getAuthorId(), event.getFeedId(),
                iosTokensList, false);
    }

    /**
     * 向该岛所有岛民推送回答通知消息
     *
     * @param event {@link FeedUpdateEvent}
     */
    private void pushNewReplyToAllSubscriber(FeedUpdateEvent event) {
        int page = 0;
        int pageSize = 500;
        RetrieveDeviceTokensResponse response;
        String islandId = event.getIslandId();
        do {
            PageRequest pageRequest = PageRequest.newBuilder()
                    .setPage(page++)
                    .setPageSize(pageSize)
                    .build();
            response = islandService.getDeviceTokenList(islandId, pageRequest);

            ProtocolStringList androidTokensList = response.getAndroidTokensList();
            ProtocolStringList iosTokensList = response.getIosTokensList();

            pushNotificationService.jPushIosNewReplyNotificationNotification(event.getAuthorId(), event.getFeedId(),
                    iosTokensList, true);
            pushNotificationService.umengPushAndroidNewReplyNotification(event.getAuthorId(),
                    event.getFeedId(), androidTokensList, true);

        } while (response.getPageResponse().getHasMore());
    }

}
