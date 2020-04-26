package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.common.LoginType;

/**
 * Represents the login executor selector interface.
 */
public interface LoginExecutorSelector {

    /**
     * Selects the login executor for the given login type.
     *
     * @param loginType {@link LoginType}.
     * @return {@link LoginExecutor}.
     */
    LoginExecutor select(LoginType loginType);

}
