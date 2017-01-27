package alex.rankinglist.widget;

import android.content.Context;
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
	}

	private void fillRankingList(List<Ranking> rankings) {
		for (Ranking ranking : rankings) {
			RankingView rankingView = new RankingView(getContext());
			rankingView.setModel(ranking);
			binding.listRankingViews.addView(rankingView, 0);

			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rankingView.getLayoutParams();
			params.weight = 1;
			params.height = 0;
			rankingView.setLayoutParams(params);
		}
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

	class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return super.onScaleBegin(detector);
		}



		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			//for (int i = 0; i < binding.listRankingViews.getChildCount(); ++i) {
				// FIXME: 26.01.2017 strange jump on (first?) zoom in
				LogUtil.log("--------------------------------");
				//View rootViewGroup = binding.listRankingViews.getChildAt(i);
				View rootViewGroup = binding.listRankingViews;
				ViewGroup.LayoutParams params = rootViewGroup.getLayoutParams();
				float scaleFactor = 1 + (detector.getScaleFactor() - 1) * 3;
				params.height = (int) (rootViewGroup.getHeight() * scaleFactor);
				rootViewGroup.setLayoutParams(params);
			//}
			return true;
		}
	}
}
