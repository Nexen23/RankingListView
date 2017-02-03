package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

import alex.rankinglist.R;
import alex.rankinglist.databinding.WidgetRankingBinding;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.widget.drawable.CorneredColorDrawable;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.Ranking;
import alex.rankinglist.widget.model.User;


public class RankingView extends FrameLayout {
	int cornerRadiusPx, spaceBetweenChilrenPx;
	WidgetRankingBinding binding;
	private int sharedHeight;

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
		binding = WidgetRankingBinding.inflate(LayoutInflater.from(getContext()), this, true);
		cornerRadiusPx = getResources().getDimensionPixelSize(R.dimen.border_corner_large);
		spaceBetweenChilrenPx = getResources().getDimensionPixelSize(R.dimen.space_normal);
		setMinimumHeight(binding.getRoot().getMinimumHeight());
	}

	public void setSharedHeight(int imaginaryHeight) {
		this.sharedHeight = imaginaryHeight;
	}

	int t = 200;
	boolean shouldHideIcon = false;

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		LogUtil.log(this, "onSizeChanged()");
		super.onSizeChanged(width, height, oldw, oldh);
		int requiredHeight = binding.tvScore.getHeight() + binding.tvTitle.getHeight() + binding.ivRank.getHeight()
				+ spaceBetweenChilrenPx * 2;
		boolean shouldHideIcon = requiredHeight > sharedHeight;
		LogUtil.i(this, "onSizeChanged: requiredHeight=%d :: realHeight=%d <shared=%d> :: shouldHideIcon=%s",
				requiredHeight, height, sharedHeight, shouldHideIcon);

		if (this.shouldHideIcon != shouldHideIcon) {
			final ImageView logoView = binding.ivRank;
			final float curAlpha = logoView.getAlpha();
			if (shouldHideIcon) {
				final float realDuration = curAlpha * t;
				logoView.animate()
						.alpha(0.0f)
						.setDuration((long) realDuration)
						.setInterpolator(new LinearInterpolator())
						.withEndAction(new Runnable() {
							@Override
							public void run() {
								logoView.setVisibility(GONE);
							}
						});
			} else {
				final float realDuration = (1 - curAlpha) * t;
				logoView.setVisibility(VISIBLE);
				logoView.animate()
						.alpha(1.0f)
						.setDuration((long) realDuration);
			}
			this.shouldHideIcon = shouldHideIcon;
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		LogUtil.log(this, "onLayout()");
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		LogUtil.log(this, "onMeasure()");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void setModel(Ranking ranking) {
		setRank(ranking.rank);
		setUsers(ranking.rank, ranking.users);
	}

	private void setUsers(Rank rank, List<User> users) {
		binding.vUsers.setModel(rank, users);
	}


	private void setRank(Rank rank) {
		binding.ivRank.setImageResource(rank.iconResId);

//		postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				binding.ivRank.animate().setInterpolator(new LinearInterpolator());
//
//				final float x = binding.ivRank.getX();
//				final float delta = 300;
//				binding.ivRank.animate().xBy(delta).setDuration(t).start();
//				postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						binding.ivRank.animate().cancel();
//
//						float xcur = binding.ivRank.getX();
//						float coef = 1 - (xcur - x) / delta;
//						int dur = (int) (coef * t);
//						binding.ivRank.animate()
//								.x(x)
//								.setDuration(dur)
//								.start();
//					}
//				}, t/2);
//			}
//		}, 500);

		binding.tvTitle.setText(rank.name);
		binding.tvScore.setText(String.format("%s%%", String.valueOf(rank.scoreMax)));
		setBackground(rank.backgroundColor, rank.scoreMin == 0, rank.scoreMax == 100);
	}

	private void setBackground(@ColorInt int color, boolean isBottomRank, boolean isTopRank) {
		final int leftTopCorner = isTopRank ? cornerRadiusPx : 0;
		final int leftBottomCorner = isBottomRank ? cornerRadiusPx : 0;
		float[] radii = {leftTopCorner, leftTopCorner, 0, 0, 0, 0, leftBottomCorner, leftBottomCorner};

		/*PaintDrawable drawable = new PaintDrawable(color);
		drawable.setCornerRadii(radii);*/
		CorneredColorDrawable drawable = new CorneredColorDrawable(color, radii);
		binding.lScoresRuler.setBackground(drawable);
	}
}
