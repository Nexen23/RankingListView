package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import alex.rankinglist.R;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.util.MathUtil;
import alex.rankinglist.util.SizeUtil;
import alex.rankinglist.widget.model.PosedUser;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;
import alex.rankinglist.widget.model.UsersGroup;


public class UsersView extends FrameLayout {

	int userViewHeightPx;
	float userViewHeightHalfPx;

	private List<UsersGroup> usersGroups;
	private List<PosedUser> users;
	private Rank rank;
	private boolean isGroupingEnabled = true;

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
		usersGroups = new ArrayList<>(SizeUtil.GetWindowHeight(this) / userViewHeightPx);
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

	public void setModel(Rank _rank, List<User> _users) {
		rank = _rank;
		users = new ArrayList<>(_users.size());

		for (User user : _users) {
			users.add(new PosedUser(rank, user));
		}
		Collections.sort(users);

		usersGroups.clear();
	}

	private void updateChilds(int width, int height) {
		if (!users.isEmpty()) {
			LogUtil.log(this, "updateChilds(height=%d)", height);

			updateUsersPoses(height);
			composeUsersGroups();
			createOrRemoveUsersGroupsViews();
			placeUsersGroupsViews();

			int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
			int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
			measureChildren(widthSpec, heightSpec);
		}
	}

	private void updateUsersPoses(int height) {
		for (PosedUser user : users) {
			user.pos.absolute = calcAbsolutePos(height, user.pos.relative);
		}
	}

	private void composeUsersGroups() {
		if (isGroupingEnabled) {
			usersGroups.clear();
			List<PosedUser> group = new LinkedList<>();
			PosedUser currentUser = users.get(0);
			int groupPos = currentUser.pos.absolute;
			group.add(currentUser);
			for (int i = 1; i < users.size(); ++i) {
				currentUser = users.get(i);
				if (currentUser.pos.absolute + userViewHeightPx > groupPos) {
					group.add(currentUser);
					groupPos = (groupPos + currentUser.pos.absolute) / 2;
				} else {
					usersGroups.add(new UsersGroup(group, groupPos));
					group = new LinkedList<>();
					group.add(currentUser);
					groupPos = currentUser.pos.absolute;
				}
			}

			usersGroups.add(new UsersGroup(group, groupPos));

			for (UsersGroup usersGroup : usersGroups) {
				String usersString = usersGroup.users.get(0).name;
				for (int i = 1; i < usersGroup.users.size(); ++i) {
					usersString = String.format("%s, %s", usersString, users.get(i).name);
				}
				LogUtil.i(this, "main=%s [%s] {%d}", usersGroup.mainUser.name, usersString, usersGroup.posAbsolute);
			}
		}
	}

	private void createOrRemoveUsersGroupsViews() {
		int groupsCount = isGroupingEnabled ? usersGroups.size() : users.size(), childsCount = getChildCount();
		for (int i = childsCount; i < groupsCount; ++i) {
			addView(new UsersGroupView(getContext()));
		}

		if (childsCount > groupsCount) {
			removeViews(groupsCount, childsCount - groupsCount);
		}
	}

	private void placeUsersGroupsViews() {
		for (int i = 0; i < getChildCount(); ++i) {
			UsersGroup usersGroup = null;
			PosedUser user = null;
			int viewPos;
			if (isGroupingEnabled) {
				usersGroup = usersGroups.get(getChildCount() - 1 - i); // HACK: find out why this cause correct z-ordering happen
				viewPos = usersGroup.posAbsolute;
			} else {
				user = users.get(getChildCount() - 1 - i);
				viewPos = user.pos.absolute;
			}

			UsersGroupView child = (UsersGroupView) getChildAt(i);
			MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
			params.topMargin = viewPos;
			child.setLayoutParams(params);

			if (isGroupingEnabled) {
				Assert.assertNotNull(usersGroup);
				if (usersGroup.users.size() > 1) {
					child.setModel(usersGroup.mainUser, usersGroup.users, calcScoreByPos(getHeight(), usersGroup.posAbsolute));
				} else {
					child.setModel(usersGroup.mainUser);
				}
			} else {
				Assert.assertNotNull(user);
				child.setModel(user);
			}
		}
	}

	private int calcAbsolutePos(int height, float relativePos) {
		float posFromTopPx = height * relativePos;
		float centeredPosFromTopPx = posFromTopPx - userViewHeightHalfPx;
		return (int) MathUtil.InRange(centeredPosFromTopPx, 0, height - userViewHeightPx);
	}

	private float calcScoreByPos(int height, int absolutePos) {
		float relativePos = (float) absolutePos / height;
		LogUtil.err(this, "%d / %d = %.3f", absolutePos, height, relativePos);
		return (rank.scoreMax - rank.scoreMin) * relativePos + rank.scoreMin;
	}
}
