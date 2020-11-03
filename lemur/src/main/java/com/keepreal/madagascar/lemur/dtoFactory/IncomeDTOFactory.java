package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.vanga.FeedChargeMessage;
import com.keepreal.madagascar.vanga.IncomeDetailMessage;
import com.keepreal.madagascar.vanga.IncomeMonthlyMessage;
import com.keepreal.madagascar.vanga.IncomeProfileMessage;
import com.keepreal.madagascar.vanga.RetrieveCurrentMonthResponse;
import com.keepreal.madagascar.vanga.SponsorIncomeMessage;
import com.keepreal.madagascar.vanga.SupportListMessage;
import com.keepreal.madagascar.vanga.SupportMembershipMessage;
import org.springframework.stereotype.Component;
import swagger.model.CurrentMonthSupportDTO;
import swagger.model.CurrentMonthSupportDTOSponsor;
import swagger.model.IncomeDetailsDTO;
import swagger.model.IncomeMonthlyDTO;
import swagger.model.IncomeProfileDTO;
import swagger.model.IncomeSupportDTO;
import swagger.model.SupportMembershipDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IncomeDTOFactory {

    private final UserService userService;
    private final IslandService islandService;
    private final UserDTOFactory userDTOFactory;

    public IncomeDTOFactory(UserService userService,
                            IslandService islandService,
                            UserDTOFactory userDTOFactory) {
        this.userService = userService;
        this.islandService = islandService;
        this.userDTOFactory = userDTOFactory;
    }

    public CurrentMonthSupportDTO valueOf(RetrieveCurrentMonthResponse response) {
        CurrentMonthSupportDTO dto = new CurrentMonthSupportDTO();
        dto.setMemberships(response.getMembershipMessageList().stream().map(this::valueOf).collect(Collectors.toList()));
        dto.setSponsor(this.valueOf(response.getSponsorMessage()));
        dto.setFeedCharge(this.valueOf(response.getFeedChargeMessage()));

        return dto;
    }

    public IncomeMonthlyDTO valueOf(IncomeMonthlyMessage message) {
        IncomeMonthlyDTO dto = new IncomeMonthlyDTO();
        dto.setCurrentMonthIncome(message.getCurrentMonthIncome());
        dto.setSupportCount(message.getSupportCount());
        dto.setMonthTimestamp(message.getMonthTimestamp());

        return dto;
    }

    public IncomeProfileDTO valueOf(IncomeProfileMessage message, String userId) {
        IncomeProfileDTO dto = new IncomeProfileDTO();
        dto.setTotalIncome(message.getTotalIncome());
        List<IslandMessage> islandMessages = this.islandService.retrieveIslandsByHostId(userId);
        dto.setTotalSubscriber(islandMessages.isEmpty() ? 0 : islandMessages.get(0).getMemberCount());
        dto.setTotalSupportCount(message.getTotalSupportCountReal());
        dto.setCurrentMonthIncome(message.getCurrentMonthIncome());
        dto.setCurrentMonthSupportCount(message.getCurrentMonthSupportCount());
        dto.setNextMonthIncome(message.getNextMonthIncome());

        return dto;
    }

    public IncomeSupportDTO valueOf(SupportListMessage message) {
        IncomeSupportDTO dto = new IncomeSupportDTO();
        dto.setAmountInCents(message.getAmountInCents());
        dto.setUser(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(message.getUserId())));

        return dto;
    }

    public IncomeDetailsDTO valueOf(IncomeDetailMessage message) {
        IncomeDetailsDTO dto = new IncomeDetailsDTO();
        dto.setUser(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(message.getUserId())));
        dto.setContent(message.getContent());
        dto.setAmountInCents(message.getAmountInCents());
        dto.setTimestamp(message.getTimestamp());

        return dto;
    }

    private SupportMembershipDTO valueOf(SupportMembershipMessage message) {
        SupportMembershipDTO dto = new SupportMembershipDTO();
        dto.setId(message.getMembershipId());
        dto.setName(message.getMembershipName());
        dto.setPriceInMonth(message.getPriceInMonth());
        dto.setIsPermanent(message.getIsPermanent());
        dto.setSupportCount(message.getSupportCount());
        dto.setIncome(message.getIncome());

        return dto;
    }

    private CurrentMonthSupportDTOSponsor valueOf(SponsorIncomeMessage message) {
        CurrentMonthSupportDTOSponsor dtoSponsor = new CurrentMonthSupportDTOSponsor();
        dtoSponsor.setIncome(message.getIncome());
        dtoSponsor.setSupportCount(message.getSupportCount());
        dtoSponsor.setName(Templates.INCOME_DTO_SPONSOR);

        return dtoSponsor;
    }

    private CurrentMonthSupportDTOSponsor valueOf(FeedChargeMessage message) {
        CurrentMonthSupportDTOSponsor dtoSponsor = new CurrentMonthSupportDTOSponsor();
        dtoSponsor.setIncome(message.getIncome());
        dtoSponsor.setSupportCount(message.getSupportCount());
        dtoSponsor.setName(Templates.INCOME_DTO_FEED_CHARGE);

        return dtoSponsor;
    }
}
