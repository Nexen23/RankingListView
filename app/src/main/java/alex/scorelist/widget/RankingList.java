package alex.scorelist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;


public class RankingList extends RecyclerView {
	public RankingList(Context context) {
		this(context, null);
	}

	public RankingList(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RankingList(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
}
