package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ConfigApi;
import swagger.model.AndroidUpdateInfoResponse;
import swagger.model.ConfigType;
import swagger.model.ConfigurationDTO;
import swagger.model.ConfigurationResponse;
import swagger.model.IOSUpdateInfoResponse;
import swagger.model.UpdateInfoDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the configuration controller.
 */
@RestController
public class ConfigurationController implements ConfigApi {

    private Map<Integer, ConfigurationDTO> iOSConfigVersionMap = new HashMap<>();
    private Map<Integer, UpdateInfoDTO> iOSUpdateInfoMap = new HashMap<>();
    private Map<Integer, UpdateInfoDTO> androidUpdateInfoMap = new HashMap<>();

    public ConfigurationController() {
        this.iOSConfigVersionMap.put(
                100, this.createIOSConfigurationDTO(10,100,10,5,10,1000, true));
        UpdateInfoDTO updateInfoDTO = new UpdateInfoDTO();
        updateInfoDTO.address("example.com");
        updateInfoDTO.currentVersion(1);
        updateInfoDTO.nextVersion(1);
        updateInfoDTO.isLatest(true);
        updateInfoDTO.message("first version");
        updateInfoDTO.shouldForce(false);

        this.iOSUpdateInfoMap.put(updateInfoDTO.getCurrentVersion(), updateInfoDTO);
        this.androidUpdateInfoMap.put(updateInfoDTO.getCurrentVersion(), updateInfoDTO);
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
                configurationDTO = this.iOSConfigVersionMap.get(version);
            case ANDROID:
            default:
        }

        if (Objects.isNull(configurationDTO)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        ConfigurationResponse response = new ConfigurationResponse();
        response.setData(configurationDTO);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the android update info get api.
     *
     * @param version  (required).
     * @return {@link AndroidUpdateInfoResponse}.
     */
    @Override
    public ResponseEntity<AndroidUpdateInfoResponse> apiV1UpdateInfoAndroidGet(Integer version) {
        UpdateInfoDTO androidUpdateInfoDTO = this.androidUpdateInfoMap.get(version);
        if (Objects.isNull(androidUpdateInfoDTO)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        AndroidUpdateInfoResponse response = new AndroidUpdateInfoResponse();
        response.setData(androidUpdateInfoDTO);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the ios update info get api.
     *
     * @param version  (required).
     * @return {@link IOSUpdateInfoResponse}.
     */
    @Override
    public ResponseEntity<IOSUpdateInfoResponse> apiV1UpdateInfoIosGet(Integer version) {
        UpdateInfoDTO iosUpdateInfoDTO = this.iOSUpdateInfoMap.get(version);
        if (Objects.isNull(iosUpdateInfoDTO)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        IOSUpdateInfoResponse response = new IOSUpdateInfoResponse();
        response.setData(iosUpdateInfoDTO);
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
