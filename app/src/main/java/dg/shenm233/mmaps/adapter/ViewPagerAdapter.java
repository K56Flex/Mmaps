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

package dg.shenm233.mmaps.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import dg.shenm233.mmaps.viewmodel.BasePager;

public class ViewPagerAdapter extends PagerAdapter {
    private List<BasePager> mPagers = new ArrayList<>();

    public ViewPagerAdapter() {
    }

    public void add(BasePager pager) {
        if (pager == null) {
            throw new IllegalArgumentException("~boom~ pager should not be null");
        }
        mPagers.add(pager);
        //TODO: call notifyDataSetChanged()
    }

    public void remove(int position) {
        mPagers.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPagers.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view; // TODO: add null view will boom~
        BasePager pager = mPagers.get(position);
        view = pager.createView(container);
        container.addView(view);
        return pager;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((BasePager) object).getBindView();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        BasePager pager = mPagers.get(position);
        return pager.getTitle();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        BasePager pager = (BasePager) object;
        container.removeView(pager.getBindView());
        pager.onDestroy();
        finishUpdate(container);
    }
}
