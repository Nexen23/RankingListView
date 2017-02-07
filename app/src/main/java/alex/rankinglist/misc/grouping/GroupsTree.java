package alex.rankinglist.misc.grouping;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Iterator;

class GroupsTree {
	private int rootsCount = 0;
	private Group root;

	public GroupsTree() {
	}

	public void clear() {
		root = null;
		rootsCount = 0;
	}

	public boolean isEmpty() {
		return rootsCount == 0;
	}

	public int getRootsCount() {
		return rootsCount;
	}

	public void addRoot(@NonNull Group groupRoot) {
		addRootAfter(null, groupRoot);
	}

	public void addRootAfter(@Nullable Group prevGroupRoot, @NonNull Group groupRootToAdd) {
		if (isEmpty()) {
			root = groupRootToAdd;
		}

		if (prevGroupRoot != null) {
			prevGroupRoot.next = groupRootToAdd;
		}
		groupRootToAdd.prev = prevGroupRoot;

		++rootsCount;
	}

	public java.util.Iterator<Group> getRootsIterator() {
		return new RootsIterator(root);
	}

	public void composeRoots(Group left, Group right, Group newNode) {
		if (left.prev != null) {
			left.prev.next = newNode;
		}
		newNode.prev = left.prev;

		if (right.next != null) {
			right.next.prev = newNode;
		}
		newNode.next = right.next;

		if (left == root) {
			root = newNode;
		}

		--rootsCount;
	}

	public void breakRoots(Group left, Group right, Group oldNode) {
		if (oldNode.prev != null) {
			oldNode.prev.next = left;
		}
		if (oldNode.next != null) {
			oldNode.next.prev = right;
		}
		if (oldNode == root) {
			root = left;
		}

		++rootsCount;
	}

	private class RootsIterator implements java.util.Iterator<Group> {
		private Group currentNode;

		private RootsIterator(Group rootNode) {
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%d = [", getRootsCount()));

		String prefix = "";
		final Iterator<Group> iterator = getRootsIterator();
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
}
