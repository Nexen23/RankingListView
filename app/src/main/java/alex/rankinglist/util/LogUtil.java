package alex.rankinglist.util;


import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

public class LogUtil {
	private static final String TAG = LogUtil.class.getName();

	public static void log(@NonNull Object sender, String message, Object... args) {
		String messageWithSender =
				String.format("[%x]{%s} %s", sender.hashCode(), sender.getClass().getSimpleName(), message);
		Log.d(TAG, String.format(messageWithSender, args));
	}

	public static void log(String message, Object... args) {
		Log.d(TAG, String.format(message, args));
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
}
