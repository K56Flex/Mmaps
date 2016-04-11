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

package dg.shenm233.mmaps.model.offlinemap;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.List;

public class ProvinceListItem implements ParentListItem {
    private OfflineMapProvince mOfflineMapProvince;
    private List<OfflineMapCity> mCites;

    public ProvinceListItem(OfflineMapProvince mapProvince) {
        mOfflineMapProvince = mapProvince;
        mCites = mapProvince.getCityList();
    }

    public OfflineMapProvince getOfflineMapProvince() {
        return mOfflineMapProvince;
    }

    @Override
    public List<OfflineMapCity> getChildItemList() {
        return mCites;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
