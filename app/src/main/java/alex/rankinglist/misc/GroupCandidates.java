package alex.rankinglist.misc;

import android.support.annotation.Px;

import alex.rankinglist.util.MathUtil;

public class GroupCandidates implements Comparable<GroupCandidates>, TreeNode.OnParentUpdate {
	private final @Px int itemSize;
	private final float itemHalfSize;

	private TreeNode left, right;
	private Float intersectingSize;

	public GroupCandidates(@Px int itemSize, TreeNode left, TreeNode right) {
		this.itemSize = itemSize;
		itemHalfSize = itemSize / 2.0f;

		this.left = left;
		this.right = right;
		left.listeners.add(this);
		right.listeners.add(this);
		updateIntersectingSize();
	}

	public Float getIntersectingSize() {
		return intersectingSize;
	}

	public TreeNode compose(int height) {
		left.listeners.remove(this);
		right.listeners.remove(this);
		return new TreeNode(itemSize, height, left, right);
	}

	@Override
	public void parentSetFor(TreeNode node, TreeNode parent) {
		node.listeners.remove(this);
		if (node == left) {
			left = parent;
			left.listeners.add(this);
		} else {
			right = parent;
			right.listeners.add(this);
		}
		updateIntersectingSize();
	}

	@Override
	public void breakNode(TreeNode node) {
		node.listeners.remove(this);
		if (node == left) {
			left = node.right;
			left.listeners.add(this);
		} else {
			right = node.left;
			right.listeners.add(this);
		}
		updateIntersectingSize();
	}

	@Override
	public int compareTo(GroupCandidates obj) {
		GroupCandidates a = this, b = obj;
		final int intersectingSizeComparison = MathUtil.Compare(b.intersectingSize, a.intersectingSize);
		if (intersectingSizeComparison != 0) {
			return intersectingSizeComparison;
		} else {
			final int relativePosesComparison =
					MathUtil.Compare(a.right.posRelative - a.left.posRelative, b.right.posRelative - b.left.posRelative);
			if (relativePosesComparison != 0) {
				return relativePosesComparison;
			 } else {
				final int namesComparison = a.left.mainUser.name.compareTo(b.left.mainUser.name);
				return namesComparison;
			}
		}
	}

	private void updateIntersectingSize() {
		final Float height = calcIntersectingSizeWithNoBound();
		final boolean leftIsBorder = left.isLeftBorderWhen(height), rightIsBorder = right.isRightBorderWhen(height);

		if (!leftIsBorder && !rightIsBorder) { // general intersect of non borders
			intersectingSize = height;
			return;
		}

		if (leftIsBorder && !rightIsBorder) {
			intersectingSize = (itemSize + itemHalfSize) / right.posRelative;
			return;
		}

		if (!leftIsBorder && rightIsBorder) {
			intersectingSize = (itemSize + itemHalfSize) / (1 - left.posRelative);
			return;
		}

		if (leftIsBorder && rightIsBorder) {
			intersectingSize = ((float) (itemSize + itemSize));
			return;
		}

		throw new IllegalStateException();
	}

	private Float calcIntersectingSizeWithNoBound() {
		return itemSize / (right.posRelative - left.posRelative);
	}
}
