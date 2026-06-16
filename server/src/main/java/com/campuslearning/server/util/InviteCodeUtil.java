package com.campuslearning.server.util;

import java.util.Random;

/**
 * 服务端邀请码生成工具。
 */
public final class InviteCodeUtil {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Random RANDOM = new Random();

    private InviteCodeUtil() {
    }

    public static String generate() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            builder.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return builder.toString();
    }
}
