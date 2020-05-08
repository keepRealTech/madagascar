package com.keepreal.madagascar.coua.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-08
 **/

public class UIdGeneratorUtils {

    private static final int base = 10_000_000;
    private static final int range = 89_999_999;
    private static Set<Integer> specialIdList = new HashSet<>();

    public static int nextUId() {
        int uId = generateUId();
        while (isSpecial(uId)) {
            uId = generateUId();
        }

        return uId;
    }

    private static boolean isSpecial(int uId) {
        return specialIdList.contains(uId);
    }

    private static int generateUId() {
        return new Random().nextInt(range) + base;
    }
}
