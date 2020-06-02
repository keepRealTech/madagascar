package com.keepreal.madagascar.lemur.config;

import com.keepreal.madagascar.lemur.releaseManager.DefaultReleaseManager;
import com.keepreal.madagascar.lemur.releaseManager.ReleaseManager;
import com.keepreal.madagascar.lemur.releaseManager.StochasticReleaseManager;
import com.keepreal.madagascar.lemur.textFilter.DefaultContentFilter;
import com.keepreal.madagascar.lemur.textFilter.TextContentFilter;
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
    private String sensitiveWordsFilePath;

    /**
     * Constructs the release manager for android.
     *
     * @return {@link ReleaseManager}.
     */
    @Bean
    public ReleaseManager getAndroidReleaseManager() {
        switch (this.androidUpgradeStrategy) {
            case ("Stochastic"):
                return new StochasticReleaseManager(androidUpgradeRatio);
            case ("Default"):
            default:
                return new DefaultReleaseManager();
        }
    }

    /**
     * Initializes the text filter.
     *
     * @return {@link TextContentFilter}.
     */
    @Bean
    public TextContentFilter getTextFilter() {
        return new DefaultContentFilter(this.sensitiveWordsFilePath);
    }

}
