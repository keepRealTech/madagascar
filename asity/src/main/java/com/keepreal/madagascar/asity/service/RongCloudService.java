package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.asity.config.RongCloudConfiguration;
import io.rong.RongCloud;
import io.rong.models.user.UserModel;
import org.springframework.stereotype.Service;

/**
 * Represents the rong cloud service.
 */
@Service
public class RongCloudService {

    private final RongCloud client;

    /**
     * Constructs the rong cloud service.
     *
     * @param rongCloudConfiguration {@link RongCloudConfiguration}.
     */
    public RongCloudService(RongCloudConfiguration rongCloudConfiguration) {
        this.client = RongCloud.getInstance(rongCloudConfiguration.getAppKey(), rongCloudConfiguration.getAppSecret());
    }

    /**
     * Registers user and returns token.
     * @param userId        User id.
     * @param userName      User name.
     * @param portraitUrl   User portrait url.
     * @return User token.
     */
    public String register(String userId, String userName, String portraitUrl) throws Exception {
        UserModel userModel = new UserModel()
                .setId(userId)
                .setName(userName)
                .setPortrait(portraitUrl);
        return this.client.user.register(userModel).getToken();
    }

}
