package alex.data.model;


import java.util.List;

public class Ranking {
	public final Rank rank;
	public final List<User> users;

	public Ranking(Rank rank, List<User> users) {
		this.rank = rank;
		this.users = users;
	}
}
