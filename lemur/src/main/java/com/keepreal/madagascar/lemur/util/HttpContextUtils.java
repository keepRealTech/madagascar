package com.keepreal.madagascar.lemur.util;

import com.keepreal.madagascar.lemur.filters.GeneralContextFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * Represents the http context utils.
 */
public class HttpContextUtils {

    /**
     * Extracts the user id from the authentication context.
     *
     * @return User id. Null if no authentication.
     */
    public static String getUserIdFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (Objects.isNull(authentication)) {
            return null;
        }

        return String.valueOf(authentication.getPrincipal());
    }

    /**
     * Extracts the version number from the servlet request context.
     *
     * @return Version.
     */
    public static String getVersionFromContext() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return String.valueOf(Objects.requireNonNull(attributes).getAttribute(GeneralContextFilter.VERSION, RequestAttributes.SCOPE_REQUEST));
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
