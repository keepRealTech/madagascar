package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.lemur.service.RepostService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Represents the short code redirect url.
 */
@Controller
public class RedirectController {

    private final RepostService repostService;

    /**
     * Constructs the redirect controller.
     *
     * @param repostService {@link RepostService}.
     */
    public RedirectController(RepostService repostService) {
        this.repostService = repostService;
    }

    /**
     * Implements the get short code redirect api.
     *
     * @param shortCode Short code.
     * @return Redirect url.
     */
    @GetMapping(value = "/s/{shortCode}")
    public String apiRedirectShortUrl(@PathVariable("shortCode") String shortCode) {
        return String.format("redirect:https://home.keepreal.cn/%s", this.repostService.getRedirectUrl(shortCode));
    }

}
