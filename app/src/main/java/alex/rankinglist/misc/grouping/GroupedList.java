package alex.rankinglist.misc.grouping;


import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Px;

import junit.framework.Assert;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import alex.rankinglist.misc.EventsSource;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;

public class GroupedList extends EventsSource<GroupedList.EventsListener> {
	private final @Px int itemSize;
	private Integer size;

	public Group groupsRoot;
	public int groupsCount = 0;

	private LinkedList<GroupCandidates> groupsCandidates = new LinkedList<>();
	private Stack<Group> groupsHistory = new Stack<>();

	public GroupedList(@Px int itemSize) {
		this.itemSize = itemSize;
	}

	public void clearData() {
		size = null;
		groupsCandidates.clear();
		groupsHistory.clear();
		groupsCount = 0;
		groupsRoot = null;
		clearListeners();
	}

	public void setData(@NonNull Rank rank, @NonNull List<User> items) {
		if (groupsCount != 0) {
			clearData();
		}

		if (!items.isEmpty()) {
			LinkedList<Group> usersGroups = new LinkedList<>();
			for (User item : items) {
				usersGroups.add(new Group(itemSize, rank, item));
			}
			Collections.sort(usersGroups);
			groupsCount = usersGroups.size();

			groupsCandidates.clear();
			ListIterator<Group> iter = usersGroups.listIterator();
			Group prevNode = iter.next(), curNode;
			groupsRoot = prevNode;
			while (iter.hasNext()) {
				curNode = iter.next();
				prevNode.next = curNode;
				curNode.prev = prevNode;
				groupsCandidates.add(new GroupCandidates(itemSize, prevNode, curNode));
				prevNode = curNode;
			}
			Collections.sort(groupsCandidates);
		}
	}

	public boolean setSize(@IntRange(from=0) int newSize) {
		if (newSize < itemSize) {
			final String message = String.format("Size(%d) must be greater than view size(%d)", newSize, itemSize);
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
		composeGroups();
	}

	protected void doBreaking() {
		breakGroups();
	}

	protected void updateGroupsPositions() {
		Group node = groupsRoot;
		int i = 0;
		while (node != null) {
			node = node.next;
			++i;
		}
		Assert.assertSame(groupsCount, i);
	}

	private void composeGroups() {
		if (!groupsCandidates.isEmpty()) {
			GroupCandidates first = groupsCandidates.getFirst();
			while (first.getIntersectingSize() >= size) {
				Group newNode = first.compose();
				if (newNode.getLeft() == groupsRoot) {
					groupsRoot = newNode;
				}
				groupsHistory.push(newNode);
				groupsCount--;

				groupsCandidates.removeFirst();
				Collections.sort(groupsCandidates);

				final Group constNode = newNode;
				forEachListener(new Actor<EventsListener>() {
					@Override
					public void actOn(EventsListener listener) {
						listener.onGroup(constNode.getLeft(), constNode.getRight(), constNode);
					}
				});

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
			Group node = groupsHistory.peek();
			Float rightPos = node.getRight().getAbsolutePos(size);
			Float leftPos = node.getLeft().getAbsolutePos(size);
			if (rightPos >= (leftPos + itemSize)) {
				groupsHistory.pop();

				node.breakNode();

				if (node.prev != null) {
					node.prev.next = node.getLeft();
				}
				if (node.next != null) {
					node.next.prev = node.getRight();
				}
				if (node == groupsRoot) {
					groupsRoot = node.getLeft();
				}

				groupsCandidates.add(new GroupCandidates(itemSize, node.getLeft(), node.getRight()));
				++groupsCount;

				final Group constNode = node;
				forEachListener(new Actor<EventsListener>() {
					@Override
					public void actOn(EventsListener listener) {
						listener.onBreak(constNode, constNode.getLeft(), constNode.getRight());
					}
				});
			} else {
				break;
			}
			Collections.sort(groupsCandidates);
		}
	}

	public Integer getSize() {
		return size;
	}

	public java.util.Iterator<Group> getGroupsIterator() {
		return new Iterator(groupsRoot);
	}

	public int getGroupsCount() {
		return groupsCount;
	}

	void innerLog(StringBuilder b, Group node, boolean isLeft) {
		if (node.getLeft() != null) {
			b.append('{');
			innerLog(b, node.getLeft(), true);
			b.append(", ");
			innerLog(b, node.getRight(), false);
			b.append('}');
		} else {
			b.append(String.format("%s", node.getData().name));
		}
	}

	public String toTreeString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%d = [", groupsCount));

		Group node = groupsRoot;
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


	private class Iterator implements java.util.Iterator<Group> {
		private Group currentNode;

		public Iterator(Group rootNode) {
			this.currentNode = rootNode;
		}

		@Override
		public boolean hasNext() {
			return currentNode != null;
		}

		@Override
		public Group next() {
			Group prevNode = currentNode;
			currentNode = currentNode.next;
			return prevNode;
		}
	}

	public interface EventsListener {
		void onGroup(Group a, Group b, Group composedGroup);
		void onBreak(Group removedGroup, Group a, Group b);
	}
}
