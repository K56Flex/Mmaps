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

package dg.shenm233.mmaps.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.amap.api.services.core.PoiItem;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.model.LocationManager;
import dg.shenm233.mmaps.presenter.MapsModule;
import dg.shenm233.mmaps.ui.maps.FavoritesFragment;
import dg.shenm233.mmaps.ui.maps.MapsFragment;

public class MainActivity extends BaseActivity implements IDrawerView {
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.status_bar_color));

        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        initNavigationView();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_content, getMapsFragment(), MapsFragment.class.getName());
        ft.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationManager.destroy();
    }

    private Fragment getMapsFragment() {
        Fragment mapsFragment = getSupportFragmentManager().findFragmentByTag(MapsFragment.class.getName());
        if (mapsFragment == null) {
            mapsFragment = new MapsFragment();
        }
        return mapsFragment;
    }

    private void initNavigationView() {
        NavigationView nv = (NavigationView) mDrawerLayout.findViewById(R.id.navigation_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                MapsModule mapsModule = ((MapsFragment) getMapsFragment()).getMapsModule();
                int itemId = item.getItemId();
                boolean isChecked = item.isChecked();
                if (itemId == R.id.navigation_satellite) {
                    if (!isChecked) {
                        mapsModule.setMapType(MapsModule.MAP_TYPE_SATELLITE);
                    } else {
                        mapsModule.setMapType(MapsModule.MAP_TYPE_NORMAL);
                    }
                    item.setChecked(!isChecked);
                    mDrawerLayout.closeDrawers();
                } else if (itemId == R.id.navigation_traffic) {
                    mapsModule.setTrafficEnabled(!isChecked);
                    item.setChecked(!isChecked);
                    mDrawerLayout.closeDrawers();
                } else if (itemId == R.id.navigation_offline) {
                    Intent intent = new Intent(MainActivity.this, OfflineMapActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.navigation_settings) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.navigation_favorite) {
                    mDrawerLayout.closeDrawers();
                    switchToFavorite();
                }
                return true;
            }
        });
    }

    public void onPoiItemResult(PoiItem poi) {
        ((MapsFragment) getMapsFragment()).onPoiItemResult(poi);
    }

    private void switchToFavorite() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(FavoritesFragment.class.getName()) != null) return;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_content, new FavoritesFragment(), FavoritesFragment.class.getName());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void openDrawer() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    @Override
    public void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void enableDrawer(boolean enable) {
        if (!enable) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public boolean onBackKeyPressed() {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (fragment instanceof IDrawerView) {
            if (!((IDrawerView) fragment).onBackKeyPressed())
                super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
