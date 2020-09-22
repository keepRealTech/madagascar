package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.lemur.config.IOSClientConfiguration;
import com.keepreal.madagascar.lemur.service.RedirectService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Represents the short code redirect url.
 */
@Controller
public class RedirectController {

    private final IOSClientConfiguration iosClientConfiguration;
    private final RedirectService redirectService;

    /**
     * Constructs the redirect controller.
     *
     * @param iosClientConfiguration    {@link IOSClientConfiguration}.
     * @param redirectService           {@link RedirectService}.
     */
    public RedirectController(IOSClientConfiguration iosClientConfiguration,
                              RedirectService redirectService) {
        this.iosClientConfiguration = iosClientConfiguration;
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
        return String.format("redirect:%s/%s", this.iosClientConfiguration.getHtmlHostName(), this.redirectService.getRedirectUrl(shortCode));
    }

}
