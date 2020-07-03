package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.ShellSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the shell sku repository.
 */
@Repository
public interface ShellSkuRepository extends JpaRepository<ShellSku, String> {

    List<ShellSku> findAllByActiveIsTrueAndIsWechatPayAndDeletedIsFalse(Boolean isWechatPay);

}
