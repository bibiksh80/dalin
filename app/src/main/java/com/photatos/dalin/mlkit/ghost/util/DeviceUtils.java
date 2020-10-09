package com.photatos.dalin.mlkit.ghost.util;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;

public class DeviceUtils {

    public static int getScreenWidth(@NonNull Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static float dpToPx(final float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

}
