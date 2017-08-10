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

import alex.data.model.Rank;
import alex.data.model.User;
import alex.domain.R;
import alex.domain.grouping.GroupNode;
import alex.domain.grouping.GroupedList;
import alex.domain.misc.Wrapper;
import alex.domain.util.LogUtil;
import alex.domain.util.MathUtil;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

public class GroupedListTest {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private static final double DELTA = MathUtil.EPSILON;
	private static final @Px int VIEW_SIZE = 105;
	private static final float VIEW_HALF_SIZE = VIEW_SIZE / 2.0f;
	private static Rank rank;
	private static GroupedList groupedList;

	@BeforeClass
	public static void onBeforeClass() {
		LogUtil.setEnabled(false);
		rank = new Rank("Test", 0, 100, R.drawable.test_stub, Color.GREEN);
		groupedList = new GroupedList(VIEW_SIZE);
	}

	//region setData()
	@Test
	public void setData_callsAreIndependent() {
		groupedList.setData(rank, users(10, 70));
		groupedList.setSpace(1000);

		groupedList.setData(rank, users(30, 40));
		groupedList.setSpace(1000);

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

		groupedList.setSpace(1000);

		assertSingleGroupIsLocated(leftBorderPos());
	}

	@Test
	public void setData_viewNotExceedMaxBound() {
		groupedList.setData(rank, users(95));

		groupedList.setSpace(1000);

		assertSingleGroupIsLocated(rightBorderPos(1000));
	}

	@Test
	public void setData_viewPositionDependsOnScore() {
		final float score = 30;
		final int size = 1000;
		groupedList.setData(rank, users(score));

		groupedList.setSpace(size);

		assertSingleGroupIsLocated(score / 100.0f * size);
	}

	@Test
	public void setData_doNothingWithoutSpaceChange() {
		groupedList.setData(rank, users(33, 45, 56, 67));

		groupedList.setSpace(1000);
		final boolean didGroupingCalls = groupedList.setSpace(1000);

		assertThat(didGroupingCalls, is(false));
	}

	@Test
	public void setData_emptyUsersListNotThrow() {
		groupedList.setData(rank, users());

		final boolean didGroupingCalls = groupedList.setSpace(1000);

		assertThat(didGroupingCalls, is(false));
	}
	//endregion

	//region Compose groups
	@Test
	public void setSpace_singleUserStaysUnchanged() {
		groupedList.setData(rank, users(30));

		groupedList.setSpace(1000);
		groupedList.setSpace(200);
		groupedList.setSpace(1000);

		assertSingleGroupIsLocated(300);
		assertGroupsTreeIs("1 = [0]");
	}

	@Test
	public void setSpace_group2UsersNearTheCenter() {
		groupedList.setData(rank, users(30, 40));

		groupedList.setSpace(1000);

		assertSingleGroupIsLocated(350);
	}

	@Test
	public void setSpace_group2UsersNearTheMinBound() {
		groupedList.setData(rank, users(6, 14));

		groupedList.setSpace(1000);

		assertSingleGroupIsLocated(100);
	}

	@Test
	public void setSpace_group2UsersNearTheMaxBound() {
		groupedList.setData(rank, users(86, 94));

		groupedList.setSpace(1000);

		assertSingleGroupIsLocated(900);
	}

	@Test
	public void setSpace_group2UsersNearTheBounds() {
		groupedList.setData(rank, users(0, 100));

		groupedList.setSpace(200);

		assertSingleGroupIsLocated(100);
	}

	@Test
	public void setSpace_groupUsersWithSameScore() {
		groupedList.setData(rank, users(33, 33, 40, 40));

		groupedList.setSpace(1000);

		assertGroupsTreeIs("1 = [{{0, 1}, {2, 3}}]");
	}

	@Test
	public void setSpace_groupRandomUsers() {
		groupedList.setData(rank, users(17, 25, 33, 51, 52));

		groupedList.setSpace(500);

		assertGroupsTreeIs("2 = [{{0, 1}, 2} + {3, 4}]");
	}

