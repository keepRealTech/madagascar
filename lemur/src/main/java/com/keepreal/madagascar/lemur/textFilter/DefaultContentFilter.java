package com.keepreal.madagascar.lemur.textFilter;

import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Represents the default text content filter implementation.
 */
public class DefaultContentFilter implements TextContentFilter {

    private Set<String> dictionary;

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
