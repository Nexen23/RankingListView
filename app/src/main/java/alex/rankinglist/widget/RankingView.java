package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import alex.rankinglist.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class RankingView extends LinearLayout {
	private static final float
			SCALE_FACTOR_MIN = 0.1f,
			SCALE_FACTOR_MAX = 5.f;

	@BindView(R.id.l_users) FrameLayout usersLayout;
	@BindView(R.id.tv_score) TextView scoreLabel;
	@BindView(R.id.tv_title) TextView titleLabel;
	@BindView(R.id.iv_rank) ImageView rankImage;
	@BindView(R.id.l_scores_ruler) ViewGroup scoresRulerLayout;

	Integer baseHeight;

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
		setOrientation(HORIZONTAL);

		ButterKnife.bind(this);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (baseHeight == null) {
			baseHeight = h;
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	public void setData(String title, int scoreMax, @ColorInt int color, @DrawableRes int rankImageId) {
		scoreLabel.setText(String.format("%s%%", String.valueOf(scoreMax)));
		titleLabel.setText(title);
		rankImage.setImageResource(rankImageId);
		scoresRulerLayout.setBackgroundColor(color);
	}

	public void scale(float scaleFactor) {
		//scaleFactor = MathUtil.InRange(scaleFactor, SCALE_FACTOR_MIN, SCALE_FACTOR_MAX); // TODO: 25.01.2017 remove
		ViewGroup.LayoutParams params = getLayoutParams();
		params.height = (int) (baseHeight * scaleFactor);
		setLayoutParams(params);
	}
}
