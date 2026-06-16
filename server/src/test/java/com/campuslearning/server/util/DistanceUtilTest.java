package com.campuslearning.server.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 服务端距离工具测试。
 */
class DistanceUtilTest {

    @Test
    void shouldReturnZeroWhenCoordinatesAreSame() {
        Assertions.assertEquals(0D, DistanceUtil.calculateMeters(30.0, 120.0, 30.0, 120.0), 0.0001D);
    }

    @Test
    void shouldReturnPositiveDistanceWhenCoordinatesDiffer() {
        double distance = DistanceUtil.calculateMeters(30.0, 120.0, 30.001, 120.001);
        Assertions.assertTrue(distance > 0D);
    }
}
