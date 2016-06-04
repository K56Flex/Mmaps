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

import android.content.Intent;
import android.content.res.Resources;
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

import dg.shenm233.library.litefragment.LiteFragment;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.ui.IDrawerView;
import dg.shenm233.mmaps.util.AMapUtils;

public class ChooseOnMap extends LiteFragment
        implements View.OnClickListener {
    private IMapsFragment mMapsFragment;

    private ViewGroup titleView;
    private ViewGroup buttonBarView;

    private Marker marker;

    public ChooseOnMap(IMapsFragment mapsFragment) {
        mMapsFragment = mapsFragment;
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        titleView = (ViewGroup) inflater.inflate(R.layout.search_choose_on_map_title, container, false);

        ViewGroup buttonBarView = (ViewGroup) inflater.inflate(R.layout.button_bar, container, false);
        ((CoordinatorLayout.LayoutParams) buttonBarView.getLayoutParams()).gravity = Gravity.BOTTOM;
        this.buttonBarView = buttonBarView;
        buttonBarView.findViewById(R.id.action_ok).setOnClickListener(this);
        buttonBarView.findViewById(R.id.action_back).setOnClickListener(this);
        buttonBarView.setTag(R.id.action_my_location, true);
        buttonBarView.setTag(R.id.zoom_in, true);
        buttonBarView.setTag(R.id.zoom_out, true);
        setOnStartAnimation(R.animator.slide_in_top);
        setOnStopAnimation(R.animator.slide_out_top);
        setViewToAnimate(titleView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewGroup container = getViewContainer();
        Resources res = getContext().getResources();
        ((IDrawerView) mMapsFragment).enableDrawer(false);

        container.addView(titleView);
        container.addView(buttonBarView);

        mMapsFragment.setStatusBarColor(res.getColor(R.color.primary_color));
        mMapsFragment.setDirectionsBtnVisibility(View.GONE);
        mMapsFragment.setMapViewVisibility(View.VISIBLE);

        Marker marker = mMapsFragment.getMapsModule().addMarker();
        marker.setIcon(BitmapDescriptorFactory
                .fromBitmap(BitmapFactory.decodeResource(res, R.drawable.pin)));
        marker.setDraggable(false);
        this.marker = marker;

        Rect rect = new Rect();
        container.getGlobalVisibleRect(rect);
        marker.setPositionByPixels(rect.centerX(), rect.centerY()); // 设置marker的位置为"居中"
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewGroup container = getViewContainer();

        marker.destroy();
        mMapsFragment.setStatusBarColor(Color.TRANSPARENT);
        mMapsFragment.setMapViewVisibility(View.INVISIBLE);

        container.removeView(titleView);
        container.removeView(buttonBarView);
        mMapsFragment.setDirectionsBtnVisibility(View.VISIBLE);

        ((IDrawerView) mMapsFragment).enableDrawer(true);
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
            tip.setName(getContext().getText(R.string.chosen_on_map).toString());
            Intent result = new Intent();
            result.putExtra("result", tip);
            setResult(ACTION_SUCCESS, result);
            finish();
        } else if (id == R.id.action_back) {
            finish();
        }
    }
}
