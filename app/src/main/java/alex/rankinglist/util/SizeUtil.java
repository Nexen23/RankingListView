package alex.rankinglist.util;


import android.view.View;

public class SizeUtil {
	public static int GetWindowHeight(View view) {
		return view.getContext().getResources().getDisplayMetrics().heightPixels;
	}
}
