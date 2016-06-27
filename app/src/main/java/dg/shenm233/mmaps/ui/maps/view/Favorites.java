package dg.shenm233.mmaps.ui.maps.view;

import android.content.Intent;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Tip;

import dg.shenm233.library.litefragment.LiteFragment;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.ui.maps.MapsFragment;

/**
 * FavoritesFragment的外壳
 */
public class Favorites extends LiteFragment {
    private boolean isCalledFavFragment = false;
    private IMapsFragment mMapsFragment;

    public Favorites(IMapsFragment mapsFragment) {
        mMapsFragment = mapsFragment;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isCalledFavFragment) {
            isCalledFavFragment = true;
            ((MapsFragment) mMapsFragment).chooseFromFavorites();
        } else { // 如果再一次启动则直接退出
            finish();
        }
    }

    public void onFavFragmentResult(PoiItem poiItem) {
        Intent result = new Intent();
        Tip tip = new Tip();
        tip.setName(poiItem.getTitle());
        tip.setPostion(poiItem.getLatLonPoint());
        result.putExtra("result", tip);
        setResult(ACTION_SUCCESS, result);
        finish();
    }
}
