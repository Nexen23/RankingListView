package alex.rankinglist.widget.model;

import java.util.LinkedList;
import java.util.List;

import alex.rankinglist.util.MathUtil;

public class UsersGroup implements Comparable<UsersGroup> {
	public final PosedUser mainUser;
	public final List<PosedUser> users = new LinkedList<>();
	public final Position pos;

	public UsersGroup(PosedUser mainUser) {
		this.mainUser = mainUser;
		users.add(mainUser);
		pos = mainUser.pos;
	}

	/**
	 * @param mainUser - is one of the users
	 * @param users - must be ascending sorted
	 */
	public UsersGroup(PosedUser mainUser, List<PosedUser> users) {
		this.mainUser = mainUser;
		users.addAll(users);
		float relativePosSum = 0;
		for (PosedUser user : users) {
			relativePosSum += user.pos.relative;
		}
		pos = new Position(relativePosSum / users.size());
	}

	/**
	 * @param mainUser - is one of the users of a or b
	 * @param a - users must be ascending sorted
	 * @param b - users must be ascending sorted
	 */
	public UsersGroup(PosedUser mainUser, UsersGroup a, UsersGroup b) {
		this.mainUser = mainUser;
		pos = new Position((a.pos.relative + b.pos.relative) / 2);

		if (a.users.isEmpty() || b.users.isEmpty()) {
			users.addAll(a.users);
			users.addAll(b.users);
		} else {
			if (b.users.get(0).score > a.users.get(0).score) {
				users.addAll(a.users);
				users.addAll(b.users);
			} else {
				users.addAll(b.users);
				users.addAll(a.users);
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UsersGroup group = (UsersGroup) o;

		if (!mainUser.equals(group.mainUser)) return false;
		if (!users.equals(group.users)) return false;
		return pos.equals(group.pos);

	}

	@Override
	public int hashCode() {
		int result = mainUser.hashCode();
		result = 31 * result + users.hashCode();
		result = 31 * result + pos.hashCode();
		return result;
	}

	@Override
	public int compareTo(UsersGroup o) {
		if (pos.relative < o.pos.relative) {
			return -1;
		} else if (MathUtil.IsEqual(pos.relative, o.pos.relative)) {
			return 0;
		} else {
			return 1;
		}
	}
}
