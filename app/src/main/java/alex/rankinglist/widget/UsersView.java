package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import junit.framework.Assert;

import java.util.List;

import alex.rankinglist.R;
import alex.rankinglist.misc.ClusteredList;
import alex.rankinglist.misc.TreeNode;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;


public class UsersView extends FrameLayout {
	private ClusteredList clusteredList;
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
		clusteredList = new ClusteredList(getResources().getDimensionPixelSize(R.dimen.user_view_height));
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
		if (clusteredList.updateChilds(w, h)) {
			createOrRemoveGroupsViews();
			updateGroupsViews();

			int widthSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
			int heightSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
			measureChildren(widthSpec, heightSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		LogUtil.log(this, "onLayout()");
		super.onLayout(changed, left, top, right, bottom);
	}

	public void setModel(Rank rank, List<User> users) {
		this.rank = rank;
		clusteredList.setData(rank, users);
	}



	private void createOrRemoveGroupsViews() {
		// Create
		int childsCount = getChildCount(), groupsCount = clusteredList.usersGroupsCount;
		for (int i = childsCount; i < groupsCount; ++i) {
			addView(new UsersGroupView(getContext()));
		}

		// Remove
		if (childsCount > groupsCount) {
			removeViews(groupsCount, childsCount - groupsCount);
		}
	}

	private void updateGroupsViews() {
		Assert.assertSame(getChildCount(), clusteredList.usersGroupsCount);

		TreeNode group = clusteredList.usersGroupsRoot;
		for (int i = 0; i < clusteredList.usersGroupsCount; ++i) {
			UsersGroupView child = (UsersGroupView) getChildAt(i);
//			MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
//			params.topMargin = group.posAbsolute.intValue();
//			child.setLayoutParams(params);
			child.setY(group.posAbsolute.intValue());

			if (group.isLeaf()) {
				child.setModel(group.mainUser);
			} else {
				child.setModel(group.mainUser, group.groupSize, calcScoreByRelativePos(group.posRelative));
			}

			group = group.next;
		}
	}

	private float calcScoreByRelativePos(float relativePos) {
		return (rank.scoreMax - rank.scoreMin) * (1 - relativePos) + rank.scoreMin;
	}
}
