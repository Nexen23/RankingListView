package alex.rankinglist.misc.grouping;


class GroupsTree {
	int rootsCount = 0;
	Group root;

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

	public java.util.Iterator<Group> getRootsIterator() {
		return new RootsIterator(root);
	}

	class RootsIterator implements java.util.Iterator<Group> {
		private Group currentNode;

		public RootsIterator(Group rootNode) {
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
}
