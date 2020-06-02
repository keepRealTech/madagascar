package com.keepreal.madagascar.lemur.config;

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
    private String sensitiveWordsFilePath;

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
