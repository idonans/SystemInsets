package android.view;

import android.graphics.Rect;

public class SystemInsetsViewCompatHelper {

    public static boolean callFitSystemWindows(View view, Rect insets) {
        return view.fitSystemWindows(insets);
    }

}
