package com.keepreal.madagascar.fossa.util;


import com.aliyun.openservices.shade.org.apache.commons.codec.digest.DigestUtils;

import java.util.Random;

public class RepostCodeUtils {

    private static final String str62keys = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Integer LENGTH = 6;

    public static String getRandomString() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < RepostCodeUtils.LENGTH; i++) {
            int number = random.nextInt(62);
            sb.append(RepostCodeUtils.str62keys.charAt(number));
        }
        return sb.toString();
    }

    public static String shorten(String url) {
        String key = "SECRET"; // 自定义生成MD5加密字符串前的混合KEY
        String hex = DigestUtils.md5Hex(key + url);
        int hexLen = hex.length();
        int subHexLen = hexLen / 8;
        String[] shortStr = new String[subHexLen];

        for (int i = 0; i < subHexLen; i++) {
            StringBuilder outChars = new StringBuilder();
            int j = i + 1;
            String subHex = hex.substring(i * 8, j * 8);
            long idx = Long.valueOf("3FFFFFFF", 16) & Long.valueOf(subHex, 16);
            for (int k = 0; k < LENGTH; k++) {
                int index = (int) (Long.valueOf("0000003D", 16) & idx);
                outChars.append(str62keys.charAt(index));
                idx = idx >> 5;
            }
            shortStr[i] = outChars.toString();
        }
        return String.join("", shortStr);
    }

    public static String decode(String str62) {
        StringBuilder id = new StringBuilder();
        // 从最后往前以4字节为一组读取字符
        for (int i = str62.length() - 4; i > -4; i = i - 4) {
            int offset = i < 0 ? 0 : i;
            int len = i < 0 ? str62.length() % 4 : 4;
            long encode = encode62ToInt(left(str62, offset, len));
            String str = String.valueOf(encode);
            if (offset > 0)
                str = leftPad(str, 7); // 若不是第一组，则不足7位补0
            id.insert(0, str);
        }
        return id.toString();
    }

    public static String encode(String mid) {

        StringBuilder result = new StringBuilder();
        for (int i = mid.length() - 7; i > -7; i -= 7) {
            int offset1 = (i < 0) ? 0 : i;
            int offset2 = i + 7;
            String num = intToEncode62(left(mid, offset1, offset2 - offset1));
            result.insert(0, num);
        }
        return result.toString();
    }

    private static String intToEncode62(String mid) {
        long int_mid = Long.parseLong(mid);
        StringBuilder result = new StringBuilder();
        do {
            long a = int_mid % 62;
            result.insert(0, str62keys.charAt((int) a));
            int_mid = (int_mid - a) / 62;
        } while (int_mid > 0);

        return leftPad(result.toString(), 4);
    }

    private static long encode62ToInt(String str62) {
        long i10 = 0;

        for (int i = 0; i < str62.length(); i++) {
            double n = str62.length() - i - 1;
            i10 += str62keys.indexOf(str62.charAt(i)) * Math.pow(62, n);
        }
        String temp = leftPad(String.valueOf(i10), 7);
        // Long.TryParse(temp, out i10);
        try {
            i10 = Long.parseLong(temp);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return i10;
    }

    // 左边补
    private static String leftPad(String s, int size) {
        int length = s.length();
        if (length == 0) {
            return s;
        }
        int pads = size - length;
        if (pads <= 0) {
            return s;
        }
        return padding(pads).concat(s);
    }

    private static String padding(int repeat) {
        if (repeat < 0) {
            throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
        }
        char[] buf = new char[repeat];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = '0';
        }
        return new String(buf);
    }

    private static String left(String s, int begin, int len) {
        int length = length(s);
        if (length <= len) {
            return s;
        }
        return s.substring(begin, begin > 0 ? begin + len : len);
    }

    private static int length(String s) {
        return s != null ? s.length() : 0;
    }

}
