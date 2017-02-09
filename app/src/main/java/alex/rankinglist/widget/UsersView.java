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
	private boolean didSkippedFirstGrouping = false;
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
		//moveDuration = getResources().getInteger(R.integer.animation_fast_duration);
		fadeDuration = 150;
		//moveDuration = 3000;
		groupedList = new GroupedList(getResources().getDimensionPixelSize(R.dimen.group_view_height));
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
			if (!didSkippedFirstGrouping) {
				didSkippedFirstGrouping = true;
				groupedList.addListener(this);

				for (GroupNode group : groupedList) {
					final GroupView view = new GroupView(getContext());
					groupsViews.put(group, view);
					addView(view);
				}
			}
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
	}

	boolean composingAnimationEnabled = true;

	void setComposeAnimation(final View view, final GroupNode a, GroupNode target) {
		float finalNormalizedPos = target.getAbsolutePos(getHeight()) / getHeight();
		ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
		view.setTag(new AnimationData(animator, a, finalNormalizedPos, false, target));

		animator.setDuration(fadeDuration).setInterpolator(new LinearInterpolator());
		animator.addUpdateListener(animation -> {
			final AnimationData tag = (AnimationData) view.getTag();
			tag.setAnimatedValue(((float) animation.getAnimatedValue()));
			tag.updateY(view);
		});
		animator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				animatedViews.remove(view);
				removeView(view);
				final AnimationData tag = (AnimationData) view.getTag();
				tag.setAnimatedValue(1);
				tag.updateY(view);
				view.setTag(null);
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

	@Override
	public void onGroup(GroupNode composedGroup, GroupNode a, GroupNode b) {
		GroupView composedView = new GroupView(getContext());
		groupsViews.put(composedGroup, composedView);
		if (composingAnimationEnabled) {
			setViewModel(composedView, composedGroup);
			composedView.animate().cancel();
			composedView.setAlpha(0);
			composedView.animate()
					.alpha(1)
					//.xBy(-100)
					.setDuration(fadeDuration)
					.setInterpolator(new LinearInterpolator());
					//.setInterpolator(new DecelerateInterpolator());
		}

		setComposingAnimationForLeaf(a, composedGroup);
		setComposingAnimationForLeaf(b, composedGroup);


		addView(composedView, 0);
		composedView.bringToFront();
	}

	void setComposingAnimationForLeaf(final GroupNode node, GroupNode target) {
		final GroupView view = groupsViews.remove(node);
		setViewModel(view, node);
		if (composingAnimationEnabled) {
			view.animate().cancel();
			if (view.getTag() instanceof AnimationData) {
				((AnimationData) view.getTag()).animator.cancel();
			}

			animatedViews.add(view);
			setComposeAnimation(view, node, target);
		} else {
			removeView(view);
		}
	}

	boolean breakingAnimationEnabled = false;

	@Override
	public void onBreak(GroupNode removedGroup, GroupNode a, GroupNode b) {
		final GroupView removedView = groupsViews.remove(removedGroup);
		if (breakingAnimationEnabled) {
			removedView.animate().cancel();
			if (removedView.getTag() instanceof AnimationData) {
				((AnimationData) removedView.getTag()).animator.cancel();
			}
			removedView.animate()
					.alpha(0)
					.setDuration(fadeDuration)
					//.setInterpolator(new DecelerateInterpolator())
					.setInterpolator(new LinearInterpolator())
					.withEndAction(() -> {
						removeView(removedView);
						animatedViews.remove(removedView);
					});
			removedView.setTag(removedGroup);
			animatedViews.add(removedView);
		} else {
			removeView(removedView);
		}

		float initPos = removedGroup.getAbsolutePos(getHeight());


		GroupView newView = new GroupView(getContext());
		addView(newView, 0);
		groupsViews.put(a, newView);
		if (breakingAnimationEnabled) {
			setAnimation(newView, a, initPos);
			setViewModel(newView, a);
		}

		newView = new GroupView(getContext());
		addView(newView, 0);
		groupsViews.put(b, newView);
		if (breakingAnimationEnabled) {
			setAnimation(newView, b, initPos);
			setViewModel(newView, b);
		}
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
			if (view.getTag() instanceof AnimationData) {
				final AnimationData tag = (AnimationData) view.getTag();
				tag.updateY(view);
			} else if (view.getTag() instanceof GroupNode) {
				final GroupNode group = (GroupNode) view.getTag();
				view.setY(group.getAbsolutePos(getHeight()));
			} else {
				throw new IllegalStateException("all animated views should have tag of GroupNode or AnimationData -type");
			}
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
		public float normalizedPos;
		public Animator animator;
		private float animatedValue = 0;

		private boolean posIsInit = true; // or final
		private GroupNode target;

		public AnimationData(Animator animator, GroupNode group, float normalizedPos) {
			this.animator = animator;
			this.group = group;
			this.normalizedPos = normalizedPos;
		}

		public AnimationData(Animator animator, GroupNode group, float normalizedPos, boolean posIsInit, GroupNode target) {
			this.animator = animator;
			this.group = group;
			this.normalizedPos = normalizedPos;
			this.posIsInit = posIsInit;
			this.target = target;
		}

		public void setAnimatedValue(float animatedValue) {
			this.animatedValue = animatedValue;
		}

		public void updateY(View view) {
			final float shouldBeAbsolutePos = group.getAbsolutePos(getHeight());
			float absolutePos = getHeight() * normalizedPos;

			if (!posIsInit) {
				absolutePos = target.getAbsolutePos(getHeight());
			}

			final float distance = absolutePos - shouldBeAbsolutePos;

			view.setY((posIsInit ? absolutePos : shouldBeAbsolutePos) - distance * animatedValue * (posIsInit ? 1 : -1));
			//LogUtil.d(this, "ANIMATION:: %.2f", animatedValue);
		}
	}
}
