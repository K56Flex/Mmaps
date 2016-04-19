/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dg.shenm233.mmaps.util;

import android.content.Context;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.RouteSearch;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.MainApplication;
import dg.shenm233.mmaps.R;

public class AMapUtils {
    /**
     * 把LatLonPoint对象转化为LatLon对象
     */
    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }

    public static List<LatLng> convertToLatLng(List<LatLonPoint> list) {
        List<LatLng> newList = new ArrayList<>(list.size());
        for (LatLonPoint point : list) {
            newList.add(convertToLatLng(point));
        }

        return newList;
    }

    /**
     * 把LatLon对象转化为LatLonPoint对象
     */
    public static LatLonPoint convertToLatLonPoint(LatLng latLng) {
        return new LatLonPoint(latLng.latitude, latLng.longitude);
    }

    public static List<LatLonPoint> convertToLatLonPoint(List<LatLng> list) {
        List<LatLonPoint> newList = new ArrayList<>(list.size());
        for (LatLng point : list) {
            newList.add(convertToLatLonPoint(point));
        }

        return newList;
    }

    public static NaviLatLng convertToNaviLatLng(LatLonPoint latLonPoint) {
        return new NaviLatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
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
     * 转换RouteSearch的驾车模式为对应的导航模式
     *
     * @param mode RouteSearch的驾车模式
     * @return 对应的驾车导航模式
     */
    public static int convertDriveModeForNavi(int mode) {
        switch (mode) {
            case RouteSearch.DrivingDefault:
                return AMapNavi.DrivingDefault;
            case RouteSearch.DrivingSaveMoney:
                return AMapNavi.DrivingSaveMoney;
            case RouteSearch.DrivingAvoidCongestion:
                return AMapNavi.DrivingAvoidCongestion;
            /*
            case RouteSearch.DrivingNoHighWay:
                return AMapNavi.DrivingNoHighWay;
            case RouteSearch.DrivingSaveMoneyAvoidCongestion:
                return AMapNavi.DrivingSaveMoneyAvoidCongestion;
            case RouteSearch.DrivingNoHighWaySaveMoney:
                return AMapNavi.DrivingNoHighWaySaveMoney;
            case RouteSearch.DrivingNoHighAvoidCongestionSaveMoney:
                return AMapNavi.DrivingNoHighAvoidCongestionSaveMoney;
            */
            case RouteSearch.DrivingShortDistance:
                return AMapNavi.DrivingShortDistance;
            case RouteSearch.DrivingNoExpressways:
                return AMapNavi.DrivingNoExpressways;
        }

        // TODO: AMapNavi.DrivingFastestTime (时间优先，躲避拥堵)

        return -1;
    }

    /**
     * 转换BusPath为文本格式,用于BusPathView
     * 输出格式如下:
     * 56路 > 58路 > W58min
     * W 代表 步行
     *
     * @param path
     * @return
     */
    public static String convertBusPathToText(BusPath path) {
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
                            .append(CommonUtils.getFriendlyDuration(busWalkItem.getDuration()))
                            .append(" > ");
                }
            }
        }
        return sb.toString();
    }

    // TODO; 高德还没支持显示每班车的间隔时间
    public static String getFirstStationDuration(BusPath path) {
        Context context = MainApplication.getAppContext();
        String s = null;
        List<BusStep> busSteps = path.getSteps();
        for (BusStep busStep : busSteps) {
            RouteBusLineItem busLineItem = busStep.getBusLine();
            if (busLineItem != null) {
                s = context.getString(R.string.bus_duration_start_from,
                        busLineItem.getDepartureBusStation().getBusStationName());
                return s;
            }
        }
        return s;
    }

    public static String convertErrorCodeToText(int code) {
        Context context = MainApplication.getAppContext();
        switch (code) {
            case AMapException.CODE_AMAP_CLIENT_NETWORK_EXCEPTION:
                return context.getString(R.string.error_no_connection);
            case AMapException.CODE_AMAP_OVER_DIRECTION_RANGE:
                return context.getString(R.string.error_over_directions_range);
            case AMapException.CODE_AMAP_SERVICE_INVALID_PARAMS:
                return context.getString(R.string.error_invalid_parameter);
            case AMapException.CODE_AMAP_CLIENT_SOCKET_TIMEOUT_EXCEPTION:
                return context.getString(R.string.error_socket_timeout);
            case AMapException.CODE_AMAP_ROUTE_OUT_OF_SERVICE:
                return context.getString(R.string.error_out_of_service);
            case AMapException.CODE_AMAP_DAILY_QUERY_OVER_LIMIT:
                return context.getString(R.string.error_out_of_quota);
            case AMapException.CODE_AMAP_ROUTE_FAIL:
                return context.getString(R.string.error_route_failure);

            default:
                return context.getString(R.string.error_unknown);
        }
    }
}