	@Test
	public void setSpace_groupManyUsersNearTheBounds() {
		groupedList.setData(rank, users(0, 2, 18, 33, 45, 71, 77, 89, 99, 100));

		groupedList.setSpace(210);

		assertGroupsTreeIs("1 = [{{{{0, 1}, 2}, {3, 4}}, {{5, 6}, {7, {8, 9}}}}]");
	}

	@Test
	public void setSpace_throwsIfNotEnoughPlaceForAnyUser() {
		groupedList.setData(rank, users(10, 90));

		exception.expect(isA(IllegalArgumentException.class));
		groupedList.setSpace(100);
	}

	@Test
	public void setSpace_notIntersectedUsersAreNotGrouped() {
		groupedList.setData(rank, users(10, 21, 32, 43, 54, 65, 76, 87));

		groupedList.setSpace(1000);

		assertGroupsTreeIs("8 = [0 + 1 + 2 + 3 + 4 + 5 + 6 + 7]");
	}
	//endregion

	//region Break groups
	@Test
	public void setSpace_breakGroupNearTheCenter() {
		groupedList.setData(rank, users(30, 40));

		groupedList.setSpace(10_000);
		groupedList.setSpace(1_000);
		groupedList.setSpace(10_000);

		assertGroupsTreeIs("2 = [0 + 1]");
	}

	@Test
	public void setSpace_breakGroupNearTheMinBound() {
		groupedList.setData(rank, users(6, 14));

		groupedList.setSpace(10_000);
		groupedList.setSpace(1_000);
		groupedList.setSpace(10_000);

		assertGroupsTreeIs("2 = [0 + 1]");
	}

	@Test
	public void setSpace_breakGroupNearTheMaxBound() {
		groupedList.setData(rank, users(86, 94));

		groupedList.setSpace(10_000);
		groupedList.setSpace(1_000);
		groupedList.setSpace(10_000);

		assertGroupsTreeIs("2 = [0 + 1]");
	}

	@Test
	public void setSpace_breakGroupNearTheBounds() {
		groupedList.setData(rank, users(0, 100));

		groupedList.setSpace(1000);
		groupedList.setSpace(200);
		groupedList.setSpace(1000);

		assertGroupsTreeIs("2 = [0 + 1]");
	}

	@Test
	public void setSpace_groupWithUsersWithSameScoreShouldNotBreak() {
		groupedList.setData(rank, users(33, 44, 33, 44));

		groupedList.setSpace(50_000);

		assertGroupsTreeIs("2 = [{0, 2} + {1, 3}]");
	}

	@Test
	public void setSpace_breakRandomGroups() {
		groupedList.setData(rank, users(17, 25, 33, 51, 52));

		groupedList.setSpace(200);
		groupedList.setSpace(50_000);

		assertGroupsTreeIs("5 = [0 + 1 + 2 + 3 + 4]");
	}
	//endregion

	//region Order of groups composing and breaking
	@Test
	public void setSpace_groupUsersInCorrectOrderNearTheMinBound() {
		groupedList.setData(rank, users(0, 5, 10, 15));

		groupedList.setSpace(225);

		assertSingleGroupIsLocated(leftBorderPos());
		assertGroupsTreeIs("1 = [{{0, 1}, {2, 3}}]");
	}

	@Test
	public void setSpace_groupUsersInCorrectOrderNearTheMaxBound() {
		groupedList.setData(rank, users(85, 90, 95, 100));

		groupedList.setSpace(225);

		assertSingleGroupIsLocated(rightBorderPos(225));
		assertGroupsTreeIs("1 = [{{0, 1}, {2, 3}}]");
	}

