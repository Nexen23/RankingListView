package alex.rankinglist.util;


public class MathUtil {
	static public float InRange(float value, float min, float max) {
		return Math.max(min, Math.min(value, max));
	}
}
