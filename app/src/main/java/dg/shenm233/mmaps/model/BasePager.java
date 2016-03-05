package dg.shenm233.mmaps.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class BasePager {
    protected Context mContext;

    /**
     * 本Pager中的View
     */
    protected View mBindView;

    /**
     * 该Pager名
     */
    protected CharSequence mTitle;

    public BasePager(Context context) {
        mContext = context;
    }

    /**
     * 创建子View
     *
     * @param rootView 父View
     * @return 创建的子View
     */
    public final View createView(ViewGroup rootView) {
        View child = onCreateView(rootView);
        mBindView = child;
        return child;
    }

    /**
     * 创建子View
     *
     * @param rootView 父View
     * @return 创建的子View
     * 注意: 创建的子View不要自行添加到其他任何View中
     */
    public abstract View onCreateView(ViewGroup rootView);

    public View getBindView() {
        return mBindView;
    }

    public void setTitle(CharSequence s) {
        mTitle = s;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void onDestroy() {

    }
}
