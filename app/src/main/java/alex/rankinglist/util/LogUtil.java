package alex.rankinglist.util;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

public class LogUtil {
	private static final String TAG = LogUtil.class.getName();
	private static boolean enabled = true;

	private static String format(@Nullable Object sender, String message, Object... args) {
		String messageWithSender = message;
		if (sender != null) {
			messageWithSender = String.format("[%x]{%s} %s", sender.hashCode(), sender.getClass().getSimpleName(), message);
		}
		return String.format(messageWithSender, args);
	}

	public static void log(@NonNull Object sender, String message, Object... args) {
		if (enabled) {
			Log.d(TAG, format(sender, message, args));
		}
	}

	public static void log(String message, Object... args) {
		Log.d(TAG, format(null, message, args));
	}

	public static String MeasureSpecToString(int measureSpec) {
		String mode = "EXACTLY";
		switch (View.MeasureSpec.getMode(measureSpec)) {
			case View.MeasureSpec.AT_MOST:
				mode = "AT_MOST";
				break;

			case View.MeasureSpec.UNSPECIFIED:
				mode = "UNSPECIFIED";
				break;
		}

		return String.format("(size=%d, mode=%s)", View.MeasureSpec.getSize(measureSpec), mode);
	}

	public static void err(@NonNull Object sender, String message, Object... args) {
		if (enabled) {
			Log.e(TAG, format(sender, message, args));
		}
	}

	public static void i(@NonNull Object sender, String message, Object... args) {
		if (enabled) {
			Log.i(TAG, format(sender, message, args));
		}
	}

	public static void setEnabled(boolean enabled) {
		LogUtil.enabled = enabled;
	}
}
