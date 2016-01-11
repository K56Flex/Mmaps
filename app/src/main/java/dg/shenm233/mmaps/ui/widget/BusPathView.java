package dg.shenm233.mmaps.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import dg.shenm233.mmaps.R;

public class BusPathView extends TextView {
    private Drawable[] mDrawables;
    private String[] mDetailText;

    private final TextPaint mTextPaint;
    private Rect mTempRect = new Rect();

    public BusPathView(Context context) {
        this(context, null);
    }

    public BusPathView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public BusPathView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BusPathView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mTextPaint = getPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        int myWidth = 0;
        int myHeight = 0;

        final Drawable[] drawables = mDrawables;

        final int length = drawables != null ? drawables.length : 0;
        Rect textBounds = mTempRect;
        for (int i = 0; i < length; i++) {
            final String text = mDetailText[i];

            myWidth += drawables[i].getIntrinsicWidth();
            myHeight = Math.max(myHeight, drawables[i].getIntrinsicHeight());

            mTextPaint.getTextBounds(text, 0, text.length(), textBounds);
            myHeight = Math.max(textBounds.height(), myHeight);
            myWidth += textBounds.width();
        }


        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = myWidth;
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(widthSize, width);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = myHeight;
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(heightSize, height);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final Drawable[] drawables = mDrawables;
        final String[] strings = mDetailText;

        final int length = drawables != null ? drawables.length : 0;

        int alpha = (int) (255 * getAlpha());

        Rect textBounds = mTempRect;
        int startTop = 0, startLeft = 0;
        for (int i = 0; i < length; i++) {
            final Drawable d = drawables[i];
            final int w = d.getIntrinsicWidth();
            final int h = d.getIntrinsicHeight();
            d.setBounds(startLeft, startTop, startLeft + w, startTop + h);
            d.setAlpha(alpha);
            d.draw(canvas);

            startLeft += w;

            String text = strings[i];
            mTextPaint.getTextBounds(text, 0, text.length(), textBounds);

            canvas.drawText(strings[i], startLeft,
                    Math.abs(h - textBounds.height()) / 2 + textBounds.height(), mTextPaint);
            startLeft += textBounds.width();
        }
    }

    public void setBusPath(String pathText) {
        String[] text = pathText.split(" > ");
        int length = text.length;
        Drawable[] drs = new Drawable[length];
        String[] strings = new String[length];

        Resources res = getResources();
        final Drawable walkDrawable = res.getDrawable(R.drawable.ic_walk);
        final Drawable busDrawable = res.getDrawable(R.drawable.ic_bus);

        for (int i = 0; i < length; i++) {
            String s = text[i].trim();
            if (s.contains("W")) {
                drs[i] = walkDrawable;
                s = s.replace("W", "");
            } else if (!s.isEmpty()) {
                drs[i] = busDrawable;
            }
            strings[i] = s;
        }

        mDrawables = drs;
        mDetailText = strings;
    }
}
