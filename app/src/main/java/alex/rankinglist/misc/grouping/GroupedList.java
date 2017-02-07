package alex.rankinglist.misc.grouping;


import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Px;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import alex.rankinglist.misc.EventsSource;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;

public class GroupedList extends EventsSource<GroupedList.EventsListener> implements Iterable<Group> {
	private final @Px int itemSize;
	private Integer space;

	public GroupsTree groupsTree = new GroupsTree();
	private LinkedList<GroupCandidates> groupsCandidates = new LinkedList<>();
	private Stack<Group> groupsHistory = new Stack<>();

	public GroupedList(@Px int itemSize) {
		this.itemSize = itemSize;
	}

	public void clearData() {
		space = null;
		groupsCandidates.clear();
		groupsHistory.clear();
		groupsTree.clear();
		clearListeners();
	}

	public void setData(@NonNull Rank rank, @NonNull List<User> items) {
		if (space != null) {
			clearData();
		}

		if (!items.isEmpty()) {
			LinkedList<Group> usersGroups = new LinkedList<>();
			for (User item : items) {
				usersGroups.add(new Group(itemSize, rank, item));
			}
			Collections.sort(usersGroups);
			groupsTree.rootsCount = usersGroups.size();

			groupsCandidates.clear();
			ListIterator<Group> iter = usersGroups.listIterator();
			Group prevNode = iter.next(), curNode;
			groupsTree.root = prevNode;
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

	public boolean setSpace(@IntRange(from=0) int newSpace) {
		if (newSpace < itemSize) {
			final String message = String.format("newSpace(%d) must be greater than view itemSize(%d)", newSpace, itemSize);
			throw new IllegalArgumentException(message);
		}

		if (!groupsTree.isEmpty() && (space == null || newSpace != space)) {
			LogUtil.log(this, "setSpace(oldSpace=%d, newSpace=%d)", space, newSpace);
			Integer oldSpace = space;
			this.space = newSpace;

			if (oldSpace == null || oldSpace > newSpace) {
				composeGroups();
			} else {
				breakGroups();
			}
			return true;
		}
		return false;
	}

	private void composeGroups() {
		if (!groupsCandidates.isEmpty()) {
			GroupCandidates first = groupsCandidates.getFirst();
			while (first.getIntersectingSize() >= space) {
				Group newNode = first.compose();
				if (newNode.getLeftNode() == groupsTree.root) {
					groupsTree.root = newNode;
				}
				groupsHistory.push(newNode);
				groupsTree.rootsCount--;

				groupsCandidates.removeFirst();
				Collections.sort(groupsCandidates);

				final Group constNode = newNode;
				forEachListener(new Actor<EventsListener>() {
					@Override
					public void actOn(EventsListener listener) {
						listener.onGroup(constNode.getLeftNode(), constNode.getRightNode(), constNode);
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
			Float rightPos = node.getRightNode().getAbsolutePos(space);
			Float leftPos = node.getLeftNode().getAbsolutePos(space);
			if (rightPos >= (leftPos + itemSize)) {
				groupsHistory.pop();

				node.breakNode();

				if (node.prev != null) {
					node.prev.next = node.getLeftNode();
				}
				if (node.next != null) {
					node.next.prev = node.getRightNode();
				}
				if (node == groupsTree.root) {
					groupsTree.root = node.getLeftNode();
				}

				groupsCandidates.add(new GroupCandidates(itemSize, node.getLeftNode(), node.getRightNode()));
				++groupsTree.rootsCount;

				final Group constNode = node;
				forEachListener(new Actor<EventsListener>() {
					@Override
					public void actOn(EventsListener listener) {
						listener.onBreak(constNode, constNode.getLeftNode(), constNode.getRightNode());
					}
				});
			} else {
				break;
			}
			Collections.sort(groupsCandidates);
		}
	}

	public Integer getSpace() {
		return space;
	}

	public int getGroupsCount() {
		return groupsTree.getRootsCount();
	}







	@Override
	public java.util.Iterator<Group> iterator() {
		return groupsTree.getRootsIterator();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%d = [", groupsTree.rootsCount));

		String prefix = "";
		final Iterator<Group> iterator = groupsTree.getRootsIterator();
		while (iterator.hasNext()) {
			builder.append(prefix);
			iterateTree(builder, iterator.next());
			prefix = " + ";
		}

		builder.append("]");

		return builder.toString();
	}

	private void iterateTree(final StringBuilder builder, Group group) {
		if (group.getLeftNode() != null) {
			builder.append('{');
			iterateTree(builder, group.getLeftNode());
			builder.append(", ");
			iterateTree(builder, group.getRightNode());
			builder.append('}');
		} else {
			builder.append(group.getData().name);
		}
	}

	public interface EventsListener {
		void onGroup(Group a, Group b, Group composedGroup);
		void onBreak(Group removedGroup, Group a, Group b);
	}
}
