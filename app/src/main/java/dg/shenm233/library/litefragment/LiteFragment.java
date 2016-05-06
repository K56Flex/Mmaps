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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * a ~simple~ Fragment,not a lite of android's Fragment.
 * this "fragment" doesn't have any life cycle.
 * Further more : https://corner.squareup.com/2014/10/advocating-against-android-fragments.html
 */
public abstract class LiteFragment implements OnLiteFragmentResult {
    public static final int ACTION_SUCCESS = 0;
    public static final int ACTION_FAILED = -1;

    private Context mContext;

    /**
     * the tag of LiteFragment,used for manager finding.
     */
    private String mFragmentTag;

    /**
     * a bundle of arguments for supporting LiteFragment construction.
     */
    private Bundle mArguments;

    private LiteFragmentManager mManager;

    private int mRequestCode = -1;
    private Intent mRequestIntent = null;
    private int mResultCode = ACTION_SUCCESS;
    private Intent mResultData = null;
    private boolean hasResult = false;

    /**
     * default construction.
     * it is not recommended that subclasses do no have other construction with Context parameter,
     * because the LiteFragmentManager will handle Context.
     */
    public LiteFragment() {

    }

    private boolean isCreated = false;

    boolean isCreated() {
        return isCreated;
    }

    /**
     * called to do first creation of this LiteFragment,
     * you can call getContext() to get Context at this time.
     * this method will be called before onCreateView() and onStart() .
     * Note: requires to call super method.
     */
    protected void onCreate() {
        isCreated = true;
    }

    private boolean isCreatedView = false;

    boolean isCreatedView() {
        return isCreatedView;
    }

    /**
     * create view for your LiteFragment,it will be called after onCreate() and before onStart() .
     * Note: do not add child views to view container at this time,just add child views in onStart() .
     * Note: requires to call super method.
     *
     * @param inflater  LayoutInflater that used for inflate views.
     * @param container the parent view group that LiteFragment will attach to.
     */
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        isCreatedView = true;
    }

    private boolean isStarted = false;

    boolean isStarted() {
        return isStarted;
    }

    /**
     * starts LiteFragment,you should add child views to view container at this time.
     * Note: requires to call super method.
     */
    protected void onStart() {
        isStarted = true;
    }

    /**
     * stops LiteFragment,you should remove child views from view container at this time.
     * Note: requires to call super method.
     */
    protected void onStop() {
        isStarted = false;
    }

    /**
     * destroys child view,will be called after onStop() if it is popped out of back stack.
     * Note: requires to call super method.
     *
     * @param container the parent view group that LiteFragment will detach from.
     */
    protected void onDestroyView(ViewGroup container) {
        isCreatedView = false;
    }

    /**
     * destroys LiteFragment,will be called after onDestroy() if it is popped out of back stack.
     * Note: requires to call super method.
     */
    protected void onDestroy() {
        isCreated = false;
    }

    /**
     * set result for previous caller of the back stack.
     * Note: if you want to exit current LiteFragment immediately,just call finish() after this method.
     *
     * @param resultCode ACTION_SUCCESS or ACTION_FAILED.
     * @param result     result data.
     */
    protected final void setResult(int resultCode, Intent result) {
        hasResult = true;
        mResultCode = resultCode;
        mResultData = result;
    }

    int getResultCode() {
        return mResultCode;
    }

    /**
     * exit the LiteFragment,also pop it out of the back stack.
     */
    public void finish() {
        mManager.popForResult();
    }

    /**
     * get parent view group that hosts the LiteFragment.
     *
     * @return view container hosts the LiteFragment.
     */
    public ViewGroup getViewContainer() {
        return mManager.getViewContainer();
    }

    /**
     * starts a LiteFragment and adds it to back stack.
     *
     * @param f the LiteFragment you want to start.
     */
    public void startLiteFragment(LiteFragment f) {
        mManager.addToBackStack(f);
    }

    /**
     * starts a LiteFragment for which if you would like a result when it finish.
     * when it finished and set result, onFragmentResult(int,int,Intent) will be called,
     * and pass result to LiteFragment which started LiteFragment for result previously.
     *
     * @param requestCode   a unique code used as request code.
     * @param f             the LiteFragment you want to start.
     * @param requestIntent more parameters supply for requesting.
     */
    public void startLiteFragmentForResult(int requestCode, LiteFragment f, Intent requestIntent) {
        f.setRequestCode(requestCode);
        f.setRequestIntent(requestIntent);
        mManager.addToBackStack(f);
    }

    /**
     * indicates whether current LiteFragment has set result,
     * the field hasResult always be set by setResult(int resultCode, Intent result) .
     *
     * @return
     */
    protected final boolean hasResult() {
        return hasResult;
    }

    /**
     * get result data that set by setResult(int resultCode, Intent result) .
     *
     * @return result data.
     */
    protected final Intent getResult() {
        return mResultData;
    }

    /**
     * called when your started LiteFragment finishes.
     * you will receive this call immediately when started LiteFragment finishes,
     * it will be called before onStart() .
     *
     * @param requestCode the requestCode that you request before.
     * @param resultCode  ACTION_SUCCESS or ACTION_FAILED .
     * @param data        result data.
     */
    public void onFragmentResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * Called when the LiteFragment has detected the user's press of the back key.
     *
     * @return true if consumed back pressed event.
     */
    public boolean onBackPressed() {
        return false;
    }

    /**
     * set arguments for supplying LiteFragment.
     *
     * @param args arguments for supplying LiteFragment
     */
    public void setArguments(Bundle args) {
        mArguments = args != null ? new Bundle(args) : null;
    }

    /**
     * get arguments for supplying LiteFragment.
     *
     * @return arguments for supplying LiteFragment
     */
    public final Bundle getArguments() {
        return mArguments;
    }

    void setLiteFragmentManager(LiteFragmentManager manager) {
        mManager = manager;
    }

    /**
     * get the manager of LiteFragment.
     *
     * @return the manager controlling this LiteFragment.
     */
    public final LiteFragmentManager getLiteFragmentManager() {
        return mManager;
    }

    public Context getContext() {
        return mContext;
    }

    void setContext(Context context) {
        mContext = context;
    }

    public String getTag() {
        return mFragmentTag;
    }

    public void setTag(String tag) {
        mFragmentTag = tag;
    }

    void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }

    /**
     * get request code which was set when it had started by another LiteFragment for requesting result.
     *
     * @return request code.
     */
    protected final int getRequestCode() {
        return mRequestCode;
    }

    void setRequestIntent(Intent requestIntent) {
        mRequestIntent = requestIntent == null ? null : new Intent(requestIntent);
    }

    /**
     * get request intent which was set when it had started by another LiteFragment for requesting result.
     *
     * @return request intent.
     */
    protected Intent getRequestIntent() {
        return mRequestIntent;
    }
}
