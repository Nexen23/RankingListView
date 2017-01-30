package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import junit.framework.Assert;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import alex.rankinglist.R;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.util.MathUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;


public class UsersView extends FrameLayout {
	Integer prevHeight;
	int userViewHeightPx;
	float userViewHeightHalfPx;

	private boolean isGroupingEnabled = true;
	private LinkedList<TreeNode> usersGroups = new LinkedList<>();
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
		for (User user : users) {
			usersGroups.add(new TreeNode(rank, user));
		}
		Collections.sort(usersGroups);
	}

	private void updateChilds(int width, int height) {
		if (!usersGroups.isEmpty()) {
			LogUtil.log(this, "updateChilds(height=%d)", height);

			if (prevHeight == null || prevHeight > height) {
				updateGroupsPoses(height);
				composeGroups(height);
			} else {
				breakGroups(height);
				updateGroupsPoses(height);
			}
			prevHeight = height;

			createOrRemoveGroupsViews();
			updateGroupsViews();

			for (TreeNode usersGroup : usersGroups) {
				LogUtil.i(this, "main=%s [%d] {%d}", usersGroup.mainUser.name, usersGroup.groupSize, usersGroup.posAbsolute);
			}

			int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
			int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
			measureChildren(widthSpec, heightSpec);
		}
	}

	private void updateGroupsPoses(int height) {
		for (TreeNode group : usersGroups) {
			group.updateAbsolutePos(height);
		}
	}

	private void composeGroups(int height) {
		if (isGroupingEnabled) {
			ListIterator<TreeNode> iter = usersGroups.listIterator();
			TreeNode prevGroup = iter.next(), curGroup;
			usersGroups = new LinkedList<>();

			while (iter.hasNext()) {
				curGroup = iter.next();
				if (prevGroup.posAbsolute + userViewHeightPx > curGroup.posAbsolute) {
					curGroup = new TreeNode(height, prevGroup, curGroup);
				} else {
					usersGroups.add(prevGroup);
				}
				prevGroup = curGroup;
			}

			usersGroups.add(prevGroup);
		}
	}

	private void breakGroups(int height) {
		if (isGroupingEnabled) {
			ListIterator<TreeNode> iter = usersGroups.listIterator();
			TreeNode node;
			usersGroups = new LinkedList<>();
			LinkedList<TreeNode> depthTraversalList = new LinkedList<>();

			while (iter.hasNext()) {
				depthTraversalList.clear();
				depthTraversalList.add(iter.next());
				while (!depthTraversalList.isEmpty()) {
					node = depthTraversalList.getLast();
					depthTraversalList.removeLast();

					if (node.isLeaf() ||
							node.right.calcAndGetAbsolutePos(height)
									< (node.left.calcAndGetAbsolutePos(height) + userViewHeightPx)) {
						usersGroups.add(node);
					} else {
						depthTraversalList.add(node.right);
						depthTraversalList.add(node.left);
					}
				}
			}
		}
	}

	private void createOrRemoveGroupsViews() {
		// Create
		int childsCount = getChildCount(), groupsCount = usersGroups.size();
		for (int i = childsCount; i < groupsCount; ++i) {
			addView(new UsersGroupView(getContext()));
		}

		// Remove
		if (childsCount > groupsCount) {
			removeViews(groupsCount, childsCount - groupsCount);
		}
	}

	private void updateGroupsViews() {
		Assert.assertSame(getChildCount(), usersGroups.size());

		for (int i = 0; i < usersGroups.size(); ++i) {
			TreeNode group = usersGroups.get(i);

			UsersGroupView child = (UsersGroupView) getChildAt(i);
			MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
			params.topMargin = group.posAbsolute;
			child.setLayoutParams(params);

			if (group.isLeaf()) {
				child.setModel(group.mainUser);
			} else {
				child.setModel(group.mainUser, group.groupSize, calcScoreByRelativePos(group.posRelative));
			}
		}
	}

	private int calcAbsolutePos(int height, float relativePos) {
		float posFromTopPx = height * relativePos;
		float centeredPosFromTopPx = posFromTopPx - userViewHeightHalfPx;
		return (int) MathUtil.InRange(centeredPosFromTopPx, 0, height - userViewHeightPx);
	}

	private float calcScoreByRelativePos(float relativePos) {
		return (rank.scoreMax - rank.scoreMin) * (1 - relativePos) + rank.scoreMin;
	}

	class TreeNode implements Comparable<TreeNode> {
		Float posRelative;
		Integer posAbsolute;
		TreeNode left, right;
		int groupSize;

		User mainUser;

		public TreeNode(int height, TreeNode left, TreeNode right) {
			this.left = left;
			this.right = right;
			mainUser = left.mainUser;
			groupSize = left.groupSize + right.groupSize;
			posRelative = (left.posAbsolute + right.posAbsolute + userViewHeightPx) / (2.0f * height);
			updateAbsolutePos(height);
		}

		public TreeNode(Rank rank, User mainUser) {
			this.mainUser = mainUser;
			this.posRelative = (rank.scoreMax - mainUser.score) / (rank.scoreMax - rank.scoreMin);
			groupSize = 1;
		}

		public void updateAbsolutePos(int height) {
			posAbsolute = calcAbsolutePos(height, posRelative);
		}

		public int calcAndGetAbsolutePos(int height) {
			return posAbsolute = calcAbsolutePos(height, posRelative);
		}

		public boolean isLeaf() {
			return left == null && right == null;
		}

		@Override
		public int compareTo(TreeNode o) {
			return Float.compare(posRelative, o.posRelative);
		}
	}
}
