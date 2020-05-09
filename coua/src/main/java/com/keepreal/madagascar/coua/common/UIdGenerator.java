package com.keepreal.madagascar.coua.common;

import com.keepreal.madagascar.coua.dao.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-08
 **/

@Component
public class UIdGenerator {

    private final UserInfoRepository userInfoRepository;

    private static final int base = 10_000_000;
    private static final int range = 89_999_999;
    private static Set<Integer> specialIdSet = new HashSet<>();

    @Autowired
    public UIdGenerator(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    public synchronized int nextUId() {
        int uId = generateUId();
        while (isSpecial(uId) || isExist(uId)) {
            uId = generateUId();
        }

        return uId;
    }

    private boolean isSpecial(int uId) {
        return specialIdSet.contains(uId);
    }

    private boolean isExist(int uId) {
        return userInfoRepository.countByuId(uId) > 0;
    }

    private int generateUId() {
        return new Random().nextInt(range) + base;
    }
}
