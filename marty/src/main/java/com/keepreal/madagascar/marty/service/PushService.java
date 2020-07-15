package com.keepreal.madagascar.marty.service;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenResponse;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensResponse;
import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.marty.model.PushType;
import org.springframework.stereotype.Service;

@Service
public class PushService {

    private final UserService userService;
    private final IslandService islandService;
    private final UmengPushService umengPushService;
    private final JpushService jpushService;
    private final PushNotificationService pushNotificationService;

    /**
     * @param userService       {@link UserService}.
     * @param islandService     {@link IslandService}.
     * @param umengPushService  {@link UmengPushService}.
     * @param jpushService      {@link JpushService}.
     * @param pushNotificationService
     */
    public PushService(UserService userService,
                       IslandService islandService,
                       UmengPushService umengPushService,
                       JpushService jpushService,
                       PushNotificationService pushNotificationService) {
        this.userService = userService;
        this.islandService = islandService;
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
}
