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

    private Drawable mRightIcon = null;

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

        myWidth += (length - 1) * (mRightIcon != null ? mRightIcon.getIntrinsicWidth() : 0);

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
        int canvasWidth = canvas.getWidth();

        final Drawable[] drawables = mDrawables;
        final String[] strings = mDetailText;
        final Drawable rightIcon = mRightIcon;

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

            int textTop = Math.abs(h - textBounds.height()) / 2 + textBounds.height();

            // check whether there is enough space for drawing text,otherwise draw 3 dots
            if (startLeft + textBounds.width() > canvasWidth - w) {
                canvas.drawText("...", startLeft, textTop, mTextPaint);
                break;
            }

            canvas.drawText(strings[i], startLeft, textTop, mTextPaint);
            startLeft += textBounds.width();

            if (rightIcon != null && i < length - 1) {
                // use old width,because these icon width is 24dp ;P
                rightIcon.setBounds(startLeft, startTop, startLeft + w, startTop + h);
                rightIcon.setAlpha(alpha);
                rightIcon.draw(canvas);
                startLeft += w;
            }

            // check whether there is enough space for drawing next icon,otherwise draw 3 dots
            if (startLeft > canvasWidth - w) {
                canvas.drawText("...", startLeft, textTop, mTextPaint);
                break;
            }
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

        if (mRightIcon == null) {
            mRightIcon = res.getDrawable(R.drawable.ic_chevron_right);
        }

        requestLayout();
    }
}
