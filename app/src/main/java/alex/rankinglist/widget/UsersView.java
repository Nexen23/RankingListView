package alex.rankinglist.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
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
	private int fadeDuration, moveDuration;
	private boolean composingAnimationEnabled = true, breakingAnimationEnabled = false,
		anyAnimationEnabled = composingAnimationEnabled || breakingAnimationEnabled;

	private Rank rank;
	private GroupedList groupedList;
	private boolean didInitViews = false;
	private HashMap<GroupNode, GroupView> groupsViews = new HashMap<>(), animationsViews = new HashMap<>();
	private LinkedList<GroupingAnimation> animations = new LinkedList<>();

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
		fadeDuration = 1500;
		moveDuration = fadeDuration;
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
			if (!didInitViews) {
				// skip first grouping events
				didInitViews = true;
				initViews();
			}
			Assert.assertEquals(getChildCount(), groupsViews.size() + animationsViews.size());
			updateChilds();
			updateAnimations();

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

	private void initViews() {
		groupedList.addListener(this);

		for (GroupNode group : groupedList) {
			addGroupView(group);
		}
	}

	private GroupView addGroupView(GroupNode group) {
		final GroupView view = newView(group);
		addView(view);
		groupsViews.put(group, view);
		return view;
	}

	private void removeGroupView(GroupNode group) {
		final GroupView removedView = groupsViews.remove(group);
		removeView(removedView);
	}

	private void makeAnimationView(GroupNode group) {
		final GroupView usedView = groupsViews.remove(group);
		animationsViews.put(group, usedView);
	}

	private void removeAnimationView(GroupNode group) {
		final GroupView removedView = animationsViews.remove(group);
		removeView(removedView);
	}

	private void updateChilds() {
		for (GroupNode group : groupedList) {
			GroupView view = groupsViews.get(group);
			view.setY(group.getAbsolutePos(getHeight()));
		}
	}

	private void updateAnimations() {
		for (GroupingAnimation animation : animations) {
			animation.update();
		}
	}

	@Override
	public void onBreak(GroupNode removedGroup, GroupNode a, GroupNode b) {
		addGroupView(a);
		addGroupView(b);

		if (breakingAnimationEnabled) {
			//startBreakingAnimation(removedGroup, a, b);
		} else {
			removeGroupView(removedGroup);
		}
	}

	@Override
	public void onGroup(GroupNode composedGroup, GroupNode a, GroupNode b) {
		GroupView composedView = addGroupView(composedGroup);
		composedView.bringToFront();

		if (composingAnimationEnabled) {
			startComposingAnimation(composedGroup, a, b);
		} else {
			removeGroupView(a);
			removeGroupView(b);
		}
	}

	private void startComposingAnimation(GroupNode composedGroup, GroupNode a, GroupNode b) {
		final ComposingAnimation animation = new ComposingAnimation(composedGroup, a, b);
		makeAnimationView(a);
		makeAnimationView(b);
		stopAnimation(animation.aView);
		stopAnimation(animation.bView);
		animations.add(animation);

		animation.jointGroupView.setTag(animation);
		animation.start();
	}

	private void startBreakingAnimation(GroupNode removedGroup, GroupNode a, GroupNode b) {
		final BreakingAnimation animation = new BreakingAnimation(removedGroup, a, b);
		stopAnimation(animation.aView);
		stopAnimation(animation.bView);
		animations.add(animation);

		animation.aView.setTag(animation);
		animation.bView.setTag(animation);
		animation.start();
	}

	private void stopAnimation(GroupView view) {
		if (anyAnimationEnabled && view.getTag() != null) {
			final GroupingAnimation animation = (GroupingAnimation) view.getTag();
			animation.cancel();
			view.animate().cancel();
			animations.remove(animation);
		}
	}

	class BreakingAnimation extends GroupingAnimation {
		BreakingAnimation(GroupNode composedGroup, GroupNode a, GroupNode b) {
			super(composedGroup, a, b);
		}

		@Override
		void cleanUp() {
			removeGroupView(jointGroup);
			aView.setTag(null);
			bView.setTag(null);
			animations.remove(this);
		}

		@Override
		void update() {
			updatePosition(aView, a);
			updatePosition(bView, b);
		}

		void updatePosition(ViewGroup view, GroupNode thisGroup) {
			final float fromPos = jointGroup.getAbsolutePos(getHeight());
			final float toPos = thisGroup.getAbsolutePos(getHeight());
			final float distance = toPos - fromPos;

			view.setY(fromPos + distance * animatedValue);
		}
	}

	class ComposingAnimation extends GroupingAnimation {
		ComposingAnimation(GroupNode composedGroup, GroupNode a, GroupNode b) {
			super(composedGroup, a, b);
		}

		@Override
		void cleanUp() {
			removeAnimationView(a);
			removeAnimationView(b);
			jointGroupView.setTag(null);
			animations.remove(this);
		}

		@Override
		void update() {
			updatePosition(aView, a);
			updatePosition(bView, b);
		}

		void updatePosition(ViewGroup view, GroupNode thisGroup) {
			final float targetGroupPos = jointGroup.getAbsolutePos(getHeight());
			final float thisGroupPos = thisGroup.getAbsolutePos(getHeight());
			final float distance = targetGroupPos - thisGroupPos;

			view.setY(thisGroupPos + distance * animatedValue);
		}
	}

	abstract class GroupingAnimation {
		final ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
		float animatedValue = 0;
		final GroupNode jointGroup, a, b;
		final GroupView jointGroupView, aView, bView;

		public GroupingAnimation(GroupNode jointGroup, GroupNode a, GroupNode b) {
			animator.addListener(new CleanUpAnimatorListener());
			animator.setDuration(moveDuration)
					.setInterpolator(new LinearInterpolator());
			animator.addUpdateListener(animation -> setAnimatedValue((float) animation.getAnimatedValue()));

			this.jointGroup = jointGroup;
			this.a = a;
			this.b = b;

			jointGroupView = groupsViews.get(jointGroup);
			aView = groupsViews.get(a);
			bView = groupsViews.get(b);
		}

		final void start() {
			animator.start();
			jointGroupView.setAlpha(0);
			jointGroupView.animate()
					.alpha(1)
					.setDuration(fadeDuration)
					.setInterpolator(new LinearInterpolator())
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationCancel(Animator animation) {
							jointGroupView.setAlpha(1);
						}
					});
		}

		final void cancel() {
			animator.cancel();
			jointGroupView.animate().cancel();
		}

		final void setAnimatedValue(float animatedValue) {
			this.animatedValue = animatedValue;
			update();
		}

		abstract void cleanUp();
		abstract void update();

		class CleanUpAnimatorListener extends AnimatorListenerAdapter {
			@Override
			public void onAnimationEnd(Animator animation) {
				cleanUp();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				cleanUp();
				LogUtil.d(this, "ANIMATION : canceled");
			}
		}
	}








	/*void setComposeAnimation(final View view, final GroupNode a, GroupNode target) {
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

				Both<View> pair = composingViews.get(target);
				if (pair != null) {
					if (pair.first == view) {
						pair.first = null;
					}
					if (pair.second == view) {
						pair.second = null;
					}

					if (pair.first == null && pair.second == null) {
						composingViews.remove(target);
					}
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				LogUtil.d(this, "ANIMATION:: canceled", animation.getDuration());
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		animator.start();
	}

	HashMap<View, Both<View>> composingViews = new HashMap<>();

	@Override
	public void onGroup3(GroupNode composedGroup, GroupNode a, GroupNode b) {
		GroupView composedView = newView();
		groupsViews.put(composedGroup, composedView);
		if (composingAnimationEnabled) {
			setViewData(composedView, composedGroup);
			composedView.animate().cancel();
			composedView.setAlpha(0);
			composedView.animate()
					.alpha(1)
					//.xBy(-100)
					.setDuration(fadeDuration)
					.setInterpolator(new LinearInterpolator());
					//.setInterpolator(new DecelerateInterpolator());
		}

		final Both<View> composingParts = Both.create(setComposingAnimationForLeaf(a, composedGroup),
				setComposingAnimationForLeaf(b, composedGroup));
		composingViews.put(composedView, composingParts);


		addView(composedView, 0);
		composedView.bringToFront();
	}

	GroupView setComposingAnimationForLeaf(final GroupNode node, GroupNode target) {
		final GroupView view = groupsViews.remove(node);
		stopComposingAnimationFor(view);
		setViewData(view, node);
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
		return view;
	}

	void stopComposingAnimationFor(final View view) {
		final Both<View> composingParts = composingViews.remove(view);

		if (composingParts != null) {
			if (composingParts.first != null && composingParts.first.getTag() != null
					&& composingParts.first.getTag() instanceof AnimationData) {
				((AnimationData) composingParts.first.getTag()).animator.cancel();
			}

			if (composingParts.second != null && composingParts.second.getTag() != null
					&& composingParts.second.getTag() instanceof AnimationData) {
				((AnimationData) composingParts.second.getTag()).animator.cancel();
			}
		}
	}

	@Override
	public void onBreak3(GroupNode removedGroup, GroupNode a, GroupNode b) {
		final GroupView removedView = groupsViews.remove(removedGroup);
		if (composingAnimationEnabled) {
			stopComposingAnimationFor(removedView);
		}
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


		GroupView newView = newView();
		addView(newView, 0);
		groupsViews.put(a, newView);
		if (breakingAnimationEnabled) {
			setAnimation(newView, a, initPos);
			setViewData(newView, a);
		}

		newView = newView();
		addView(newView, 0);
		groupsViews.put(b, newView);
		if (breakingAnimationEnabled) {
			setAnimation(newView, b, initPos);
			setViewData(newView, b);
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

	static class Both<T> {
		public T first, second;

		public Both(T first, T second) {
			this.first = first;
			this.second = second;
		}

		public static <T> Both<T> create(T first, T second) {
			return new Both<>(first, second);
		}
	}*/

	GroupView newView(GroupNode group) {
		final GroupView view = new GroupView(getContext());

		if (group.isLeaf()) {
			view.setModel(group.getData());
		} else {
			view.setModel(group.getData(), group.getItemsCount(), group.getAvgScore(rank));
		}

		return view;
	}
}
