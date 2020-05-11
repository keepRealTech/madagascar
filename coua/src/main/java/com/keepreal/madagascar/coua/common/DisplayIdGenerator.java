package com.keepreal.madagascar.coua.common;

import com.keepreal.madagascar.coua.dao.UserInfoRepository;
import com.keepreal.madagascar.coua.util.DisplayIdFilterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-08
 **/

@Component
public class DisplayIdGenerator {

    private final UserInfoRepository userInfoRepository;

    private static final String base = "00_000_000";
    private static final int range = 99_999_999;
    private static final int uid_length = 8;

    @Autowired
    public DisplayIdGenerator(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    public synchronized String nextUId() {
        String uId = generateUId();
        while (DisplayIdFilterUtils.isSpecial(uId) || isExist(uId)) {
            uId = generateUId();
        }

        return uId;
    }

    private boolean isExist(String uId) {
        return userInfoRepository.countByDisplayId(uId) > 0;
    }

    private String generateUId() {
        StringBuilder sb = new StringBuilder(base);
        int randomNum = new Random().nextInt(range);
        sb.append(randomNum);
        return sb.substring(Math.abs(uid_length - sb.length()));
    }
}
