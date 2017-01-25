package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
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
	public static final String TAG = RankingView.class.getName();

	@BindView(R.id.l_users) FrameLayout usersLayout;
	@BindView(R.id.tv_score) TextView scoreLabel;
	@BindView(R.id.tv_title) TextView titleLabel;
	@BindView(R.id.iv_rank) ImageView rankImage;
	@BindView(R.id.l_scores_ruler) ViewGroup scoresRulerLayout;

	Integer minHeight;

	public RankingView(Context context) {
		super(context);
		init();
	}

	public RankingView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
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
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (minHeight == null) {
			minHeight = scoreLabel.getHeight() + titleLabel.getHeight();
			Log.i(TAG, String.format("onSizeChanged: minHeight = %d", minHeight));
		}
	}

	public void setData(String title, int scoreMax, @ColorInt int color, @DrawableRes int rankImageId) {
		scoreLabel.setText(String.format("%s%%", String.valueOf(scoreMax)));
		titleLabel.setText(title);
		rankImage.setImageResource(rankImageId);
		scoresRulerLayout.setBackgroundColor(color);
	}

	public void scale(float scaleFactor) {
		scaleFactor = 1 + (scaleFactor - 1) * 3;

		ViewGroup.LayoutParams params = getLayoutParams();
		int prevHeight = getHeight();
		params.height = Math.max(minHeight, (int) (prevHeight * scaleFactor));
		if ((minHeight + rankImage.getHeight()) < getHeight()) {
			rankImage.setVisibility(VISIBLE);
		} else {
			rankImage.setVisibility(GONE);
		}
		Log.d(TAG, String.format("scaleFactor = %f, prevHeight = %d, newHeight = %d", scaleFactor, prevHeight, params.height));
		setLayoutParams(params);
	}
}
