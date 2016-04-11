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
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.List;

import dg.shenm233.mmaps.R;

public class SearchMapsPresenter {
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
        poiSearch.setOnPoiSearchListener(onPoiSearchListener);
        poiSearch.searchPOIAsyn();
    }

    public void setOnTipsListener(OnTipsListener l) {
        mOnTipsListener = l;
    }

    public void requestInputTips(String string, String city) {
        if (mInputTips == null)
            mInputTips = new Inputtips(mContext, inputTipsListener);
        try {
            mInputTips.requestInputtips(string, city);
        } catch (AMapException e) {
            //
        }
    }

    private PoiSearch.OnPoiSearchListener onPoiSearchListener =
            new PoiSearch.OnPoiSearchListener() {
                @Override
                public void onPoiSearched(PoiResult poiResult, int rCode) {
                    List<PoiItem> poiItems = null;
                    if (rCode == 0) {
                        if (poiResult != null && poiResult.getQuery() != null) { // 搜索poi的结果
                            if (poiResult.getQuery().equals(curQuery)) { // 是否查找同一条
                                poiItems = poiResult.getPois();// 取得第一页的poiItem数据，页数从数字0开始
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
                public void onPoiItemSearched(PoiItem poiItem, int rCode) {

                }
            };

    private Inputtips.InputtipsListener inputTipsListener =
            new Inputtips.InputtipsListener() {
                @Override
                public void onGetInputtips(List<Tip> list, int rCode) {
                    if (rCode == 0) {
                        if (mOnTipsListener != null)
                            mOnTipsListener.onGetInputTips(list);
                    }
                }
            };
}
