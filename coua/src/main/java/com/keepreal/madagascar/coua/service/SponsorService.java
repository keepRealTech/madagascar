package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.SponsorGiftMessage;
import com.keepreal.madagascar.coua.SponsorMessage;
import com.keepreal.madagascar.coua.UpdateSponsorByIslandIdRequest;
import com.keepreal.madagascar.coua.dao.SponsorGiftRepository;
import com.keepreal.madagascar.coua.dao.SponsorRepository;
import com.keepreal.madagascar.coua.model.Sponsor;
import com.keepreal.madagascar.coua.model.SponsorGift;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Represents the sponsor service.
 */
@Service
@Slf4j
public class SponsorService {
    private final SponsorRepository sponsorRepository;
    private final SponsorGiftRepository sponsorGiftRepository;
    private final LongIdGenerator idGenerator;
    private final SkuService skuService;

    /**
     * Constructs the sponsor service.
     *
     * @param sponsorRepository {@link SponsorRepository}
     * @param sponsorGiftRepository {@link SponsorGiftRepository}
     * @param idGenerator {@link LongIdGenerator}
     * @param skuService {@link SkuService}
     */
    public SponsorService(SponsorRepository sponsorRepository,
                          SponsorGiftRepository sponsorGiftRepository,
                          LongIdGenerator idGenerator,
                          SkuService skuService) {
        this.sponsorRepository = sponsorRepository;
        this.sponsorGiftRepository = sponsorGiftRepository;
        this.idGenerator = idGenerator;
        this.skuService = skuService;
    }

    /**
     * 通过岛id获取支持一下信息
     *
     * @param islandId 岛id
     * @return {@link Sponsor}
     */
    public Sponsor retrieveSponsorByIslandId(String islandId) {
        return this.sponsorRepository.findTopByIslandIdAndActiveIsTrueAndDeletedIsFalse(islandId);
    }

    public Sponsor retrieveSponsorByHostId(String hostId) {
        return this.sponsorRepository.findTopByHostIdAndActiveIsTrueAndDeletedIsFalse(hostId);
    }

    /**
     * 创建默认的支持一下信息
     *
     * @param islandId 岛id
     * @param hostId 岛主id
     * @return {@link Sponsor}
     */
    @Transactional
    public Sponsor createDefaultSponsor(String islandId, String hostId) {
        Sponsor sponsor = new Sponsor();
        sponsor.setId(String.valueOf(idGenerator.nextId()));
        sponsor.setHostId(hostId);
        sponsor.setIslandId(islandId);
        sponsor.setDescription(Constants.DEFAULT_SPONSOR_DESCRIPTION);
        sponsor.setPricePerUnit(Constants.DEFAULT_SPONSOR_GIFT_UNIT_PRICE);
        sponsor.setGiftId(Constants.DEFAULT_SPONSOR_GIFT_ID);
        return this.sponsorRepository.save(sponsor);
    }

    /**
     * Converts the {@link Sponsor} to the {@link SponsorMessage}
     *
     * @param sponsor {@link Sponsor}
     * @return {@link SponsorMessage}
     */
    public SponsorMessage getSponsorMessage(Sponsor sponsor) {
        if (Objects.isNull(sponsor)) {
            return null;
        }

        return SponsorMessage.newBuilder()
                .setId(sponsor.getId())
                .setHostId(sponsor.getHostId())
                .setIslandId(sponsor.getIslandId())
                .setDescription(sponsor.getDescription())
                .setPricePerUnit(sponsor.getPricePerUnit())
                .setGiftId(sponsor.getGiftId())
                .build();
    }

    /**
     * 根据条件获取支持一下礼物表情
     *
     * @param onlyDefault 是否为预设礼物
     * @return {@link SponsorGift}
     */
    public List<SponsorGift> retrieveSponsorGiftByCondition(boolean onlyDefault) {
        if (onlyDefault) {
            return this.sponsorGiftRepository.findAllByIsDefaultAndDeletedIsFalseOrderByCreatedTimeAsc(true);
        }
        return this.sponsorGiftRepository.findAllByDeletedIsFalseOrderByCreatedTimeAsc();
    }

    /**
     * 根据id获取支持一下礼物
     *
     * @param giftId gift id
     * @return {@link SponsorGift}
     */
    public SponsorGift retrieveSponsorGiftById(String giftId) {
        return this.sponsorGiftRepository.findTopByIdAndDeletedIsFalse(giftId);
    }

    /**
     * Converts the {@link SponsorGift} to the {@link SponsorGiftMessage}
     *
     * @param sponsorGift {@link SponsorGift}
     * @return {@link SponsorGiftMessage}
     */
    public SponsorGiftMessage getSponsorGiftMessage(SponsorGift sponsorGift) {
        if (Objects.isNull(sponsorGift)) {
            return null;
        }

        return SponsorGiftMessage.newBuilder()
                .setId(sponsorGift.getId())
                .setUri(sponsorGift.getUri())
                .setEmoji(sponsorGift.getEmoji())
                .setName(sponsorGift.getName())
                .setText(sponsorGift.getText())
                .build();
    }

    /**
     * Updates sponsor
     *
     * @param sponsor {@link Sponsor}
     * @return {@link Sponsor}
     */
    public Sponsor updateSponsor(Sponsor sponsor) {
        return this.sponsorRepository.save(sponsor);
    }

    /**
     * Updates sponsor and skus;
     *
     * @param sponsor {@link Sponsor}
     * @param request {@link UpdateSponsorByIslandIdRequest}
     * @return {@link Sponsor}
     */
    public Sponsor updateSponsorAndSku(Sponsor sponsor, UpdateSponsorByIslandIdRequest request) {
        String newGiftId = null;
        if (request.hasGiftId()) {
            newGiftId = request.getGiftId().getValue();
            sponsor.setGiftId(newGiftId);
        }

        Long newPricePerUnit = null;
        if (request.hasPricePerUnit()) {
            newPricePerUnit = request.getPricePerUnit().getValue();
            sponsor.setPricePerUnit(newPricePerUnit);
        }

        if (request.hasDescription()) {
            sponsor.setDescription(request.getDescription().getValue());
        }

        this.skuService.updateSponsorSkusBySponsorId(sponsor.getId(), newGiftId, newPricePerUnit);
        return this.updateSponsor(sponsor);
    }

}
