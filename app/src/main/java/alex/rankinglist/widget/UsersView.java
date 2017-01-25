package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import alex.rankinglist.R;


public class UsersView extends FrameLayout {
	private LayoutInflater layoutInflater;

	public UsersView(Context context) {
		super(context);
		init();
	}

	public UsersView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public UsersView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		layoutInflater = LayoutInflater.from(getContext());
		generateUsers(4);
	}

	void generateUsers(@IntRange(from=0) int count) {
		for (int i = 0; i < count; ++i) {
			generateUser();
		}
	}

	void generateUser() {
		View userView = layoutInflater.inflate(R.layout.widget_user_view, this, true);
	}
}
