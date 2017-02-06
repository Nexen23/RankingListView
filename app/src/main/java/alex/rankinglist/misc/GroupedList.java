package alex.rankinglist.misc;


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
	Integer prevHeight;
	private final @Px int userViewHeightPx;
	private final float userViewHeightHalfPx;

	public TreeNode usersGroupsRoot;
	public int usersGroupsCount = 0;
	LinkedList<DistanceNode> groupsDistances = new LinkedList<>();
	Stack<TreeNode> groupsHistory = new Stack<>();

	public GroupedList(@Px int userViewHeightPx) {
		this.userViewHeightPx = userViewHeightPx;
		userViewHeightHalfPx = userViewHeightPx / 2.0f;
	}

	public void clearData() {
		usersGroupsCount = 0;
		groupsDistances = new LinkedList<>();
		groupsHistory = new Stack<>();
		usersGroupsRoot = null;
		prevHeight = null;
	}

	public void setData(Rank rank, List<User> users) {
		if (usersGroupsRoot != null) {
			clearData();
		}

		if (!users.isEmpty()) {
			LinkedList<TreeNode> usersGroups = new LinkedList<>();
			for (User user : users) {
				usersGroups.add(new TreeNode(userViewHeightPx, rank, user));
			}
			Collections.sort(usersGroups);
			usersGroupsCount = usersGroups.size();

			groupsDistances.clear();
			ListIterator<TreeNode> iter = usersGroups.listIterator();
			TreeNode prevNode = iter.next(), curNode;
			usersGroupsRoot = prevNode;
			while (iter.hasNext()) {
				curNode = iter.next();
				prevNode.next = curNode;
				curNode.prev = prevNode;
				groupsDistances.add(new DistanceNode(userViewHeightPx, prevNode, curNode));
				prevNode = curNode;
			}
		}
	}

	public boolean setSize(int height) {
		if (height < userViewHeightPx) {
			final String message = String.format("Size(%d) must be greater than view size(%d)", height, userViewHeightPx);
			throw new IllegalArgumentException(message);
		}

		if (usersGroupsCount > 0) {
			LogUtil.log(this, "setSize(height=%d)", height);

			LogUtil.err(this, "----------- = [ %d ---------------", height);
			logGroups();
			updateGroupsPoses(height);
			if (prevHeight == null || prevHeight > height) {
				updateDistances();
				composeByDistances(height);
			} else {
				breakByHistory(height);
			}
			prevHeight = height;

			logGroups();
			return true;
		}
		return false;
	}


	private void updateGroupsPoses(int height) {
		TreeNode node = usersGroupsRoot;
		int i = 0;
		while (node != null) {
			node.updateAbsolutePos(height);
			node = node.next;
			++i;
		}
		Assert.assertSame(usersGroupsCount, i);
	}

	private void updateDistances() {
		for (DistanceNode node : groupsDistances) {
			node.updateIntersectingHeight();
		}
		Collections.sort(groupsDistances);
	}


	void logGroups() {
		LogUtil.i(this, toTreeString());
	}

	boolean logged = false;
	private void logDistances() {
//		String str = String.format("%d = [ ", groupsDistances.size());
//		for (DistanceNode currentNode : groupsDistances) {
//			str = String.format("%s(%.5f: %s--%s) ", str, currentNode.distance, currentNode.from.mainUser.name, currentNode.to.mainUser.name);
//		}
//		LogUtil.i(this, "%s]", str);
//
//		str = String.format("^ %d = [ ", usersGroupsCount);
//		TreeNode currentNode = usersGroupsRoot;
//		while (currentNode != null) {
//			str = String.format("%s(%s: %.5f) ", str, currentNode.mainUser.name, currentNode.posAbsolute);
//			currentNode = currentNode.next;
//		}
//		LogUtil.log(this, "%s]", str);

		/*str = String.format("^ %d = [ ", groupsDistances.size() - 1);
		ListIterator<DistanceNode> iter = groupsDistances.listIterator();
		DistanceNode prev = iter.next(), cur;
		while (iter.hasNext()) {
			cur = iter.next();
			str = String.format("%s(%d: %s--%s <> %s--%s) ", str, prev.compareTo(cur),
					prev.from.mainUser.name, prev.to.mainUser.name,
					cur.from.mainUser.name, cur.to.mainUser.name);
			prev = cur;
		}
		LogUtil.log(this, "%s]", str);*/
	}

	private void composeByDistances(int height) {
		logged = false;
		if (!groupsDistances.isEmpty()) {
			DistanceNode first = groupsDistances.getFirst();
			while (first.intersectingHeight >= height) {
				if (!logged) {
					//logged = true;
					//logDistances();
				}

				TreeNode newNode = first.compose(height);
				if (newNode.left == usersGroupsRoot) {
					usersGroupsRoot = newNode;
				}
				groupsHistory.push(newNode);
				usersGroupsCount--;

				groupsDistances.removeFirst();
				Collections.sort(groupsDistances);
				if (groupsDistances.isEmpty()) {
					break;
				} else {
					first = groupsDistances.getFirst();
				}
			}
		}
		//logDistances();
	}

	private void breakByHistory(int height) {
		while (!groupsHistory.isEmpty()) {
			TreeNode node = groupsHistory.peek();
			Float rightPos = node.right.calcAndGetAbsolutePos(height);
			Float leftPos = node.left.calcAndGetAbsolutePos(height);
			if (rightPos >= (leftPos + userViewHeightPx)) {
				groupsHistory.pop();

				node.breakNode();

				if (node.prev != null) {
					node.prev.next = node.left;
				}
				if (node.next != null) {
					node.next.prev = node.right;
				}
				if (node == usersGroupsRoot) {
					usersGroupsRoot = node.left;
				}

				groupsDistances.add(new DistanceNode(userViewHeightPx, node.left, node.right));
				++usersGroupsCount;
			} else {
				break;
			}
		}
	}



	public java.util.Iterator<TreeNode> getGroupsIterator() {
		return new Iterator(usersGroupsRoot);
	}

	public int getGroupsCount() {
		return usersGroupsCount;
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
		builder.append(String.format("%d = [", usersGroupsCount));

		TreeNode node = usersGroupsRoot;
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


	public class Group<T> {

	}

	private class TwoWayNode<T> {
		private TwoWayNode<T> prev, next;
		private T data;
	}

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
}
