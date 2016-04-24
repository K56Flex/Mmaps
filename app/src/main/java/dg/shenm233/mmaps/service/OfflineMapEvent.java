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

package dg.shenm233.mmaps.service;

public class OfflineMapEvent {
    public final static String MANAGER_READY_EVENT = "manager_ready";
    public final static String DOWNLOAD_EVENT = "download";
    public final static String CHECK_UPDATE_EVENT = "check_update";
    public final static String REMOVE_EVENT = "remove";

    public final String eventType;

    /**
     * 城市名/省名
     */
    public String name;

    /**
     * 详细描述
     */
    public String description;

    /**
     * 下载状态
     */
    public int statusCode;

    /**
     * 下载进度
     */
    public int completeCode;

    /**
     * 该城市地图是否有更新
     */
    public boolean hasUpdate = false;

    /**
     * 删除地图是否成功
     */
    public boolean removeSuccess = false;


    public OfflineMapEvent(String eventType) {
        this.eventType = eventType;
    }
}
