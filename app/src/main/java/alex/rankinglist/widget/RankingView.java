package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import alex.rankinglist.R;
import alex.rankinglist.util.MathUtil;
import butterknife.BindView;
import butterknife.ButterKnife;


public class RankingView extends LinearLayout {
	private static final float
			SCALE_FACTOR_MIN = 0.1f,
			SCALE_FACTOR_MAX = 5.f;

	@BindView(R.id.l_users) FrameLayout usersLayout;
	@BindView(R.id.tv_score) TextView scoreLabel;
	@BindView(R.id.tv_title) TextView titleLabel;
	@BindView(R.id.rb_score) RatingBar scoreBar;

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



	public void scale(float scaleFactor) {
		scaleFactor = MathUtil.InRange(scaleFactor, SCALE_FACTOR_MIN, SCALE_FACTOR_MAX);
		ViewGroup.LayoutParams params = getLayoutParams();
		params.height = (int) (baseHeight * scaleFactor);
		setLayoutParams(params);
	}
}
