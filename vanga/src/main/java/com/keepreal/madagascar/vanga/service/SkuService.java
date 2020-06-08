package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.repository.ShellSkuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuService {

    private final ShellSkuRepository shellSkuRepository;

    public SkuService(ShellSkuRepository shellSkuRepository) {
        this.shellSkuRepository = shellSkuRepository;
    }

    /**
     * Retrieves all active shell skus.
     *
     * @return {@link ShellSku}.
     */
    public List<ShellSku> retrieveShellSkusByActiveIsTrue() {
        return this.shellSkuRepository.findAllByActiveIsTrueAndDeletedIsFalse();
    }

    
}
