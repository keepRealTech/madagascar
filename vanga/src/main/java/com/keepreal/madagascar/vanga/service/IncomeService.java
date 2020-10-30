package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.IncomeProfileMessage;
import com.keepreal.madagascar.vanga.model.IncomeDetail;
import com.keepreal.madagascar.vanga.model.IncomeProfile;
import com.keepreal.madagascar.vanga.model.IncomeSupport;
import com.keepreal.madagascar.vanga.repository.IncomeDetailRepository;
import com.keepreal.madagascar.vanga.repository.IncomeProfileRepository;
import com.keepreal.madagascar.vanga.repository.IncomeSupportRepository;
import com.keepreal.madagascar.vanga.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeService {

    private final IncomeDetailRepository incomeDetailRepository;
    private final IncomeProfileRepository incomeProfileRepository;
    private final IncomeSupportRepository incomeSupportRepository;
    private final PaymentRepository paymentRepository;

    public IncomeService(IncomeDetailRepository incomeDetailRepository,
                         IncomeProfileRepository incomeProfileRepository,
                         IncomeSupportRepository incomeSupportRepository,
                         PaymentRepository paymentRepository) {
        this.incomeDetailRepository = incomeDetailRepository;
        this.incomeProfileRepository = incomeProfileRepository;
        this.incomeSupportRepository = incomeSupportRepository;
        this.paymentRepository = paymentRepository;
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

    // TODO: add lock
    public IncomeDetail updateIncomeDetail(String userId, long timestamp, int supprotCount, long amountInCents) {

        return null;
    }

    public IncomeProfile updateIncomeProfile(String userId, int supportCount, long amountInCents) {

        return null;
    }

    public IncomeSupport updateIncomeSupport(String userId, String supporterId, long cents) {

        return null;
    }
}
