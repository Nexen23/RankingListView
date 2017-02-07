package alex.rankinglist.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

import alex.rankinglist.R;
import alex.rankinglist.databinding.WidgetRankingsListBinding;
import alex.rankinglist.util.FixUtil;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.util.MathUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.Ranking;
import alex.rankinglist.widget.model.User;


public class RankingsListView extends ScrollView {
	WidgetRankingsListBinding binding;
	ScaleGestureDetector scaleDetector;

	private Integer rankingsViewGroupHeightMin;
	private int maxWidth = Integer.MAX_VALUE;
	private Float nextScrollPos;

	public RankingsListView(Context context) {
		super(context);
		init();
	}

	public RankingsListView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		parseAttrs(attrs);
		init();
	}

	public RankingsListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		parseAttrs(attrs);
		init();
	}

	private void parseAttrs(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RankingsListView);
		maxWidth = a.getDimensionPixelSize(R.styleable.RankingsListView_max_width, Integer.MAX_VALUE);
		a.recycle();
	}

	private void init() {
		binding = WidgetRankingsListBinding.inflate(LayoutInflater.from(getContext()), this, true);
		scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
		FixUtil.fixFlinchesOnFirstScale(scaleDetector);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		scaleDetector.onTouchEvent(ev);
		if (ev.getPointerCount() == 1) {
			super.onTouchEvent(ev);
		}
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		if(maxWidth > 0 && maxWidth < measuredWidth) {
			int measureMode = MeasureSpec.getMode(widthMeasureSpec);
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, measureMode);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		updateChildsSharedHeight();
		super.onLayout(changed, l, t, r, b);
		updateScrollPosition();
	}

	public void setModel(List<Rank> ranks, List<User> users) {
		List<Ranking> rankings = new ArrayList<>();
		for (Rank rank : ranks) {
			List<User> usersInRank = new ArrayList<>();
			for (User user : users) {
				if ((user.score > rank.scoreMin && user.score <= rank.scoreMax)
						|| rank.scoreMin == 0 && user.score == 0) {
					usersInRank.add(user);
				}
			}

			rankings.add(new Ranking(rank, usersInRank));
		}

		fillRankingsList(rankings);
	}

	private void fillRankingsList(List<Ranking> rankings) {
		LinearLayout rankingsViewGroup = binding.lRankings;

		rankingsViewGroupHeightMin = 0;

		for (Ranking ranking : rankings) {
			RankingView rankingView = new RankingView(getContext());
			rankingView.setModel(ranking);
			rankingsViewGroup.addView(rankingView, 0);

			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rankingView.getLayoutParams();
			params.weight = 1;
			params.height = 0;
			rankingView.setLayoutParams(params);
			rankingsViewGroupHeightMin += rankingView.getMinimumHeight();
		}

		rankingsViewGroupHeightMin += rankingsViewGroup.getPaddingBottom() + rankingsViewGroup.getPaddingTop();
	}

	private void updateChildsSharedHeight() {
		int minChildHeight = Integer.MAX_VALUE;
		for (int i = 0; i < binding.lRankings.getChildCount(); ++i) {
			minChildHeight = Math.min(minChildHeight, binding.lRankings.getChildAt(i).getMeasuredHeight());
		}

		for (int i = 0; i < binding.lRankings.getChildCount(); ++i) {
			RankingView child = (RankingView) binding.lRankings.getChildAt(i);
			child.setSharedHeight(minChildHeight);
		}
	}

	private void updateScrollPosition() {
		// postpones scroll due to actual height will changed after onLayout(), so scrollPos may be out of view bounds
		if (nextScrollPos != null) {
			setScrollY(nextScrollPos.intValue());
			nextScrollPos = null;
		}
	}

	class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		private static final int SCALE_SPEED = 4;

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			final float scaleFactor = getScaleFactor(detector), focusY = detector.getFocusY();

			ViewGroup container = binding.lRankings;
			ViewGroup.LayoutParams params = container.getLayoutParams();
			float containerHeight = params.height > 0 ? params.height : container.getHeight();
			float scrollY = nextScrollPos == null ? getScrollY() : nextScrollPos;
			float focusedItemY = scrollY + focusY;
			float focusedItemNormalizedPos = focusedItemY / containerHeight;

			float newHeight = MathUtil.InRange(containerHeight * scaleFactor, rankingsViewGroupHeightMin, 1_000_000.0f),
					newScrollY = newHeight * focusedItemNormalizedPos - focusY;

			// TODO: 08.02.2017 remove after debugging
			LogUtil.d("---[factor=%.3f(dist=%.3f)] prev:%d(%.3f), next:%d(%.3f); height::newHeight = %d::%d (PARAMS:%d) ++ focusY=%.2f",
					scaleFactor, detector.getCurrentSpan(),
					(int) focusedItemY, focusedItemNormalizedPos,
					(int) (newScrollY + focusY), (newScrollY + focusY) / newHeight,
					(int) containerHeight, (int) newHeight, params.height,
					focusY);

			params.height = (int) newHeight;
			container.setLayoutParams(params);

			nextScrollPos = newScrollY;
			return true;
		}

		float getScaleFactor(ScaleGestureDetector detector) {
			return 1 + (detector.getScaleFactor() - 1) * SCALE_SPEED;
		}
	}
}
