package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import alex.rankinglist.R;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.util.MathUtil;
import alex.rankinglist.widget.model.PosedUser;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;
import alex.rankinglist.widget.model.UsersGroup;


public class UsersView extends FrameLayout {
	int userViewHeightPx;
	float userViewHeightHalfPx;

	private List<UsersGroup> usersGroups = new LinkedList<>();
	private List<PosedUser> users;
	private Rank rank;

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
		userViewHeightPx = getResources().getDimensionPixelSize(R.dimen.user_view_height);
		userViewHeightHalfPx = userViewHeightPx / 2.0f;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		LogUtil.log(this, "onMeasure() %s", LogUtil.MeasureSpecToString(heightMeasureSpec));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		LogUtil.log(this, "onSizeChanged()");
		super.onSizeChanged(w, h, oldw, oldh);
		updateChilds(w, h);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		LogUtil.log(this, "onLayout()");
		super.onLayout(changed, left, top, right, bottom);
	}

	public void setModel(Rank rank, List<User> users) {
		this.rank = rank;
		usersGroups.clear();

		this.users = new ArrayList<>(users.size());
		for (User user : users) {
			PosedUser posedUser = new PosedUser(rank, user);
			this.users.add(posedUser);
			usersGroups.add(new UsersGroup(posedUser));
		}

		Collections.sort(usersGroups);
	}

	private void updateChilds(int width, int height) {
		if (!usersGroups.isEmpty()) {
			LogUtil.log(this, "updateChilds(height=%d)", height);

			updateUsersPoses(height);
			updateUsersGroupsPoses(height);
			composeOrBreakUsersGroups(height);
			createOrRemoveUsersGroupsViews();
			placeUsersGroupsViews();

			int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
			int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
			measureChildren(widthSpec, heightSpec);
		}
	}

	private int calcAbsolutePos(int height, float relativePos) {
		float posFromTopPx = height * relativePos;
		float centeredPosFromTopPx = posFromTopPx - userViewHeightPx / 2;
		return (int) MathUtil.InRange(centeredPosFromTopPx, 0, height - userViewHeightPx);
	}

	private void updateUsersPoses(int height) {
		for (PosedUser user : users) {
			user.pos.absolute = calcAbsolutePos(height, user.pos.relative);
		}
	}

	private void updateUsersGroupsPoses(int height) {
		for (UsersGroup group : usersGroups) {
			group.pos.absolute = calcAbsolutePos(height, group.pos.relative);
		}
	}

	private void composeOrBreakUsersGroups(int height) {
		if (!usersGroups.isEmpty()) {
			ListIterator<UsersGroup> groupsIter = usersGroups.listIterator();
			UsersGroup itGroup = groupsIter.next();
//			while (groupsIter.hasNext()) {
//				if ()
//			}
		}
	}

	private void createOrRemoveUsersGroupsViews() {
		int groupsCount = usersGroups.size(), childsCount = getChildCount();
		for (int i = childsCount; i < groupsCount; ++i) {
			addView(new UsersGroupView(getContext()));
		}

		if (childsCount > groupsCount) {
			removeViews(groupsCount, childsCount - groupsCount);
		}
	}

	private void placeUsersGroupsViews() {
		for (int i = 0; i < getChildCount(); ++i) {
			UsersGroup usersGroup = usersGroups.get(getChildCount() - 1 - i); // HACK: find out why this cause correct z-ordering happen

			UsersGroupView child = (UsersGroupView) getChildAt(i);
			MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
			params.topMargin = usersGroup.pos.absolute;
			child.setLayoutParams(params);

			float groupScore = (1 - usersGroup.pos.relative) * (rank.scoreMax - rank.scoreMin) + rank.scoreMin;
			child.setModel(usersGroup.mainUser, usersGroup.users, groupScore);
		}
	}
}
