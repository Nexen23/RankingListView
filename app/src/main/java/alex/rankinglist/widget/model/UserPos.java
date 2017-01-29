package alex.rankinglist.widget.model;

public class UserPos {
	public final Float relative;
	public Integer absolute;

	public UserPos(Rank rank, User user) {
		relative = (rank.scoreMax - user.score) / (rank.scoreMax - rank.scoreMin);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UserPos userPos = (UserPos) o;

		if (!relative.equals(userPos.relative)) return false;
		return absolute != null ? absolute.equals(userPos.absolute) : userPos.absolute == null;

	}

	@Override
	public int hashCode() {
		int result = relative.hashCode();
		result = 31 * result + (absolute != null ? absolute.hashCode() : 0);
		return result;
	}
}
