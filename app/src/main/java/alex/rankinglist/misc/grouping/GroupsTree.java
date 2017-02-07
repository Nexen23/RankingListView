package alex.rankinglist.misc.grouping;


import java.util.Iterator;
import java.util.List;

class GroupsTree {
	private int rootsCount = 0;
	private GroupNode root;

	/**
	 * @param groups must be sorted
	 */
	public void addGroups(List<GroupNode> groups) {
		clear();

		GroupNode prevNode = null;
		for (GroupNode curNode : groups) {
			if (prevNode == null) {
				root = curNode;
			} else {
				prevNode.next = curNode;
				curNode.prev = prevNode;
			}
			prevNode = curNode;
			++rootsCount;
		}
	}

	public void updateMergedNodes(GroupNode mergedNode) {
		GroupNode left = mergedNode.getLeftNode(), right = mergedNode.getRightNode();

		if (left.prev != null) {
			left.prev.next = mergedNode;
		}
		mergedNode.prev = left.prev;

		if (right.next != null) {
			right.next.prev = mergedNode;
		}
		mergedNode.next = right.next;

		if (left == root) {
			root = mergedNode;
		}

		--rootsCount;
	}

	public void updateBrokenNode(GroupNode brokenNode) {
		GroupNode left = brokenNode.getLeftNode(), right = brokenNode.getRightNode();

		if (brokenNode.prev != null) {
			brokenNode.prev.next = left;
		}
		if (brokenNode.next != null) {
			brokenNode.next.prev = right;
		}
		if (brokenNode == root) {
			root = left;
		}

		++rootsCount;
	}

	public int getRootsCount() {
		return rootsCount;
	}

	public boolean isEmpty() {
		return rootsCount == 0;
	}

	public void clear() {
		root = null;
		rootsCount = 0;
	}

	public java.util.Iterator<GroupNode> getRootsIterator() {
		return new RootsIterator(root);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%d = [", getRootsCount()));

		String prefix = "";
		final Iterator<GroupNode> iterator = getRootsIterator();
		while (iterator.hasNext()) {
			builder.append(prefix);
			iterateTree(builder, iterator.next());
			prefix = " + ";
		}

		builder.append("]");

		return builder.toString();
	}

	private void iterateTree(final StringBuilder builder, GroupNode group) {
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

	private class RootsIterator implements java.util.Iterator<GroupNode> {
		private GroupNode currentNode;

		private RootsIterator(GroupNode rootNode) {
			this.currentNode = rootNode;
		}

		@Override
		public boolean hasNext() {
			return currentNode != null;
		}

		@Override
		public GroupNode next() {
			GroupNode prevNode = currentNode;
			currentNode = currentNode.next;
			return prevNode;
		}
	}
}
