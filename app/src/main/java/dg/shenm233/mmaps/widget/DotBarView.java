package dg.shenm233.mmaps.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import dg.shenm233.mmaps.R;

// TODO: support padding
public class DotBarView extends ImageView {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private final static int[] OTHER_ATTRS = {
            android.R.attr.orientation
    };

    private int mOrientation;
    private int mSingleItemWidth;
    private int mSingleItemHeight;
    private Drawable mDotDrawable;

    public DotBarView(Context context) {
        this(context, null);
    }

    public DotBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DotBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (attrs != null) {
            TypedArray other = context.obtainStyledAttributes(attrs, OTHER_ATTRS);

            setOrientation(other.getInt(0, VERTICAL));

            other.recycle();

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DotBarView);

            setSingleItemWidth(a.getDimensionPixelSize(R.styleable.DotBarView_singleItemWidth, 24));
            setSingleItemHeight(a.getDimensionPixelSize(R.styleable.DotBarView_singleItemHeight, 24));

            setDrawableAsDot(a.getDrawable(R.styleable.DotBarView_drawableAsDot));

            a.recycle();
        }
    }

    public void setOrientation(int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            requestLayout();
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setSingleItemWidth(int width) {
        mSingleItemWidth = width;
    }

    public void setSingleItemHeight(int height) {
        mSingleItemHeight = height;
    }

    public int getSingleItemWidth() {
        return mSingleItemWidth;
    }

    public int getSingleItemHeight() {
        return mSingleItemHeight;
    }

    public void setDrawableAsDot(Drawable d) {
        mDotDrawable = d;
        updateDrawableAsDot(d);

        invalidate();
    }

    public Drawable getDrawableAsDot() {
        return mDotDrawable;
    }

    private void updateDrawableAsDot(Drawable d) {
        if (d == null) {
            return;
        }

        int dWidth = d.getIntrinsicWidth();
        int dHeight = d.getIntrinsicHeight();

        // TODO: is necessary to resize ?

        d.setBounds(0, 0, dWidth, dHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w, h;

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            w = width;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            w = getSingleItemWidth();
            if (w > width) {
                w = width;
            }
        } else { //UNSPECIFIED
            w = getSingleItemWidth();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            h = height;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            h = getSingleItemHeight();
            if (h > height) {
                h = height;
            }
        } else { //UNSPECIFIED
            h = getSingleItemHeight();
        }

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Drawable background = getBackground();
        if (background != null) {
            background.draw(canvas);
        }

        int totalWidth = getMeasuredWidth();
        int totalHeight = getMeasuredHeight();

        final Drawable drawable = getDrawable();
        if (drawable != null) {
            drawable.draw(canvas);
        }

        final Drawable dotDrawable = getDrawableAsDot();
        if (dotDrawable != null) {
            int itemWidth = getSingleItemWidth();
            int itemHeight = getSingleItemHeight();

            int count;
            if (mOrientation == HORIZONTAL) {
                count = totalWidth / itemWidth;
                for (int i = 0; i < count; i++) {
                    if (i == 0) {
                        canvas.translate(itemWidth, 0);
                        continue;
                    }

                    dotDrawable.draw(canvas);
                    canvas.translate(itemWidth, 0);
                }
            } else { // VERTICAL
                count = totalHeight / itemHeight;
                for (int i = 0; i < count; i++) {
                    if (i == 0) {
                        canvas.translate(0, itemHeight);
                        continue;
                    }

                    dotDrawable.draw(canvas);
                    canvas.translate(0, itemHeight);
                }
            }
        }
    }
}
