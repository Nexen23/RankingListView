package alex.domain.util;


import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class FixUtil {
	public static void fixFlinchesOnFirstScale(ScaleGestureDetector scaleGestureDetector) {
		long time = SystemClock.uptimeMillis();
		MotionEvent motionEvent = MotionEvent.obtain(time - 100, time, MotionEvent.ACTION_CANCEL,
				0.0f, 0.0f, 0);
		scaleGestureDetector.onTouchEvent(motionEvent);
		motionEvent.recycle();
	}
}
