package com.keepreal.madagascar.coua.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-09
 **/

public class DisplayIdFilterUtils {

    private static List<String> patternList = new ArrayList<>();

    static {
        // 重复号码，镜子号码
        patternList.add("^(<a>\\d)(\\d)(\\d)(\\d)\\1\\2\\3\\4$");
        patternList.add("^(\\d)(\\d)(\\d)(\\d)\\4\\3\\2\\1$");
        // AABB
        patternList.add("^\\d*(\\d)\\1(\\d)\\2\\d*$");
        // AAABBB
        patternList.add("^\\d*(\\d)\\1\\1(\\d)\\2\\2\\d*$");
        // ABABAB
        patternList.add("^(\\d)(\\d)\\1\\2\\1\\2\\1\\2$");
        // ABCABC
        patternList.add("^(\\d)(\\d)(\\d)\\1\\2\\3$");
        // ABBABB
        patternList.add("^(\\d)(\\d)\\2\\1\\2\\2$");
        // AABAAB
        patternList.add("^(\\d)\\1(\\d)\\1\\1\\2$");
        // 4-8 位置重复
        patternList.add("^\\d*(\\d)\\1{2,}\\d*$");
        // 4位以上 位递增或者递减（7890也是递增）
        patternList.add("(?:(?:0(?=1)|1(?=2)|2(?=3)|3(?=4)|4(?=5)|5(?=6)|6(?=7)|7(?=8)|8(?=9)|9(?=0)){2,}|(?:0(?=9)|9(?=8)|8(?=7)|7(?=6)|6(?=5)|5(?=4)|4(?=3)|3(?=2)|2(?=1)|1(?=0)){2,})\\d");
    }

    public static boolean isSpecial(String input) {
        for (String pa : patternList) {
            if (Pattern.matches(pa, input))
                return true;
        }
        return false;
    }
}
