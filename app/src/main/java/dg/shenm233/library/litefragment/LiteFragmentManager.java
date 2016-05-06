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

package dg.shenm233.library.litefragment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Stack;

/**
 * provides a simple LiteFragment back stack.
 */
public class LiteFragmentManager {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ViewGroup mViewContainer;

    /**
     * a back stack for LiteFragments
     */
    private final Stack<LiteFragment> mBackStack = new Stack<>();

    public LiteFragmentManager(Context context, ViewGroup viewContainer) {
        mContext = context;
        mViewContainer = viewContainer;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void addToBackStack(LiteFragment fragment) {
        LiteFragment prev = peek();
        if (prev != null) {
            stopLiteFragmentInternal(prev, false);
        }
        mBackStack.push(fragment);
        startLiteFragmentInternal(fragment, true);
    }

    public LiteFragment peek() {
        if (mBackStack.empty()) {
            return null;
        }
        return mBackStack.peek();
    }

    public void pop() {
        if (mBackStack.empty()) {
            return;
        }
        LiteFragment f = mBackStack.pop();
        stopLiteFragmentInternal(f, true);
        f = peek();
        if (f != null) {
            startLiteFragmentInternal(f, true);
        }
    }

    public void pop(String tag, boolean inclusive) {
        if (tag == null || tag.isEmpty()) {
            return;
        }
        while (!mBackStack.isEmpty()) {
            LiteFragment f = mBackStack.peek();
            if (tag.equals(f.getTag())) {
                if (inclusive) {
                    mBackStack.pop();
                    stopLiteFragmentInternal(f, true);
                }
                break;
            }
            mBackStack.pop();
            stopLiteFragmentInternal(f, true);
        }
    }

    public void popAll() {
        while (!mBackStack.isEmpty()) {
            LiteFragment f = mBackStack.pop();
            stopLiteFragmentInternal(f, true);
        }
    }

    void popForResult() {
        if (mBackStack.empty()) {
            return;
        }
        LiteFragment prev = mBackStack.pop();
        LiteFragment f = peek();
        if (f != null) {
            if (prev.hasResult()) {
                Intent resultData = new Intent(prev.getResult());
                f.onFragmentResult(prev.getRequestCode(), prev.getResultCode(), resultData);
            }
        }
        stopLiteFragmentInternal(prev, true);
        if (f != null) {
            startLiteFragmentInternal(f, true);
        }
    }

    private void startLiteFragmentInternal(LiteFragment f, boolean visibleImmediate) {
        f.setContext(mContext);
        f.setLiteFragmentManager(this);
        if (!f.isCreated()) {
            f.onCreate();
        }
        if (!f.isCreatedView()) {
            f.onCreateView(mLayoutInflater, mViewContainer);
        }
        if (visibleImmediate && !f.isStarted()) {
            f.onStart();
        }
    }

    private void stopLiteFragmentInternal(LiteFragment f, boolean destroy) {
        f.onStop();
        if (destroy) {
            f.onDestroyView(mViewContainer);
            f.onDestroy();
        }
    }

    public LiteFragment findLiteFragmentByTag(String tag) {
        for (LiteFragment fragment : mBackStack) {
            String fragmentTag = fragment.getTag();
            if (fragmentTag != null && fragmentTag.equals(tag)) {
                return fragment;
            }
        }
        return null;
    }

    public Context getContext() {
        return mContext;
    }

    public ViewGroup getViewContainer() {
        return mViewContainer;
    }
}
