package com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder;

import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.lemur.dtoFactory.IslandDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.UserDTOFactory;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.tenrecs.NoticeNotificationMessage;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import org.springframework.util.StringUtils;
import swagger.model.NoticeDTO;
import swagger.model.NoticeType;
import swagger.model.NotificationDTO;
import swagger.model.NotificationType;
import swagger.model.SkuMembershipDTO;

import java.util.Objects;

/**
 * Represents the island notice notification dto builder.
 */
public class NoticeNotificationDTOBuilder implements NotificationDTOBuilder {



    private NotificationMessage notificationMessage;
    private IslandService islandService;
    private IslandDTOFactory islandDTOFactory;
    private UserService userService;
    private UserDTOFactory userDTOFactory;

    /**
     * Sets the notification message.
     *
     * @param notificationMessage {@link NotificationMessage}.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    @Override
    public NoticeNotificationDTOBuilder setNotificationMessage(NotificationMessage notificationMessage) {
        this.notificationMessage = notificationMessage;
        return this;
    }

    /**
     * Sets the {@link IslandService}.
     *
     * @param islandService {@link IslandService}.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    public NoticeNotificationDTOBuilder setIslandService(IslandService islandService) {
        this.islandService = islandService;
        return this;
    }

    /**
     * Sets the {@link IslandDTOFactory}.
     *
     * @param islandDTOFactory {@link IslandDTOFactory}.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    public NoticeNotificationDTOBuilder setIslandDTOFactory(IslandDTOFactory islandDTOFactory) {
        this.islandDTOFactory = islandDTOFactory;
        return this;
    }

    /**
     * Sets the {@link UserService}.
     *
     * @param userService {@link UserService}.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    public NoticeNotificationDTOBuilder setUserService(UserService userService) {
        this.userService = userService;
        return this;
    }

    /**
     * Sets the {@link UserDTOFactory}.
     *
     * @param userDTOFactory {@link UserDTOFactory}.
     * @return {@link NoticeNotificationDTOBuilder}.
     */
    public NoticeNotificationDTOBuilder setUserDTOFactory(UserDTOFactory userDTOFactory) {
        this.userDTOFactory = userDTOFactory;
        return this;
    }

    /**
     * Builds the {@link NotificationDTO}.
     *
     * @return {@link NotificationDTO}.
     */
    @Override
    public NotificationDTO build() {
        if (Objects.isNull(this.notificationMessage)) {
            return null;
        }

        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(this.notificationMessage.getId());
        notificationDTO.setHasRead(this.notificationMessage.getHasRead());
        notificationDTO.setNotificationType(NotificationType.ISLAND_NOTICE);
        notificationDTO.setCreatedAt(this.notificationMessage.getTimestamp());
        notificationDTO.setNotice(this.valueOf(this.notificationMessage.getNoticeNotification()));

        return notificationDTO;
    }

    /**
     * Converts the {@link NoticeNotificationMessage} into {@link NoticeDTO}.
     *
     * @param noticeMessage {@link NoticeNotificationMessage}.
     * @return {@link NoticeDTO}.
     */
    private NoticeDTO valueOf(NoticeNotificationMessage noticeMessage) {
        if (Objects.isNull(noticeMessage)) {
            return null;
        }

        NoticeDTO noticeDTO = new NoticeDTO();

        switch (noticeMessage.getType()) {
            case NOTICE_TYPE_ISLAND_NEW_SUBSCRIBER:
                noticeDTO.setNoticeType(NoticeType.ISLAND_NOTICE_NEW_SUBSCRIBER);

                if (Objects.isNull(noticeMessage.getSubscribeNotice())) {
                    return noticeDTO;
                }

                noticeDTO.setIsland(
                        this.islandDTOFactory.briefValueOf(
                                this.islandService.retrieveIslandById(
                                        noticeMessage.getSubscribeNotice().getIslandId())));
                noticeDTO.setSubscriber(
                        this.userDTOFactory.briefValueOf(
                                this.userService.retrieveUserById(
                                        noticeMessage.getSubscribeNotice().getSubscriberId())));
                return noticeDTO;
            case NOTICE_TYPE_ISLAND_NEW_MEMBER:
                noticeDTO.setNoticeType(NoticeType.ISLAND_NOTICE_NEW_MEMBER);

                if (Objects.isNull(noticeMessage.getMemberNotice())) {
                    return noticeDTO;
                }

                noticeDTO.setMember(
                        this.userDTOFactory.briefValueOf(
                                this.userService.retrieveUserById(
                                        noticeMessage.getMemberNotice().getMemberId())));

                if (StringUtils.isEmpty(noticeMessage.getMemberNotice().getIslandId())) { // process support message
                    SkuMembershipDTO skuMembershipDTO = new SkuMembershipDTO();
                    skuMembershipDTO.setPriceInCents(noticeMessage.getMemberNotice().getPriceInCents());
                    noticeDTO.setMembership(skuMembershipDTO);
                    noticeDTO.setContent(Templates.LEMUR_NOTICE_SUPPORT_CONTENT);
                    noticeDTO.setPriceInCents(noticeMessage.getMemberNotice().getPriceInCents());
                    return noticeDTO;
                }

                noticeDTO.setIsland(
                        this.islandDTOFactory.briefValueOf(
                                this.islandService.retrieveIslandById(
                                        noticeMessage.getMemberNotice().getIslandId())));
                
                SkuMembershipDTO skuMembershipDTO = new SkuMembershipDTO();
                skuMembershipDTO.setId(noticeMessage.getMemberNotice().getMembershipId());
                skuMembershipDTO.setMembershipName(noticeMessage.getMemberNotice().getMembershipName());
                skuMembershipDTO.setPriceInCents(noticeMessage.getMemberNotice().getPriceInCents());
                skuMembershipDTO.setTimeInMonths(noticeMessage.getMemberNotice().getTimeInMonths());
                noticeDTO.setMembership(skuMembershipDTO);
                noticeDTO.setContent(noticeMessage.getMemberNotice().getPermanent() ?
                        String.format(Templates.LEMUR_NOTICE_MEMBER_PERMANENT_CONTENT, noticeMessage.getMemberNotice().getMembershipName()) :
                        String.format(Templates.LEMUR_NOTICE_MEMBER_CONTENT, noticeMessage.getMemberNotice().getTimeInMonths(), noticeMessage.getMemberNotice().getMembershipName()));
                noticeDTO.setPriceInCents(noticeMessage.getMemberNotice().getPriceInCents());
                return noticeDTO;
            case NOTICE_TYPE_FEED_NEW_PAYMENT:
                noticeDTO.setNoticeType(NoticeType.ISLAND_NOTICE_NEW_MEMBER);

                if (Objects.isNull(noticeMessage.getFeedPaymentNotice())) {
                    return noticeDTO;
                }

                SkuMembershipDTO skuMembership = new SkuMembershipDTO();
                skuMembership.setPriceInCents(noticeMessage.getFeedPaymentNotice().getPriceInCents());
                noticeDTO.setMembership(skuMembership);
                noticeDTO.setMember(
                        this.userDTOFactory.briefValueOf(
                                this.userService.retrieveUserById(
                                        noticeMessage.getFeedPaymentNotice().getUserId())));
                noticeDTO.setPriceInCents(noticeMessage.getFeedPaymentNotice().getPriceInCents());
                noticeDTO.setContent(Templates.LEMUR_NOTICE_FEED_PAYMENT_CONTENT);

                return noticeDTO;
            default:
        }

        return noticeDTO;
    }

}
