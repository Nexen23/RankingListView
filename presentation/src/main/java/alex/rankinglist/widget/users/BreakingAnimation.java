package alex.rankinglist.widget.users;

import alex.domain.grouping.GroupNode;

class BreakingAnimation extends GroupingAnimation {
	protected BreakingAnimation(UsersView usersView, GroupNode composedGroup, GroupNode a, GroupNode b) {
		super(usersView, composedGroup, a, b);
		usersView.makeFrontAnimationView(composedGroup);
		aView.setTag(this);
		bView.setTag(this);
	}

	static void Start(UsersView usersView, GroupNode composedGroup, GroupNode a, GroupNode b) {
		final BreakingAnimation animation = new BreakingAnimation(usersView, composedGroup, a, b);
		animation.start();
	}

	@Override
	protected void cleanUp() {
		aView.setTag(null);
		bView.setTag(null);
		usersView.removeAnimationView(jointGroup);
		super.cleanUp();
	}

	@Override
	protected void updateTwainView(int space, GroupView view, GroupNode group) {
		final float fromPos = jointGroup.getAbsolutePos(space);
		final float toPos = group.getAbsolutePos(space);
		final float distance = toPos - fromPos;

		view.setY(fromPos + distance * animatedValue);
	}

	@Override
	protected AlphaAnimation getJointViewAnimation() {
		return AlphaAnimation.FADE_OUT;
	}
}
