package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.coua.SponsorGiftMessage;
import com.keepreal.madagascar.coua.SponsorMessage;
import com.keepreal.madagascar.lemur.service.SponsorService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.vanga.SponsorHistoryMessage;
import org.springframework.stereotype.Component;
import swagger.model.GiftImageDTO;
import swagger.model.SponsorHistoryDTO;
import swagger.model.SponsorshipDTO;

import java.util.stream.Collectors;

/**
 * Represents the sku dto factory.
 */
@Component
public class SponsorDTOFactory {

    private final SponsorService sponsorService;
    private final UserDTOFactory userDTOFactory;
    private final UserService userService;

    /**
     * Constructs the SponsorDTOFactory.
     *
     * @param sponsorService {@link SponsorService}
     * @param userDTOFactory {@link UserDTOFactory}
     * @param userService {@link UserService}
     */
    public SponsorDTOFactory(SponsorService sponsorService,
                             UserDTOFactory userDTOFactory,
                             UserService userService) {
        this.sponsorService = sponsorService;
        this.userDTOFactory = userDTOFactory;
        this.userService = userService;
    }

    /**
     * Converts the {@link SponsorGiftMessage} to the {@link GiftImageDTO}
     *
     * @param giftMessage {@link SponsorGiftMessage}
     * @return {@link GiftImageDTO}
     */
    public GiftImageDTO valueOf(SponsorGiftMessage giftMessage) {
        GiftImageDTO dto = new GiftImageDTO();
        dto.setId(giftMessage.getId());
        dto.setUri(giftMessage.getUri());
        dto.setEmoji(giftMessage.getEmoji());
        dto.setName(giftMessage.getName());
        dto.setText(giftMessage.getText());
        return dto;
    }

    /**
     * Converts the {@link SponsorMessage} to the {@link SponsorshipDTO}
     *
     * @param sponsorMessage {@link SponsorMessage}
     * @return {@link SponsorshipDTO}
     */
    public SponsorshipDTO valueOf(SponsorMessage sponsorMessage) {
        SponsorshipDTO dto = new SponsorshipDTO();
        dto.setDescription(sponsorMessage.getDescription());
        dto.setDefaultGifts(this.sponsorService.retrieveSponsorGiftsByCondition(true)
                .stream()
                .map(this::valueOf)
                .collect(Collectors.toList()));
        dto.setGift(this.valueOf(this.sponsorService.retrieveSponsorGiftByGiftId(sponsorMessage.getGiftId())));
        dto.setPricePerUnit(sponsorMessage.getPricePerUnit());
        dto.setHost(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(sponsorMessage.getHostId())));
        return dto;
    }

    /**
     * Converts the {@link SponsorHistoryMessage} to the {@link SponsorHistoryDTO}
     *
     * @param message {@link SponsorHistoryMessage}
     * @return {@link SponsorHistoryDTO}
     */
    public SponsorHistoryDTO valueOf(SponsorHistoryMessage message) {
        SponsorHistoryDTO sponsorHistoryDTO = new SponsorHistoryDTO();
        sponsorHistoryDTO.setName(userService.retrieveUserById(message.getUserId()).getName());
        sponsorHistoryDTO.setGift(this.valueOf(this.sponsorService.retrieveSponsorGiftByGiftId(message.getGiftId())));
        return sponsorHistoryDTO;
    }

}
