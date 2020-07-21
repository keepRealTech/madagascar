package com.keepreal.madagascar.marty.service;

import com.google.common.collect.Lists;
import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenResponse;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensByUserIdListResponse;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensResponse;
import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.marty.model.PushType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PushService {

    private final UserService userService;
    private final IslandService islandService;
    private final ChatService chatService;
    private final UmengPushService umengPushService;
    private final JpushService jpushService;
    private final PushNotificationService pushNotificationService;

    /**
     * @param userService       {@link UserService}.
     * @param islandService     {@link IslandService}.
     * @param chatService       {@link ChatService}.
     * @param umengPushService  {@link UmengPushService}.
     * @param jpushService      {@link JpushService}.
     * @param pushNotificationService
     */
    public PushService(UserService userService,
                       IslandService islandService,
                       ChatService chatService, UmengPushService umengPushService,
                       JpushService jpushService,
                       PushNotificationService pushNotificationService) {
        this.userService = userService;
        this.islandService = islandService;
        this.chatService = chatService;
        this.umengPushService = umengPushService;
        this.jpushService = jpushService;
        this.pushNotificationService = pushNotificationService;
    }

    public void pushMessageByType(String userId, PushType pushType) {
        RetrieveDeviceTokenResponse response = userService.retrieveUserDeviceToken(userId);
        ProtocolStringList androidTokensList = response.getAndroidTokensList();
        ProtocolStringList iosTokensList = response.getIosTokensList();

        String androidTokens = androidTokensList.toString().substring(1, androidTokensList.toString().length() - 1);
        umengPushService.pushMessageByType(androidTokens, pushType);

        jpushService.pushIOSMessageByType(pushType, (String[]) iosTokensList.toArray());
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

            String androidTokens = androidTokensList.toString().substring(1, androidTokensList.toString().length() - 1);
            umengPushService.pushNewFeedByType(androidTokens, islandId, pushType);
            jpushService.pushIOSNewFeedMessage(islandId, pushType, (String[]) iosTokensList.toArray());
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
        androidPartition.forEach(list -> umengPushService.pushUpdateBulletin(list.toString().substring(1, list.toString().length() - 1), chatGroupId, bulletin, pushType));

        List<List<String>> iOSPartition = Lists.partition(iosTokensList, 800);
        iOSPartition.forEach(list -> jpushService.pushIOSUpdateBulletinMessage(chatGroupId, bulletin, pushType, (String[]) list.toArray()));
    }
}
