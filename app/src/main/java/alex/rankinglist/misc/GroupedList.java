package alex.rankinglist.misc;


import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Px;

import junit.framework.Assert;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import alex.rankinglist.util.LogUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;

public class GroupedList {
	private final @Px int itemSizePx;
	private final float itemHalfSizePx;
	private Integer size;

	public TreeNode groupsRoot;
	public int groupsCount = 0;

	private LinkedList<GroupCandidate> groupsCandidates = new LinkedList<>();
	private Stack<TreeNode> groupsHistory = new Stack<>();

	public GroupedList(@Px int itemSizePx) {
		this.itemSizePx = itemSizePx;
		itemHalfSizePx = itemSizePx / 2.0f;
	}

	public void clearData() {
		size = null;
		groupsCandidates = new LinkedList<>();
		groupsHistory = new Stack<>();
		groupsCount = 0;
		groupsRoot = null;
		listeners.clear();
	}

	public void setData(@NonNull Rank rank, @NonNull List<User> items) {
		if (groupsCount != 0) {
			clearData();
		}

		if (!items.isEmpty()) {
			LinkedList<TreeNode> usersGroups = new LinkedList<>();
			for (User item : items) {
				usersGroups.add(new TreeNode(itemSizePx, rank, item));
			}
			Collections.sort(usersGroups);
			groupsCount = usersGroups.size();

			groupsCandidates.clear();
			ListIterator<TreeNode> iter = usersGroups.listIterator();
			TreeNode prevNode = iter.next(), curNode;
			groupsRoot = prevNode;
			while (iter.hasNext()) {
				curNode = iter.next();
				prevNode.next = curNode;
				curNode.prev = prevNode;
				groupsCandidates.add(new GroupCandidate(itemSizePx, prevNode, curNode));
				prevNode = curNode;
			}
		}
	}

	public boolean setSize(@IntRange(from=0) int newSize) {
		if (newSize < itemSizePx) {
			final String message = String.format("Size(%d) must be greater than view size(%d)", newSize, itemSizePx);
			throw new IllegalArgumentException(message);
		}

		if (groupsCount != 0 && (size == null || newSize != size)) {
			LogUtil.log(this, "setSize(oldSize=%d, newSize=%d)", size, newSize);
			Integer oldSize = size;
			this.size = newSize;

			if (oldSize == null || oldSize > newSize) {
				doComposing();
			} else {
				doBreaking();
			}
			return true;
		}
		return false;
	}

	protected void doComposing() {
		updateGroupsPositions();
		updateGroupsCandidates();
		composeGroups();
	}

	protected void doBreaking() {
		updateGroupsPositions();
		breakGroups();
	}

	protected void updateGroupsPositions() {
		TreeNode node = groupsRoot;
		int i = 0;
		while (node != null) {
			node.updateAbsolutePos(size);
			node = node.next;
			++i;
		}
		Assert.assertSame(groupsCount, i);
	}

	private void updateGroupsCandidates() {
		for (GroupCandidate node : groupsCandidates) {
			node.updateIntersectingHeight();
		}
		Collections.sort(groupsCandidates);
	}

	private void composeGroups() {
		if (!groupsCandidates.isEmpty()) {
			GroupCandidate first = groupsCandidates.getFirst();
			while (first.intersectingHeight >= size) {
				TreeNode newNode = first.compose(size);
				if (newNode.left == groupsRoot) {
					groupsRoot = newNode;
				}
				groupsHistory.push(newNode);
				groupsCount--;

				groupsCandidates.removeFirst();
				Collections.sort(groupsCandidates);

				for (EventsListener listener : listeners) {
					listener.onGroup(newNode.left, newNode.right, newNode);
				}

				if (groupsCandidates.isEmpty()) {
					break;
				} else {
					first = groupsCandidates.getFirst();
				}
			}
		}
	}

	private void breakGroups() {
		while (!groupsHistory.isEmpty()) {
			TreeNode node = groupsHistory.peek();
			Float rightPos = node.right.calcAndGetAbsolutePos(size);
			Float leftPos = node.left.calcAndGetAbsolutePos(size);
			if (rightPos >= (leftPos + itemSizePx)) {
				groupsHistory.pop();

				node.breakNode();

				if (node.prev != null) {
					node.prev.next = node.left;
				}
				if (node.next != null) {
					node.next.prev = node.right;
				}
				if (node == groupsRoot) {
					groupsRoot = node.left;
				}

				groupsCandidates.add(new GroupCandidate(itemSizePx, node.left, node.right));
				++groupsCount;

				for (EventsListener listener : listeners) {
					listener.onBreak(node, node.left, node.right);
				}
			} else {
				break;
			}
		}
	}



	public java.util.Iterator<TreeNode> getGroupsIterator() {
		return new Iterator(groupsRoot);
	}

	public int getGroupsCount() {
		return groupsCount;
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

	public String toTreeString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%d = [", groupsCount));

		TreeNode node = groupsRoot;
		while (node != null) {
			innerLog(builder, node, false);
			if (node.next != null) {
				builder.append(" + ");
			}
			node = node.next;
		}

		builder.append("]");

		return builder.toString();
	}

	private List<EventsListener> listeners = new LinkedList<>();

	public void addListener(EventsListener listener) {
		listeners.add(listener);
	}

	public void removeListener(EventsListener listener) {
		listeners.remove(listener);
	}


	/*public class Group<T> {

	}

	private class TwoWayNode<T> {
		private TwoWayNode<T> prev, next;
		private T data;
	}*/

	private class Iterator implements java.util.Iterator<TreeNode> {
		private TreeNode currentNode;

		public Iterator(TreeNode rootNode) {
			this.currentNode = rootNode;
		}

		@Override
		public boolean hasNext() {
			return currentNode != null;
		}

		@Override
		public TreeNode next() {
			TreeNode prevNode = currentNode;
			currentNode = currentNode.next;
			return prevNode;
		}
	}

	public interface EventsListener {
		void onGroup(TreeNode a, TreeNode b, TreeNode composedGroup);
		void onBreak(TreeNode removedGroup, TreeNode a, TreeNode b);
	}
}
