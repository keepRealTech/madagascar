package com.keepreal.madagascar.lemur.textFilter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the default text content filter implementation.
 */
@Slf4j
public class DefaultContentFilter implements TextContentFilter {

    private Set<String> dictionary = new HashSet<>();

    /**
     * Constructs the default content filter.
     *
     * @param dictionaryFilePath Dictionary file path.
     */
    public DefaultContentFilter(String dictionaryFilePath) {
        try {
            String content = new String(Base64.getDecoder().decode(Files.readAllBytes(Paths.get(dictionaryFilePath))), StandardCharsets.UTF_16);
            this.dictionary = Arrays.stream(content.split("\r\n")).filter(line -> !line.isEmpty()).map(String::toUpperCase).collect(Collectors.toSet());
        } catch (IOException ignored) {
        } finally {
            log.info("Content filter initialized with {} words.", this.dictionary.size());
        }
    }

    /**
     * Implementes the basic filter logic.
     *
     * @param content Content text.
     * @return True if not allowed.
     */
    @Override
    public boolean isDisallowed(String content) {
        content = content.replace((char) 12288, ' ');
        content = content.trim();

        if (StringUtils.isEmpty(content)) {
            return false;
        }

        content = content.toUpperCase();
        return this.dictionary.parallelStream().anyMatch(content::contains);
    }

}
