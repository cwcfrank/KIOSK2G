package tw.com.hokei.kiosk2g;

import android.app.Activity;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

/**
 * 模拟点击屏幕、滑动屏幕等操作
 */
public class CustomTouchEvent {

    /**
     * 模拟向下滑动事件
     *
     * @param distance 滑动的距离
     * @param activity 传进去的活动对象
     */
    public static void setMoveToBottom(int distance, Activity activity) {
        activity.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, 400, 500, 0));
        activity.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE, 400, 500 + distance, 0));
        activity.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP, 400, 500 + distance, 0));
    }

    /**
     * 模拟向右滑动事件
     *
     * @param distance 滑动的距离
     * @param activity 传进去的活动对象
     */
    public static void setMoveToRight(int distance, Activity activity) {
        activity.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, 400, 500, 0));
        activity.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE, 400 + distance, 500, 0));
        activity.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP, 400 + distance, 500, 0));
    }

    /**
     * 模拟向右滑动事件2
     *
     * @param distance 滑动的距离
     * @param view 传进去的活动对象
     */
    public static void setMoveToRight2(int distance, View view) {
        view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, 100, 100, 0));
        view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE, 100 + distance, 100, 0));
        view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP, 100 + distance, 100, 0));
    }

    /**
     * 模拟向左滑动事件2
     *
     * @param distance 滑动的距离
     * @param view 传进去的活动对象
     */
    public static void setMoveToLeft(int distance, View view) {
        view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, 100, 100, 0));
        view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE, 100 - distance, 100, 0));
        view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP, 100 - distance, 100, 0));
    }
}
