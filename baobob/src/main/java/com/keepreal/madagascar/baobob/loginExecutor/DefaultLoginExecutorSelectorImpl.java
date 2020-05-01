package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.config.OauthWechatLoginConfiguration;
import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.common.LoginType;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.stereotype.Component;

/**
 * Represents the default login executor selector.
 */
@Component
public class DefaultLoginExecutorSelectorImpl implements LoginExecutorSelector {

    private final UserService userService;
    private final OauthWechatLoginConfiguration oauthWechatLoginConfiguration;
    private final AuthorizationServerEndpointsConfiguration endpoints;

    /**
     * Constructs the {@link DefaultLoginExecutorSelectorImpl}.
     * Note that the endpoint will not be initialized in bean injection process.
     *
     * @param userService                   {@link UserService}.
     * @param oauthWechatLoginConfiguration {@link OauthWechatLoginConfiguration}.
     * @param endpoints                     {@link AuthorizationServerEndpointsConfiguration}.
     */
    public DefaultLoginExecutorSelectorImpl(UserService userService,
                                            OauthWechatLoginConfiguration oauthWechatLoginConfiguration,
                                            AuthorizationServerEndpointsConfiguration endpoints) {
        this.userService = userService;
        this.oauthWechatLoginConfiguration = oauthWechatLoginConfiguration;
        this.endpoints = endpoints;
    }

    /**
     * Selects the login executor.
     *
     * @param loginType {@link LoginType}.
     * @return {@link LoginExecutor}.
     */
    @Override
    public LoginExecutor select(LoginType loginType) {
        switch (loginType) {
            case LOGIN_OAUTH_WECHAT:
                return new OauthWechatLoginExecutor(this.userService,
                        this.oauthWechatLoginConfiguration,
                        new LocalTokenGranter(
                                endpoints.getEndpointsConfigurer().getTokenServices(),
                                endpoints.getEndpointsConfigurer().getClientDetailsService(),
                                endpoints.getEndpointsConfigurer().getOAuth2RequestFactory()));
            case LOGIN_PASSWORD:
                return new DummyPasswordLoginExecutor(this.userService,
                        new LocalTokenGranter(
                                endpoints.getEndpointsConfigurer().getTokenServices(),
                                endpoints.getEndpointsConfigurer().getClientDetailsService(),
                                endpoints.getEndpointsConfigurer().getOAuth2RequestFactory()));
            case UNRECOGNIZED:
            default:
                return new DummyLoginExecutorImpl();
        }
    }

}
