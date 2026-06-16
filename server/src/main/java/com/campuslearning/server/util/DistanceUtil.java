package com.campuslearning.server.util;

/**
 * 服务端距离计算工具，位置签到最终以这里的结果为准。
 */
public final class DistanceUtil {

    private static final double EARTH_RADIUS = 6371000D;

    private DistanceUtil() {
    }

    public static double calculateMeters(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double diffLat = Math.toRadians(lat2 - lat1);
        double diffLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(diffLat / 2) * Math.sin(diffLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(diffLng / 2) * Math.sin(diffLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}
