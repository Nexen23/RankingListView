package alex.data.model;


import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;

public class Rank {
	public final String name;
	@IntRange(from=0, to=100)
	public final int scoreMin, scoreMax;
	@ColorInt
	public final int backgroundColor;
	@DrawableRes
	public final int iconResId;

	public Rank(String name, @IntRange(from = 0, to = 100) int scoreMin, @IntRange(from = 0, to = 100) int scoreMax,
	            @DrawableRes int iconResId, @ColorInt int backgroundColor) {
		this.name = name;
		this.scoreMin = scoreMin;
		this.scoreMax = scoreMax;
		this.iconResId = iconResId;
		this.backgroundColor = backgroundColor;
	}
}
