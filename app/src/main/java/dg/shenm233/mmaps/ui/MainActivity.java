package dg.shenm233.mmaps.ui;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.WindowManager;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.presenter.MapsModule;
import dg.shenm233.mmaps.ui.maps.MapsFragment;

public class MainActivity extends BaseActivity implements IDrawerView {
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        initNavigationView();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_content, getMapsFragment(), MapsFragment.class.getName());
        ft.commit();
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
                } else if (itemId == R.id.navigation_traffic) {
                    mapsModule.setTrafficEnabled(!isChecked);
                    item.setChecked(!isChecked);
                }
                return true;
            }
        });
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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (fragment instanceof IDrawerView) {
            if (!((IDrawerView) fragment).onBackKeyPressed())
                super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
