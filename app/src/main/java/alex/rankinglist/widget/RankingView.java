package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import alex.rankinglist.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class RankingView extends LinearLayout {
	@BindView(R.id.l_users) FrameLayout usersLayout;
	@BindView(R.id.tv_score) TextView scoreLabel;
	@BindView(R.id.tv_title) TextView titleLabel;
	@BindView(R.id.rb_score) RatingBar scoreBar;

	public RankingView(Context context) {
		this(context, null);
	}

	public RankingView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RankingView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.widget_ranking_view, this, true);
		ButterKnife.bind(this);
	}
}