	@Test
	public void setSpace_breakGroupsShouldBeInReverseOrderOfComposing() {
		final Stack<Pair<GroupNode, GroupNode>> groups = new Stack<>();
		groupedList.addListener(new GroupedList.EventsListener() {
			@Override
			public void onGroup(GroupNode composedGroup, GroupNode a, GroupNode b) {
				groups.push(Pair.create(a, b));
			}

			@Override
			public void onBreak(GroupNode removedGroup, GroupNode a, GroupNode b) {
				final Pair<GroupNode, GroupNode> lastGroup = groups.pop();
				if (lastGroup.first != a || lastGroup.second != b) {
					String got = String.format("Broken group of User(%s) & User(%s)",
							removedGroup.getLeftNode().getData().name, removedGroup.getRightNode().getData().name);
					String expected = String.format("should be composed of User(%s) & User(%s)",
							a.getData().name, b.getData().name);
					throw new IllegalStateException(got + "\n--" + expected);
				}
			}
		});

		groupedList.setData(rank, users(17, 25, 33, 51, 52));

		groupedList.setSpace(200);
		groupedList.setSpace(10_000);
	}

	@Test
	public void setSpace_regroupingAfterBreakingShouldBeInTheSameOrderWithAnySpeed() {
		final LinkedList<GroupNode> groupsHistory = new LinkedList<>();
		final Wrapper<ListIterator<GroupNode>> iterator = Wrapper.wrap(groupsHistory.listIterator());
		groupedList.addListener(new GroupedList.EventsListener() {
			boolean isDirectionForward = true;

			@Override
			public void onGroup(GroupNode composedGroup, GroupNode a, GroupNode b) {
				if (!isDirectionForward) {
					iterator.data.next();
					isDirectionForward = true;
				}
				if (iterator.data.hasNext()) {
					GroupNode expectedGroup = iterator.data.next();
					if (!composedGroup.equals(expectedGroup)) {
						String got = String.format("Composed group of User(%s) & User(%s)",
								composedGroup.getLeftNode().getData().name, composedGroup.getRightNode().getData().name);
						String expected = String.format("was previously composed of User(%s) & User(%s)",
								expectedGroup.getLeftNode().getData().name, expectedGroup.getRightNode().getData().name);
						throw new IllegalStateException(got + "\n--" + expected);
					}
				} else {
					groupsHistory.addLast(composedGroup);
					iterator.data = groupsHistory.listIterator(groupsHistory.size());
				}
			}

			@Override
			public void onBreak(GroupNode removedGroup, GroupNode a, GroupNode b) {
				if (isDirectionForward) {
					iterator.data.previous();
					isDirectionForward = false;
				}
				iterator.data.previous();
			}
		});

		groupedList.setData(rank, users(0, 17, 25, 33, 51, 52, 55, 57, 61, 69, 70, 80, 95, 98, 100));

		// Fast zoom out
		groupedList.setSpace(200);

		// Slow zoom out
		for (int space = 10_000; space >= 200; space -= 200) {
			groupedList.setSpace(space);
		}

		assertGroupsTreeIs("1 = [{{0, {{1, 2}, 3}}, {{{{4, 5}, {6, 7}}, 8}, {{{9, 10}, 11}, {12, {13, 14}}}}}]");
	}
	//endregion

	//region Helpers
	private void assertSingleGroupIsLocated(float viewCenterPosPx) {
		assertThat(groupedList.getGroupsCount(), is(1));
		assertThat(getGroupCenteredPos(groupedList.iterator().next()), closeTo(viewCenterPosPx, DELTA));
	}

	private void assertGroupsTreeIs(String treeString) {
		assertThat(groupedList.toString(), is(treeString));
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
		return VIEW_HALF_SIZE;
	}

	private float rightBorderPos(int space) {
		return space - VIEW_HALF_SIZE;
	}

	public double getGroupCenteredPos(GroupNode group) {
		return group.getAbsolutePos(groupedList.getSpace()) + VIEW_HALF_SIZE;
	}
	//endregion
}
