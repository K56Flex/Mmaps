package dg.shenm233.mmaps.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

import dg.shenm233.drag2expandview.Drag2ExpandView;

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
            } else if (dependency instanceof Drag2ExpandView) {
                return true;
            }

            Object depend = dependency.getTag(child.getId()); // 获取child是否应该依赖dependency?
            return depend != null && (boolean) depend;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingButton child,
                                              View dependency) {
            float targetTransY = getFabTranslationYForOtherView(parent, child);

            if (mFabTranslationY != targetTransY) { // only
                mFabTranslationY = targetTransY;
                child.setTranslationY(targetTransY);
            }
            return false;
        }

        @Override
        public void onDependentViewRemoved(CoordinatorLayout parent, FloatingButton child,
                                           View dependency) {
            if (dependency instanceof Drag2ExpandView) {
                float targetTransY = getFabTranslationYForOtherView(parent, child);
                if (mFabTranslationY != targetTransY) {
                    mFabTranslationY = targetTransY;
                    child.setTranslationY(targetTransY);
                }
            }
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, FloatingButton child,
                                     int layoutDirection) {
            float targetTransY = 0.0f;
            List<View> dependencies = parent.getDependencies(child);
            for (View v : dependencies) {
                if (v instanceof Drag2ExpandView) {
                    targetTransY = Math.min(targetTransY,
                            v.getTranslationY() - ((Drag2ExpandView) v).getHeaderHeight());
                } else {
                    if (parent.doViewsOverlap(v, child)) {
                        int bottomYMargin = ((CoordinatorLayout.LayoutParams) v.getLayoutParams()).bottomMargin;
                        float offset = v.getTranslationY() - v.getHeight() - bottomYMargin;

                        targetTransY = Math.min(targetTransY, offset);
                    }
                }
            }
            parent.onLayoutChild(child, layoutDirection);
            // no need to translate Y if dependencies not require child to do
            if (targetTransY != 0.0f) {
                mFabTranslationY = targetTransY;
                child.setTranslationY(targetTransY);
            }
            return true;
        }

        private float getFabTranslationYForOtherView(CoordinatorLayout parent, FloatingButton fab) {
            float minOffset = 0;
            final List<View> dependencies = parent.getDependencies(fab);
            for (View v : dependencies) {
                if (v instanceof Drag2ExpandView) {
                    minOffset = v.getTranslationY() - ((Drag2ExpandView) v).getHeaderHeight();
                } else if (parent.doViewsOverlap(v, fab)) {
                    int bottomYMargin = ((CoordinatorLayout.LayoutParams) v.getLayoutParams()).bottomMargin;
                    float offset = v.getTranslationY() - v.getHeight() - bottomYMargin;

                    minOffset = Math.min(minOffset, offset);
                }
            }
            return minOffset;
        }
    }
}
