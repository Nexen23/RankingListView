package alex.rankinglist.util;


public class MathUtil {
	public static final float EPSILON = 0.001f;

	public static float InRange(float value, float min, float max) {
		return Math.max(min, Math.min(value, max));
	}

	public static boolean IsEqual(Float a, Float b) {
		return Math.abs(a - b) < EPSILON;
	}

	public static int Compare(float a, float b) {
		if (Math.abs(a - b) < EPSILON) {
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
}
