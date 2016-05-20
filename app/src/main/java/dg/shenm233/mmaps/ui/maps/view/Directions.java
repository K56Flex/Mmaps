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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;

import dg.shenm233.drag2expandview.Drag2ExpandView;
import dg.shenm233.library.litefragment.LiteFragment;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.BusStepsAdapter;
import dg.shenm233.mmaps.adapter.CardListAdapter;
import dg.shenm233.mmaps.adapter.DriveWalkStepsAdapter;
import dg.shenm233.mmaps.presenter.DirectionsPresenter;
import dg.shenm233.mmaps.presenter.IDirectionsView;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.util.AMapUtils;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;

public class Directions extends LiteFragment
        implements IDirectionsView {
    public final static String STARTING_POINT = "starting_point"; // TIP
    public final static String DESTINATION = "destination"; // TIP

    private final static int ROUTE_DRIVE = 0;
    private final static int ROUTE_BUS = 1;
    private final static int ROUTE_WALK = 2;

    private IMapsFragment mMapsFragment;

    private DirectionsPresenter mDirectionsPresenter;
    private DriveWalkStepsAdapter mDriveWalkStepsAdapter;
    private BusStepsAdapter mBusStepsAdapter;

    private ViewGroup mDirectionsBoxView;
    private TextView startingPointText;
    private TextView destinationText;
    private ImageButton mMoreBtn;
    private ImageButton mSwapBtn;

    private ViewGroup mResultViewContainer;
    private ProgressBar mProgressBar;
    private RecyclerView mRouteResultListView;

    private CardListAdapter mResultAdapter;

    private int curSelectedTab = ROUTE_BUS; // 当前被选择Tab 值为 ROUTE_DRIVE,ROUTE_BUS or ROUTE_WALK

    private int curDriveRouteMode = 0; // 当前规划驾车路径的策略(比如默认，避开高速公路等)
    private int curBusRouteMode = 0; // 当前规划公交路径的策略
    private int curWalkRouteMode = 0; // 当前规划行走路径的策略

    private boolean nightBus = true; // 是否包括夜班车

    private Drag2ExpandView mRouteAbstractView;
    private TextView mDistanceTextView;
    private TextView mEtcTextView;
    private RecyclerView mStepListView;

    public Directions(IMapsFragment mapsFragment) {
        mMapsFragment = mapsFragment;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mDirectionsPresenter = new DirectionsPresenter(getContext(), this, mMapsFragment.getMapsModule());
        initAdapters();
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        ViewGroup directionsBoxView = (ViewGroup) inflater.inflate(R.layout.directions_box, container, false);
        mDirectionsBoxView = directionsBoxView;
        CustomOnClickListener onClickListener = new CustomOnClickListener();

        ImageButton backBtn = (ImageButton) directionsBoxView.findViewById(R.id.action_back);
        backBtn.setOnClickListener(onClickListener);
        ImageButton swapBtn = (ImageButton) directionsBoxView.findViewById(R.id.maps_directions_swap);
        mSwapBtn = swapBtn;
        swapBtn.setOnClickListener(onClickListener);

        ImageButton moreBtn = (ImageButton) directionsBoxView.findViewById(R.id.action_more);
        mMoreBtn = moreBtn;
        moreBtn.setOnClickListener(onClickListener);

        ViewGroup from = (ViewGroup) directionsBoxView.findViewById(R.id.maps_directions_from);
        from.setOnClickListener(onClickListener);
        startingPointText = (TextView) from.findViewById(R.id.maps_directions_from_text);
        ViewGroup to = (ViewGroup) directionsBoxView.findViewById(R.id.maps_directions_to);
        to.setOnClickListener(onClickListener);
        destinationText = (TextView) to.findViewById(R.id.maps_directions_to_text);

        initTabs();

        initResultViewContainer(inflater);

//        directionsBoxView.setVisibility(View.GONE);
//        resultViewContainer.setVisibility(View.GONE);
//        rootView.addView(directionsBoxView);
//        rootView.addView(resultViewContainer);

        initRouteAbstractView(inflater);
        setOnStartAnimation(R.anim.slide_in_top);
        setOnStopAnimation(R.anim.slide_out_top);
        setViewToAnimate(directionsBoxView);
    }

    private void initTabs() {
        TabLayout tabLayout = (TabLayout) mDirectionsBoxView.findViewById(R.id.maps_directions_tab);
        TabLayout.Tab carTab = tabLayout.newTab().setIcon(R.drawable.ic_directions_car);
        TabLayout.Tab transitTab = tabLayout.newTab().setIcon(R.drawable.ic_directions_transit);
        TabLayout.Tab walkTab = tabLayout.newTab().setIcon(R.drawable.ic_directions_walk);
        tabLayout.addTab(carTab, ROUTE_DRIVE);
        tabLayout.addTab(transitTab, ROUTE_BUS, true);
        tabLayout.addTab(walkTab, ROUTE_WALK);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                curSelectedTab = tab.getPosition();
                queryRoute();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initResultViewContainer(LayoutInflater inflater) {
        ViewGroup resultViewContainer = (ViewGroup) inflater.inflate(R.layout.directions_result, getViewContainer(), false);
        mResultViewContainer = resultViewContainer;
        mProgressBar = (ProgressBar) resultViewContainer.findViewById(R.id.progress_bar);
        RecyclerView listView = (RecyclerView) resultViewContainer.findViewById(R.id.route_listview);
        mRouteResultListView = listView;
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setAdapter(mResultAdapter = new CardListAdapter());
    }

    private void initRouteAbstractView(LayoutInflater inflater) {
        Drag2ExpandView view = (Drag2ExpandView)
                inflater.inflate(R.layout.route_abstract, getViewContainer(), false);
        view.setVisibility(View.GONE);
        mRouteAbstractView = view;

        ViewGroup headerView = (ViewGroup) view.findViewById(R.id.route_abstract_header);
        final FloatingActionButton button = (FloatingActionButton) headerView.findViewById(R.id.action_navigation);
        final View headerText = headerView.findViewById(R.id.route_abstract_header_text);
        mDistanceTextView = (TextView) headerView.findViewById(R.id.route_tv_distance_duration);
        mEtcTextView = (TextView) headerView.findViewById(R.id.route_tv_etc);
        view.findViewById(R.id.action_navigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNavigation();
            }
        });

        mStepListView = (RecyclerView) view.findViewById(R.id.route_steps_listview);
        mStepListView.setLayoutManager(new LinearLayoutManager(getContext()));

        view.setOnDragListener(new Drag2ExpandView.OnDragListener() {
            @Override
            public void onDrag(Drag2ExpandView v, float dragOffset) {
                int buttonHeight = button.getHeight();
                if (dragOffset >= 0.9f && dragOffset <= 1.f) {
                    button.setTranslationY(dragOffset * buttonHeight / 2);
                    headerText.setTop(0);
                } else if (dragOffset >= 0.f && dragOffset <= 0.1f) {
                    button.setTranslationY(dragOffset * buttonHeight);
                    headerText.setTop(buttonHeight / 2);
                }
            }

            @Override
            public void onStateChanged(Drag2ExpandView v, int oldState, int newState) {

            }
        });
    }

    private void initAdapters() {
        Context context = getContext();

        mDriveWalkStepsAdapter = new DriveWalkStepsAdapter(context);
        mBusStepsAdapter = new BusStepsAdapter(context);
        OnViewClickListener listener = new OnViewClickListener() {
            @Override
            public void onClick(View v, Object data) {
                if (curSelectedTab == ROUTE_WALK) {
                    mDirectionsPresenter.moveCameraToWalkStep(data);
                    mRouteAbstractView.collapseView();
                } else if (curSelectedTab == ROUTE_DRIVE) {
                    mDirectionsPresenter.moveCameraToDriveStep(data);
                    mRouteAbstractView.collapseView();
                } else if (curSelectedTab == ROUTE_BUS) {
                    mDirectionsPresenter.moveCameraToBusStep(data);
                    mRouteAbstractView.collapseView();
                }
            }
        };
        mBusStepsAdapter.setOnViewClickListener(listener);
        mDriveWalkStepsAdapter.setOnViewClickListener(listener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewGroup container = getViewContainer();

        setStartingPointFromArgs();
        setDestPointFromArgs();

        mMapsFragment.setDirectionsBtnVisibility(View.GONE);
        mMapsFragment.setStatusBarColor(getContext().getResources().getColor(R.color.primary_color));
//        mDirectionsBoxView.setVisibility(View.VISIBLE);
        mResultViewContainer.setVisibility(View.VISIBLE);
        container.addView(mDirectionsBoxView);
        container.addView(mResultViewContainer);
        container.addView(mRouteAbstractView);

        queryRoute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewGroup container = getViewContainer();
        mDirectionsPresenter.exit();
        restoreViewState();
//        mDirectionsBoxView.setVisibility(View.GONE);
//        mResultViewContainer.setVisibility(View.GONE);
        container.removeView(mDirectionsBoxView);
        container.removeView(mResultViewContainer);
        container.removeView(mRouteAbstractView);
        mMapsFragment.setDirectionsBtnVisibility(View.VISIBLE);
        mMapsFragment.setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    public boolean onBackPressed() {
        if (mRouteAbstractView.getViewState() == Drag2ExpandView.STATE_EXPAND) {
            mRouteAbstractView.collapseView();
            return true;
        }

        if (curSelectedTab == ROUTE_BUS && mResultViewContainer.getVisibility() != View.VISIBLE) {
            mRouteAbstractView.setVisibility(View.INVISIBLE);
            mResultViewContainer.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private void setStartingPointFromArgs() {
        Tip tip = getArguments().getParcelable(STARTING_POINT);
        if (tip != null) {
            LatLonPoint point = tip.getPoint();
            // 检查出发点/终点是否相同
            if (point != null && point.equals(destinationText.getTag())) {
                return;
            }
            startingPointText.setText(tip.getName());
            startingPointText.setTag(point);
        }
    }

    private void setDestPointFromArgs() {
        Tip tip = getArguments().getParcelable(DESTINATION);
        if (tip != null) {
            LatLonPoint point = tip.getPoint();
            // 检查出发点/终点是否相同
            if (point != null && point.equals(startingPointText.getTag())) {
                return;
            }
            destinationText.setText(tip.getName());
            destinationText.setTag(point);
        }
    }

    private void restoreViewState() {
        mProgressBar.setVisibility(View.GONE);
        mRouteAbstractView.setVisibility(View.GONE);
    }

    private void swapDirections() {
        Object from_tag = startingPointText.getTag();
        Object to_tag = destinationText.getTag();
        startingPointText.setTag(to_tag);
        destinationText.setTag(from_tag);
        CharSequence tmp = startingPointText.getText();
        startingPointText.setText(destinationText.getText());
        destinationText.setText(tmp);
        queryRoute();
    }

    private void queryRoute() {
        LatLonPoint startPoint = (LatLonPoint) startingPointText.getTag();
        LatLonPoint endPoint = (LatLonPoint) destinationText.getTag();
        if (endPoint == null || startPoint == null)
            return;

        mRouteAbstractView.setVisibility(View.INVISIBLE);

        mResultViewContainer.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mRouteResultListView.setVisibility(View.INVISIBLE);

        String sPointStr = startingPointText.getText().toString(); // 出发点的文本
        String dPointStr = destinationText.getText().toString(); // 目的点的文本

        if (curSelectedTab == ROUTE_DRIVE) {
            mDriveWalkStepsAdapter.setStartingPointText(sPointStr);
            mDriveWalkStepsAdapter.setDestPointText(dPointStr);

            mDirectionsPresenter.queryDriveRoute(startPoint, endPoint, curDriveRouteMode);
        } else if (curSelectedTab == ROUTE_BUS) {
            mBusStepsAdapter.setStartingPointText(sPointStr);
            mBusStepsAdapter.setDestPointText(dPointStr);

            mDirectionsPresenter.queryBusRoute(startPoint, endPoint, curBusRouteMode, nightBus);
        } else if (curSelectedTab == ROUTE_WALK) {
            mDriveWalkStepsAdapter.setStartingPointText(sPointStr);
            mDriveWalkStepsAdapter.setDestPointText(dPointStr);

            mDirectionsPresenter.queryWalkRoute(startPoint, endPoint, curWalkRouteMode);
        }
        // 隐藏交换按钮，显示三点
        mMoreBtn.setVisibility(View.VISIBLE);
        mSwapBtn.setVisibility(View.INVISIBLE);
    }

    private void startNavigation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true)
                .setTitle(R.string.navi_attention)
                .setMessage(R.string.navi_attention_msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startNavigationReal();
                    }
                })
                .show();
    }

    private void startNavigationReal() {
        if (curSelectedTab == ROUTE_DRIVE) {
            mDirectionsPresenter.startDriveNavigation(curDriveRouteMode);
        } else if (curSelectedTab == ROUTE_WALK) {
            mDirectionsPresenter.startWalkNavigation(curWalkRouteMode);
        } else {
            Toast.makeText(getContext(), "sorry,not supported", Toast.LENGTH_SHORT).show();
        }
    }

    //TODO: 这部分涉及硬编码，注意一下高德sdk的更新
    private void showRouteOptions() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.route_options);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Directions.this.queryRoute();
            }
        });
        if (curSelectedTab == ROUTE_DRIVE) {
            final boolean[] checkboxes = AMapUtils.covertAMapDriveModeToBools(curDriveRouteMode);
            builder.setMultiChoiceItems(R.array.route_options_drive,
                    checkboxes, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            checkboxes[which] = isChecked;
                            int checkboxesCount = checkboxes.length;
                            ListView l = ((AlertDialog) dialog).getListView();
                            if ((which == 3 || which == 4) && checkboxes[which]) {
                                for (int i = 0; i < checkboxesCount; i++) {
                                    if (i != which) { // 取消其他复选框
                                        checkboxes[i] = false;
                                        l.setItemChecked(i, false);
                                    }
                                }
                            } else {
                                for (int i = 3; i < checkboxesCount; i++) {
                                    checkboxes[i] = false;
                                    l.setItemChecked(i, false);
                                }
                            }
                        }
                    });
            builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    curDriveRouteMode = AMapUtils.convertToAMapDriveMode(checkboxes);
                    dialog.dismiss();
                }
            }).show();
        } else if (curSelectedTab == ROUTE_BUS) {
            builder.setSingleChoiceItems(R.array.route_options_bus, curBusRouteMode,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            curBusRouteMode = which; /* 注意string-array要跟RouteSearch的顺序一一对应
                                                        为了少写switch判断就这么干了 */
                            dialog.dismiss();
                        }
                    });
            builder.show();
        } else if (curSelectedTab == ROUTE_WALK) {
            builder.setSingleChoiceItems(R.array.route_options_walk, curWalkRouteMode,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            curWalkRouteMode = which; /* 注意string-array要跟RouteSearch的顺序一一对应
                                                        为了少写switch判断就这么干了 */
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Intent data) {
        Tip tip = data.getParcelableExtra("result");
        if (requestCode == R.id.maps_directions_from) {
            getArguments().putParcelable(STARTING_POINT, tip);
        } else if (requestCode == R.id.maps_directions_to) {
            getArguments().putParcelable(DESTINATION, tip);
        }
    }

    @Override
    public void showPathOnMap() {
        switch (curSelectedTab) {
            case ROUTE_DRIVE:
            case ROUTE_WALK:
                mStepListView.setAdapter(mDriveWalkStepsAdapter);
                mDriveWalkStepsAdapter.notifyDataSetChanged();
                break;
            case ROUTE_BUS:
                mStepListView.setAdapter(mBusStepsAdapter);
                mBusStepsAdapter.notifyDataSetChanged();
                break;
        }
        mMapsFragment.setMapViewVisibility(View.VISIBLE);
        mResultViewContainer.setVisibility(View.INVISIBLE);
        mRouteAbstractView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showRouteList() {
        mMapsFragment.setMapViewVisibility(View.INVISIBLE);
        mResultViewContainer.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mRouteResultListView.setVisibility(View.VISIBLE);
        mResultAdapter.notifyDataSetChanged();
    }

    @Override
    public CardListAdapter getResultAdapter() {
        return mResultAdapter;
    }

    @Override
    public DriveWalkStepsAdapter getDriveWalkStepsAdapter() {
        return mDriveWalkStepsAdapter;
    }

    @Override
    public BusStepsAdapter getBusStepsAdapter() {
        return mBusStepsAdapter;
    }

    @Override
    public void setDistanceTextOnAbstractView(String s) {
        mDistanceTextView.setText(s);
    }

    @Override
    public void setEtcTextOnAbstractView(String s) {
        mEtcTextView.setText(s);
    }

    @Override
    public int getRouteType() {
        return curSelectedTab;
    }

    @Override
    public int getDriveRouteMode() {
        return curDriveRouteMode;
    }

    @Override
    public int getBusRouteMode() {
        return curBusRouteMode;
    }

    @Override
    public int getWalkRouteMode() {
        return curWalkRouteMode;
    }

    private class CustomOnClickListener implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        @Override
        public void onClick(View v) {
            int viewId = v.getId();
            if (viewId == R.id.action_back) {
                finish();
            } else if (viewId == R.id.maps_directions_swap) {
                swapDirections();
            } else if (viewId == R.id.maps_directions_from || viewId == R.id.maps_directions_to) {
                Bundle args = new Bundle();
                args.putBoolean(SearchBox.SHOW_CHOOSE_ON_MAP, true);
                args.putBoolean(SearchBox.HIDE_POI_WITHOUT_LOC,true);
                SearchBox searchBox = new SearchBox(mMapsFragment);
                searchBox.setArguments(args);
                int requestCode = viewId;
                startLiteFragmentForResult(requestCode, searchBox, null);
            } else if (viewId == R.id.action_more) {
                PopupMenu popupMenu = new PopupMenu(getContext(), mMoreBtn);
                popupMenu.inflate(R.menu.directions_more);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.maps_directions_swap:
                    swapDirections();
                    break;
                case R.id.route_options:
                    showRouteOptions();
                    break;
            }
            return true;
        }
    }
}
