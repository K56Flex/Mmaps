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
