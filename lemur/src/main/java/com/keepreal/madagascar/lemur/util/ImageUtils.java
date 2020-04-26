package com.keepreal.madagascar.lemur.util;

import java.util.UUID;

/**
 * Represents a image utils.
 */
public class ImageUtils {

    /**
     * Builds a random image uri.
     */
    public static String buildImageUri() {
        return UUID.randomUUID().toString();
    }

}
