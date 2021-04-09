package io.github.idonans.systeminsets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SystemInsetsViewCompatHelper;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 辅助处理自定义 window insets。自定义 window insets 在每一个 child view 的分发逻辑。
 * 支持：顺序等额分发(默认), 逆序等额分发，顺序串行分发，逆序串行分发。
 */
public class SystemInsetsLayerLayout extends FrameLayout {

    private final boolean DEBUG = false;

    public SystemInsetsLayerLayout(Context context) {
        this(context, null);
    }

    public SystemInsetsLayerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SystemInsetsLayerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SystemInsetsLayerLayout(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public static final int RESULT_ALWAYS_TRUE = 0;
    public static final int RESULT_ALWAYS_FALSE = 1;
    public static final int RESULT_MERGE_CHILD = 2;

    @IntDef({RESULT_ALWAYS_TRUE, RESULT_ALWAYS_FALSE, RESULT_MERGE_CHILD})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DispatchResult {
    }

    @DispatchResult
    private int mSystemInsetsLayerDispatchResult = RESULT_MERGE_CHILD;

    public static final int TYPE_ORDER_COPY = 0;
    public static final int TYPE_ORDER = 1;
    public static final int TYPE_REVERSE_COPY = 2;
    public static final int TYPE_REVERSE = 3;

    @IntDef({TYPE_ORDER_COPY, TYPE_ORDER, TYPE_REVERSE_COPY, TYPE_REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DispatchType {
    }

    @DispatchType
    private int mSystemInsetsLayerDispatchType = TYPE_ORDER_COPY;

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a =
                context.obtainStyledAttributes(
                        attrs, R.styleable.SystemInsetsLayerLayout, defStyleAttr, defStyleRes);

        mSystemInsetsLayerDispatchResult = a.getLayoutDimension(
                R.styleable.SystemInsetsLayerLayout_systemInsetsLayerDispatchResult,
                mSystemInsetsLayerDispatchResult);
        mSystemInsetsLayerDispatchType = a.getLayoutDimension(
                R.styleable.SystemInsetsLayerLayout_systemInsetsLayerDispatchType,
                mSystemInsetsLayerDispatchType);

        a.recycle();

        if (mSystemInsetsLayerDispatchResult != RESULT_ALWAYS_TRUE
                && mSystemInsetsLayerDispatchResult != RESULT_ALWAYS_FALSE
                && mSystemInsetsLayerDispatchResult != RESULT_MERGE_CHILD) {
            throw new IllegalArgumentException("invalid [SystemInsetsLayerLayout_systemInsetsLayerDispatchResult] " + mSystemInsetsLayerDispatchResult);
        }

        if (DEBUG) {
            SystemInsetsLog.d("system insets layer dispatch result %s", mSystemInsetsLayerDispatchResult);
        }
    }

    public void setSystemInsetsLayerDispatchResult(@DispatchResult int systemInsetsLayerDispatchResult) {
        if (mSystemInsetsLayerDispatchResult != systemInsetsLayerDispatchResult) {
            mSystemInsetsLayerDispatchResult = systemInsetsLayerDispatchResult;
            ViewCompat.requestApplyInsets(this);
        }
    }

    @DispatchResult
    public int getSystemInsetsLayerDispatchResult() {
        return mSystemInsetsLayerDispatchResult;
    }

    public void setSystemInsetsLayerDispatchType(@DispatchType int systemInsetsLayerDispatchType) {
        if (mSystemInsetsLayerDispatchType != systemInsetsLayerDispatchType) {
            mSystemInsetsLayerDispatchType = systemInsetsLayerDispatchType;
            ViewCompat.requestApplyInsets(this);
        }
    }

    @DispatchType
    public int getSystemInsetsLayerDispatchType() {
        return mSystemInsetsLayerDispatchType;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        boolean mergeSystemWindowInsetConsumed = false;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final int targetIndex = (mSystemInsetsLayerDispatchType == TYPE_ORDER
                    || mSystemInsetsLayerDispatchType == TYPE_ORDER_COPY)
                    ? i : count - 1 - i;
            final WindowInsets targetWindowInsets = (mSystemInsetsLayerDispatchType == TYPE_ORDER
                    || mSystemInsetsLayerDispatchType == TYPE_REVERSE)
                    ? insets : new WindowInsets(insets);
            WindowInsets childInsets = getChildAt(targetIndex).dispatchApplyWindowInsets(targetWindowInsets);
            boolean systemWindowInsetConsumed = childInsets.getSystemWindowInsetLeft() == 0
                    && childInsets.getSystemWindowInsetTop() == 0
                    && childInsets.getSystemWindowInsetRight() == 0
                    && childInsets.getSystemWindowInsetBottom() == 0;
            mergeSystemWindowInsetConsumed |= systemWindowInsetConsumed;
        }

        if (mSystemInsetsLayerDispatchResult == RESULT_ALWAYS_TRUE) {
            return insets.consumeSystemWindowInsets();
        }

        if (mSystemInsetsLayerDispatchResult == RESULT_ALWAYS_FALSE) {
            return insets;
        }

        // RESULT_MERGE_CHILD

        if (mergeSystemWindowInsetConsumed) {
            return insets.consumeSystemWindowInsets();
        }
        return insets;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        boolean mergeSystemWindowInsetConsumed = false;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final int targetIndex = (mSystemInsetsLayerDispatchType == TYPE_ORDER
                    || mSystemInsetsLayerDispatchType == TYPE_ORDER_COPY)
                    ? i : count - 1 - i;
            final Rect targetWindowInsets = (mSystemInsetsLayerDispatchType == TYPE_ORDER
                    || mSystemInsetsLayerDispatchType == TYPE_REVERSE)
                    ? insets : new Rect(insets);

            mergeSystemWindowInsetConsumed |= SystemInsetsViewCompatHelper.callFitSystemWindows(
                    getChildAt(targetIndex),
                    targetWindowInsets);
        }

        if (mSystemInsetsLayerDispatchResult == RESULT_ALWAYS_TRUE) {
            return true;
        }

        if (mSystemInsetsLayerDispatchResult == RESULT_ALWAYS_FALSE) {
            return false;
        }

        // RESULT_MERGE_CHILD

        return mergeSystemWindowInsetConsumed;
    }

}
