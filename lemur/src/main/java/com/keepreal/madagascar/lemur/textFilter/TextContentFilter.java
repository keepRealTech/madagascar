package com.keepreal.madagascar.lemur.textFilter;

/**
 * Represents the text content filter.
 */
public interface TextContentFilter {

    /**
     * Checks if the content contains disallowed patterns.
     *
     * @param content Content text.
     * @return True if not allowed.
     */
    boolean isDisallowed(String content);

}
