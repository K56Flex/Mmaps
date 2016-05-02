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
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.amap.api.services.help.Tip;

import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.SearchTipsAdapter;
import dg.shenm233.mmaps.database.RecentSearchTips;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.presenter.ISearchBox;
import dg.shenm233.mmaps.presenter.SearchBoxPresenter;
import dg.shenm233.mmaps.ui.IDrawerView;
import dg.shenm233.mmaps.ui.maps.ViewContainer;
import dg.shenm233.mmaps.util.AnimUtils;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;

public class SearchBox extends ViewContainer
        implements ISearchBox, View.OnClickListener {

    public interface OnSearchItemClickListener {
        void onSearchItemClick(Tip tip);
    }

    public static final int ID = 0;
    public static final String ONLY_SEARCH_BOX = "only_search_box"; //boolean
    public static final String BACK_BTN_AS_DRAWER = "back_btn_as_drawer"; //boolean
    public static final String SHOW_CHOOSE_ON_MAP = "show_choose_on_map"; //boolean

    private Context mContext;
    private IMapsFragment mMapsFragment;
    private SearchBoxPresenter mSearchBoxPresenter;

    private ViewGroup rootView;
    private ViewGroup mSearchBox;
    private EditText mSearchEditText;
    private ImageButton mBackBtn;

    private ViewGroup mSearchResultContainer;
    private ViewGroup mChooseOnMapBtn;

    private SearchTipsAdapter mResultAdapter;

    private boolean enableTextTip = true;

    /**
     * 标记当前的Back按钮是否用于Drawer
     */
    private boolean isBackBtnAsDrawer = false;

    public SearchBox(ViewGroup rootView, IMapsFragment mapsFragment) {
        this.rootView = rootView;
        Context context = rootView.getContext();
        mContext = context;

        mMapsFragment = mapsFragment;
        mSearchBoxPresenter = new SearchBoxPresenter(context, this, mapsFragment.getMapsModule());

        onCreateView();
    }

    @Override
    public void onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup searchBox = (ViewGroup) inflater.inflate(R.layout.search_box, rootView, false);
        mSearchBox = searchBox;

        ImageButton btn = (ImageButton) searchBox.findViewById(R.id.opendrawer_or_back);
        mBackBtn = btn;
        btn.setOnClickListener(this);

        final ImageButton editTextClearBtn = (ImageButton) searchBox.findViewById(R.id.edit_text_clear);
        editTextClearBtn.setOnClickListener(this);

        EditText searchEditText = (EditText) searchBox.findViewById(R.id.search_edittext);
        mSearchEditText = searchEditText;
        searchEditText.setCursorVisible(false);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    editTextClearBtn.setVisibility(View.VISIBLE);
                } else {
                    editTextClearBtn.setVisibility(View.INVISIBLE);
                }
                if (enableTextTip) {
                    String str = s.toString().trim();
                    mSearchBoxPresenter.requestInputTips(str, "");
                }
            }
        });
        searchEditText.setOnClickListener(this);

        ViewGroup searchResultContainer = (ViewGroup) inflater.inflate(R.layout.search_result, rootView, false);
        mSearchResultContainer = searchResultContainer;

        ViewGroup chooseOnMapBtn = (ViewGroup) searchResultContainer.findViewById(R.id.search_choose_on_map);
        mChooseOnMapBtn = chooseOnMapBtn;
        chooseOnMapBtn.setOnClickListener(this);

        RecyclerView resultListView = (RecyclerView) searchResultContainer.findViewById(R.id.result_listview);
        SearchTipsAdapter searchTipsAdapter = new SearchTipsAdapter(mContext);
        mResultAdapter = searchTipsAdapter;
        resultListView.setAdapter(searchTipsAdapter);
        searchTipsAdapter.setOnViewClickListener(new OnViewClickListener() {
            @Override
            public void onClick(View v, Object data) {
                final Tip tip = (Tip) data;
                if (getOnlySearchBox()) {
                    enableTextTip = false;
                    mSearchEditText.setText(tip.getName());
                    enableTextTip = true;
                }
                if (!CommonUtils.isStringEmpty(tip.getName())) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            RecentSearchTips.getInstance().insertTip(tip);
                        }
                    }).start();
                }
                ((OnSearchItemClickListener) mMapsFragment).onSearchItemClick(tip);
            }
        });
    }

    private boolean isSearchBoxVisible() {
        return mSearchBox.getParent() != null;
    }

    private boolean isResultContainerVisible() {
        return mSearchResultContainer.getParent() != null;
    }

    private void setSearchBoxVisible(boolean visible) {
        if (visible) {
            if (!isSearchBoxVisible()) {
                AnimUtils.viewSlideInTop(mSearchBox);
                rootView.addView(mSearchBox);
            }
        } else {
            rootView.removeView(mSearchBox);
        }
    }

    private void setResultContainerVisible(boolean visible) {
        if (visible) {
            if (!isResultContainerVisible()) {
                rootView.addView(mSearchResultContainer);
            }
        } else {
            rootView.removeView(mSearchResultContainer);
        }
    }

    @Override
    public void show() {
        if (getOnlySearchBox()) {
            setSearchBoxVisible(true);
            mSearchEditText.setCursorVisible(false);
            mMapsFragment.setMapViewVisibility(View.VISIBLE);
        } else {
            mMapsFragment.setMapViewVisibility(View.INVISIBLE);
            setSearchBoxVisible(true);
            setResultContainerVisible(true);
            mSearchEditText.setCursorVisible(true);

            ((IDrawerView) mMapsFragment).enableDrawer(false);
        }
        if (getBackBtnAsDrawer()) {
            isBackBtnAsDrawer = true;
            mBackBtn.setImageDrawable(mContext.getDrawable(R.drawable.ic_menu));
        } else {
            isBackBtnAsDrawer = false;
            mBackBtn.setImageDrawable(mContext.getDrawable(R.drawable.ic_arrow_back));
        }
        if (getChooseOnMap()) {
            mChooseOnMapBtn.setVisibility(View.VISIBLE);
        } else {
            mChooseOnMapBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void exit() {
        CommonUtils.hideKeyboard(mSearchEditText);
        setResultContainerVisible(false);
        setSearchBoxVisible(false);

        ((IDrawerView) mMapsFragment).enableDrawer(true);
    }

    @Override
    public void onDestroyView() {

    }

    @Override
    public boolean onBackPressed() {
        if (getOnlySearchBox()) {
            if (isResultContainerVisible()) {
                showOnlySearchBox();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onGetInputTips(List<Tip> tipList) {
        mResultAdapter.setList(tipList);
        mResultAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.opendrawer_or_back) {
            if (isBackBtnAsDrawer) {
                ((IDrawerView) mMapsFragment).openDrawer();
                return;
            }
            if (getOnlySearchBox()) {
                showOnlySearchBox();
            } else {
                mMapsFragment.getViewContainerManager().popBackStack();
            }
        } else if (viewId == R.id.edit_text_clear) {
            // 只有mSearchResultContainer不可见时允许回调
            if (!isResultContainerVisible()) {
                mMapsFragment.onClearSearchText();
            }
            mSearchEditText.setText("");
        } else if (viewId == R.id.search_edittext) {
            mSearchEditText.setCursorVisible(true);
            mMapsFragment.setMapViewVisibility(View.INVISIBLE);
            setResultContainerVisible(true);
            isBackBtnAsDrawer = false;
            mBackBtn.setImageDrawable(mContext.getDrawable(R.drawable.ic_arrow_back));
        } else if (viewId == R.id.search_choose_on_map) {
            mMapsFragment.getViewContainerManager().putViewContainer(
                    new ChooseOnMap(rootView, mMapsFragment), null, false, ChooseOnMap.ID);
        }
    }

    public void clearSearchText() {
        mSearchEditText.setText("");
    }

    private boolean getOnlySearchBox() {
        Object arg = args.get(SearchBox.ONLY_SEARCH_BOX);
        return arg != null && (boolean) arg;
    }

    private boolean getBackBtnAsDrawer() {
        Object arg = args.get(SearchBox.BACK_BTN_AS_DRAWER);
        return arg != null && (boolean) arg;
    }

    private boolean getChooseOnMap() {
        Object arg = args.get(SearchBox.SHOW_CHOOSE_ON_MAP);
        return arg != null && (boolean) arg;
    }

    private void showOnlySearchBox() {
        setResultContainerVisible(false);
        CommonUtils.hideKeyboard(mSearchEditText);
        mMapsFragment.setMapViewVisibility(View.VISIBLE);
        if (getBackBtnAsDrawer()) {
            isBackBtnAsDrawer = true;
            mBackBtn.setImageDrawable(mContext.getDrawable(R.drawable.ic_menu));
        } else {
            isBackBtnAsDrawer = false;
            mBackBtn.setImageDrawable(mContext.getDrawable(R.drawable.ic_arrow_back));
        }
        mSearchEditText.setCursorVisible(false);
    }
}
