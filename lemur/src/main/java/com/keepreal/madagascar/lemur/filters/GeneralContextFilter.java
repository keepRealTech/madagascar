package com.keepreal.madagascar.lemur.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * Represents the general context filter.
 */
@Component
public class GeneralContextFilter extends OncePerRequestFilter {

    public static final String VERSION = "VERSION";

    /**
     * Implements the general context extract logic.
     *
     * @param request     {@link HttpServletRequest}.
     * @param response    {@link HttpServletResponse}.
     * @param filterChain {@link FilterChain}.
     * @throws IOException      {@link IOException}.
     * @throws ServletException {@link ServletException}.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws IOException, ServletException {
        String version = request.getParameter(GeneralContextFilter.VERSION);

        if (!StringUtils.isEmpty(version)) {
            Objects.requireNonNull(RequestContextHolder.getRequestAttributes()).setAttribute(GeneralContextFilter.VERSION, version, RequestAttributes.SCOPE_REQUEST);
        }

        filterChain.doFilter(request, response);
    }

}
