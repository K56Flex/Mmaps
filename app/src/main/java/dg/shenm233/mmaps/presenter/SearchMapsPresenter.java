package dg.shenm233.mmaps.presenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.List;

import dg.shenm233.mmaps.R;

public class SearchMapsPresenter implements PoiSearch.OnPoiSearchListener, Inputtips.InputtipsListener {
    public interface OnPoiSearchListener {
        void onSearchPoiResult(@Nullable List<PoiItem> poiItems);
    }

    public interface OnTipsListener {
        void onGetInputTips(List<Tip> tipList);
    }

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private MapsModule mMapsModule;

    private OnPoiSearchListener mOnPoiSearchListener;
    private OnTipsListener mOnTipsListener;

    private Inputtips mInputTips;

    private PoiSearch.Query curQuery;
    private List<PoiItem> lastPoiItems;

    public SearchMapsPresenter(Context context, MapsModule mapsModule) {
        mContext = context;
        mMapsModule = mapsModule;
    }

    private void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(mContext.getText(R.string.searching));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (dialog == mProgressDialog)
                        mOnPoiSearchListener = null; // 由于sdk没有提供直接取消搜索的API，所以把监听器设为null
                }
            });
        }
        mProgressDialog.show();
    }

    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    public void searchPoi(String keyWord, String city, OnPoiSearchListener l) {
        curQuery = new PoiSearch.Query(keyWord, "", city);
        curQuery.setPageSize(10); // 设置每页最多返回多少条poiitem
        curQuery.setPageNum(0); // 设置查第一页
        mOnPoiSearchListener = l;

        showProgress();

        PoiSearch poiSearch = new PoiSearch(mContext, curQuery);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    public PoiItem getPoiItemFromLastSearch(Integer i) {
        return lastPoiItems.get(i);
    }

    public void setOnTipsListener(OnTipsListener l) {
        mOnTipsListener = l;
    }

    public void requestInputTips(String string, String city) {
        if (mInputTips == null)
            mInputTips = new Inputtips(mContext, this);
        try {
            mInputTips.requestInputtips(string, city);
        } catch (AMapException e) {
            //
        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int rCode) {
        List<PoiItem> poiItems = null;
        if (rCode == 0) {
            if (poiResult != null && poiResult.getQuery() != null) { // 搜索poi的结果
                if (poiResult.getQuery().equals(curQuery)) { // 是否查找同一条
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    lastPoiItems = poiItems;
                }
            }
        }
        if (poiItems == null || poiItems.size() == 0) {
            Toast.makeText(mContext, R.string.no_result, Toast.LENGTH_SHORT).show();
        }
        hideProgress();
        if (mOnPoiSearchListener != null) {
            mOnPoiSearchListener.onSearchPoiResult(poiItems);
            mOnPoiSearchListener = null;
        }
    }

    @Override
    public void onPoiItemDetailSearched(PoiItemDetail poiItemDetail, int rCode) {

    }

    @Override
    public void onGetInputtips(List<Tip> list, int rCode) {
        if (rCode == 0) {
            if (mOnTipsListener != null)
                mOnTipsListener.onGetInputTips(list);
        }
    }
}
