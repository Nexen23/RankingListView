package alex.rankinglist.misc;

import android.graphics.Color;
import android.support.annotation.Px;
import android.support.v4.util.Pair;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import alex.rankinglist.R;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.util.MathUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

public class GroupedListTest {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private static final double DELTA = MathUtil.EPSILON;
	private static final @Px int VIEW_SIZE = 105;
	private static Rank rank;
	private static GroupedList groupedList;

	@BeforeClass
	public static void onBeforeClass() {
		LogUtil.setEnabled(false);
		rank = new Rank("Test", 0, 100, R.mipmap.ic_launcher, Color.GREEN);
		groupedList = new GroupedList(VIEW_SIZE);
	}

	//region setData()
	@Test
	public void setData_callsAreIndependent() {
		groupedList.setData(rank, users(10, 70));
		groupedList.setSize(1000);

		groupedList.setData(rank, users(30, 40));
		groupedList.setSize(1000);

		assertSingleGroupIsLocated(350);
	}

	@Test
	public void setData_groupsAreSortedByPosition() {
		groupedList.setData(rank, users(17, 77, 33, 25, 78, 70));

		assertGroupsTreeIs("6 = [0 + 3 + 2 + 5 + 1 + 4]");
	}

	@Test
	public void setData_viewNotExceedMinBound() {
		groupedList.setData(rank, users(5));

		groupedList.setSize(1000);

		assertSingleGroupIsLocated(leftBorderPos());
	}

	@Test
	public void setData_viewNotExceedMaxBound() {
		groupedList.setData(rank, users(95));

		groupedList.setSize(1000);

		assertSingleGroupIsLocated(rightBorderPos(1000));
	}

	@Test
	public void setData_viewPositionDependsOnScore() {
		final float score = 30;
		final int size = 1000;
		groupedList.setData(rank, users(score));

		groupedList.setSize(size);

		assertSingleGroupIsLocated(score / 100.0f * size);
	}
	//endregion

	//region Compose groups
	@Test
	public void setSize_singleUserStaysUnchanged() {
		groupedList.setData(rank, users(30));

		groupedList.setSize(1000);
		groupedList.setSize(200);
		groupedList.setSize(1000);

		assertSingleGroupIsLocated(300);
		assertGroupsTreeIs("1 = [0]");
	}

	@Test
	public void setSize_group2UsersNearTheCenter() {
		groupedList.setData(rank, users(30, 40));

		groupedList.setSize(1000);

		assertSingleGroupIsLocated(350);
	}

	@Test
	public void setSize_group2UsersNearTheMinBound() {
		groupedList.setData(rank, users(6, 14));

		groupedList.setSize(1000);

		assertSingleGroupIsLocated(100);
	}

	@Test
	public void setSize_group2UsersNearTheMaxBound() {
		groupedList.setData(rank, users(86, 94));

		groupedList.setSize(1000);

		assertSingleGroupIsLocated(900);
	}

	@Test
	public void setSize_group2UsersNearTheBounds() {
		groupedList.setData(rank, users(0, 100));

		groupedList.setSize(200);

		assertSingleGroupIsLocated(100);
	}

	@Test
	public void setSize_groupUsersWithSameScore() {
		groupedList.setData(rank, users(33, 33, 40, 40));

		groupedList.setSize(1000);

		assertGroupsTreeIs("1 = [{{0, 1}, {2, 3}}]");
	}

	@Test
	public void setSize_groupRandomUsers() {
		groupedList.setData(rank, users(17, 25, 33, 51, 52));

		groupedList.setSize(500);

		assertGroupsTreeIs("2 = [{{0, 1}, 2} + {3, 4}]");
	}

	@Test
	public void setSize_groupManyUsersNearTheBounds() {
		groupedList.setData(rank, users(0, 2, 18, 33, 45, 71, 77, 89, 99, 100));

		groupedList.setSize(210);

		assertGroupsTreeIs("1 = [{{{{0, 1}, 2}, {3, 4}}, {{5, 6}, {7, {8, 9}}}}]");
	}

	@Test
	public void setSize_throwsIfNotEnoughPlaceForAnyUser() {
		groupedList.setData(rank, users(10, 90));

		exception.expect(isA(IllegalArgumentException.class));
		groupedList.setSize(100);
	}

	@Test
	public void setSize_notIntersectedUsersAreNotGrouped() {
		groupedList.setData(rank, users(10, 21, 32, 43, 54, 65, 76, 87));

		groupedList.setSize(1000);

		assertGroupsTreeIs("8 = [0 + 1 + 2 + 3 + 4 + 5 + 6 + 7]");
	}
	//endregion

	//region Break groups
	@Test
	public void setSize_breakGroupNearTheCenter() {
		groupedList.setData(rank, users(30, 40));

		groupedList.setSize(10_000);
		groupedList.setSize(1_000);
		groupedList.setSize(10_000);

		assertGroupsTreeIs("2 = [0 + 1]");
	}

	@Test
	public void setSize_breakGroupNearTheMinBound() {
		groupedList.setData(rank, users(6, 14));

		groupedList.setSize(10_000);
		groupedList.setSize(1_000);
		groupedList.setSize(10_000);

		assertGroupsTreeIs("2 = [0 + 1]");
	}

	@Test
	public void setSize_breakGroupNearTheMaxBound() {
		groupedList.setData(rank, users(86, 94));

		groupedList.setSize(10_000);
		groupedList.setSize(1_000);
		groupedList.setSize(10_000);

		assertGroupsTreeIs("2 = [0 + 1]");
	}

	@Test
	public void setSize_breakGroupNearTheBounds() {
		groupedList.setData(rank, users(0, 100));

		groupedList.setSize(1000);
		groupedList.setSize(200);
		groupedList.setSize(1000);

		assertGroupsTreeIs("2 = [0 + 1]");
	}

