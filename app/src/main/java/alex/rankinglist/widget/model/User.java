package alex.rankinglist.widget.model;


import android.support.annotation.FloatRange;

public class User {
	final public String name;
	@FloatRange(from=0, to=100) public
	final float score;

	public User(String name, float score) {
		this.name = name;
		this.score = score;
	}
}
