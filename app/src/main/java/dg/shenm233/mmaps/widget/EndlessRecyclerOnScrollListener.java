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

package dg.shenm233.mmaps.widget;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    /**
     * current page index
     */
    private int page = 0;

    /**
     * max page count
     */
    private int maxPageCount = Integer.MAX_VALUE;

    private boolean noMore = false;
    private boolean isLoading = false;

    public EndlessRecyclerOnScrollListener() {
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            onScrolled(recyclerView, (LinearLayoutManager) layoutManager);
        }
    }

    protected void onScrolled(RecyclerView recyclerView, LinearLayoutManager layoutManager) {
        if (noMore || isLoading()) {
            return;
        }
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) {
            return;
        }
        final int count = adapter.getItemCount();
        if (count <= 0) {
            return;
        }
        // check whether scrolled to bottom
        if (layoutManager.findLastCompletelyVisibleItemPosition() == count - 1) {
            setLoading(true);
            onLoadMore(page);
        }
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public boolean isLoading() {
        return isLoading;
    }

    /**
     * stop endless scrolling if no more data to load.
     *
     * @param noMore true if there are really no more data.
     */
    public void noMore(boolean noMore) {
        this.noMore = noMore;
    }

    /**
     * if prior page was loaded successfully or not,you should call this method to decide next page
     * index.
     *
     * @param success the prior page was loaded successfully or not.
     */
    public void lastPageLoaded(boolean success) {
        if (page >= getMaxPageCount() - 1) { // already reach max page count,stop load more
            noMore(true);
            return;
        }
        if (success) {
            page++;
        }
    }

    public void setMaxPageCount(int count) {
        maxPageCount = count;
    }

    public int getMaxPageCount() {
        return maxPageCount;
    }

    /**
     * invoked by this class if it needs to load page,you should invoke lastPageLoaded(boolean success)
     * if data loaded or not.
     *
     * @param pageIndex indicates that you should load.
     */
    public abstract void onLoadMore(int pageIndex);
}
