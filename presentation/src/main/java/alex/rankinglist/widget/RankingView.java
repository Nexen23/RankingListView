package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

import alex.rankinglist.R;
import alex.rankinglist.databinding.WidgetRankingBinding;
import alex.domain.util.LogUtil;
import alex.rankinglist.widget.drawable.CorneredColorDrawable;
import alex.data.model.Rank;
import alex.data.model.Ranking;
import alex.data.model.User;


public class RankingView extends FrameLayout {
	WidgetRankingBinding binding;
	private @Px int cornerRadius, spaceBetweenChildren;
	private int animationDuration;

	private @Px int sharedHeight;
	private boolean shouldHideIcon = false;

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
		cornerRadius = getResources().getDimensionPixelSize(R.dimen.border_corner_large);
		spaceBetweenChildren = getResources().getDimensionPixelSize(R.dimen.space_normal);
		animationDuration = getResources().getInteger(R.integer.animation_normal_duration);
		setMinimumHeight(binding.getRoot().getMinimumHeight());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		LogUtil.d(this, "onMeasure(%s)", LogUtil.MeasureSpecToString(heightMeasureSpec));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		LogUtil.d(this, "onSizeChanged()");
		super.onSizeChanged(width, height, oldw, oldh);

		int requiredHeight = binding.tvScore.getHeight() + binding.tvTitle.getHeight() + binding.ivIcon.getHeight()
				+ spaceBetweenChildren * 2;
		boolean shouldHideIcon = requiredHeight > sharedHeight;
		LogUtil.i(this, "onSizeChanged: requiredHeight=%d, realHeight=%d, sharedHeight=%d --> shouldHideIcon=%s",
				requiredHeight, height, sharedHeight, shouldHideIcon);

		if (this.shouldHideIcon != shouldHideIcon) {
			this.shouldHideIcon = shouldHideIcon;
			if (shouldHideIcon) {
				hideIcon();
			} else {
				showIcon();
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		LogUtil.d(this, "onLayout()");
		super.onLayout(changed, left, top, right, bottom);
	}

	public void setModel(Ranking ranking) {
		setRank(ranking.rank);
		setUsers(ranking.rank, ranking.users);
	}

	public void setSharedHeight(int imaginaryHeight) {
		this.sharedHeight = imaginaryHeight;
	}


	public void onVisibleFrameChanged() {
		binding.lUsers.onVisibleFrameChanged();
	}

	private void setUsers(Rank rank, List<User> users) {
		binding.lUsers.setModel(rank, users);
	}

	private void setRank(Rank rank) {
		binding.ivIcon.setImageResource(rank.iconResId);
		binding.tvTitle.setText(rank.name);
		binding.tvScore.setText(String.format("%s%%", String.valueOf(rank.scoreMax)));
		setBackground(rank.backgroundColor, rank.scoreMin == 0, rank.scoreMax == 100);
	}

	private void setBackground(@ColorInt int color, boolean isBottomRank, boolean isTopRank) {
		final int leftTopCorner = isTopRank ? cornerRadius : 0;
		final int leftBottomCorner = isBottomRank ? cornerRadius : 0;
		float[] radii = {leftTopCorner, leftTopCorner, 0, 0, 0, 0, leftBottomCorner, leftBottomCorner};

		/*PaintDrawable drawable = new PaintDrawable(color);
		drawable.setCornerRadii(radii);
		binding.lRank.setBackground(drawable);*/
		CorneredColorDrawable drawable = new CorneredColorDrawable(color, radii);
		binding.lRank.setBackground(drawable);
	}

	private void hideIcon() {
		final ImageView iconView = binding.ivIcon;
		final float realDuration = iconView.getAlpha() * animationDuration;
		iconView.animate()
				.alpha(0.0f)
				.setDuration((long) realDuration)
				.setInterpolator(new LinearInterpolator())
				.withEndAction(() -> iconView.setVisibility(GONE));
	}

	private void showIcon() {
		final ImageView iconView = binding.ivIcon;
		final float realDuration = (1 - iconView.getAlpha()) * animationDuration;
		iconView.setVisibility(VISIBLE);
		iconView.animate()
				.alpha(1.0f)
				.setDuration((long) realDuration);
	}
}
