package dg.shenm233.mmaps.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.ui.maps.MapsFragment;

public class MainActivity extends BaseActivity implements IDrawerView {
    private DrawerLayout mDrawerLayout;
    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_content, getMapsFragment());
        ft.commit();
    }

    private Fragment getMapsFragment() {
        Fragment mapsFragment = getSupportFragmentManager().findFragmentByTag(MapsFragment.class.getName());
        if (mapsFragment == null) {
            mapsFragment = new MapsFragment();
        }
        return mapsFragment;
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
