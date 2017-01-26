package alex.rankinglist.widget.model;


import java.util.List;

public class RankedUsers {
	public final Rank rank;
	public final List<User> users;

	public RankedUsers(Rank rank, List<User> users) {
		this.rank = rank;
		this.users = users;
	}
}
