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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

import dg.shenm233.drag2expandview.Drag2ExpandView;

public class FabBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
    private float mFabTranslationY;

    public FabBehavior() {
        super();
    }

    public FabBehavior(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent,
                                   FloatingActionButton child, View dependency) {
        if (dependency instanceof FloatingActionButton) {
            return true;
        } else if (dependency instanceof Drag2ExpandView) {
            return true;
        }

        Object depend = dependency.getTag(child.getId()); // 获取child是否应该依赖dependency?
        return depend != null && (boolean) depend;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child,
                                          View dependency) {
        float targetTransY = getFabTranslationYForOtherView(parent, child);

        if (mFabTranslationY != targetTransY) { // only
            mFabTranslationY = targetTransY;
            child.setTranslationY(targetTransY);
        }
        return false;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, FloatingActionButton child,
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
    public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionButton child,
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

    private float getFabTranslationYForOtherView(CoordinatorLayout parent, FloatingActionButton fab) {
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
