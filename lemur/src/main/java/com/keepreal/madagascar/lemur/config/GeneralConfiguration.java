package com.keepreal.madagascar.lemur.config;

import com.keepreal.madagascar.lemur.releaseManager.DefaultReleaseManager;
import com.keepreal.madagascar.lemur.releaseManager.ReleaseManager;
import com.keepreal.madagascar.lemur.releaseManager.StochasticReleaseManager;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Represents the general configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "general", ignoreUnknownFields = false)
@Data
public class GeneralConfiguration {

    private String singleOfficialIslandId;
    private List<String> officialIslandIdList;
    private String androidUpgradeStrategy;
    private Integer androidUpgradeRatio;

    /**
     * Constructs the release manager for android.
     *
     * @return
     */
    @Bean
    public ReleaseManager getAndroidReleaseManager() {
        switch (this.androidUpgradeStrategy) {
            case("Stochastic"):
                return new StochasticReleaseManager(androidUpgradeRatio);
            default:
                return new DefaultReleaseManager();
        }
    }

}
