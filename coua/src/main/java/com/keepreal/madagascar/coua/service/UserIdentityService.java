package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
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
    private final LongIdGenerator idGenerator;

    @Autowired
    public UserIdentityService(UserIdentityRepository userIdentityRepository, LongIdGenerator idGenerator) {
        this.userIdentityRepository = userIdentityRepository;
        this.idGenerator = idGenerator;
    }

    public void saveUserIdentities(List<Integer> userIdentitiesTypes, String userId) {
        List<UserIdentity> userIdentityList = userIdentitiesTypes.stream().map(type -> UserIdentity.builder()
                .id(String.valueOf(idGenerator.nextId()))
                .userId(userId)
                .identityType(type)
                .build()).collect(Collectors.toList());

        userIdentityRepository.saveAll(userIdentityList);
    }

    public void updateUserIdentities(List<Integer> userIdentitiesTypes, String userId) {
        userIdentityRepository.deleteUserIdentitiesByUserId(userId);
        saveUserIdentities(userIdentitiesTypes, userId);
    }

    public List<Integer> getAllIdentitiesByUserId(String userId) {
        return userIdentityRepository.getUserIdentityTypesByUserId(userId);
    }
}
