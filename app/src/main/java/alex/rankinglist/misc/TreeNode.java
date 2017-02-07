package alex.rankinglist.misc;

import java.util.LinkedList;

import alex.rankinglist.util.MathUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;

public class TreeNode implements Comparable<TreeNode> {
	public Float posRelative;
	public Float posAbsolute;
	public TreeNode left, right;
	public int groupSize;
	public Float heightToLeftBorder, heightToRightBorder;

	public User mainUser;


	public TreeNode prev, next; // for lists moving and updating
	public LinkedList<OnParentUpdate> listeners = new LinkedList<>();

	private final int userViewHeightPx;
	private final float userViewHeightHalfPx;

	public TreeNode(int userViewHeightPx, final int height, final TreeNode left, final TreeNode right) {
		this.userViewHeightPx = userViewHeightPx;
		userViewHeightHalfPx = userViewHeightPx / 2.0f;
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

		mainUser = left.mainUser;
		groupSize = left.groupSize + right.groupSize;

		final boolean leftIsBorder = left.isLeftBorder((float) height),
				rightIsBorder = right.isRightBorder((float) height);
		// FIXME: 04.02.2017 relativePoses can be equal
		final int wouldIntersectWhenHeight = (int) (userViewHeightPx / (right.posRelative - left.posRelative));

		while (true) {
			if (!leftIsBorder && !rightIsBorder) {
				intersectNoBorders(height);
				break;
			}

			final boolean intersectedBeforeLeftBecameBorder = wouldIntersectWhenHeight >= left.heightToLeftBorder,
					intersectedBeforeRightBecameBorder = wouldIntersectWhenHeight >= right.heightToRightBorder;

			if (leftIsBorder && !rightIsBorder) {
				if (intersectedBeforeLeftBecameBorder) { // intersect before left became border
					intersectNoBorders(height);
				} else { // intersect with left border
					intersectWithLeftBorder();
				}
				break;
			}

			if (!leftIsBorder && rightIsBorder) {
				if (intersectedBeforeRightBecameBorder) { // intersect before right became border
					intersectNoBorders(height);
				} else { // intersect with right border
					intersectWithRightBorder();
				}
				break;
			}

			if (leftIsBorder && rightIsBorder) {
				if (intersectedBeforeLeftBecameBorder) {
					if (intersectedBeforeRightBecameBorder) {
						intersectNoBorders(height);
					} else {
						intersectWithRightBorder();
					}
				} else {
					if (intersectedBeforeRightBecameBorder) {
						intersectWithLeftBorder();
					} else { // both are borders
						intersectBothBorders();
					}
				}
				break;
			}
			throw new IllegalStateException();
		}

		updateAbsolutePos(height);

		left.setParent(this);
		right.setParent(this);
	}

	void intersectNoBorders(int height) {
		//final float prev = (left.posAbsolute + right.posAbsolute + userViewHeightPx) / (2.0f * height);
		float now = (right.posRelative + left.posRelative) / 2;

		setRelativePos(now);

		/*if (!MathUtil.IsEqual(prev, now)) {
			LogUtil.err(this, "%.4f(real) != %.4f(best)", prev, now);
		}*/
		//Assert.assertEquals(posRelative, test, MathUtil.EPSILON);
	}

	void intersectWithLeftBorder() {
		// FIXME: 04.02.2017 relativePos can be 0
		float intersectingHeight = (userViewHeightPx + userViewHeightHalfPx) / right.posRelative;
		setRelativePos(userViewHeightPx / intersectingHeight);
	}

	void intersectWithRightBorder() {
		// FIXME: 04.02.2017 relativePos can be 1
		float intersectingHeight = (userViewHeightPx + userViewHeightHalfPx) / (1 - left.posRelative);
		setRelativePos((intersectingHeight - userViewHeightPx) / intersectingHeight);
	}

	void intersectBothBorders() {
		setRelativePos(0.5f);
	}







	void setParent(TreeNode parent) {
		LinkedList<OnParentUpdate> listenersClone = new LinkedList<>(listeners);
		for (OnParentUpdate listener : listenersClone) {
			listener.parentSetFor(this, parent);
		}
	}

	public void breakNode() {
		LinkedList<OnParentUpdate> listenersClone = new LinkedList<>(listeners);
		for (OnParentUpdate listener : listenersClone) {
			listener.breakNode(this);
		}
	}

	public TreeNode(int userViewHeightPx, Rank rank, User mainUser) {
		this.mainUser = mainUser;
		this.userViewHeightPx = userViewHeightPx;
		userViewHeightHalfPx = userViewHeightPx / 2.0f;
		setRelativePos((rank.scoreMax - mainUser.score) / (rank.scoreMax - rank.scoreMin));
		groupSize = 1;
	}

	public void setRelativePos(float relative) {
		heightToLeftBorder = userViewHeightHalfPx / relative;
		heightToRightBorder = userViewHeightHalfPx / (1 - relative);
		posRelative = relative;
	}

	public boolean isLeftBorder(Float height) {
		return height <= heightToLeftBorder;
	}

	public boolean isRightBorder(Float height) {
		return height <= heightToRightBorder;
	}

	public void updateAbsolutePos(int height) {
		posAbsolute = calcAbsolutePos(height, posRelative);
	}

	public Float calcAndGetAbsolutePos(int height) {
		return posAbsolute = calcAbsolutePos(height, posRelative);
	}

	private float calcAbsolutePos(int height, float relativePos) {
		float posFromTopPx = height * relativePos;
		float centeredPosFromTopPx = posFromTopPx - userViewHeightHalfPx;
		return MathUtil.InRange(centeredPosFromTopPx, 0, height - userViewHeightPx);
	}

	public boolean isLeaf() {
		return left == null && right == null;
	}

	public double getCenterPosPx() {
		return posAbsolute + userViewHeightHalfPx;
	}

	@Override
	public int compareTo(TreeNode o) {
		return Float.compare(posRelative, o.posRelative);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TreeNode treeNode = (TreeNode) o;

		if (groupSize != treeNode.groupSize) return false;
		if (!posRelative.equals(treeNode.posRelative)) return false;
		if (!left.equals(treeNode.left)) return false;
		if (!right.equals(treeNode.right)) return false;
		return mainUser.equals(treeNode.mainUser);

	}

	@Override
	public int hashCode() {
		int result = posRelative.hashCode();
		result = 31 * result + left.hashCode();
		result = 31 * result + right.hashCode();
		result = 31 * result + groupSize;
		result = 31 * result + mainUser.hashCode();
		return result;
	}

	public interface OnParentUpdate {
		void parentSetFor(TreeNode node, TreeNode parent);
		void breakNode(TreeNode node);
	}
}
