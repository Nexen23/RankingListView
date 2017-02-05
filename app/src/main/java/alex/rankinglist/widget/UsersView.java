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
import alex.rankinglist.misc.TreeNode;
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
		if (!users.isEmpty()) {
			LinkedList<TreeNode> usersGroups = new LinkedList<>();
			for (User user : users) {
				usersGroups.add(new TreeNode(userViewHeightPx, rank, user));
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
	}






	public class DistanceNode implements Comparable<DistanceNode>, TreeNode.OnParentUpdate {
		TreeNode from, to;
		Float intersectingHeight;

		public DistanceNode(TreeNode from, TreeNode to) {
			this.from = from;
			this.to = to;
			from.listeners.add(this);
			to.listeners.add(this);
		}

		public void updateIntersectingHeight() {
			//distance = to.posAbsolute - (from.posAbsolute + userViewHeightPx);

			TreeNode left = from, right = to;

			// FIXME: 04.02.2017 relativePoses can be equal
			final Float wouldIntersectWhenHeight = (userViewHeightPx / (right.posRelative - left.posRelative));
			final Float height = wouldIntersectWhenHeight;
			final boolean leftIsBorder = left.isLeftBorder(height), rightIsBorder = right.isRightBorder(height);

			while (true) {
				if (!leftIsBorder && !rightIsBorder) { // general intersect of non borders
					intersectingHeight = height;
					break;
				}

				if (leftIsBorder && !rightIsBorder) {
					// FIXME: 04.02.2017 relativePos can be 0
					intersectingHeight = (userViewHeightPx + userViewHeightHalfPx) / right.posRelative;
					break;
				}

				if (!leftIsBorder && rightIsBorder) {
					// FIXME: 04.02.2017 relativePos can be 1
					intersectingHeight = (userViewHeightPx + userViewHeightHalfPx) / (1 - left.posRelative);
					break;
				}

				if (leftIsBorder && rightIsBorder) {
					intersectingHeight = userViewHeightPx * 2.0f;
					break;
				}
				throw new IllegalStateException();
			}
		}




		public void compose(int height) {
			from.listeners.remove(this);
			to.listeners.remove(this);
			final TreeNode newNode = new TreeNode(userViewHeightPx, height, from, to);
			if (from == usersGroupsRoot) {
				usersGroupsRoot = newNode;
			}

			groupsHistory.push(newNode);
			usersGroupsCount--;
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
			updateIntersectingHeight();
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
			updateIntersectingHeight();
		}

		@Override
		public int compareTo(DistanceNode o) {
			DistanceNode a = this, b = o;
			final int heightsComparison = MathUtil.Compare(b.intersectingHeight, a.intersectingHeight);
			if (heightsComparison != 0) {
				return heightsComparison;
			} else {
				final int relativePosesComparison =
						MathUtil.Compare(a.to.posRelative - a.from.posRelative, b.to.posRelative - b.from.posRelative);
				if (relativePosesComparison != 0) {
					return relativePosesComparison;
 				} else {
					final int namesComparison = a.from.mainUser.name.compareTo(b.from.mainUser.name);
					return namesComparison;
				}
			}
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
			node.updateIntersectingHeight();
		}
		Collections.sort(groupsDistances);
	}

	void innerLog(StringBuilder b, TreeNode node, boolean isLeft) {
		if (node.left != null) {
			b.append('{');
			innerLog(b, node.left, true);
			b.append(", ");
			innerLog(b, node.right, false);
			b.append('}');
		} else {
			b.append(String.format("%s", node.mainUser.name));
		}
	}

	void logGroups() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%d = [", usersGroupsCount));

		TreeNode node = usersGroupsRoot;
		while (node != null) {
			innerLog(builder, node, false);
			if (node.next != null) {
				builder.append(" + ");
			}
			node = node.next;
		}

		builder.append("]");

		LogUtil.i(this, builder.toString());
	}

	boolean logged = false;
	private void logDistances() {
//		String str = String.format("%d = [ ", groupsDistances.size());
//		for (DistanceNode node : groupsDistances) {
//			str = String.format("%s(%.5f: %s--%s) ", str, node.distance, node.from.mainUser.name, node.to.mainUser.name);
//		}
//		LogUtil.i(this, "%s]", str);
//
//		str = String.format("^ %d = [ ", usersGroupsCount);
//		TreeNode node = usersGroupsRoot;
//		while (node != null) {
//			str = String.format("%s(%s: %.5f) ", str, node.mainUser.name, node.posAbsolute);
//			node = node.next;
//		}
//		LogUtil.log(this, "%s]", str);

		/*str = String.format("^ %d = [ ", groupsDistances.size() - 1);
		ListIterator<DistanceNode> iter = groupsDistances.listIterator();
		DistanceNode prev = iter.next(), cur;
		while (iter.hasNext()) {
			cur = iter.next();
			str = String.format("%s(%d: %s--%s <> %s--%s) ", str, prev.compareTo(cur),
					prev.from.mainUser.name, prev.to.mainUser.name,
					cur.from.mainUser.name, cur.to.mainUser.name);
			prev = cur;
		}
		LogUtil.log(this, "%s]", str);*/
	}

	private void composeByDistances(int height) {
		logged = false;
		if (!groupsDistances.isEmpty()) {
			DistanceNode first = groupsDistances.getFirst();
			while (first.intersectingHeight >= height) {
				if (!logged) {
					//logged = true;
					//logDistances();
				}
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
		//logDistances();
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

			LogUtil.err(this, "----------- = [ %d ---------------", height);
			logGroups();
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
			logGroups();

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
