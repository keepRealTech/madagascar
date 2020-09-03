package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.SupportSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportSkuRepository extends JpaRepository<SupportSku, String> {

    List<SupportSku> findAllByActiveIsTrueAndDeletedIsFalseOrderByPriceInCents();
}
