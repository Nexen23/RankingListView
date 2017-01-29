package alex.rankinglist.widget.model;

import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

public class UsersGroup {
	public final PosedUser mainUser;
	public final List<PosedUser> users = new LinkedList<>();
	public final int posAbsolute;

	/**
	 * @param users - must be ascending sorted
	 */
	public UsersGroup(List<PosedUser> users, int posAbsolute) {
		this(null, users, posAbsolute);
	}

	/**
	 * @param mainUser - is one of the users
	 * @param users - must be ascending sorted
	 */
	public UsersGroup(@Nullable PosedUser mainUser, List<PosedUser> users, int posAbsolute) {
		this.mainUser = mainUser == null ? users.get(0) : mainUser;
		this.users.addAll(users);
		this.posAbsolute = posAbsolute;
	}
}
