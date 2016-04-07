package dg.shenm233.drag2expandview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Thanks for Flavien Laurent 's tutorial about ViewDragHelper
 * flavienlaurent.com/blog/2013/08/28/each-navigation-drawer-hides-a-viewdraghelper/
 * and also reference SlidingUpPanel (https://github.com/umano/AndroidSlidingUpPanel)
 */

public class Drag2ExpandView extends ViewGroup {
    //    public final static int STATE_IDLE = 0;
    public final static int STATE_DRAGGING = 1;
    public final static int STATE_EXPAND = 2;
    public final static int STATE_COLLAPSE = 3;
    public final static int STATE_ANCHORED = 4;

    public final static int DEFAULT_STATE = STATE_COLLAPSE;


    private static int[] OTHER_ATTRS = new int[]{
            android.R.attr.gravity
    };

    private final ViewDragHelper mViewDragHelper;
    private ViewDragCallback mViewDragCallback;

    private int mHeaderHeight = 0;

    /**
     * the main view,is sliding
     */
    private View mMainView;

    private int mScrollableViewResId;
    /**
     *
     */
    private View mScrollableView;

    private int mDragViewResId;
    /**
     * the view that to be dragged
     */
    private View mDragView;

    /**
     * anchor offset is that sliding to special position
     */
    private float mAnchorOffset = 1.0f;

    private int mDragRange = 0; // always > 0
    private float mDragOffset;

    private boolean mIsUpSliding = false;

    private float mPreviousDownX = 0;
    private float mPreviousDownY = 0;

    private int mViewState = DEFAULT_STATE;

    private boolean isFirstLayout = true;

    private ScrollableViewHelper mScrollableViewHelper;
    private boolean mLetScrollableViewHandle;

    private OnDragListener mOnDragListener;

    public Drag2ExpandView(Context context) {
        this(context, null);
    }

    public Drag2ExpandView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Drag2ExpandView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Drag2ExpandView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (attrs != null) {
            TypedArray o = context.obtainStyledAttributes(attrs, OTHER_ATTRS);
            if (o != null) {
                int gravity = o.getInt(0, Gravity.NO_GRAVITY);
                setGravity(gravity);
                o.recycle();
            }

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Drag2ExpandView);
            if (a != null) {
                mHeaderHeight = a.getDimensionPixelSize(R.styleable.Drag2ExpandView_sHeaderHeight, -1);
                mScrollableViewResId = a.getResourceId(R.styleable.Drag2ExpandView_sScrollableView, -1);
                mDragViewResId = a.getResourceId(R.styleable.Drag2ExpandView_sViewToDrag, -1);
                mAnchorOffset = a.getFloat(R.styleable.Drag2ExpandView_sAnchorOffset, 1.0f);
                a.recycle();
            }
        }

        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mViewDragCallback = new ViewDragCallback());
        mScrollableViewHelper = new ScrollableViewHelper();

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mScrollableViewResId != -1) {
            setScrollableView(findViewById(mScrollableViewResId));
        }
        if (mDragViewResId != -1) {
            setViewToDrag(findViewById(mDragViewResId));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int childCount = getChildCount();
        if (childCount != 1) {
            throw new IllegalStateException("only support one child view");
        }

        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("width must be exactly value or MATCH_PARENT");
        }

        final View mainView = getChildAt(0);

        int paddingWidth = getPaddingLeft() + getPaddingRight();
        int paddingHeight = getPaddingTop() + getPaddingBottom();


        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize - paddingWidth, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize - paddingHeight, MeasureSpec.EXACTLY);

        mainView.measure(childWidthMeasureSpec, childHeightMeasureSpec);

        mMainView = mainView;

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();

        int top;

        if (isFirstLayout) {
            mDragRange = getHeight() - mHeaderHeight;
            mDragOffset = 0.0f;
            isFirstLayout = false;
        }

        top = computePanelTopPosition(mDragOffset);

        mMainView.layout(paddingLeft, top,
                paddingLeft + mMainView.getMeasuredWidth(), top + mMainView.getMeasuredHeight());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        int maskAction = action & MotionEvent.ACTION_MASK;
        if (maskAction == MotionEvent.ACTION_DOWN) {
            mLetScrollableViewHandle = false;
            mPreviousDownX = x;
            mPreviousDownY = y;
        } else if (maskAction == MotionEvent.ACTION_MOVE) {
            if (!isViewUnder(mScrollableView, (int) x, (int) y)) {
                return super.dispatchTouchEvent(ev);
            }

            if (mScrollableViewHelper.getScrollableViewScrollPosition(mScrollableView, mIsUpSliding) > 0) {
                mLetScrollableViewHandle = true;
                return super.dispatchTouchEvent(ev);
            }
        }

        // default
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mLetScrollableViewHandle) {
            mViewDragHelper.cancel();
            return false;
        }

        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mLetScrollableViewHandle) {
            mViewDragHelper.processTouchEvent(ev);
            if (mViewDragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING) {
                return true;
            }
        }

        final float x = ev.getX();
        final float y = ev.getY();

        boolean isDragViewUnder = isViewUnder(mDragView, (int) x, (int) y);

        final int action = ev.getAction();
        int maskAction = action & MotionEvent.ACTION_MASK;
        if (maskAction == MotionEvent.ACTION_DOWN) {
            mPreviousDownX = x;
            mPreviousDownY = y;
        } else if (maskAction == MotionEvent.ACTION_UP) {
            float dx = mPreviousDownX - x;
            float dy = mPreviousDownY - y;
            int slop = mViewDragHelper.getTouchSlop();
            if (dx * dx + dy * dy < slop * slop && isDragViewUnder) {
                if (mViewState == STATE_COLLAPSE) {
                    smoothSlideViewTo(mAnchorOffset); //expand
                } else {
                    smoothSlideViewTo(0.0f); // collapse
                }
            }
        }

        return isDragViewUnder;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // if size changed,just re-layout
        if (h != oldh) {
            isFirstLayout = true;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isFirstLayout = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isFirstLayout = true;
    }

    public void setGravity(int gravity) {
        if (gravity == Gravity.BOTTOM) {
            mIsUpSliding = true;
        } else if (gravity == Gravity.TOP) {
            mIsUpSliding = false;
        } else {
            throw new IllegalStateException("gravity must be BOTTOM or TOP");
        }
        mViewState = DEFAULT_STATE;
        mDragOffset = 0; // collapse
        if (!isFirstLayout) {
            requestLayout();
        }
    }

    public int getHeaderHeight() {
        return mHeaderHeight;
    }

    public void expandView() {
        smoothSlideViewTo(1.0f);
    }

    public void collapseView() {
        smoothSlideViewTo(0.0f);
    }

    public int getViewState() {
        return mViewState;
    }

    public void setScrollableView(View scrollableView) {
        mScrollableView = scrollableView;
    }

    public void setViewToDrag(View dragView) {
        mDragView = dragView;
    }

    public void setOnDragListener(OnDragListener l) {
        mOnDragListener = l;
    }

    private boolean smoothSlideViewTo(float offset) {
        int finalY = computePanelTopPosition(offset);
        final View slidingView = mMainView;

        if (mViewDragHelper.smoothSlideViewTo(slidingView, slidingView.getLeft(), finalY)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    private void onDragView(int top) {
        mDragOffset = computeSlideOffset(top);
        requestLayout();
        dispatchOnDrag();
    }

    private void dispatchOnDrag() {
        if (mOnDragListener != null) {
            mOnDragListener.onDrag(this, mDragOffset);
        }
    }

    private void dispatchOnDragStateChanged(int oldState, int newState) {
        if (mOnDragListener != null) {
            mOnDragListener.onStateChanged(this, oldState, newState);
        }
    }

    private boolean isViewUnder(View view, int x, int y) {
        if (view == null) return false;
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
    }

    /*
     * Computes the top position of the panel based on the slide offset.
     */
    private int computePanelTopPosition(float slideOffset) {
        int slidingViewHeight = mMainView != null ? mMainView.getMeasuredHeight() : 0;
        int slidePixelOffset = (int) (slideOffset * mDragRange);
        // Compute the top of the panel if its collapsed
        return mIsUpSliding
                ? getMeasuredHeight() - getPaddingBottom() - mHeaderHeight - slidePixelOffset
                : getPaddingTop() - slidingViewHeight + mHeaderHeight + slidePixelOffset;
    }

    /*
     * Computes the slide offset based on the top position of the panel
     */
    private float computeSlideOffset(int topPosition) {
        // Compute the panel top position if the panel is collapsed (offset 0)
        final int topBoundCollapsed = computePanelTopPosition(0);

        // Determine the new slide offset based on the collapsed top position and the new required
        // top position
        return mIsUpSliding
                ? (float) (topBoundCollapsed - topPosition) / mDragRange
                : (float) (topPosition - topBoundCollapsed) / mDragRange;
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mMainView;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            int oldState = mViewState;

            if (state == ViewDragHelper.STATE_DRAGGING) {
                mViewState = STATE_DRAGGING;
                dispatchOnDragStateChanged(oldState, STATE_DRAGGING);
            } else if (state == ViewDragHelper.STATE_IDLE) {
                if (mDragOffset == 0.0f) {
                    mViewState = STATE_COLLAPSE;
                    dispatchOnDragStateChanged(oldState, STATE_COLLAPSE);
                } else if (mDragOffset == mAnchorOffset) {
                    mViewState = STATE_ANCHORED;
                    dispatchOnDragStateChanged(oldState, STATE_ANCHORED);
                } else {
                    mViewState = STATE_EXPAND;
                    dispatchOnDragStateChanged(oldState, STATE_EXPAND);
                }
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            onDragView(top);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            // up:yvel < 0,down:yvel > 0
            float direction = mIsUpSliding ? -yvel : yvel;
            float offset = mDragOffset;
            if (direction > 0 && mDragOffset < mAnchorOffset) {
                offset = mAnchorOffset;
            } else if (direction > 0 && mDragOffset >= mAnchorOffset) {
                offset = 1.0f; // expand
            } else if (direction < 0 && mDragOffset > mAnchorOffset) {
                offset = mAnchorOffset;
            } else if (direction < 0 && mDragOffset <= mAnchorOffset) {
                offset = 0.0f; // collapse
            }
            smoothSlideViewTo(offset);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mDragRange;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int collapsedTop = computePanelTopPosition(0.0f);
            final int expandedTop = computePanelTopPosition(1.0f);
            // Restrict top
            if (mIsUpSliding) {
                return Math.min(Math.max(top, expandedTop), collapsedTop);
            } else {
                return Math.min(Math.max(top, collapsedTop), expandedTop);
            }
        }
    }

    public interface OnDragListener {
        void onDrag(Drag2ExpandView v, float dragOffset);

        void onStateChanged(Drag2ExpandView v, int oldState, int newState);
    }
}
