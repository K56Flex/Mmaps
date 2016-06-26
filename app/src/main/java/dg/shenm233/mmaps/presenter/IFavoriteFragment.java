package dg.shenm233.mmaps.presenter;

import com.amap.api.services.core.PoiItem;

import dg.shenm233.mmaps.adapter.CardListAdapter;

public interface IFavoriteFragment {
    CardListAdapter getResultAdapter();

    void onPoiItemResult(PoiItem poi);
}
