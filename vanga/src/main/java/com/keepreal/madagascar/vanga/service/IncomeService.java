package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.IncomeDetail;
import com.keepreal.madagascar.vanga.model.IncomeProfile;
import com.keepreal.madagascar.vanga.model.IncomeSupport;
import com.keepreal.madagascar.vanga.repository.IncomeDetailRepository;
import com.keepreal.madagascar.vanga.repository.IncomeProfileRepository;
import com.keepreal.madagascar.vanga.repository.IncomeSupportRepository;
import com.keepreal.madagascar.vanga.util.AutoRedisLock;
import com.keepreal.madagascar.vanga.util.DateUtils;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeService {

    private final IncomeDetailRepository incomeDetailRepository;
    private final IncomeProfileRepository incomeProfileRepository;
    private final IncomeSupportRepository incomeSupportRepository;
    private final RedissonClient redissonClient;
    private final LongIdGenerator idGenerator;

    public IncomeService(IncomeDetailRepository incomeDetailRepository,
                         IncomeProfileRepository incomeProfileRepository,
                         IncomeSupportRepository incomeSupportRepository,
                         RedissonClient redissonClient,
                         LongIdGenerator idGenerator) {
        this.incomeDetailRepository = incomeDetailRepository;
        this.incomeProfileRepository = incomeProfileRepository;
        this.incomeSupportRepository = incomeSupportRepository;
        this.redissonClient = redissonClient;
        this.idGenerator = idGenerator;
    }

    public IncomeProfile findIncomeProfileByUserId(String userId) {
        return this.incomeProfileRepository.findIncomeProfileByUserIdAndDeletedIsFalse(userId);
    }

    public List<IncomeDetail> findIncomeDetailsByUserId(String userId) {
        return this.incomeDetailRepository.findIncomeDetailsByUserIdAndDeletedIsFalse(userId);
    }

    public Page<IncomeSupport> findIncomeSupportsByUserId(String userId, Pageable pageable) {
        return this.incomeSupportRepository.findIncomeSupportsByUserIdAndDeletedIsFalse(userId, pageable);
    }

    public IncomeDetail updateIncomeDetail(String userId, long timestamp, long amountInCents) {
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("income-detail-%s", userId))) {
            long startTimestamp = DateUtils.startOfMonthTimestamp(timestamp);
            IncomeDetail incomeDetail = this.incomeDetailRepository.findIncomeDetailByUserIdAndMonthTimestampAndDeletedIsFalse(userId, startTimestamp);
            if (incomeDetail == null) {
                incomeDetail = new IncomeDetail();
                incomeDetail.setAmountInCents(amountInCents);
                incomeDetail.setSupportCount(1);
                incomeDetail.setMonthTimestamp(startTimestamp);
                incomeDetail.setUserId(userId);
                return this.createIncomeDetail(incomeDetail);
            } else {
                incomeDetail.setSupportCount(incomeDetail.getSupportCount() + 1);
                incomeDetail.setAmountInCents(incomeDetail.getAmountInCents() + amountInCents);
                return this.incomeDetailRepository.save(incomeDetail);
            }
        }
    }

    public IncomeProfile updateIncomeProfile(String userId, long amountInCents) {
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("income-profile-%s", userId))) {
            IncomeProfile incomeProfile = this.incomeProfileRepository.findIncomeProfileByUserIdAndDeletedIsFalse(userId);
            if (incomeProfile == null) {
                incomeProfile = new IncomeProfile();
                incomeProfile.setSupportCountReal(1);
                incomeProfile.setSupportCountShow(1);
                incomeProfile.setAmountInCents(amountInCents);
                incomeProfile.setUserId(userId);
                return this.createIncomeProfile(incomeProfile);
            } else {
                incomeProfile.setSupportCountReal(incomeProfile.getSupportCountReal() + 1);
                incomeProfile.setSupportCountShow(incomeProfile.getSupportCountShow() + 1);
                incomeProfile.setAmountInCents(incomeProfile.getAmountInCents() + amountInCents);
                return this.incomeProfileRepository.save(incomeProfile);
            }
        }
    }

    public IncomeSupport updateIncomeSupport(String userId, String supporterId, long cents) {
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("income-support-%s", userId))) {
            IncomeSupport incomeSupport = this.incomeSupportRepository.findIncomeSupportByUserIdAndSupporterIdAndDeletedIsFalse(userId, supporterId);
            if (incomeSupport == null) {
                incomeSupport = new IncomeSupport();
                incomeSupport.setUserId(userId);
                incomeSupport.setSupporterId(supporterId);
                incomeSupport.setCents(cents);
                return this.createIncomeSupport(incomeSupport);
            } else {
                incomeSupport.setCents(incomeSupport.getCents() + cents);
                return this.incomeSupportRepository.save(incomeSupport);
            }
        }
    }

    private IncomeDetail createIncomeDetail(IncomeDetail incomeDetail) {
        incomeDetail.setId(String.valueOf(this.idGenerator.nextId()));
        return this.incomeDetailRepository.save(incomeDetail);
    }

    private IncomeProfile createIncomeProfile(IncomeProfile incomeProfile) {
        incomeProfile.setId(String.valueOf(this.idGenerator.nextId()));
        return this.incomeProfileRepository.save(incomeProfile);
    }

    private IncomeSupport createIncomeSupport(IncomeSupport incomeSupport) {
        incomeSupport.setId(String.valueOf(this.idGenerator.nextId()));
        return this.incomeSupportRepository.save(incomeSupport);
    }
}
