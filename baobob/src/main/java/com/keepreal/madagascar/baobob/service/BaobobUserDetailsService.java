package com.keepreal.madagascar.baobob.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Represents the user details service implementation.
 * Note this is now a dummy implementation for refresh token to skip reauthentication.
 */
@Service
public class BaobobUserDetailsService implements UserDetailsService {

    /**
     * Implements the load user by username method.
     *
     * @param id The user id extracted from jwt token.
     * @return {@link UserDetails}.
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        return new User(id, "", new ArrayList<>());
    }

}
