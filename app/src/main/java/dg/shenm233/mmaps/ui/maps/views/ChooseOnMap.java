package dg.shenm233.mmaps.ui.maps.views;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.help.Tip;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.ui.maps.ViewContainerManager;
import dg.shenm233.mmaps.util.AMapUtils;

public class ChooseOnMap extends ViewContainerManager.ViewContainer
        implements View.OnClickListener {
    public static final int CHOOSE_ON_MAP_ID = 2;

    private Context mContext;
    private IMapsFragment mMapsFragment;

    private ViewGroup rootView;
    private ViewGroup mainView;
    private Marker marker;

    public ChooseOnMap(ViewGroup rootView, IMapsFragment mapsFragment) {
        this.rootView = rootView;
        mContext = rootView.getContext();
        mMapsFragment = mapsFragment;

        onCreateView();
    }

    @Override
    public void onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup mainView = (ViewGroup) inflater.inflate(R.layout.search_choose_on_map, rootView, false);
        this.mainView = mainView;
        Button ok = (Button) mainView.findViewById(R.id.action_ok);
        ok.setOnClickListener(this);
        Button back = (Button) mainView.findViewById(R.id.action_back);
        back.setOnClickListener(this);
    }

    @Override
    public void show() {
        rootView.addView(mainView);
        mMapsFragment.setMapViewVisibility(View.VISIBLE);

        Marker marker = mMapsFragment.getMapsModule().addMarker();
        marker.setIcon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker.setDraggable(false);
        this.marker = marker;

        Rect rect = new Rect();
        rootView.getGlobalVisibleRect(rect);
        //TODO: 输入法出现时会导致marker不能居中,另外暂时不考虑使用屏幕分辨率来确定
        marker.setPositionByPixels(rect.centerX(), rect.centerY()); // 设置marker的位置为"居中"
    }

    @Override
    public void exit() {
        marker.destroy();
        mMapsFragment.setMapViewVisibility(View.INVISIBLE);
        rootView.removeView(mainView);
    }

    @Override
    public void onDestroyView() {

    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.action_ok) {
            Tip tip = new Tip();
            tip.setPostion(AMapUtils.convertToLatLonPoint(marker.getPosition()));
            tip.setName(mContext.getText(R.string.chosen_on_map).toString());
            ((SearchBox.OnSearchItemClickListener) mMapsFragment).onSearchItemClick(tip);
        } else if (id == R.id.action_back) {
            mMapsFragment.getViewContainerManager().popBackStack();
        }
    }
}
