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

public class ClusteredList {
	Integer prevHeight;
	private final @Px int userViewHeightPx;
	private final float userViewHeightHalfPx;

	public TreeNode usersGroupsRoot;
	public int usersGroupsCount = 0;
	LinkedList<DistanceNode> groupsDistances = new LinkedList<>();
	Stack<TreeNode> groupsHistory = new Stack<>();

	public ClusteredList(@Px int userViewHeightPx) {
		this.userViewHeightPx = userViewHeightPx;
		userViewHeightHalfPx = userViewHeightPx / 2.0f;
	}

	public void setData(Rank rank, List<User> users) {
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

	public boolean updateChilds(int width, int height) {
		if (usersGroupsCount > 0) {
			LogUtil.log(this, "updateChilds(height=%d)", height);

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

	void logGroups() {
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

		LogUtil.i(this, builder.toString());
	}

	boolean logged = false;
	private void logDistances() {
//		String str = String.format("%d = [ ", groupsDistances.size());
//		for (DistanceNode node : groupsDistances) {
//			str = String.format("%s(%.5f: %s--%s) ", str, node.distance, node.from.mainUser.name, node.to.mainUser.name);
//		}
//		LogUtil.i(this, "%s]", str);
//
//		str = String.format("^ %d = [ ", usersGroupsCount);
//		TreeNode node = usersGroupsRoot;
//		while (node != null) {
//			str = String.format("%s(%s: %.5f) ", str, node.mainUser.name, node.posAbsolute);
//			node = node.next;
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
}
