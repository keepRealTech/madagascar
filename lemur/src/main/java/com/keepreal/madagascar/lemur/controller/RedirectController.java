package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.lemur.service.RedirectService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Represents the short code redirect url.
 */
@Controller
public class RedirectController {

    private final RedirectService redirectService;

    /**
     * Constructs the redirect controller.
     *
     * @param redirectService {@link RedirectService}.
     */
    public RedirectController(RedirectService redirectService) {
        this.redirectService = redirectService;
    }

    /**
     * Implements the get short code redirect api.
     *
     * @param shortCode Short code.
     * @return Redirect url.
     */
    @GetMapping(value = "/s/{shortCode}")
    public String apiRedirectShortUrl(@PathVariable("shortCode") String shortCode) {
        return String.format("redirect:https://www.keepreal.cn/%s", this.redirectService.getRedirectUrl(shortCode));
    }

}
