package alex.domain.util;


import android.view.MotionEvent;

public class MathUtil {
	public static final float EPSILON = 0.001f;

	public static float InRange(float value, float min, float max) {
		return Math.max(min, Math.min(value, max));
	}

	public static boolean IsEqual(Float a, Float b) {
		return Math.abs(a - b) < EPSILON || (a.isInfinite() && b.isInfinite());
	}

	public static int Compare(Float a, Float b) {
		if (IsEqual(a, b)) {
			return 0;
		} else {
			if (a < b) {
				return -1;
			}
			if (a > b) {
				return 1;
			}
			throw new IllegalStateException();
		}
	}

	public static float Distance(MotionEvent.PointerCoords p1, MotionEvent.PointerCoords p2) {
		float x1 = p1.x;
		float y1 = p1.y;
		float x2 = p2.x;
		float y2 = p2.y;
		return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}
}