	@Test
	public void setSize_groupWithUsersWithSameScoreShouldNotBreak() {
		groupedList.setData(rank, users(33, 44, 33, 44));

		groupedList.setSize(50_000);

		assertGroupsTreeIs("2 = [{0, 2} + {1, 3}]");
	}

	@Test
	public void setSize_breakRandomGroups() {
		groupedList.setData(rank, users(17, 25, 33, 51, 52));

		groupedList.setSize(200);
		groupedList.setSize(50_000);

		assertGroupsTreeIs("5 = [0 + 1 + 2 + 3 + 4]");
	}
	//endregion

	//region Order of groups composing and breaking
	@Test
	public void setSize_groupUsersInCorrectOrderNearTheMinBound() {
		groupedList.setData(rank, users(0, 5, 10, 15));

		groupedList.setSize(225);

		assertSingleGroupIsLocated(leftBorderPos());
		assertGroupsTreeIs("1 = [{{0, 1}, {2, 3}}]");
	}

	@Test
	public void setSize_groupUsersInCorrectOrderNearTheMaxBound() {
		groupedList.setData(rank, users(85, 90, 95, 100));

		groupedList.setSize(225);

		assertSingleGroupIsLocated(rightBorderPos(225));
		assertGroupsTreeIs("1 = [{{0, 1}, {2, 3}}]");
	}

	@Test
	public void setSize_breakGroupsShouldBeInReverseOrderOfComposing() {
		final Stack<Pair<TreeNode, TreeNode>> groups = new Stack<>();
		groupedList.addListener(new GroupedList.EventsListener() {
			@Override
			public void onGroup(TreeNode a, TreeNode b, TreeNode composedGroup) {
				groups.push(Pair.create(a, b));
			}

			@Override
			public void onBreak(TreeNode removedGroup, TreeNode a, TreeNode b) {
				final Pair<TreeNode, TreeNode> lastGroup = groups.pop();
				if (lastGroup.first != a || lastGroup.second != b) {
					String message = String.format("Broken group of User(%s) & User(%s)\n--should be composed of User(%s) & User(%s)",
							removedGroup.left.mainUser.name, removedGroup.right.mainUser.name,
							a.mainUser.name, b.mainUser.name);
					throw new IllegalStateException(message);
				}
			}
		});

		groupedList.setData(rank, users(17, 25, 33, 51, 52));

		groupedList.setSize(200);
		groupedList.setSize(10_000);
	}

	@Test
	public void setSize_regroupingAfterBreakingShouldBeInTheSameOrderWithAnySpeed() {
		final LinkedList<TreeNode> groupsHistory = new LinkedList<>();
		final Wrapper<ListIterator<TreeNode>> iterator = Wrapper.wrap(groupsHistory.listIterator());
		groupedList.addListener(new GroupedList.EventsListener() {
			boolean isDirectionForward = true;

			@Override
			public void onGroup(TreeNode a, TreeNode b, TreeNode composedGroup) {
				if (!isDirectionForward) {
					iterator.data.next();
					isDirectionForward = true;
				}
				if (iterator.data.hasNext()) {
					TreeNode expectedGroup = iterator.data.next();
					if (!composedGroup.equals(expectedGroup)) {
						String message = String.format("Composed group of User(%s) & User(%s)\n--was previously composed of User(%s) & User(%s)",
								composedGroup.left.mainUser.name, composedGroup.right.mainUser.name,
								expectedGroup.left.mainUser.name, expectedGroup.right.mainUser.name);
						composedGroup.equals(expectedGroup);
						throw new IllegalStateException(message);
					}
				} else {
					groupsHistory.addLast(composedGroup);
					iterator.data = groupsHistory.listIterator(groupsHistory.size());
				}
			}

			@Override
			public void onBreak(TreeNode removedGroup, TreeNode a, TreeNode b) {
				if (isDirectionForward) {
					iterator.data.previous();
					isDirectionForward = false;
				}
				iterator.data.previous();
			}
		});

		groupedList.setData(rank, users(0, 17, 25, 33, 51, 52, 55, 57, 61, 69, 70, 80, 95, 98, 100));

		// Fast zoom out
		groupedList.setSize(200);

		// Slow zoom out
		for (int size = 10_000; size >= 200; size -= 200) {
			groupedList.setSize(size);
		}

		assertGroupsTreeIs("1 = [{{0, {{1, 2}, 3}}, {{{{4, 5}, {6, 7}}, 8}, {{{9, 10}, 11}, {12, {13, 14}}}}}]");
	}
	//endregion

	private void assertSingleGroupIsLocated(float viewCenterPosPx) {
		assertThat(groupedList.getGroupsCount(), is(1));
		assertThat(groupedList.getGroupsIterator().hasNext(), is(true));
		assertThat(groupedList.getGroupsIterator().next().getCenterPosPx(), closeTo(viewCenterPosPx, DELTA));
	}

	private void assertGroupsTreeIs(String treeString) {
		assertThat(groupedList.toTreeString(), is(treeString));
	}

	private User user(float invertedScore, String name) {
		return new User(name, 100 - invertedScore);
	}

	private List<User> users(float... usersInvertedScores) {
		List<User> users = new ArrayList<>(usersInvertedScores.length);
		int i = 0;
		for (float invertedScore : usersInvertedScores) {
			users.add(user(invertedScore, Integer.toString(i)));
			++i;
		}
		return users;
	}

	private float leftBorderPos() {
		return VIEW_SIZE / 2.0f;
	}

	private float rightBorderPos(int size) {
		return size - VIEW_SIZE / 2.0f;
	}
}
