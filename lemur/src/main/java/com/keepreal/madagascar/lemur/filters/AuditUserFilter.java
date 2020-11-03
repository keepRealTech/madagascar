package com.keepreal.madagascar.lemur.filters;

import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.lemur.service.GeoIpService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the audit user filter.
 */
public class AuditUserFilter extends OncePerRequestFilter {

    private static final Set<String> AUDIT_URI_PATTERNS_SET = new HashSet<>(Arrays.asList(
            "^/api/v1/feeds/public",
            "^/api/v1/islands/discovery",
            "^/api/v1/feeds/[0-9]+/iosPay",
            "^/api/v1/islands/[0-9]+/support/iosPay",
            "^/api/v2/islands/[0-9]+/sponsors/iosPay",
            "^/api/v1/islands/[0-9]+/memberSubscription/iosPay"));
    private static final String ISLAND_SEARCH_URI = "/api/v1/islands";

    private final UserService userService;
    private final GeoIpService geoIpService;

    /**
     * Constructs the audit user filter.
     *
     * @param userService  {@link UserService}.
     * @param geoIpService {@link GeoIpService}.
     */
    public AuditUserFilter(UserService userService,
                           GeoIpService geoIpService) {
        this.userService = userService;
        this.geoIpService = geoIpService;
    }

    /**
     * Implements the audit filter redispatch logic.
     *
     * @param request     {@link HttpServletRequest}.
     * @param response    {@link HttpServletResponse}.
     * @param filterChain {@link FilterChain}.
     * @throws IOException      {@link IOException}.
     * @throws ServletException {@link ServletException}.
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String route = request.getRequestURI();
        route = route.replaceFirst("/api/v[0-9.]+/", "/api/v0/");
        request.getRequestDispatcher(route).forward(request, response);
    }

    /**
     * Defines whether logic goes through the filter.
     *
     * @param request {@link HttpServletRequest}.
     * @return False if should filter.
     */
    @Override
    protected boolean shouldNotFilter(@NotNull HttpServletRequest request) {
        String ip = HttpContextUtils.getRemoteIpFromContext();
        String userId = HttpContextUtils.getUserIdFromContext();
        UserMessage user = this.userService.retrieveUserById(userId);

        boolean isAuditUser = this.geoIpService.fromStates(ip)
                || Constants.AUDIT_USER_IDS.contains(userId)
                || user.getUnionId().contains(".")
                || user.getUsername().startsWith("1-")
                || user.getMobile().startsWith("1-");

        if (!isAuditUser) {
            return true;
        }

        String uri = request.getRequestURI();
        if (AuditUserFilter.AUDIT_URI_PATTERNS_SET.stream().anyMatch(uri::matches)) {
            return false;
        } else if (AuditUserFilter.ISLAND_SEARCH_URI.equals(uri) && Objects.nonNull(request.getParameter("name"))) {
            return false;
        }

        return true;
    }

}
