package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.config.AndroidClientConfiguration;
import com.keepreal.madagascar.lemur.config.IOSClientConfiguration;
import com.keepreal.madagascar.lemur.releaseManager.ReleaseManager;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ConfigApi;
import swagger.model.AndroidSetupInfoResponse;
import swagger.model.AndroidUpdateInfoResponse;
import swagger.model.ConfigType;
import swagger.model.ConfigurationDTO;
import swagger.model.ConfigurationResponse;
import swagger.model.IOSUpdateInfoResponse;
import swagger.model.SetupInfoDTO;
import swagger.model.UpdateInfoDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the configuration controller.
 */
@RestController
public class ConfigurationController implements ConfigApi {

    private final ReleaseManager releaseManager;
    private final SetupInfoDTO androidSetupInfoDTO;
    private final Map<Integer, ConfigurationDTO> iOSConfigVersionMap = new HashMap<>();
    private final Map<Integer, Map<String, Boolean>> androidConfigVersionMap = new HashMap<>();
    private final Map<Integer, UpdateInfoDTO> iOSUpdateInfoMap = new HashMap<>();
    private final Map<Integer, UpdateInfoDTO> androidUpdateInfoMap = new HashMap<>();

    /**
     * Constructs the configurations controller.
     *
     * @param releaseManager             {@link ReleaseManager}.
     * @param iosClientConfiguration     IOS client configuration.
     * @param androidClientConfiguration Android client configuration.
     */
    public ConfigurationController(ReleaseManager releaseManager,
                                   IOSClientConfiguration iosClientConfiguration,
                                   AndroidClientConfiguration androidClientConfiguration) {
        this.releaseManager = releaseManager;
        this.androidSetupInfoDTO = androidClientConfiguration.getSetupInfo();
        this.androidConfigVersionMap.putAll(androidClientConfiguration.getVersionInfoMap());
        this.iOSConfigVersionMap.putAll(iosClientConfiguration.getVersionInfoMap());
        this.androidUpdateInfoMap.putAll(androidClientConfiguration.getUpdateInfoMap());
        this.iOSUpdateInfoMap.putAll(iosClientConfiguration.getUpdateInfoMap());
    }

    /**
     * Implements the get configuration api.
     *
     * @param configType (required) {@link ConfigType}.
     * @return {@link ConfigurationResponse}.
     */
    @Cacheable(value = "config")
    @Override
    public ResponseEntity<ConfigurationResponse> apiV1ConfigsGet(ConfigType configType, Integer version, String channel) {
        ConfigurationDTO configurationDTO = new ConfigurationDTO();
        switch (configType) {
            case IOS:
                configurationDTO = this.iOSConfigVersionMap.get(version);
                break;
            case ANDROID:
                configurationDTO = this.createAndroidConfigurationDTO(version, channel);
                break;
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
     * @param version (required).
     * @return {@link AndroidUpdateInfoResponse}.
     */
    @Override
    public ResponseEntity<AndroidUpdateInfoResponse> apiV1UpdateInfoAndroidGet(Integer version) {
        UpdateInfoDTO androidUpdateInfoDTO = this.androidUpdateInfoMap.get(version);
        if (Objects.isNull(androidUpdateInfoDTO)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        String userId = HttpContextUtils.getUserIdFromContext();
        androidUpdateInfoDTO.setIsLatest(
                !this.releaseManager.shouldUpdate(
                        userId, androidUpdateInfoDTO.getCurrentVersion(), androidUpdateInfoDTO.getNextVersion()));

        AndroidUpdateInfoResponse response = new AndroidUpdateInfoResponse();
        response.setData(androidUpdateInfoDTO);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the ios update info get api.
     *
     * @param version (required).
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
     * Implements the android setup info get api.
     *
     * @return {@link AndroidSetupInfoResponse}.
     */
    @CrossOrigin
    @Cacheable(value = "setupInfo-android")
    @CrossOrigin
    @Override
    public ResponseEntity<AndroidSetupInfoResponse> apiV1SetupInfoAndroidGet() {
        AndroidSetupInfoResponse response = new AndroidSetupInfoResponse();
        response.setData(this.androidSetupInfoDTO);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Constructs the android configuration DTOs.
     *
     * @param version Query version.
     * @param channel Channel name.
     * @return {@link ConfigurationDTO}.
     */
    private ConfigurationDTO createAndroidConfigurationDTO(Integer version, String channel) {
        ConfigurationDTO configurationDTO = new ConfigurationDTO();
        configurationDTO.setIsAccountLogin(false);
        Map<String, Boolean> channelMap = this.androidConfigVersionMap.get(version);

        if (Objects.isNull(channelMap)
                || Objects.isNull(channelMap.get(channel.toLowerCase()))
                || !channelMap.get(channel.toLowerCase())) {
            return configurationDTO;
        }

        configurationDTO.setIsAccountLogin(true);
        return configurationDTO;
    }

}
