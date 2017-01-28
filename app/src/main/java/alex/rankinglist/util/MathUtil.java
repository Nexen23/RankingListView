package alex.rankinglist.util;


public class MathUtil {
	private static final float EPSILON = 0.0001f;

	public static float InRange(float value, float min, float max) {
		return Math.max(min, Math.min(value, max));
	}

	public static boolean IsEqual(Float a, Float b) {
		return Math.abs(a - b) < EPSILON;
	}
}
