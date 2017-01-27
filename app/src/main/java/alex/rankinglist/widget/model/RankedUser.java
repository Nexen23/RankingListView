package alex.rankinglist.widget.model;


import android.support.annotation.FloatRange;

public class RankedUser {
	public final User user;
	@FloatRange(from=0, to=1)
	public final float relativeRank;

	public RankedUser(User user, Rank rank) {
		this.user = user;
		this.relativeRank = (user.score - rank.scoreMin) / (rank.scoreMax - rank.scoreMin);
	}
}
