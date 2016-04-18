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

package dg.shenm233.mmaps.ui.maps.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.design.widget.CoordinatorLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.help.Tip;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.ui.IDrawerView;
import dg.shenm233.mmaps.ui.maps.ViewContainer;
import dg.shenm233.mmaps.util.AMapUtils;
import dg.shenm233.mmaps.util.AnimUtils;

public class ChooseOnMap extends ViewContainer
        implements View.OnClickListener {
    public static final int ID = 2;

    private Context mContext;
    private IMapsFragment mMapsFragment;

    private ViewGroup rootView;
    private ViewGroup titleView;
    private ViewGroup buttonBarView;

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
        titleView = (ViewGroup) inflater.inflate(R.layout.search_choose_on_map_title, rootView, false);

        ViewGroup buttonBarView = (ViewGroup) inflater.inflate(R.layout.button_bar, rootView, false);
        ((CoordinatorLayout.LayoutParams) buttonBarView.getLayoutParams()).gravity = Gravity.BOTTOM;
        this.buttonBarView = buttonBarView;
        buttonBarView.findViewById(R.id.action_ok).setOnClickListener(this);
        buttonBarView.findViewById(R.id.action_back).setOnClickListener(this);
        buttonBarView.setTag(R.id.action_my_location, true);
    }

    @Override
    public void show() {
        ((IDrawerView) mMapsFragment).enableDrawer(false);

        ViewGroup rootView = this.rootView;
        AnimUtils.viewSlideInTop(titleView);
        rootView.addView(titleView);
        rootView.addView(buttonBarView);

        mMapsFragment.setStatusBarColor(mContext.getResources().getColor(R.color.primary_color));
        mMapsFragment.setDirectionsBtnVisibility(View.GONE);
        mMapsFragment.setMapViewVisibility(View.VISIBLE);

        Marker marker = mMapsFragment.getMapsModule().addMarker();
        marker.setIcon(BitmapDescriptorFactory
                .fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pin)));
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
        mMapsFragment.setStatusBarColor(Color.TRANSPARENT);
        mMapsFragment.setMapViewVisibility(View.INVISIBLE);
        AnimUtils.viewSlideOutTop(titleView);
        rootView.removeView(titleView);
        rootView.removeView(buttonBarView);
        mMapsFragment.setDirectionsBtnVisibility(View.VISIBLE);

        ((IDrawerView) mMapsFragment).enableDrawer(true);
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
