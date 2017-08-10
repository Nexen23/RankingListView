package alex.domain.grouping;

import android.support.annotation.NonNull;
import android.support.annotation.Px;

import alex.domain.util.MathUtil;

class GroupCandidates implements Comparable<GroupCandidates>, GroupNode.Events {
	private final @Px int itemSize;
	private final float itemHalfSize;

	private GroupNode left, right;
	private float intersectingSize;

	public GroupCandidates(@Px int itemSize, GroupNode left, GroupNode right) {
		this.itemSize = itemSize;
		itemHalfSize = itemSize / 2.0f;

		this.left = left;
		this.right = right;
		left.addListener(this);
		right.addListener(this);
		updateIntersectingSize();
	}

	public boolean isComposable(int space) {
		return intersectingSize >= space;
	}

	public GroupNode compose() {
		left.removeListener(this);
		right.removeListener(this);
		return new GroupNode(itemSize, intersectingSize, left, right);
	}

	@Override
	public void onParentSetFor(GroupNode node, GroupNode parent) {
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
	public void onNodeBroken(GroupNode node) {
		node.removeListener(this);
		if (node == left) {
			left = node.getRightNode();
			left.addListener(this);
		} else {
			right = node.getLeftNode();
			right.addListener(this);
		}
		updateIntersectingSize();
	}

	@Override
	public int compareTo(@NonNull GroupCandidates obj) {
		GroupCandidates a = this, b = obj;
		final int intersectingSizeComparison = MathUtil.Compare(b.intersectingSize, a.intersectingSize);
		if (intersectingSizeComparison != 0) {
			return intersectingSizeComparison;
		} else {
			final int relativePosesComparison =
					MathUtil.Compare(a.right.getNormalizedPos() - a.left.getNormalizedPos(),
							b.right.getNormalizedPos() - b.left.getNormalizedPos());
			if (relativePosesComparison != 0) {
				return relativePosesComparison;
			 } else {
				final int namesComparison = a.left.getData().name.compareTo(b.left.getData().name);
				return namesComparison;
			}
		}
	}

	private void updateIntersectingSize() {
		final Float space = calcIntersectingSpaceWithNoBound();
		final boolean leftIsBorder = left.isLeftBorderWhen(space), rightIsBorder = right.isRightBorderWhen(space);

		if (!leftIsBorder && !rightIsBorder) {
			intersectingSize = space;
			return;
		}

		if (leftIsBorder && !rightIsBorder) {
			intersectingSize = (itemSize + itemHalfSize) / right.getNormalizedPos();
			return;
		}

		if (!leftIsBorder && rightIsBorder) {
			intersectingSize = (itemSize + itemHalfSize) / (1 - left.getNormalizedPos());
			return;
		}

		if (leftIsBorder && rightIsBorder) {
			intersectingSize = ((float) (itemSize + itemSize));
			return;
		}

		throw new IllegalStateException();
	}

	private Float calcIntersectingSpaceWithNoBound() {
		return itemSize / (right.getNormalizedPos() - left.getNormalizedPos());
	}
}
