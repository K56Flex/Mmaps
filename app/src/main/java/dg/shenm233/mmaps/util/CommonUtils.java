package dg.shenm233.mmaps.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.DecimalFormat;

import dg.shenm233.mmaps.R;

public class CommonUtils {
    public static void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 将秒转换为时:分的形式
     */
    public static String getFriendlyDuration(Context context, long sec) {
        StringBuilder sb = new StringBuilder();
        if (sec <= 60)
            return sb.append(1).append(context.getText(R.string.minute))
                    .toString();
        else if (sec <= 3600)
            return sb.append(sec / 60).append(context.getText(R.string.minute))
                    .toString();
        else {
            return sb.append(sec / 3600).append(context.getText(R.string.hour)).append(" ")
                    .append((sec % 3600) / 60).append(context.getText(R.string.minute))
                    .toString();
        }
    }

    /**
     * 将米数转换成更友好的样式
     */
    public static String getFriendlyLength(int lenMeter) {
        if (lenMeter > 10000) // 10 km
        {
            int dis = lenMeter / 1000;
            return dis + ChString.Kilometer;
        }

        if (lenMeter > 1000) {
            float dis = (float) lenMeter / 1000;
            DecimalFormat fnum = new DecimalFormat("##0.0");
            String dstr = fnum.format(dis);
            return dstr + ChString.Kilometer;
        }

        return lenMeter + ChString.Meter;
    }

    /**
     * 添加“元”单位
     */
    public static String getFriendlyCost(float yuan) {
        return yuan + "元";
    }

    public static boolean isStringEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
