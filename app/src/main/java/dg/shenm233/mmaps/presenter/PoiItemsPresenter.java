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

import android.content.Context;
import android.util.Log;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.api.maps.overlay.PoiOverlayS;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PoiItemsPresenter {
    private Context mContext;
    private IPoiItemsView mPoiItemsView;
    private MapsModule mMapsModule;

    private List<PoiOverlayS> mPoiOverlayList = new ArrayList<>();
    private List<PoiItem> mPoiItemList = new ArrayList<>();

    public PoiItemsPresenter(Context context, IPoiItemsView poiItemsView, MapsModule mapsModule) {
        mContext = context;
        mPoiItemsView = poiItemsView;
        mMapsModule = mapsModule;
    }

    private Subscription mCurPoiQuery;

    public void searchPoiItems(String keyword, String category, String city, int page) {
        final PoiSearch.Query query = new PoiSearch.Query(keyword, category, city);
        query.setPageNum(page);
        query.setPageSize(10);

        Log.d("PoiItemsPresenter", "load page " + page);

        mCurPoiQuery = Observable.create(new Observable.OnSubscribe<PoiResult>() {
            @Override
            public void call(Subscriber<? super PoiResult> subscriber) {
                PoiSearch search = new PoiSearch(mContext, query);
                subscriber.onStart();
                try {
                    subscriber.onNext(search.searchPOI());
                    subscriber.onCompleted();
                } catch (AMapException e) {
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Func1<PoiResult, List<PoiItem>>() {
                    @Override
                    public List<PoiItem> call(PoiResult poiResult) {
                        if (poiResult == null || poiResult.getPois() == null) {
                            return null;
                        }
                        List<PoiItem> pois = poiResult.getPois();
                        if (pois.size() == 0) {
                            return null;
                        }
                        mPoiOverlayList.add(mMapsModule.addPoiOverlay(pois));
                        return pois;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<PoiItem>>() {
                    @Override
                    public void onCompleted() {
                        mPoiItemsView.setPoiList(mPoiItemList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mPoiItemsView.onPoiPageLoaded(false);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<PoiItem> poiItems) {
                        if (isUnsubscribed()) {
                            return;
                        }
                        if (poiItems != null) {
                            mPoiItemList.addAll(poiItems);
                            mPoiItemsView.onPoiPageLoaded(true);
                        } else {
                            mPoiItemsView.onPoiPageLoaded(false);
                        }
                    }
                });
    }

    public void exit() {
        stopCurPoiQuery();
        for (PoiOverlayS poiOverlay : mPoiOverlayList) {
            poiOverlay.removeFromMap();
        }
    }

    public void reAddPoiMarkers() {
        for (PoiOverlayS poiOverlay : mPoiOverlayList) {
            poiOverlay.addToMap();
        }
    }

    private void stopCurPoiQuery() {
        if (mCurPoiQuery != null) {
            mCurPoiQuery.unsubscribe();
            mCurPoiQuery = null;
        }
    }
}
