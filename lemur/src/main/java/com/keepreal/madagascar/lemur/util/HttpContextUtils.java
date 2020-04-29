package com.keepreal.madagascar.lemur.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Represents the http context utils.
 */
public class HttpContextUtils {

    /**
     * Extracts the user id from the authentication context.
     *
     * @return User id.
     */
    public static String getUserIdFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return String.valueOf(authentication.getPrincipal());
    }

}
