package dg.shenm233.mmaps.presenter;

import android.content.Context;
import android.view.View;

import com.amap.api.services.core.PoiItem;

import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.CardListAdapter;
import dg.shenm233.mmaps.database.FavoriteLocations;
import dg.shenm233.mmaps.viewmodel.card.Card;
import dg.shenm233.mmaps.viewmodel.card.FavoriteLocationCard;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class FavoritesPresenter {
    private IFavoriteFragment mFavoriteFragment;
    private Context mContext;

    public FavoritesPresenter(Context context, IFavoriteFragment favoriteFragment) {
        mContext = context;
        mFavoriteFragment = favoriteFragment;
    }

    public void reloadData() {
        Observable.create(new Observable.OnSubscribe<List<PoiItem>>() {
            @Override
            public void call(Subscriber<? super List<PoiItem>> subscriber) {
                subscriber.onStart();
                subscriber.onNext(FavoriteLocations.getInstance().getSavedList());
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PoiItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<PoiItem> poiItems) {
                        CardListAdapter cardListAdapter = mFavoriteFragment.getResultAdapter();
                        cardListAdapter.clear();
                        for (PoiItem poiItem : poiItems) {
                            FavoriteLocationCard card = new FavoriteLocationCard(mContext);
                            setupCard(card, poiItem);
                            cardListAdapter.add(card);
                        }
                        cardListAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupCard(FavoriteLocationCard card, PoiItem poi) {
        card.setName(poi.getTitle());
        card.setDistrict(poi.getAdName());
        card.setType(0x10);
        card.setTag(poi);
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(View view, Card card) {
                int id = view.getId();
                PoiItem poiItem = (PoiItem) card.getTag();
                if (id == R.id.favorite_item_remove) {
                    remove(poiItem, (FavoriteLocationCard) card);
                    return;
                }
                mFavoriteFragment.onPoiItemResult(poiItem);
            }
        });
    }

    private void remove(final PoiItem poiItem, final FavoriteLocationCard card) {
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean deleted = FavoriteLocations.getInstance().remove(poiItem);
                subscriber.onNext(deleted);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        if (result) {
                            CardListAdapter cardListAdapter = mFavoriteFragment.getResultAdapter();
                            cardListAdapter.remove(card);
                            cardListAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
