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
