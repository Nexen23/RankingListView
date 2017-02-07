package alex.rankinglist.misc.grouping;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.Px;

import alex.rankinglist.misc.EventsSource;
import alex.rankinglist.util.MathUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;

public class GroupNode extends EventsSource<GroupNode.Events> implements Comparable<GroupNode> {
	private final @Px int itemSize;
	private final float itemHalfSize;

	private @FloatRange(from=0, to=1) Float normalizedPos;
	private Float spaceWhenIsLeftBorder, spaceWhenIsRightBorder, spaceWhenChildsMerged;

	private @IntRange(from=1) int itemsCount;
	private GroupNode leftNode, rightNode;
	private User data;

	GroupNode prev, next;

	public GroupNode(@Px int itemSize, float spaceWhenChildsMerged, GroupNode leftNode, GroupNode rightNode) {
		this.itemSize = itemSize;
		itemHalfSize = itemSize / 2.0f;
		this.spaceWhenChildsMerged = spaceWhenChildsMerged;
		this.leftNode = leftNode;
		this.rightNode = rightNode;
		this.data = leftNode.data;
		itemsCount = leftNode.itemsCount + rightNode.itemsCount;

		setNormalizedPos((rightNode.normalizedPos + leftNode.normalizedPos) / 2);
		this.leftNode.notifyParentSet(this);
		this.rightNode.notifyParentSet(this);
	}

	public GroupNode(int itemSize, Rank rank, User data) {
		this.data = data;
		this.itemSize = itemSize;
		itemHalfSize = itemSize / 2.0f;
		setNormalizedPos((rank.scoreMax - data.score) / (rank.scoreMax - rank.scoreMin));
		itemsCount = 1;
	}

	public void setNormalizedPos(float normalizedPos) {
		spaceWhenIsLeftBorder = itemHalfSize / normalizedPos;
		spaceWhenIsRightBorder = itemHalfSize / (1 - normalizedPos);
		this.normalizedPos = normalizedPos;
	}

	public boolean isLeftBorderWhen(Float space) {
		return space <= spaceWhenIsLeftBorder;
	}

	public boolean isRightBorderWhen(Float space) {
		return space <= spaceWhenIsRightBorder;
	}

	public boolean areChildsIntersected(int space) {
		return space <= spaceWhenChildsMerged;
	}

	public Float getAbsolutePos(int space) {
		float posFromTopPx = space * normalizedPos;
		float centeredPosFromTopPx = posFromTopPx - itemHalfSize;
		return MathUtil.InRange(centeredPosFromTopPx, 0, space - itemSize);
	}

	public float getAvgScore(Rank rank) {
		return (rank.scoreMax - rank.scoreMin) * (1 - normalizedPos) + rank.scoreMin;
	}

	public Float getNormalizedPos() {
		return normalizedPos;
	}

	public int getItemsCount() {
		return itemsCount;
	}

	public GroupNode getLeftNode() {
		return leftNode;
	}

	public GroupNode getRightNode() {
		return rightNode;
	}

	public boolean isLeaf() {
		return leftNode == null && rightNode == null;
	}

	public User getData() {
		return data;
	}

	public void notifyParentSet(final GroupNode parent) {
		forEachListener(onParentUpdate -> onParentUpdate.onParentSetFor(GroupNode.this, parent));
	}

	public void notifyNodeBroken() {
		forEachListener(onParentUpdate -> onParentUpdate.onNodeBroken(GroupNode.this));
	}

	@Override
	public int compareTo(GroupNode o) {
		return Float.compare(normalizedPos, o.normalizedPos);
	}

	public interface Events {
		void onParentSetFor(GroupNode node, GroupNode parent);
		void onNodeBroken(GroupNode node);
	}
}
