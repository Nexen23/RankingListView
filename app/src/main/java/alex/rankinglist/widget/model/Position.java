package alex.rankinglist.widget.model;

public class Position {
	public final Float relative;
	public Integer absolute;

	public Position(Float relative) {
		this.relative = relative;
	}

	public Position(Rank rank, User user) {
		relative = (rank.scoreMax - user.score) / (rank.scoreMax - rank.scoreMin);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Position position = (Position) o;

		if (!relative.equals(position.relative)) return false;
		return absolute != null ? absolute.equals(position.absolute) : position.absolute == null;

	}

	@Override
	public int hashCode() {
		int result = relative.hashCode();
		result = 31 * result + (absolute != null ? absolute.hashCode() : 0);
		return result;
	}
}
