package alex.rankinglist.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import alex.rankinglist.R;
import alex.rankinglist.misc.grouping.GroupNode;
import alex.rankinglist.misc.grouping.GroupedList;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;


public class UsersView extends FrameLayout implements GroupedList.EventsListener {
	private Rank rank;
	private GroupedList groupedList;
	private LinkedList<View> animatedViews = new LinkedList<>();
	private HashMap<GroupNode, GroupView> groupsViews = new HashMap<>();
	private int fadeDuration, moveDuration;

	public UsersView(Context context) {
		super(context);
		init();
	}

	public UsersView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public UsersView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		//fadeDuration = getResources().getInteger(R.integer.animation_fast_duration);
		//moveDuration = getResources().getInteger(R.integer.animation_slow_duration);
		fadeDuration = 150;
		moveDuration = 300;
		groupedList = new GroupedList(getResources().getDimensionPixelSize(R.dimen.group_view_height));
		groupedList.addListener(this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		LogUtil.d(this, "onMeasure(%s)", LogUtil.MeasureSpecToString(heightMeasureSpec));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		LogUtil.d(this, "onSizeChanged()");
		super.onSizeChanged(w, h, oldw, oldh);
		if (groupedList.setSpace(h)) {
			updateGroupsViews();

			int widthSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
			int heightSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
			measureChildren(widthSpec, heightSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		LogUtil.d(this, "onLayout()");
		super.onLayout(changed, left, top, right, bottom);
	}

	public void setModel(Rank rank, List<User> users) {
		this.rank = rank;
		groupedList.setData(rank, users);
		for (GroupNode group : groupedList) {
			final GroupView view = new GroupView(getContext());
			groupsViews.put(group, view);
			addView(view);
		}
	}

	@Override
	public void onGroup(GroupNode composedGroup, GroupNode a, GroupNode b) {
		removeView(groupsViews.remove(a));
		GroupView recycledView = groupsViews.remove(b);
		groupsViews.put(composedGroup, recycledView);
	}

	@Override
	public void onBreak(GroupNode removedGroup, GroupNode a, GroupNode b) {
		final GroupView removedView = groupsViews.remove(removedGroup);
		removedView.animate().cancel();
		if (removedView.getTag() instanceof AnimationData) {
			((AnimationData) removedView.getTag()).animator.cancel();
		}
		removedView.setTag(removedGroup);
		animatedViews.add(removedView);
		removedView.animate()
				.alpha(0)
				.setDuration(fadeDuration)
				.setInterpolator(new LinearInterpolator())
				.withEndAction(() -> {
					removeView(removedView);
					animatedViews.remove(removedView);
				});

		float initPos = removedGroup.getAbsolutePos(getHeight());


		GroupView newView = new GroupView(getContext());
		addView(newView, 0);
		groupsViews.put(a, newView);
		setAnimation(newView, a, initPos);
		setViewModel(newView, a);

		newView = new GroupView(getContext());
		addView(newView, 0);
		groupsViews.put(b, newView);
		setAnimation(newView, b, initPos);
		setViewModel(newView, b);
	}


	void setAnimation(final View newView, final GroupNode a, float initPos) {
		float initNormalizedPos = initPos / getHeight();
		ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
		newView.setTag(new AnimationData(animator, a, initNormalizedPos));

		animator.setDuration(fadeDuration).setInterpolator(new LinearInterpolator());
		animator.addUpdateListener(animation -> {
			final AnimationData tag = (AnimationData) newView.getTag();
			tag.setAnimatedValue(((float) animation.getAnimatedValue()));
			tag.updateY(newView);
		});
		animator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				final AnimationData tag = (AnimationData) newView.getTag();
				tag.setAnimatedValue(1);
				tag.updateY(newView);
				newView.setTag(null);
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		animator.start();
	}

	private void updateGroupsViews() {
		Assert.assertEquals(getChildCount(), groupedList.getGroupsCount() + animatedViews.size());

		for (GroupNode group : groupedList) {
			GroupView view = groupsViews.get(group);
			if (view.getTag() == null) {
				view.setY(group.getAbsolutePos(getHeight()));
			} else {
				if (view.getTag() instanceof AnimationData) {
					((AnimationData) view.getTag()).updateY(view);
				} else {
					throw new IllegalStateException("all views with GroupNode in tag should be in animatedViews array");
				}
			}

			setViewModel(view, group);
		}

		for (View view : animatedViews) {
			final GroupNode group = (GroupNode) view.getTag();
			view.setY(group.getAbsolutePos(getHeight()));
		}
	}

	private void setViewModel(GroupView view, GroupNode group) {
		if (group.isLeaf()) {
			view.setModel(group.getData());
		} else {
			view.setModel(group.getData(), group.getItemsCount(), group.getAvgScore(rank));
		}
	}

	class AnimationData {
		public GroupNode group;
		public float initNormalizedPos;
		public Animator animator;
		private float animatedValue;

		public AnimationData(Animator animator, GroupNode group, float initNormalizedPos) {
			this.animator = animator;
			this.group = group;
			this.initNormalizedPos = initNormalizedPos;
		}

		public void setAnimatedValue(float animatedValue) {
			this.animatedValue = animatedValue;
		}

		public void updateY(View view) {
			final float shouldBeAbsolutePos = group.getAbsolutePos(getHeight());
			final float initAbsolutePos = getHeight() * initNormalizedPos;
			final float distance = initAbsolutePos - shouldBeAbsolutePos;

			view.setY(initAbsolutePos - distance * animatedValue);
			//LogUtil.d(this, "ANIMATION:: %.2f", animatedValue);
		}
	}
}
