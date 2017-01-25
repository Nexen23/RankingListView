package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.Random;


public class UsersView extends FrameLayout {
	private LayoutInflater layoutInflater;
	private Random random = new Random();

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
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
				generateUsers(4);
			}
		});
	}

	void generateUsers(@IntRange(from=0) int count) {
		for (int i = 0; i < count; ++i) {
			generateUser();
		}
	}

	void generateUser() {
		UserView userView = new UserView(getContext());

		String name = Integer.toHexString(random.nextInt(0xFFFFFF));
		userView.setData(name, random.nextInt(200));

		addView(userView);

		FrameLayout.LayoutParams params = (LayoutParams) userView.getLayoutParams();


		int userViewHeight = userView.getHeight();
		int height = getHeight();
		params.topMargin = random.nextInt(height - userViewHeight);
		userView.setLayoutParams(params);
	}
}
