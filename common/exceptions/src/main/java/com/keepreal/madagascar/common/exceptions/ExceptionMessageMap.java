package com.keepreal.madagascar.common.exceptions;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-09
 **/

public class ExceptionMessageMap {
    private static Map<Integer, String> msgMap = new HashMap<>();

    private static final String default_message = "";

    static {
        msgMap.put(ErrorCode.REQUEST_ISLAND_SECRET_ERROR_VALUE, "岛名已经被占用");
        //...
    }

    public static String get(int code) {
        return msgMap.get(code) == null ? default_message : msgMap.get(code);
    }

}
