package alex.rankinglist.widget.users;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.annotation.CallSuper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import alex.rankinglist.misc.grouping.GroupNode;

abstract class GroupingAnimation {
	final UsersView usersView;
	final ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
	float animatedValue = 0;
	final GroupNode jointGroup, a, b;
	final GroupView jointGroupView, aView, bView;

	protected GroupingAnimation(UsersView usersView, GroupNode jointGroup, GroupNode a, GroupNode b) {
		this.usersView = usersView;
		animator.addListener(new CleanUpAnimatorListener());
		animator.setDuration(usersView.animationsDuration)
				.setInterpolator(new DecelerateInterpolator());
		animator.addUpdateListener(animation -> setAnimatedValue((float) animation.getAnimatedValue()));

		this.jointGroup = jointGroup;
		this.a = a;
		this.b = b;

		jointGroupView = usersView.groupsViews.get(jointGroup);
		aView = usersView.groupsViews.get(a);
		bView = usersView.groupsViews.get(b);

		GroupingAnimation.Stop(aView, bView, jointGroupView);
		jointGroupView.bringToFront();
		usersView.animations.add(this);
	}

	static void Stop(View... views) {
		for (View view : views) {
			if (view.getTag() != null) {
				final GroupingAnimation animation = (GroupingAnimation) view.getTag();
				animation.cancel();
			}
		}
	}

	final void start() {
		animator.start();

		final AlphaAnimation alphaAnimation = getJointViewAnimation();
		final float from = alphaAnimation == AlphaAnimation.FADE_IN ? 0 : 1;
		final float to = alphaAnimation == AlphaAnimation.FADE_IN ? 1 : 0;

		jointGroupView.setAlpha(from);
		jointGroupView.animate()
				.alpha(to)
				.setDuration(usersView.animationsDuration)
				.setInterpolator(new LinearInterpolator())
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationCancel(Animator animation) {
						jointGroupView.setAlpha(to);
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

	final void update() {
		final int height = usersView.getHeight();
		updateTwainView(height, aView, a);
		updateTwainView(height, bView, b);
		jointGroupView.setY(jointGroup.getAbsolutePos(height));
		usersView.invalidate();
	}

	@CallSuper
	protected void cleanUp() {
		usersView.animations.remove(this);
		usersView.invalidate();
	}

	protected abstract void updateTwainView(int space, GroupView view, GroupNode group);
	protected abstract AlphaAnimation getJointViewAnimation();

	private class CleanUpAnimatorListener extends AnimatorListenerAdapter {
		@Override
		public void onAnimationEnd(Animator animation) {
			cleanUp();
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			cleanUp();
		}
	}

	enum AlphaAnimation {
		FADE_IN, FADE_OUT
	}
}
