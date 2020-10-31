package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.SponsorGiftMessage;
import com.keepreal.madagascar.coua.SponsorMessage;
import com.keepreal.madagascar.lemur.dtoFactory.SponsorDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.SupportDTOFactory;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.PaymentService;
import com.keepreal.madagascar.lemur.service.SponsorService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.vanga.RetrieveSponsorHistoryResponse;
import com.keepreal.madagascar.vanga.SupportMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.SupportApi;
import swagger.model.PutSponsorRequest;
import swagger.model.SponsorGiftsResponse;
import swagger.model.SponsorHistoryResponse;
import swagger.model.SponsorshipResponse;
import swagger.model.SupportsResponse;
import swagger.model.SupportsResponseV2;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class SupportController implements SupportApi {

    private final PaymentService paymentService;
    private final IslandService islandService;
    private final SupportDTOFactory supportDTOFactory;
    private final SponsorService sponsorService;
    private final SponsorDTOFactory sponsorDTOFactory;

    public SupportController(PaymentService paymentService,
                             IslandService islandService,
                             SupportDTOFactory supportDTOFactory,
                             SponsorService sponsorService,
                             SponsorDTOFactory sponsorDTOFactory) {
        this.paymentService = paymentService;
        this.islandService = islandService;
        this.supportDTOFactory = supportDTOFactory;
        this.sponsorService = sponsorService;
        this.sponsorDTOFactory = sponsorDTOFactory;
    }

    @CrossOrigin
    @Override
    public ResponseEntity<SupportsResponse> apiV1IslandsIdSupportGet(String id) {
        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);
        SupportMessage supportMessage = this.paymentService.retrieveSupportInfo(islandMessage.getHostId());

        SupportsResponse response = new SupportsResponse();
        response.setData(this.supportDTOFactory.valueOf(supportMessage, id));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the support get api v2.
     *
     * @param id id (required) island id
     * @return {@link SupportsResponseV2}
     */
    @CrossOrigin
    @Override
    public ResponseEntity<SupportsResponseV2> apiV2IslandsIdSupportGet(String id) {
        SponsorMessage sponsorMessage = this.sponsorService.retrieveSponsorByIslandId(id);
        SupportsResponseV2 response = new SupportsResponseV2();
        response.setData(this.supportDTOFactory.valueOf(sponsorMessage, id));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the sponsor get api.
     *
     * @param id id (required) island id
     * @return {@link SponsorshipResponse}
     */
    @Override
    public ResponseEntity<SponsorshipResponse> apiV1IslandsIdSponsorsGet(String id) {
        SponsorMessage sponsorMessage = this.sponsorService.retrieveSponsorByIslandId(id);
        SponsorshipResponse response = new SponsorshipResponse();
        response.setRtn(ErrorCode.REQUEST_SUCC_VALUE);
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        response.setData(this.sponsorDTOFactory.valueOf(sponsorMessage));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the sponsor put api.
     *
     * @param id id (required) island id
     * @param putSponsorRequest  (required) {@link PutSponsorRequest}
     * @return {@link SponsorshipResponse}
     */
    @Override
    public ResponseEntity<SponsorshipResponse> apiV1IslandsIdSponsorsPut(String id, @Valid PutSponsorRequest putSponsorRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);
        if (!userId.equals(islandMessage.getHostId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        SponsorMessage sponsorMessage = this.sponsorService.updateSponsorGiftByIslandId(id,
                    putSponsorRequest.getDescription(),
                    putSponsorRequest.getGiftId(),
                    putSponsorRequest.getPricePerUnit());

        SponsorshipResponse response = new SponsorshipResponse();
        response.setRtn(ErrorCode.REQUEST_SUCC_VALUE);
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        response.setData(this.sponsorDTOFactory.valueOf(sponsorMessage));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the sponsor gifts get api.
     *
     * @param id id (required) island id
     * @return {@link SponsorGiftsResponse}.
     */
    @Override
    public ResponseEntity<SponsorGiftsResponse> apiV1IslandsIdSponsorsGiftsGet(String id) {
        List<SponsorGiftMessage> sponsorGiftMessages = this.sponsorService.retrieveSponsorGiftsByCondition(false);
        SponsorGiftsResponse response = new SponsorGiftsResponse();
        response.setRtn(ErrorCode.REQUEST_SUCC_VALUE);
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        response.setData(sponsorGiftMessages.stream()
                .map(this.sponsorDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the sponsor history get api.
     *
     * @param id id (required) 岛id
     * @param page page number (optional, default to 0) page number
     * @param pageSize size of a page (optional, default to 10) page size
     * @return {@link SponsorHistoryResponse}
     */
    @Override
    public ResponseEntity<SponsorHistoryResponse> apiV1IslandsIdSponsorsHistoryGet(String id, @Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer pageSize) {
        RetrieveSponsorHistoryResponse retrieveSponsorHistoryResponse = this.sponsorService.retrieveSponsorHistoryByIslandId(id, page, pageSize);
        SponsorHistoryResponse response = new SponsorHistoryResponse();
        response.setRtn(ErrorCode.REQUEST_SUCC_VALUE);
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        response.setPageInfo(PaginationUtils.getPageInfo(retrieveSponsorHistoryResponse.getPageResponse()));
        response.setData(retrieveSponsorHistoryResponse.getSponsorHistoryList().stream().map(this.sponsorDTOFactory::valueOf).collect(Collectors.toList()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
