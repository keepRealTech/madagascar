package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.SponsorHistoryMessage;
import com.keepreal.madagascar.vanga.model.Order;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.SponsorHistory;
import com.keepreal.madagascar.vanga.model.SponsorSku;
import com.keepreal.madagascar.vanga.repository.SponsorHistoryRepository;
import com.keepreal.madagascar.vanga.util.AutoRedisLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Represents the sponsor history service.
 */
@Service
public class SponsorHistoryService {
    private final SponsorHistoryRepository sponsorHistoryRepository;
    private final RedissonClient redissonClient;
    private final SkuService skuService;
    private final LongIdGenerator idGenerator;

    /**
     * Constructs the sponsor history service.
     *
     * @param sponsorHistoryRepository {@link SponsorHistoryRepository}
     * @param redissonClient {@link RedissonClient}
     * @param skuService {@link SkuService}
     * @param idGenerator {@link LongIdGenerator}
     */
    public SponsorHistoryService(SponsorHistoryRepository sponsorHistoryRepository,
                                 RedissonClient redissonClient,
                                 SkuService skuService,
                                 LongIdGenerator idGenerator) {
        this.sponsorHistoryRepository = sponsorHistoryRepository;
        this.redissonClient = redissonClient;
        this.skuService = skuService;
        this.idGenerator = idGenerator;
    }

    /**
     * 获取支持一下历史总数
     *
     * @param islandId 岛id
     * @return 支持一下历史总数
     */
    public Long retrieveSponsorHistoryCountByIslandId(String islandId) {
        return this.sponsorHistoryRepository.getSponsorCountByIslandId(islandId);
    }

    /**
     * 获取支持一下历史
     *
     * @param islandId 岛id
     * @param pageable 分页参数
     * @return {@link SponsorHistory}
     */
    public Page<SponsorHistory> retrieveSponsorHistoryOrderByCreatedTimeDesc(String islandId, Pageable pageable) {
        return this.sponsorHistoryRepository.findAllByIslandIdAndDeletedIsFalseOrderByCreatedTimeDesc(islandId, pageable);
    }

    /**
     * Converts the {@link SponsorHistory} to the {@link SponsorHistoryMessage}.
     *
     * @param sponsorHistory {@link SponsorHistory}
     * @return {@link SponsorHistoryMessage}
     */
    public SponsorHistoryMessage getSponsorHistoryMessage(SponsorHistory sponsorHistory) {
        if (Objects.isNull(sponsorHistory)) {
            return null;
        }

        return SponsorHistoryMessage.newBuilder()
                .setUserId(sponsorHistory.getUserId())
                .setIslandId(sponsorHistory.getIslandId())
                .setHostId(sponsorHistory.getHostId())
                .setSponsorId(sponsorHistory.getSponsorId())
                .setGiftId(sponsorHistory.getGiftId())
                .setCostInCents(sponsorHistory.getCostInCents())
                .build();
    }

    /**
     * Sponsors with order.
     *
     * @param order {@link Order}.
     */
    @Transactional
    public void addSponsorHistoryWithOrderAndPayment(Order order, Payment payment) {
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("sponsor-%s", order.getUserId()))) {

            SponsorSku sku = this.skuService.retrieveSponsorSkuById(order.getPropertyId());
            if (Objects.isNull(sku)) {
                return;
            }

            long quantity;

            if (sku.getCustomSku()) {
                quantity = payment.getAmountInCents() / sku.getPriceInCents();
            } else {
                quantity = sku.getQuantity();
            }

            List<SponsorHistory> newSponsorHistory = new ArrayList<>();

            LongStream.range(0, quantity)
                    .forEach(i -> {
                        newSponsorHistory.add(SponsorHistory.builder()
                                .id(String.valueOf(this.idGenerator.nextId()))
                                .userId(payment.getUserId())
                                .islandId(sku.getIslandId())
                                .hostId(payment.getPayeeId())
                                .sponsorId(sku.getSponsorId())
                                .giftId(sku.getGiftId())
                                .costInCents(payment.getAmountInCents())
                                .build());
                    });

            //todo 测试开启batch_size 表现
            this.savaAll(newSponsorHistory);
        }
    }

    /**
     * add sponsor history
     *
     * @param sponsorHistoryIterable {@link SponsorHistory}
     * @return {@link SponsorHistory}
     */
    public List<SponsorHistory> savaAll(Iterable<SponsorHistory> sponsorHistoryIterable) {
        return this.sponsorHistoryRepository.saveAll(sponsorHistoryIterable);
    }

}
