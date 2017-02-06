package alex.rankinglist.misc;

import android.graphics.Color;
import android.support.annotation.Px;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import alex.rankinglist.R;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.util.MathUtil;
import alex.rankinglist.util.RandomUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GroupedListTest {
	private static final double DELTA = MathUtil.EPSILON;
	private static final @Px int VIEW_SIZE = 105;
	private static final float VIEW_HALF_SIZE = VIEW_SIZE / 2.0f;
	private static Rank rank;
	private static GroupedList groupedList;

	@BeforeClass
	public static void onBeforeClass() {
		LogUtil.setEnabled(false);
		rank = new Rank("Test", 0, 100, R.mipmap.ic_launcher, Color.GREEN);
		groupedList = new GroupedList(VIEW_SIZE);
	}

	//region Compose
	@Test
	public void compose_2To1_noBorders() {
		groupedList.setData(rank, users(30, 40));

		groupedList.updateChilds(1000);

		assertSingleGroupLocated(350);
	}

	@Test
	public void compose_2To1_topBorder() {
		groupedList.setData(rank, users(6, 14));

		groupedList.updateChilds(1000);

		assertSingleGroupLocated(100);
	}

	@Test
	public void compose_2To1_bottomBorder() {
		groupedList.setData(rank, users(86, 94));

		groupedList.updateChilds(1000);

		assertSingleGroupLocated(900);
	}

	@Test
	public void compose_2To1_bothBorders() {
		groupedList.setData(rank, users(0, 100));

		groupedList.updateChilds(200);

		assertSingleGroupLocated(100);
	}

	/*@Test
	public void compose_4To2_topBorder() {
	}

	@Test
	public void compose_4To2_bottomBorder() {
	}

	@Test
	public void compose_ManyToMany_noBorders() {
	}

	@Test
	public void compose_ManyToMany_topBorder() {
	}

	@Test
	public void compose_ManyToMany_bottomBorder() {
	}

	@Test
	public void compose_ManyToMany_bothBorders() {
	}*/
	//endregion

	//region Bounds
	@Test
	public void setData_viewNotExceedTopBorder() {
		groupedList.setData(rank, users(5));

		groupedList.updateChilds(1000);

		assertSingleGroupLocated(VIEW_HALF_SIZE);
	}

	@Test
	public void setData_viewNotExceedBottomBorder() {
		groupedList.setData(rank, users(95));

		groupedList.updateChilds(1000);

		assertSingleGroupLocated(1000 - VIEW_HALF_SIZE);
	}

	@Test
	public void setData_viewPosDependsOnScore() {
		final float score = 30;
		final int size = 1000;
		groupedList.setData(rank, users(score));

		groupedList.updateChilds(size);

		assertSingleGroupLocated(score / 100.0f * size);
	}
	//endregion




	public void assertSingleGroupLocated(float viewCenterPosPx) {
		assertThat(groupedList.getGroupsCount(), is(1));
		assertThat(groupedList.getGroupsIterator().hasNext(), is(true));
		assertThat(groupedList.getGroupsIterator().next().getCenterPosPx(), closeTo(viewCenterPosPx, DELTA));
	}


	public User user(float invertedScore, String name) {
		return new User(name, 100 - invertedScore);
	}

	public User user(float invertedScore) {
		return user(invertedScore, RandomUtil.GenerateName());
	}

	public List<User> users(User... users) {
		return Arrays.asList(users);
	}

	public List<User> users(float... usersInvertedScores) {
		List<User> users = new ArrayList<>(usersInvertedScores.length);
		for (float invertedScore : usersInvertedScores) {
			users.add(user(invertedScore));
		}
		return users;
	}
}
