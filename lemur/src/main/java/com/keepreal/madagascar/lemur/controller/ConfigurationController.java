package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.stats_events.annotation.StatsEventTrigger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ConfigApi;
import swagger.model.ConfigType;
import swagger.model.ConfigurationDTO;
import swagger.model.ConfigurationResponse;

/**
 * Represents the configuration controller.
 */
@RestController
public class ConfigurationController implements ConfigApi {

    /**
     * Implements the get configuration api.
     *
     * @param configType  (required) {@link ConfigType}.
     * @return {@link ConfigurationResponse}.
     */
    @Cacheable(value = "config")
    @StatsEventTrigger(
            category = StatsEventCategory.STATS_CAT_FEED,
            action = StatsEventAction.STATS_ACT_CREATE,
            metadata = ""
    )
    @Override
    public ResponseEntity<ConfigurationResponse> apiV1ConfigsGet(ConfigType configType) {
        ConfigurationDTO configurationDTO = new ConfigurationDTO();
        switch (configType) {
            case IOS:
                configurationDTO.setAudit(true);
            case ANDROID:
            default:
        }

        ConfigurationResponse response = new ConfigurationResponse();
        response.setData(configurationDTO);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
