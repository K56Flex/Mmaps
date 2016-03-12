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
