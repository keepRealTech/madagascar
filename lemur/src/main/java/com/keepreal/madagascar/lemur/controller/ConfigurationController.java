package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ConfigApi;
import swagger.model.ConfigType;
import swagger.model.ConfigurationDTO;
import swagger.model.ConfigurationResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the configuration controller.
 */
@RestController
public class ConfigurationController implements ConfigApi {

    private Map<Integer, ConfigurationDTO> IOSConfigVersionMap = new HashMap<>();

    public ConfigurationController() {
        IOSConfigVersionMap.put(0, this.createIOSConfigurationDTO(null,null,null,null,null,null,null));
        IOSConfigVersionMap.put(100, this.createIOSConfigurationDTO(5,100,5,5,5,1000, true));
    }

    /**
     * Implements the get configuration api.
     *
     * @param configType  (required) {@link ConfigType}.
     * @return {@link ConfigurationResponse}.
     */
    @Cacheable(value = "config")
    @Override
    public ResponseEntity<ConfigurationResponse> apiV1ConfigsGet(ConfigType configType, Integer version) {
        ConfigurationDTO configurationDTO = new ConfigurationDTO();
        switch (configType) {
            case IOS:
                configurationDTO = IOSConfigVersionMap.getOrDefault(version, IOSConfigVersionMap.get(0));
            case ANDROID:
            default:
        }

        ConfigurationResponse response = new ConfigurationResponse();
        response.setData(configurationDTO);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Create ConfigurationDTO
     *
     * @param islandFeedLoopInterval
     * @param myIslandsPageSize
     * @param messageLoopInterval
     * @param guestPageSize
     * @param islandCheckInterval
     * @param configTimeout
     * @param audit
     * @return {@link ConfigurationDTO}
     */
    private ConfigurationDTO createIOSConfigurationDTO(Integer islandFeedLoopInterval,
                                                              Integer myIslandsPageSize,
                                                              Integer messageLoopInterval,
                                                              Integer guestPageSize,
                                                              Integer islandCheckInterval,
                                                              Integer configTimeout,
                                                              Boolean audit) {
        ConfigurationDTO configurationDTO = new ConfigurationDTO();
        configurationDTO.setIslandFeedLoopInterval(islandFeedLoopInterval);
        configurationDTO.setMyIslandsPageSize(myIslandsPageSize);
        configurationDTO.setMessageLoopInterval(messageLoopInterval);
        configurationDTO.setGuestPageSize(guestPageSize);
        configurationDTO.setIslandCheckInterval(islandCheckInterval);
        configurationDTO.setConfigTimeout(configTimeout);
        configurationDTO.setAudit(audit);
        return configurationDTO;
    }
}
