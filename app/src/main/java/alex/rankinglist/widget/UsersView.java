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
import java.util.Stack;

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
	private TreeNode usersGroupsRoot;
	int usersGroupsCount = 0;
	LinkedList<DistanceNode> groupsDistances = new LinkedList<>();
	Stack<TreeNode> groupsHistory = new Stack<>();
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
		LinkedList<TreeNode> usersGroups = new LinkedList<>();
		for (User user : users) {
			usersGroups.add(new TreeNode(rank, user));
		}
		Collections.sort(usersGroups);
		usersGroupsCount = usersGroups.size();

		groupsDistances.clear();
		ListIterator<TreeNode> iter = usersGroups.listIterator();
		TreeNode prevNode = iter.next(), curNode;
		usersGroupsRoot = prevNode;
		while (iter.hasNext()) {
			curNode = iter.next();
			prevNode.next = curNode;
			curNode.prev = prevNode;
			groupsDistances.add(new DistanceNode(prevNode, curNode));
			prevNode = curNode;
		}
	}


	class DistanceNode implements Comparable<DistanceNode>,OnParentUpdate {
		TreeNode from, to;
		Float distance;

		public DistanceNode(TreeNode from, TreeNode to) {
			this.from = from;
			this.to = to;
			from.listeners.add(this);
			to.listeners.add(this);
		}
		public void updateDistance() {
			distance = to.posAbsolute - (from.posAbsolute + userViewHeightPx);
		}

		public void compose(int height) {
			from.listeners.remove(this);
			to.listeners.remove(this);
			groupsHistory.push(new TreeNode(height, from, to));
			usersGroupsCount--;
		}

		@Override
		public int compareTo(DistanceNode o) {
			return Float.compare(distance, o.distance);
		}

		@Override
		public void parentSetFor(TreeNode node, TreeNode parent) {
			node.listeners.remove(this);
			if (node == from) {
				from = parent;
				from.listeners.add(this);
			} else {
				to = parent;
				to.listeners.add(this);
			}
			updateDistance();
		}

		@Override
		public void breakNode(TreeNode node) {
			node.listeners.remove(this);
			if (node == from) {
				from = node.right;
				from.listeners.add(this);
			} else {
				to = node.left;
				to.listeners.add(this);
			}
			updateDistance();
		}
	}

	private void updateGroupsPoses(int height) {
		TreeNode node = usersGroupsRoot;
		int i = 0;
		while (node != null) {
			node.updateAbsolutePos(height);
			node = node.next;
			++i;
		}
		Assert.assertSame(usersGroupsCount, i);
	}

	private void updateDistances() {
		for (DistanceNode node : groupsDistances) {
			node.updateDistance();
		}
		Collections.sort(groupsDistances);
	}

	private void composeByDistances(int height) {
		if (!groupsDistances.isEmpty()) {
			DistanceNode first = groupsDistances.getFirst();
			while (first.distance < 0) {
				first.compose(height);
				groupsDistances.removeFirst();
				Collections.sort(groupsDistances);
				if (groupsDistances.isEmpty()) {
					break;
				} else {
					first = groupsDistances.getFirst();
				}
			}
		}
	}

	private void breakByHistory(int height) {
		while (!groupsHistory.isEmpty()) {
			TreeNode node = groupsHistory.peek();
			Float rightPos = node.right.calcAndGetAbsolutePos(height);
			Float leftPos = node.left.calcAndGetAbsolutePos(height);
			if (rightPos >= (leftPos + userViewHeightPx)) {
				groupsHistory.pop();

				node.breakNode();

				if (node.prev != null) {
					node.prev.next = node.left;
				}
				if (node.next != null) {
					node.next.prev = node.right;
				}
				if (node == usersGroupsRoot) {
					usersGroupsRoot = node.left;
				}

				groupsDistances.add(new DistanceNode(node.left, node.right));
				++usersGroupsCount;
			} else {
				break;
			}
		}
	}






	private void updateChilds(int width, int height) {
		if (usersGroupsCount > 0) {
			LogUtil.log(this, "updateChilds(height=%d)", height);

			updateGroupsPoses(height);
			if (prevHeight == null || prevHeight > height) {
				updateDistances();
				composeByDistances(height);
			} else {
				breakByHistory(height);
			}
			prevHeight = height;

			createOrRemoveGroupsViews();
			updateGroupsViews();

			/*for (TreeNode usersGroup : usersGroups) {
				LogUtil.i(this, "main=%s [%d] {%f}", usersGroup.mainUser.name, usersGroup.groupSize, usersGroup.posAbsolute);
			}*/

			int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
			int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
			measureChildren(widthSpec, heightSpec);
		}
	}

	private void createOrRemoveGroupsViews() {
		// Create
		int childsCount = getChildCount(), groupsCount = usersGroupsCount;
		for (int i = childsCount; i < groupsCount; ++i) {
			addView(new UsersGroupView(getContext()));
		}

		// Remove
		if (childsCount > groupsCount) {
			removeViews(groupsCount, childsCount - groupsCount);
		}
	}

	private void updateGroupsViews() {
		Assert.assertSame(getChildCount(), usersGroupsCount);

		TreeNode group = usersGroupsRoot;
		for (int i = 0; i < usersGroupsCount; ++i) {
			UsersGroupView child = (UsersGroupView) getChildAt(i);
			MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
			params.topMargin = group.posAbsolute.intValue();
			child.setLayoutParams(params);

			if (group.isLeaf()) {
				child.setModel(group.mainUser);
			} else {
				child.setModel(group.mainUser, group.groupSize, calcScoreByRelativePos(group.posRelative));
			}

			group = group.next;
		}
	}

	private float calcAbsolutePos(int height, float relativePos) {
		float posFromTopPx = height * relativePos;
		float centeredPosFromTopPx = posFromTopPx - userViewHeightHalfPx;
		return MathUtil.InRange(centeredPosFromTopPx, 0, height - userViewHeightPx);
	}

	private float calcScoreByRelativePos(float relativePos) {
		return (rank.scoreMax - rank.scoreMin) * (1 - relativePos) + rank.scoreMin;
	}




	interface OnParentUpdate {
		void parentSetFor(TreeNode node, TreeNode parent);
		void breakNode(TreeNode node);
	}

	class TreeNode implements Comparable<TreeNode> {
		Float posRelative;
		Float posAbsolute;
		TreeNode left, right;
		int groupSize;

		User mainUser;


		TreeNode prev, next; // for lists moving and updating
		LinkedList<OnParentUpdate> listeners = new LinkedList<>();

		public TreeNode(int height, TreeNode left, TreeNode right) {
			this.left = left;
			this.right = right;

			if (left.prev != null) {
				left.prev.next = this;
			}
			prev = left.prev;

			if (right.next != null) {
				right.next.prev = this;
			}
			next = right.next;

			if (left == usersGroupsRoot) {
				usersGroupsRoot = this;
			}

			mainUser = left.mainUser;
			groupSize = left.groupSize + right.groupSize;
			posRelative = (left.posAbsolute + right.posAbsolute + userViewHeightPx) / (2.0f * height);
			updateAbsolutePos(height);

			left.setParent(this);
			right.setParent(this);
		}

		void setParent(TreeNode parent) {
			LinkedList<OnParentUpdate> listenersClone = new LinkedList<>(listeners);
			for (OnParentUpdate listener : listenersClone) {
				listener.parentSetFor(this, parent);
			}
		}

		void breakNode() {
			LinkedList<OnParentUpdate> listenersClone = new LinkedList<>(listeners);
			for (OnParentUpdate listener : listenersClone) {
				listener.breakNode(this);
			}
		}

		public TreeNode(Rank rank, User mainUser) {
			this.mainUser = mainUser;
			this.posRelative = (rank.scoreMax - mainUser.score) / (rank.scoreMax - rank.scoreMin);
			groupSize = 1;
		}

		public void updateAbsolutePos(int height) {
			posAbsolute = calcAbsolutePos(height, posRelative);
		}

		public Float calcAndGetAbsolutePos(int height) {
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
