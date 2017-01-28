package alex.rankinglist.widget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import alex.rankinglist.R;
import alex.rankinglist.databinding.WidgetRankingBinding;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.RankedUser;
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
		cornerRadiusPx = getResources().getDimensionPixelSize(R.dimen.border_corner);
		spaceBetweenChilrenPx = getResources().getDimensionPixelSize(R.dimen.space_normal);
		setMinimumHeight(binding.getRoot().getMinimumHeight());
	}

	public void setSharedHeight(int imaginaryHeight) {
		this.sharedHeight = imaginaryHeight;
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		LogUtil.log(this, "onSizeChanged()");
		super.onSizeChanged(width, height, oldw, oldh);
		int requiredHeight = binding.tvScore.getHeight() + binding.tvTitle.getHeight() + binding.ivRank.getHeight()
				+ spaceBetweenChilrenPx * 2;
		boolean shouldHideIcon = requiredHeight > sharedHeight;
		LogUtil.i(this, "onSizeChanged: requiredHeight=%d :: realHeight=%d <shared=%d> :: shouldHideIcon=%s",
				requiredHeight, height, sharedHeight, shouldHideIcon);
		if (shouldHideIcon) {
			binding.ivRank.setVisibility(GONE);
		} else {
			binding.ivRank.setVisibility(VISIBLE);
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
		List<RankedUser> rankedUsers = new ArrayList<>(users.size());
		for (int i = 0; i < users.size(); ++i) {
			rankedUsers.add(new RankedUser(users.get(i), rank));
		}
		binding.vUsers.setModel(rankedUsers);
	}

	private void setRank(Rank rank) {
		binding.ivRank.setImageResource(rank.iconResId);
		binding.tvTitle.setText(rank.name);
		binding.tvScore.setText(String.format("%s%%", String.valueOf(rank.scoreMax)));
		setBackground(rank.backgroundColor, rank.scoreMin == 0, rank.scoreMax == 100);
	}

	private void setBackground(@ColorInt int color, boolean isBottomRank, boolean isTopRank) {
		GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{color, color});
		final int leftTopCorner = isTopRank ? cornerRadiusPx : 0;
		final int leftBottomCorner = isBottomRank ? cornerRadiusPx : 0;
		drawable.setCornerRadii(new float[] {leftTopCorner, leftTopCorner, 0, 0, 0, 0, leftBottomCorner, leftBottomCorner});
		binding.lScoresRuler.setBackground(drawable);
	}
}
