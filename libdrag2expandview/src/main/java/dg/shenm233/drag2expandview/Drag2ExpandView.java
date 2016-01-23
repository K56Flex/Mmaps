package dg.shenm233.drag2expandview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
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
    private static int[] OTHER_ATTRS = new int[]{
            android.R.attr.gravity
    };

    private final ViewDragHelper mViewDragHelper;
    private ViewDragCallback mViewDragCallback;

    private int mHeaderHeight = 0;

    private View mHeaderView;
    private View mMainView;

    private int mDragRange = 0; // always > 0
    private float mDragOffset;

    private boolean mIsUpSliding = false;

    private float mFirstDownX = 0;
    private float mFirstDownY = 0;

    private int mTop;

    private boolean isFirstLayout = true;

    private int mSquareOfSlop;

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
                mHeaderHeight = a.getDimensionPixelSize(0, -1);
                a.recycle();
            }
        }

        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mViewDragCallback = new ViewDragCallback());

        int slop = mViewDragHelper.getTouchSlop();
        mSquareOfSlop = slop * slop;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        final int childCount = getChildCount();
        if (childCount != 2) {
            throw new IllegalStateException("must have two views");
        }

        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("width must be exactly value or MATCH_PARENT");
        }
        width = widthSize;

        final View headerView = getChildAt(0);
        final View mainView = getChildAt(1);

        height = heightSize;

        int paddingWidth = getPaddingLeft() + getPaddingRight();
        int paddingHeight = getPaddingTop() + getPaddingBottom();

        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = child.getLayoutParams();

            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, paddingWidth, lp.width);

            int childHeightMeasureSpec;
            if (child == headerView) {
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeaderHeight, MeasureSpec.EXACTLY);
            } else {
                childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec - mHeaderHeight,
                        paddingHeight, lp.height);
            }

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

        mHeaderView = headerView;
        mMainView = mainView;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        final View headerView = mHeaderView;
        final View mainView = mMainView;

        if (isFirstLayout) {
            mDragRange = getHeight() - mHeaderView.getMeasuredHeight();
            if (mIsUpSliding) {
                mTop = paddingTop + mDragRange;
            } else {
                mTop = paddingTop;
            }

            isFirstLayout = false;
        }

        int top = mTop;

        int headerViewRight = paddingLeft + headerView.getMeasuredWidth();
        int headerViewBottom = top + mHeaderHeight;

        headerView.layout(paddingLeft, top, headerViewRight, headerViewBottom);

        // no need to layout main view for first time
        if (!isFirstLayout) {
            mainView.layout(paddingLeft, headerViewBottom, paddingLeft + mainView.getMeasuredWidth(),
                    headerViewBottom + mainView.getMeasuredHeight());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        mViewDragHelper.processTouchEvent(ev);

        final float x = ev.getX();
        final float y = ev.getY();

        boolean isHeaderViewUnder = mViewDragHelper.isViewUnder(mHeaderView, (int) x, (int) y);

        final int action = ev.getAction();
        int maskAction = action & MotionEvent.ACTION_MASK;
        if (maskAction == MotionEvent.ACTION_DOWN) {
            mFirstDownX = x;
            mFirstDownY = y;
        } else if (maskAction == MotionEvent.ACTION_UP) {
            float dx = mFirstDownX - x;
            float dy = mFirstDownY - y;
            if (dx * dx + dy * dy < mSquareOfSlop && isHeaderViewUnder) {
                if (mDragOffset > 0) {
                    smoothSlideHeaderTo(1.0f);
                } else if (mDragOffset < 0) {
                    smoothSlideHeaderTo(0.0f);
                }
            }
        }

        return isHeaderViewUnder;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setGravity(int gravity) {
        if (gravity == Gravity.BOTTOM) {
            mDragOffset = -1;
            mIsUpSliding = true;
        } else if (gravity == Gravity.TOP) {
            mDragOffset = 1;
            mIsUpSliding = false;
        } else {
            throw new IllegalStateException("gravity must be BOTTOM or TOP");
        }
    }

    public int getHeaderHeight() {
        return mHeaderHeight;
    }

    public boolean smoothSlideHeaderTo(float offset) {
        final int paddingTop = getPaddingTop();
        int finalY = (int) (paddingTop + mDragRange * offset);
        final View headerView = mHeaderView;

        if (mViewDragHelper.smoothSlideViewTo(headerView, headerView.getLeft(), finalY)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mHeaderView;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mTop = top;
            mDragOffset = (float) -dy / mDragRange;
            requestLayout();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            // up:yvel < 0,down:yvel > 0
            float direction = -yvel;
            if (direction > 0) { // expand
                smoothSlideHeaderTo(0.0f);
            } else if (direction < 0) { // collapse
                smoothSlideHeaderTo(1.0f);
            }
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mDragRange;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - mHeaderHeight - mHeaderView.getPaddingBottom();

            return Math.min(Math.max(top, topBound), bottomBound);
        }
    }
}
