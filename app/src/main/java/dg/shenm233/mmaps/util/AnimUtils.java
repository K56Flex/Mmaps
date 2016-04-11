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

package dg.shenm233.mmaps.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import dg.shenm233.mmaps.R;

public class AnimUtils {
    public static void viewSlideInTop(View view) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.slide_in_top);
        view.startAnimation(animation);
    }

    public static void viewSlideOutTop(View view) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(),
                R.anim.slide_out_top);
        view.startAnimation(animation);
    }
}
