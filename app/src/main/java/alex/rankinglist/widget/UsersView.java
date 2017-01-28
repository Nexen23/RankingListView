package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import alex.rankinglist.R;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.util.MathUtil;
import alex.rankinglist.widget.model.RankedUser;
import alex.rankinglist.widget.model.User;
import butterknife.BindDimen;
import butterknife.ButterKnife;


public class UsersView extends FrameLayout {
	@BindDimen(R.dimen.user_view_height) int userViewHeightPx;
	float userViewHeightHalfPx;

	private Map<Float, UsersGroup> usersGroups = new HashMap<>();
	private List<RankedUser> rankedUsers;
	private Integer lastMeasuredHeight;

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
		ButterKnife.bind(this);
		userViewHeightHalfPx = userViewHeightPx / 2.0f;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		LogUtil.log(this, "onSizeChanged()");
		super.onSizeChanged(w, h, oldw, oldh);
		updateUsersPositions(w, h);
	}

	public void updateUsersPositions(int width, int height) {
		if (rankedUsers != null) {
			LogUtil.log(this, "updateUsersPositions(%d)", height);
			for (int i = 0; i < getChildCount(); ++i) {
				View child = getChildAt(i);
				LayoutParams params = (LayoutParams) child.getLayoutParams();

				float posFromTopPx = height * (1 - rankedUsers.get(i).relativeRank);
				float centeredPosFromTopPx = posFromTopPx - userViewHeightPx/2;
				params.topMargin = (int) MathUtil.InRange(centeredPosFromTopPx, 0, height - userViewHeightPx);

				child.setLayoutParams(params);
			}

			int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
			int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
			measureChildren(widthSpec, heightSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		LogUtil.log(this, "onLayout()");
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		LogUtil.log(this, "onMeasure() %s", LogUtil.MeasureSpecToString(heightMeasureSpec));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void setModel(List<RankedUser> rankedUsers) {
		this.rankedUsers = rankedUsers;
		for (RankedUser user : rankedUsers) {
			RankedUsersView rankedUsersView = new RankedUsersView(getContext());
			rankedUsersView.setModel(user);
			addView(rankedUsersView);
		}
	}

	@SuppressWarnings("WeakerAccess")
	class UsersGroup {
		public final User user;
		public final List<User> group;

		public UsersGroup(User user) {
			this(user, new LinkedList<User>());
		}

		public UsersGroup(User user, List<User> group) {
			this.user = user;
			this.group = group;
		}
	}
}
