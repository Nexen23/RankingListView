package alex.rankinglist.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

import alex.rankinglist.R;
import alex.rankinglist.databinding.WidgetRankingListBinding;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.util.MathUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.Ranking;
import alex.rankinglist.widget.model.User;


public class RankingListView extends ScrollView {
	WidgetRankingListBinding binding;
	ScaleGestureDetector scaleDetector;
	Integer rankingsListViewHeightMin;

	private int maxWidth = Integer.MAX_VALUE;

	public RankingListView(Context context) {
		super(context);
		init();
	}

	public RankingListView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		parseAttrs(attrs);
		init();
	}

	public RankingListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		parseAttrs(attrs);
		init();
	}

	private void parseAttrs(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RankingListView);
		maxWidth = a.getDimensionPixelSize(R.styleable.RankingListView_max_width, Integer.MAX_VALUE);
		a.recycle();
	}

	private void init() {
		binding = WidgetRankingListBinding.inflate(LayoutInflater.from(getContext()), this, true);
		scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

		// HACK: fix for flinches of first scale event (it ignores minSpan)
		long time = SystemClock.uptimeMillis();
		MotionEvent motionEvent = MotionEvent.obtain(time - 100, time, MotionEvent.ACTION_CANCEL,
				0.0f, 0.0f, 0);
		scaleDetector.onTouchEvent(motionEvent);
		motionEvent.recycle();
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

	private void fillRankingList(List<Ranking> rankings) {
		LinearLayout rankingsListView = binding.listRankingViews;

		rankingsListViewHeightMin = 0;

		for (Ranking ranking : rankings) {
			RankingView rankingView = new RankingView(getContext());
			rankingView.setModel(ranking);
			rankingsListView.addView(rankingView, 0);

			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rankingView.getLayoutParams();
			params.weight = 1;
			params.height = 0;
			rankingView.setLayoutParams(params);
			rankingsListViewHeightMin += rankingView.getMinimumHeight();
		}

		rankingsListViewHeightMin += rankingsListView.getPaddingBottom() + rankingsListView.getPaddingTop();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		scaleDetector.onTouchEvent(ev);
		if (ev.getPointerCount() == 1) {
			super.onTouchEvent(ev);
		} else {
			MotionEvent.PointerCoords p1 = new MotionEvent.PointerCoords();
			ev.getPointerCoords(ev.getPointerId(0), p1);
			MotionEvent.PointerCoords p2 = new MotionEvent.PointerCoords();
			ev.getPointerCoords(ev.getPointerId(1), p2);
			//LogUtil.i(this, "---------------- dist=%.3f : action=%d", MathUtil.Distance(p1, p2), ev.getActionMasked());
		}
		return true;
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

		fillRankingList(rankings);
	}

	Float scrollNext;

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int minChildHeight = Integer.MAX_VALUE;
		for (int i = 0; i < binding.listRankingViews.getChildCount(); ++i) {
			minChildHeight = Math.min(minChildHeight, binding.listRankingViews.getChildAt(i).getMeasuredHeight());
		}

		for (int i = 0; i < binding.listRankingViews.getChildCount(); ++i) {
			RankingView child = (RankingView) binding.listRankingViews.getChildAt(i);
			child.setSharedHeight(minChildHeight);
		}

		super.onLayout(changed, l, t, r, b);

		if (scrollNext != null) {
			setScrollY(scrollNext.intValue());
			scrollNext = null;
		}
	}

	private void scale(final float detectedScaleFactor, final float focusY, float currentSpan) {
		//for (int i = 0; i < binding.listRankingViews.getChildCount(); ++i) {
		// FIXME: 26.01.2017 strange jump on (first?) zoom in

		View root = binding.listRankingViews;
		ViewGroup.LayoutParams params = root.getLayoutParams();
		float rootHeight = params.height > 0 ? params.height : root.getHeight();

		float scaleFactor = 1 + (detectedScaleFactor - 1) * 4;

		float curScrollY = scrollNext == null ? getScrollY() : scrollNext;

		float scrollY = curScrollY + focusY;
		float coef = scrollY / rootHeight;

		final float newHeight = MathUtil.InRange(rootHeight * scaleFactor, rankingsListViewHeightMin, 1_000_000.0f),
				newScrollY = newHeight * coef - focusY;//scrollY * scaleFactor;

		//LogUtil.log("--------------------------------");

		LogUtil.log("-------------------------------- [factor=%.3f(dist=%.3f)] prev:%d(%.3f), next:%d(%.3f); height::newHeight = %d::%d (PARAMS:%d) ++ focusY=%.2f",
				scaleFactor, currentSpan,
				(int) scrollY, coef,
				(int) (newScrollY + focusY), (newScrollY + focusY) / newHeight,
				(int) rootHeight, (int) newHeight, params.height,
				focusY);

		//View rootViewGroup = binding.listRankingViews.getChildAt(i);
		params.height = (int) newHeight;
		root.setLayoutParams(params);

			/*post(new Runnable() {
				@Override
				public void run() {
					setScrollY((int) newScrollY);
				}
			});*/
		scrollNext = newScrollY; // postpone because actual height will changed after onLayout, so may happen overscroll here
		//setScrollY((int) newScrollY);//(newScrollY - detector.getFocusY()));
		//}
	}

	class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return super.onScaleBegin(detector);
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scale(detector.getScaleFactor(), detector.getFocusY(), detector.getCurrentSpan());
			return true;
		}
	}
}
