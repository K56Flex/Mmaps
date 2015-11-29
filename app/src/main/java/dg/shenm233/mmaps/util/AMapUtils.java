package dg.shenm233.mmaps.util;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.RouteSearch;

public class AMapUtils {
    /**
     * 把LatLonPoint对象转化为LatLon对象
     */
    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }

    /**
     * 把LatLon对象转化为LatLonPoint对象
     */
    public static LatLonPoint convertToLatLonPoint(LatLng latLng) {
        return new LatLonPoint(latLng.latitude, latLng.longitude);
    }

    /**
     * 把驾车多个选项(省钱，避免拥堵，不走高速) 转换成高德sdk对应模式
     * boolean[0] ... 省钱
     * boolean[1] ... 避免拥堵
     * boolean[2] ... 不走高速
     */
    public static int convertToAMapDriveMode(boolean[] booleans) {
        int mymode = (booleans[0] ? 1 : 0)
                + (booleans[1] ? 2 : 0)
                + (booleans[2] ? 4 : 0);
        switch (mymode) {
            case 0:
                return RouteSearch.DrivingDefault;
            case 1:
                return RouteSearch.DrivingSaveMoney;
            case 2:
                return RouteSearch.DrivingAvoidCongestion;
            case 3:
                return RouteSearch.DrivingSaveMoneyAvoidCongestion;
            case 4:
                return RouteSearch.DrivingNoHighWay;
            case 5:
                return RouteSearch.DrivingNoHighWaySaveMoney;
//            case 6:
//                return RouteSearch.DrivingNoHighWayAvoidCongestion; // 高德sdk没有该模式
            default:
                return RouteSearch.DrivingNoHighAvoidCongestionSaveMoney;
        }

        //TODO: 本方法还没有支持高德sdk里面的
        // DrivingShortDistance, DrivingNoExpressways, DrivingMultiStrategy
    }

    /**
     * 将高德sdk驾车模式转换为布尔数组
     * boolean[0] ... 省钱
     * boolean[1] ... 避免拥堵
     * boolean[2] ... 不走高速
     */
    public static boolean[] covertAMapDriveModeToBools(int driveMode) {
        boolean[] booleans = new boolean[3];
        switch (driveMode) {
            case RouteSearch.DrivingSaveMoney:
                booleans[0] = true;
                booleans[1] = booleans[2] = false;
                break;
            case RouteSearch.DrivingAvoidCongestion:
                booleans[1] = true;
                booleans[0] = booleans[2] = false;
                break;
            case RouteSearch.DrivingNoHighWay:
                booleans[2] = true;
                booleans[0] = booleans[1] = false;
                break;
            case RouteSearch.DrivingSaveMoneyAvoidCongestion:
                booleans[2] = false;
                booleans[0] = booleans[1] = true;
                break;
            case RouteSearch.DrivingNoHighWaySaveMoney:
                booleans[1] = false;
                booleans[0] = booleans[2] = true;
                break;
//            case RouteSearch.DrivingNoHighAvoidCongestion: // 高德sdk没有该模式
//                booleans[0] = false;
//                booleans[1] = booleans[2] = true;
            case RouteSearch.DrivingNoHighAvoidCongestionSaveMoney:
                booleans[0] = booleans[1] = booleans[2] = true;
                break;
            default:
                booleans[0] = booleans[1] = booleans[2] = false;
        }
        return booleans;
    }
}
