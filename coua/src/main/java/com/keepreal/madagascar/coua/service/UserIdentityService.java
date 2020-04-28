package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.coua.dao.UserIdentityRepository;
import com.keepreal.madagascar.coua.model.UserIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Service
public class UserIdentityService {

    private final UserIdentityRepository userIdentityRepository;

    @Autowired
    public UserIdentityService(UserIdentityRepository userIdentityRepository) {
        this.userIdentityRepository = userIdentityRepository;
    }

    void saveUserIdentities(List<Integer> userIdentitiesTypes, Long userId) {
        List<UserIdentity> userIdentityList = userIdentitiesTypes.stream().map(type -> {
            UserIdentity userIdentity = new UserIdentity();
            userIdentity.setUserId(userId);
            userIdentity.setIdentityType(type);
            userIdentity.setCreatedTime(System.currentTimeMillis());
            userIdentity.setUpdatedTime(System.currentTimeMillis());
            return userIdentity;
        }).collect(Collectors.toList());

        userIdentityRepository.saveAll(userIdentityList);
    }

    public List<Integer> getAllIdentitiesByUserId(Long userId) {
        return userIdentityRepository.getUserIdentityTypesByUserId(userId);
    }
}
