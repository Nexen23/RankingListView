package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.Random;


public class UsersView extends FrameLayout {
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
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
				generateUsers(0);
			}
		});
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		for (int i = 0; i < getChildCount(); ++i) {
			View child = getChildAt(i);
			FrameLayout.LayoutParams params = (LayoutParams) child.getLayoutParams();
			params.topMargin = (int) (h * ((float) child.getTag()));
			child.setLayoutParams(params);
		}
		
		super.onSizeChanged(w, h, oldw, oldh);
	}

	void generateUsers(@IntRange(from=0) int count) {
		for (int i = 0; i < count; ++i) {
			generateUser();
		}
	}

	void generateUser() {
		UserView userView = new UserView(getContext());

		String name = Integer.toHexString(random.nextInt(0xFFFFFF));
		final int maxRank = 100;
		int rank = random.nextInt(maxRank);
		@FloatRange(from=0, to=1) float viewPos = (float) rank / maxRank;
		userView.setData(name, rank);
		userView.setTag(viewPos);

		addView(userView);

		FrameLayout.LayoutParams params = (LayoutParams) userView.getLayoutParams();


		int userViewHeight = userView.getHeight(); // FIXME: 25.01.2017 zero after creation
		int height = getHeight();
		params.topMargin = (int) (viewPos * (height - userViewHeight));
		userView.setLayoutParams(params);
	}
}
