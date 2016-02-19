package dg.shenm233.mmaps.ui.maps.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.amap.api.services.help.Tip;

import java.util.List;
import java.util.Map;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.adapter.SearchTipsAdapter;
import dg.shenm233.mmaps.presenter.IMapsFragment;
import dg.shenm233.mmaps.presenter.SearchMapsPresenter;
import dg.shenm233.mmaps.ui.IDrawerView;
import dg.shenm233.mmaps.ui.maps.ViewContainerManager;
import dg.shenm233.mmaps.util.CommonUtils;

public class SearchBox extends ViewContainerManager.ViewContainer
        implements View.OnClickListener, AdapterView.OnItemClickListener {
    public interface OnSearchItemClickListener {
        void onSearchItemClick(Tip tip);
    }

    public static final int ID = 0;
    public static final String ONLY_SEARCH_BOX = "only_search_box"; //boolean
    public static final String BACK_BTN_AS_DRAWER = "back_btn_as_drawer"; //boolean
    public static final String SHOW_CHOOSE_ON_MAP = "show_choose_on_map"; //boolean

    private Context mContext;
    private IMapsFragment mMapsFragment;
    private SearchMapsPresenter mSearchMapsPresenter;

    private ViewGroup rootView;
    private ViewGroup mSearchBox;
    private EditText mSearchEditText;
    private ImageButton mBackBtn;

    private ViewGroup mSearchResultContainer;
    private ViewGroup mChooseOnMapBtn;

    private SearchTipsAdapter mResultAdapter;

    private boolean enableTextTip = true;

    public SearchBox(ViewGroup rootView, IMapsFragment mapsFragment) {
        this.rootView = rootView;
        Context context = rootView.getContext();
        mContext = context;

        mMapsFragment = mapsFragment;
        mSearchMapsPresenter = new SearchMapsPresenter(context, mapsFragment.getMapsModule());

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
                    mSearchMapsPresenter.requestInputTips(str, "");
                }
            }
        });
        searchEditText.setOnClickListener(this);

        ViewGroup searchResultContainer = (ViewGroup) inflater.inflate(R.layout.search_result, rootView, false);
        mSearchResultContainer = searchResultContainer;

        ViewGroup chooseOnMapBtn = (ViewGroup) searchResultContainer.findViewById(R.id.search_choose_on_map);
        mChooseOnMapBtn = chooseOnMapBtn;
        chooseOnMapBtn.setOnClickListener(this);

        ListView resultListView = (ListView) searchResultContainer.findViewById(R.id.result_listview);
        SearchTipsAdapter searchTipsAdapter = new SearchTipsAdapter(mContext);
        mResultAdapter = searchTipsAdapter;
        resultListView.setAdapter(searchTipsAdapter);
        resultListView.setOnItemClickListener(this);

        mSearchMapsPresenter.setOnTipsListener(new SearchMapsPresenter.OnTipsListener() {
            @Override
            public void onGetInputTips(List<Tip> tipList) {
                mResultAdapter.newTipsList(tipList);
            }
        });

        searchResultContainer.setVisibility(View.GONE);
        rootView.addView(searchResultContainer);
        rootView.addView(searchBox);
    }

    @Override
    public void show() {
        if (getOnlySearchBox()) {
            mSearchBox.setVisibility(View.VISIBLE);
            mSearchResultContainer.setVisibility(View.INVISIBLE);
            mMapsFragment.setMapViewVisibility(View.VISIBLE);
        } else {
            mMapsFragment.setMapViewVisibility(View.INVISIBLE);
            mSearchBox.setVisibility(View.VISIBLE);
            mSearchResultContainer.setVisibility(View.VISIBLE);

            ((IDrawerView) mMapsFragment).enableDrawer(false);
        }
        if (getBackBtnAsDrawer()) {
            mBackBtn.setImageDrawable(mContext.getDrawable(R.drawable.ic_menu));
        } else {
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
        mSearchEditText.setCursorVisible(false);
        CommonUtils.hideKeyboard(mSearchEditText);
        mSearchResultContainer.setVisibility(View.INVISIBLE);
        mSearchBox.setVisibility(View.INVISIBLE);

        ((IDrawerView) mMapsFragment).enableDrawer(true);
    }

    @Override
    public void onDestroyView() {

    }

    @Override
    public boolean onBackPressed() {
        if (getOnlySearchBox()) {
            if (mSearchResultContainer.getVisibility() == View.VISIBLE) {
                showOnlySearchBox();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        Map<String, Object> args = this.args;
        if (viewId == R.id.opendrawer_or_back) {
            if (getBackBtnAsDrawer()) {
                ((IDrawerView) mMapsFragment).openDrawer();
                return;
            }
            if (getOnlySearchBox()) {
                showOnlySearchBox();
            } else {
                mMapsFragment.getViewContainerManager().popBackStack();
            }
        } else if (viewId == R.id.edit_text_clear) {
            mMapsFragment.onClearSearchText();
            mSearchEditText.setText("");
        } else if (viewId == R.id.search_edittext) {
            mSearchEditText.setCursorVisible(true);
            if (getOnlySearchBox()) {
                mMapsFragment.setMapViewVisibility(View.INVISIBLE);
                mSearchResultContainer.setVisibility(View.VISIBLE);
                mBackBtn.setImageDrawable(mContext.getDrawable(R.drawable.ic_arrow_back));
                args.put(BACK_BTN_AS_DRAWER, false);
            }
        } else if (viewId == R.id.search_choose_on_map) {
            mMapsFragment.getViewContainerManager().putViewContainer(
                    new ChooseOnMap(rootView, mMapsFragment), null, false, ChooseOnMap.ID);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Tip tip = mResultAdapter.getItem(position);
        if (getOnlySearchBox()) {
            enableTextTip = false;
            mSearchEditText.setText(tip.getName());
            enableTextTip = true;
        }
        ((OnSearchItemClickListener) mMapsFragment).onSearchItemClick(tip);
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
        CommonUtils.hideKeyboard(mSearchEditText);
        mSearchResultContainer.setVisibility(View.INVISIBLE);
        mMapsFragment.setMapViewVisibility(View.VISIBLE);
        mBackBtn.setImageDrawable(mContext.getDrawable(R.drawable.ic_menu));
        args.put(BACK_BTN_AS_DRAWER, true);
        mSearchEditText.setCursorVisible(false);
    }
}
