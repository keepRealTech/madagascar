package com.keepreal.madagascar.lemur.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import java.util.Objects;

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

    /**
     * Retrieves the user remote ip address from servlet context.
     *
     * @return Caller remote ip.
     */
    public static String getRemoteIpFromContext() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("X-Forwarded-For");
    }

}
