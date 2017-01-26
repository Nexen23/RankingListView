package alex.rankinglist.widget.model;


import java.util.List;

public class RankGroup {
	public final Rank rank;
	public final List<User> users;

	public RankGroup(Rank rank, List<User> users) {
		this.rank = rank;
		this.users = users;
	}
}
