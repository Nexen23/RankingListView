package alex.rankinglist.widget.model;

public class PosedUser extends User implements Comparable<PosedUser> {
	public final UserPos pos;

	public PosedUser(Rank rank, User user) {
		super(user.name, user.score);
		pos = new UserPos(rank, this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		PosedUser posedUser = (PosedUser) o;

		return pos.equals(posedUser.pos);

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + pos.hashCode();
		return result;
	}

	@Override
	public int compareTo(PosedUser o) {
		return Float.compare(score, o.score);
	}
}
