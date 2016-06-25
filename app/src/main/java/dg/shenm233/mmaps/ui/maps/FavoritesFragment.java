package dg.shenm233.mmaps.ui.maps;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.CardListAdapter;
import dg.shenm233.mmaps.presenter.FavoritesPresenter;
import dg.shenm233.mmaps.presenter.IFavoriteFragment;

public class FavoritesFragment extends Fragment implements IFavoriteFragment {
    private FavoritesPresenter mPresenter;
    private CardListAdapter mFavListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mPresenter = new FavoritesPresenter(getContext(), this);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        RecyclerView favListView = (RecyclerView) view.findViewById(R.id.favorite_list);
        favListView.setAdapter(mFavListAdapter = new CardListAdapter());
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.reloadData();
    }

    @Override
    public CardListAdapter getResultAdapter() {
        return mFavListAdapter;
    }
}
