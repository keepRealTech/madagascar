package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.BillingInfo;
import com.keepreal.madagascar.vanga.repository.BillingInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.Objects;

/**
 * Represents the billing info service.
 */
@Service
public class BillingInfoService {

    private final BillingInfoRepository billingInfoRepository;
    private final LongIdGenerator idGenerator;

    /**
     * Constructs the billing info service.
     *
     * @param billingInfoRepository {@link BillingInfoRepository}.
     * @param idGenerator           {@link LongIdGenerator}.
     */
    public BillingInfoService(BillingInfoRepository billingInfoRepository,
                              LongIdGenerator idGenerator) {
        this.billingInfoRepository = billingInfoRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Retrieves or creates the billing info for a given user id.
     *
     * @param userId User id.
     * @return {@link BillingInfo}.
     */
    @Transactional
    public BillingInfo retrieveOrCreateBillingInfoIfNotExistsByUserId(String userId) {
        BillingInfo billingInfo = this.billingInfoRepository.findTopByUserIdAndDeletedIsFalse(userId);
        if (Objects.nonNull(billingInfo)) {
            return billingInfo;
        }

        return this.createNewBillingInfo(userId);
    }

    /**
     * Updates the billing info for a given user id.
     *
     * @param userId User id.
     * @return {@link BillingInfo}.
     */
    @Transactional
    public BillingInfo updateBillingInfoByUserId(String userId,
                                                 String name,
                                                 String mobile,
                                                 String accountNumber,
                                                 String idNumber,
                                                 String idFrontUrl,
                                                 String idBackUrl,
                                                 String aliPayAccount) {
        BillingInfo billingInfo = this.retrieveOrCreateBillingInfoIfNotExistsByUserId(userId);

        if (Objects.nonNull(name)) {
            billingInfo.setName(name);
        }

        if (Objects.nonNull(mobile)) {
            billingInfo.setMobile(mobile);
        }

        if (!StringUtils.isEmpty(accountNumber)) {
            billingInfo.setAccountNumber(accountNumber);
        }

        if (!StringUtils.isEmpty(idNumber)) {
            billingInfo.setIdNumber(idNumber);
        }

        if (!StringUtils.isEmpty(idFrontUrl)) {
            billingInfo.setIdFrontUrl(idFrontUrl);
        }

        if (!StringUtils.isEmpty(idBackUrl)) {
            billingInfo.setIdBackUrl(idBackUrl);
        }

        if (Objects.nonNull(aliPayAccount)) {
            billingInfo.setAliPayAccount(aliPayAccount);
        }

        billingInfo.setVerified(!StringUtils.isEmpty(name)
                && !StringUtils.isEmpty(mobile)
                && !StringUtils.isEmpty(accountNumber)
                && !StringUtils.isEmpty(idNumber)
                && !StringUtils.isEmpty(idFrontUrl)
                && !StringUtils.isEmpty(idBackUrl));

        return this.billingInfoRepository.save(billingInfo);
    }

    /**
     * Creates a new billing info for the given user id.
     *
     * @param userId User id.
     * @return {@link BillingInfo}.
     */
    @Transactional
    public BillingInfo createNewBillingInfo(String userId) {
        BillingInfo billingInfo = BillingInfo.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .userId(userId)
                .build();

        return this.billingInfoRepository.save(billingInfo);
    }

}
