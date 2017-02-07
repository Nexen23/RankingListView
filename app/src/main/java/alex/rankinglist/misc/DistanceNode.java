package alex.rankinglist.misc;

import alex.rankinglist.util.MathUtil;

public class DistanceNode implements Comparable<DistanceNode>, TreeNode.OnParentUpdate {
	TreeNode from, to;
	public Float intersectingHeight;

	private final int userViewHeightPx;
	private final float userViewHeightHalfPx;

	public DistanceNode(int userViewHeightPx, TreeNode from, TreeNode to) {
		this.userViewHeightPx = userViewHeightPx;
		userViewHeightHalfPx = userViewHeightPx / 2.0f;

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
				intersectingHeight = (userViewHeightPx + userViewHeightHalfPx) / right.posRelative;
				break;
			}

			if (!leftIsBorder && rightIsBorder) {
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




	public TreeNode compose(int height) {
		from.listeners.remove(this);
		to.listeners.remove(this);
		return new TreeNode(userViewHeightPx, height, from, to);
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
