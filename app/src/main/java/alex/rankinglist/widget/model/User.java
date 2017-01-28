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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		User user = (User) o;

		if (Float.compare(user.score, score) != 0) return false;
		return name.equals(user.name);

	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + (score != +0.0f ? Float.floatToIntBits(score) : 0);
		return result;
	}
}
