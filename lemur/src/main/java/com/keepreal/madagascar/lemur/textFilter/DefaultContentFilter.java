package com.keepreal.madagascar.lemur.textFilter;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the default text content filter implementation.
 */
public class DefaultContentFilter implements TextContentFilter {

    private Set<String> dictionary;

    /**
     * Constructs the default content filter.
     *
     * @param dictionaryFilePath Dictionary file path.
     */
    public DefaultContentFilter(String dictionaryFilePath) {
        try (Stream<String> lines = Files.lines(Paths.get(dictionaryFilePath), StandardCharsets.UTF_16)) {
            this.dictionary = lines.filter(line -> !line.isEmpty()).map(String::toUpperCase).collect(Collectors.toSet());
        } catch (IOException e) {
            this.dictionary = new HashSet<>();
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
        if (StringUtils.isEmpty(content)) {
            return false;
        }

        content = content.toUpperCase();
        return this.dictionary.parallelStream().anyMatch(content::contains);
    }

}
