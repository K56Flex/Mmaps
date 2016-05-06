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

import android.content.Intent;
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

import dg.shenm233.library.litefragment.LiteFragment;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.SearchTipsAdapter;
import dg.shenm233.mmaps.database.RecentSearchTips;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.presenter.ISearchBox;
import dg.shenm233.mmaps.presenter.SearchBoxPresenter;
import dg.shenm233.mmaps.ui.IDrawerView;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;

public class SearchBox extends LiteFragment
        implements ISearchBox, View.OnClickListener {
    final static int SEARCHBOX_REQUEST_CODE = 0x2b;

    public interface OnSearchItemClickListener {
        void onSearchItemClick(Tip tip);
    }

    public static final String ONLY_SEARCH_BOX = "only_search_box"; //boolean
    public static final String BACK_BTN_AS_DRAWER = "back_btn_as_drawer"; //boolean
    public static final String SHOW_CHOOSE_ON_MAP = "show_choose_on_map"; //boolean

    private IMapsFragment mMapsFragment;
    private SearchBoxPresenter mSearchBoxPresenter;

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

    public SearchBox(IMapsFragment mapsFragment) {
        mMapsFragment = mapsFragment;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        mSearchBoxPresenter = new SearchBoxPresenter(getContext(), this, mMapsFragment.getMapsModule());
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        ViewGroup searchBox = (ViewGroup) inflater.inflate(R.layout.search_box, container, false);
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

        ViewGroup searchResultContainer = (ViewGroup) inflater.inflate(R.layout.search_result, container, false);
        mSearchResultContainer = searchResultContainer;

        ViewGroup chooseOnMapBtn = (ViewGroup) searchResultContainer.findViewById(R.id.search_choose_on_map);
        mChooseOnMapBtn = chooseOnMapBtn;
        chooseOnMapBtn.setOnClickListener(this);

        RecyclerView resultListView = (RecyclerView) searchResultContainer.findViewById(R.id.result_listview);
        SearchTipsAdapter searchTipsAdapter = new SearchTipsAdapter(getContext());
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
                if (getRequestCode() != -1) {
                    Intent result = new Intent();
                    result.putExtra("result", tip);
                    setResult(ACTION_SUCCESS, result);
                    finish();
                } else {
                    ((OnSearchItemClickListener) mMapsFragment).onSearchItemClick(tip);
                }
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
                getViewContainer().addView(mSearchBox);
            }
        } else {
            getViewContainer().removeView(mSearchBox);
        }
    }

    private void setResultContainerVisible(boolean visible) {
        if (visible) {
            if (!isResultContainerVisible()) {
                getViewContainer().addView(mSearchResultContainer);
            }
        } else {
            getViewContainer().removeView(mSearchResultContainer);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasResult()) {
            finish();
            return;
        }

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
            mBackBtn.setImageDrawable(getContext().getDrawable(R.drawable.ic_menu));
        } else {
            isBackBtnAsDrawer = false;
            mBackBtn.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_back));
        }
        if (getChooseOnMap()) {
            mChooseOnMapBtn.setVisibility(View.VISIBLE);
        } else {
            mChooseOnMapBtn.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        CommonUtils.hideKeyboard(mSearchEditText);
        setResultContainerVisible(false);
        setSearchBoxVisible(false);

        ((IDrawerView) mMapsFragment).enableDrawer(true);
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
    public void onFragmentResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != ACTION_SUCCESS) return;
        if (requestCode == SEARCHBOX_REQUEST_CODE) {
            setResult(ACTION_SUCCESS, data);
        }
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
                finish();
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
            mBackBtn.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_back));
        } else if (viewId == R.id.search_choose_on_map) {
            ChooseOnMap chooseOnMap = new ChooseOnMap(mMapsFragment);
            startLiteFragmentForResult(SEARCHBOX_REQUEST_CODE, chooseOnMap, null);
        }
    }

    public void clearSearchText() {
        mSearchEditText.setText("");
    }

    private boolean getOnlySearchBox() {
        boolean arg = getArguments().getBoolean(SearchBox.ONLY_SEARCH_BOX);
        return arg;
    }

    private boolean getBackBtnAsDrawer() {
        boolean arg = getArguments().getBoolean(SearchBox.BACK_BTN_AS_DRAWER);
        return arg;
    }

    private boolean getChooseOnMap() {
        boolean arg = getArguments().getBoolean(SearchBox.SHOW_CHOOSE_ON_MAP);
        return arg;
    }

    private void showOnlySearchBox() {
        setResultContainerVisible(false);
        CommonUtils.hideKeyboard(mSearchEditText);
        mMapsFragment.setMapViewVisibility(View.VISIBLE);
        if (getBackBtnAsDrawer()) {
            isBackBtnAsDrawer = true;
            mBackBtn.setImageDrawable(getContext().getDrawable(R.drawable.ic_menu));
        } else {
            isBackBtnAsDrawer = false;
            mBackBtn.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_back));
        }
        mSearchEditText.setCursorVisible(false);
    }
}
