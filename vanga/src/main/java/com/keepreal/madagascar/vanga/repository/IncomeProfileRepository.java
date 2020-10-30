package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.IncomeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeProfileRepository extends JpaRepository<IncomeProfile, String> {

    IncomeProfile findIncomeProfileByUserIdAndDeletedIsFalse(String userId);
}
