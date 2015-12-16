package dg.shenm233.mmaps.ui.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

public class FloatingButton extends ImageButton {
    public FloatingButton(Context context) {
        super(context);
    }

    public FloatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FloatingButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static class Behavior extends CoordinatorLayout.Behavior<FloatingButton> {
        private float mFabTranslationY;

        public Behavior() {
            super();
        }

        public Behavior(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent,
                                       FloatingButton child, View dependency) {
            if (dependency instanceof FloatingButton) {
                return true;
            }

            Object depend = dependency.getTag(child.getId()); // 获取child是否应该依赖dependency?
            return depend != null && (boolean) depend;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingButton child,
                                              View dependency) {
            if (child.getVisibility() == View.VISIBLE) {
                // get original bottom margin for more space between views
                int bottomYMargin = ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).bottomMargin;
                float targetTransY = getFabTranslationYForOtherView(parent, child);
                targetTransY -= bottomYMargin;

                if (mFabTranslationY != targetTransY) { // only
                    mFabTranslationY = targetTransY;
                    child.setTranslationY(targetTransY);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, FloatingButton child,
                                     int layoutDirection) {
            return false;
        }

        private float getFabTranslationYForOtherView(CoordinatorLayout parent, FloatingButton fab) {
            float minOffset = 0;
            final List<View> dependencies = parent.getDependencies(fab);
            for (View v : dependencies) {
                if (parent.doViewsOverlap(v, fab)) {
                    minOffset = Math.min(minOffset, v.getTranslationY() - v.getHeight());
                }
            }
            return minOffset;
        }
    }
}
