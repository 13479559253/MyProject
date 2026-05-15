package com.example.demo.util;


import com.example.demo.exception.MyException;

public class PositionUtil {

    public static double calcDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
    public static void checkLatLng(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (isValidLat(lat1) || isValidLng(lng1)) {
            throw new MyException("起点经纬度非法");
        }
        if (isValidLat(lat2) || isValidLng(lng2)) {
            throw new MyException("终点经纬度非法");
        }

        if (lat1.equals(lat2) && lng1.equals(lng2)) {
            throw new MyException("起点和终点不能为同一位置");
        }

        double distance = calcDistance(lat1,lng1,lat2,lng2);
        if (distance > 50) {
            throw new MyException("行程距离不能超过50公里");
        }
    }

    private static boolean isValidLat(Double lat) {
        return lat == null || lat < -90 || lat > 90;
    }
    private static boolean isValidLng(Double lng) {
        return lng == null || lng < -180 || lng > 180;
    }

}
