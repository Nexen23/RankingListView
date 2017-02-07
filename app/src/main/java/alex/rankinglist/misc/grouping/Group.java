package alex.rankinglist.misc.grouping;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.Px;

import alex.rankinglist.misc.EventsSource;
import alex.rankinglist.util.MathUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;

public class Group extends EventsSource<Group.OnParentUpdate> implements Comparable<Group> {
	private final @Px int itemSize;
	private final float itemHalfSize;

	public @FloatRange(from=0, to=1) Float normalizedPos;
	private Float sizeToLeftBorder, sizeToRightBorder;

	private @IntRange(from=1) int itemsCount;
	private Group left, right;
	private User data;






	public Group prev, next; // for lists moving and updating



	public Group(@Px int itemSize, float size, Group left, Group right) {
		this.itemSize = itemSize;
		itemHalfSize = itemSize / 2.0f;
		this.left = left;
		this.right = right;
		this.data = left.data;
		itemsCount = left.itemsCount + right.itemsCount;

		setNormalizedPos((right.normalizedPos + left.normalizedPos) / 2);
		this.left.setParent(this);
		this.right.setParent(this);






		if (left.prev != null) {
			left.prev.next = this;
		}
		prev = left.prev;

		if (right.next != null) {
			right.next.prev = this;
		}
		next = right.next;
	}





	void setParent(final Group parent) {
		forEachListener(new Actor<OnParentUpdate>() {
			@Override
			public void actOn(OnParentUpdate onParentUpdate) {
				onParentUpdate.parentSetFor(Group.this, parent);
			}
		});
	}

	public void breakNode() {
		forEachListener(new Actor<OnParentUpdate>() {
			@Override
			public void actOn(OnParentUpdate onParentUpdate) {
				onParentUpdate.breakNode(Group.this);
			}
		});
	}

	public Group(int itemSize, Rank rank, User data) {
		this.data = data;
		this.itemSize = itemSize;
		itemHalfSize = itemSize / 2.0f;
		setNormalizedPos((rank.scoreMax - data.score) / (rank.scoreMax - rank.scoreMin));
		itemsCount = 1;
	}

	public void setNormalizedPos(float relative) {
		sizeToLeftBorder = itemHalfSize / relative;
		sizeToRightBorder = itemHalfSize / (1 - relative);
		normalizedPos = relative;
	}

	public boolean isLeftBorderWhen(Float height) {
		return height <= sizeToLeftBorder;
	}

	public boolean isRightBorderWhen(Float height) {
		return height <= sizeToRightBorder;
	}

	public Float getAbsolutePos(int size) {
		return calcAbsolutePos(size, normalizedPos);
	}

	public float getAvgScore(Rank rank) {
		return (rank.scoreMax - rank.scoreMin) * (1 - normalizedPos) + rank.scoreMin;
	}

	private float calcAbsolutePos(int size, float relativePos) {
		float posFromTopPx = size * relativePos;
		float centeredPosFromTopPx = posFromTopPx - itemHalfSize;
		return MathUtil.InRange(centeredPosFromTopPx, 0, size - itemSize);
	}

	public Group getLeft() {
		return left;
	}

	public Group getRight() {
		return right;
	}

	public boolean isLeaf() {
		return left == null && right == null;
	}

	public User getData() {
		return data;
	}

	public double getCenterPosPx(int size) {
		return getAbsolutePos(size) + itemHalfSize;
	}

	@Override
	public int compareTo(Group o) {
		return Float.compare(normalizedPos, o.normalizedPos);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Group group = (Group) o;

		if (itemsCount != group.itemsCount) return false;
		if (!normalizedPos.equals(group.normalizedPos)) return false;
		if (!left.equals(group.left)) return false;
		if (!right.equals(group.right)) return false;
		return data.equals(group.data);

	}

	@Override
	public int hashCode() {
		int result = normalizedPos.hashCode();
		result = 31 * result + left.hashCode();
		result = 31 * result + right.hashCode();
		result = 31 * result + itemsCount;
		result = 31 * result + data.hashCode();
		return result;
	}

	public int getItemsCount() {
		return itemsCount;
	}

	public interface OnParentUpdate {
		void parentSetFor(Group node, Group parent);
		void breakNode(Group node);
	}
}
