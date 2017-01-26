package alex.rankinglist.widget.model;


import android.support.annotation.IntRange;

public class User {
	final public String name;
	@IntRange(from=0) public
	final int rank;

	public User(String name, int rank) {
		this.name = name;
		this.rank = rank;
	}
}
