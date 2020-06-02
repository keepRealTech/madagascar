package com.keepreal.madagascar.lemur.textFilter;

import org.springframework.stereotype.Component;

/**
 * Represents the default text content filter implementation.
 */
@Component
public class DefaultContentFilter implements TextContentFilter {

    @Override
    public boolean isDisallowed(String content) {
        return false;
    }

}
