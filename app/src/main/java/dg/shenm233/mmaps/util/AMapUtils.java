package dg.shenm233.mmaps.util;

import android.content.Context;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.RouteSearch;

import java.util.List;

import dg.shenm233.mmaps.R;

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
     * boolean[3] ... 距离优先
     * boolean[4] ... 不走快速路
     */
    public static int convertToAMapDriveMode(boolean[] booleans) {
        if (booleans[3]) {
            return RouteSearch.DrivingShortDistance;
        }
        if (booleans[4]) {
            return RouteSearch.DrivingNoExpressways;
        }

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
        // DrivingMultiStrategy
    }

    /**
     * 将高德sdk驾车模式转换为布尔数组
     * boolean[0] ... 省钱
     * boolean[1] ... 避免拥堵
     * boolean[2] ... 不走高速
     * boolean[3] ... 距离优先
     * boolean[4] ... 不走快速路
     */
    public static boolean[] covertAMapDriveModeToBools(int driveMode) {
        boolean[] booleans = new boolean[5];
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
            case RouteSearch.DrivingShortDistance:
                booleans[3] = true;
                booleans[0] = booleans[1] = booleans[2] = booleans[4] = false;
                break;
            case RouteSearch.DrivingNoExpressways:
                booleans[4] = true;
                booleans[0] = booleans[1] = booleans[2] = booleans[3] = false;
                break;
            default:
                booleans[0] = booleans[1] = booleans[2] = false;
        }
        return booleans;
    }

    /**
     * 转换BusPath为文本格式,用于BusPathView
     * 输出格式如下:
     * 56路 > 58路 > W58min
     * W 代表 步行
     *
     * @param context
     * @param path
     * @return
     */
    public static String convertBusPathToText(Context context, BusPath path) {
        StringBuilder sb = new StringBuilder();
        List<BusStep> busSteps = path.getSteps();
        for (BusStep busStep : busSteps) {
            BusLineItem busLineItem = busStep.getBusLine();
            if (busLineItem != null) {
                sb.append(busLineItem.getBusLineName().split("\\([^)]*\\)")[0]) // 匹配括号外第一个元素
                        .append(" > ");
            } else {
                RouteBusWalkItem busWalkItem = busStep.getWalk();
                if (busWalkItem != null) {
                    sb.append("W")
                            .append(CommonUtils.getFriendlyDuration(context, busWalkItem.getDuration()))
                            .append(" > ");
                }
            }
        }
        return sb.toString();
    }

    public static String convertErrorCodeToText(Context context, int code) {
        switch (code) {
            case AMapException.ERROR_CODE_CONNECTION:
                return context.getString(R.string.error_no_connection);
            case AMapException.ERROR_CODE_OVER_DIRECTION_RANGE:
                return context.getString(R.string.error_over_directions_range);

            default:
                return context.getString(R.string.error_unknown);
        }
    }
}
