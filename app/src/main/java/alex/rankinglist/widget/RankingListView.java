package alex.rankinglist.widget;

import android.content.Context;
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

import alex.rankinglist.databinding.WidgetRankingListBinding;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.Ranking;
import alex.rankinglist.widget.model.User;


public class RankingListView extends ScrollView {
	WidgetRankingListBinding binding;
	ScaleGestureDetector scaleDetector;
	Integer rankingsListViewHeightMin;

	public RankingListView(Context context) {
		super(context);
		init();
	}

	public RankingListView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RankingListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setFillViewport(true);
		binding = WidgetRankingListBinding.inflate(LayoutInflater.from(getContext()), this, true);
		scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

		// HACK: fix for flinches of first scale event (it ignores minSpan)
		long time = SystemClock.uptimeMillis();
		MotionEvent motionEvent = MotionEvent.obtain(time - 100, time, MotionEvent.ACTION_CANCEL,
				0.0f, 0.0f, 0);
		scaleDetector.onTouchEvent(motionEvent);
		motionEvent.recycle();
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

	private void scale(final float detectedScaleFactor, final float focusY) {
		//for (int i = 0; i < binding.listRankingViews.getChildCount(); ++i) {
		// FIXME: 26.01.2017 strange jump on (first?) zoom in

		View root = binding.listRankingViews;
		ViewGroup.LayoutParams params = root.getLayoutParams();
		float rootHeight = params.height > 0 ? params.height : root.getHeight();

		float scaleFactor = 1 + (detectedScaleFactor - 1) * 4;

		float curScrollY = scrollNext == null ? getScrollY() : scrollNext;

		float scrollY = curScrollY + focusY;
		float coef = scrollY / rootHeight;

		final float newHeight = Math.max(rootHeight * scaleFactor, rankingsListViewHeightMin),
				newScrollY = newHeight * coef - focusY;//scrollY * scaleFactor;

		//LogUtil.log("--------------------------------");

		LogUtil.log("-------------------------------- [factor=%.3f] prev:%d(%.3f), next:%d(%.3f); height::newHeight = %d::%d (PARAMS:%d) ++ focusY=%.2f",
				scaleFactor,
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
			scale(detector.getScaleFactor(), detector.getFocusY());
			return true;
		}
	}
}
