package alex.rankinglist.misc.grouping;

import android.support.annotation.Px;

import alex.rankinglist.util.MathUtil;

public class GroupCandidates implements Comparable<GroupCandidates>, Group.OnParentUpdate {
	private final @Px int itemSize;
	private final float itemHalfSize;

	private Group left, right;
	private float intersectingSize;

	public GroupCandidates(@Px int itemSize, Group left, Group right) {
		this.itemSize = itemSize;
		itemHalfSize = itemSize / 2.0f;

		this.left = left;
		this.right = right;
		left.addListener(this);
		right.addListener(this);
		updateIntersectingSize();
	}

	public float getIntersectingSize() {
		return intersectingSize;
	}

	public Group compose() {
		left.removeListener(this);
		right.removeListener(this);
		return new Group(itemSize, intersectingSize, left, right);
	}

	@Override
	public void parentSetFor(Group node, Group parent) {
		node.removeListener(this);
		if (node == left) {
			left = parent;
			left.addListener(this);
		} else {
			right = parent;
			right.addListener(this);
		}
		updateIntersectingSize();
	}

	@Override
	public void breakNode(Group node) {
		node.removeListener(this);
		if (node == left) {
			left = node.getRight();
			left.addListener(this);
		} else {
			right = node.getLeft();
			right.addListener(this);
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
					MathUtil.Compare(a.right.normalizedPos - a.left.normalizedPos, b.right.normalizedPos - b.left.normalizedPos);
			if (relativePosesComparison != 0) {
				return relativePosesComparison;
			 } else {
				final int namesComparison = a.left.getData().name.compareTo(b.left.getData().name);
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
			intersectingSize = (itemSize + itemHalfSize) / right.normalizedPos;
			return;
		}

		if (!leftIsBorder && rightIsBorder) {
			intersectingSize = (itemSize + itemHalfSize) / (1 - left.normalizedPos);
			return;
		}

		if (leftIsBorder && rightIsBorder) {
			intersectingSize = ((float) (itemSize + itemSize));
			return;
		}

		throw new IllegalStateException();
	}

	private Float calcIntersectingSizeWithNoBound() {
		return itemSize / (right.normalizedPos - left.normalizedPos);
	}
}
