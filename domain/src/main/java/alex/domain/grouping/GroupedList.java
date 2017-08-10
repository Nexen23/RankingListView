package alex.domain.grouping;


import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Px;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import alex.domain.misc.EventsSource;
import alex.domain.util.LogUtil;
import alex.data.model.Rank;
import alex.data.model.User;

public class GroupedList extends EventsSource<GroupedList.EventsListener> implements Iterable<GroupNode> {
	private final @Px int itemSize;
	private @IntRange(from=0) Integer space;

	private GroupsTree groupsTree = new GroupsTree();
	private LinkedList<GroupCandidates> groupsCandidates = new LinkedList<>();
	private Stack<GroupNode> groupsHistory = new Stack<>();

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
			LinkedList<GroupNode> usersGroups = new LinkedList<>();
			for (User item : items) {
				usersGroups.add(new GroupNode(itemSize, rank, item));
			}
			Collections.sort(usersGroups);
			groupsTree.addGroups(usersGroups);

			groupsCandidates.clear();
			ListIterator<GroupNode> iter = usersGroups.listIterator();
			GroupNode prevNode = iter.next(), curNode;
			while (iter.hasNext()) {
				curNode = iter.next();
				groupsCandidates.add(new GroupCandidates(itemSize, prevNode, curNode));
				prevNode = curNode;
			}
			Collections.sort(groupsCandidates);
		}
	}

	/**
	 * Should be called after {@link #setData setData()}
	 */
	public boolean setSpace(@IntRange(from=0) int newSpace) {
		if (newSpace < itemSize) {
			final String message = String.format("Space(%d) must be greater than view itemSize(%d)", newSpace, itemSize);
			throw new IllegalArgumentException(message);
		}

		if (!groupsTree.isEmpty() && (space == null || newSpace != space)) {
			Integer oldSpace = space;
			this.space = newSpace;

			LogUtil.d(this, "setSpace(oldSpace=%d, newSpace=%d)", oldSpace, newSpace);
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
		while (!groupsCandidates.isEmpty() && groupsCandidates.getFirst().isComposable(space)) {
			final GroupNode mergedNode = groupsCandidates.pollFirst().compose();
			Collections.sort(groupsCandidates);

			groupsTree.updateMergedNodes(mergedNode);
			groupsHistory.push(mergedNode);

			forEachListener(listener -> listener.onGroup(mergedNode, mergedNode.getLeftNode(), mergedNode.getRightNode()));
		}
	}

	private void breakGroups() {
		while (!groupsHistory.isEmpty() && !groupsHistory.peek().areChildsIntersected(space)) {
			final GroupNode brokenNode = groupsHistory.pop();
			brokenNode.notifyNodeBroken();

			groupsTree.updateBrokenNode(brokenNode);
			groupsCandidates.add(new GroupCandidates(itemSize, brokenNode.getLeftNode(), brokenNode.getRightNode()));

			forEachListener(listener -> listener.onBreak(brokenNode, brokenNode.getLeftNode(), brokenNode.getRightNode()));
		}

		if (!groupsCandidates.isEmpty()) {
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
	public java.util.Iterator<GroupNode> iterator() {
		return groupsTree.getRootsIterator();
	}

	@Override
	public String toString() {
		return groupsTree.toString();
	}

	public interface EventsListener {
		void onGroup(GroupNode composedGroup, GroupNode a, GroupNode b);
		void onBreak(GroupNode removedGroup, GroupNode a, GroupNode b);
	}
}
