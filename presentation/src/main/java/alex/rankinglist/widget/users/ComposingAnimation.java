package alex.rankinglist.widget.users;

import alex.domain.grouping.GroupNode;

class ComposingAnimation extends GroupingAnimation {
	protected ComposingAnimation(UsersView usersView, GroupNode composedGroup, GroupNode a, GroupNode b) {
		super(usersView, composedGroup, a, b);
		usersView.makeBackAnimationView(a);
		usersView.makeBackAnimationView(b);
		jointGroupView.setTag(this);
	}

	static void Start(UsersView usersView, GroupNode composedGroup, GroupNode a, GroupNode b) {
		final ComposingAnimation animation = new ComposingAnimation(usersView, composedGroup, a, b);
		animation.start();
	}

	@Override
	protected void cleanUp() {
		jointGroupView.setTag(null);
		usersView.removeAnimationView(a);
		usersView.removeAnimationView(b);
		super.cleanUp();
	}

	@Override
	protected void updateTwainView(int space, GroupView view, GroupNode group) {
		final float toPos = jointGroup.getAbsolutePos(space);
		final float fromPos = group.getAbsolutePos(space);
		final float distance = toPos - fromPos;

		view.setY(fromPos + distance * animatedValue);
	}

	@Override
	protected AlphaAnimation getJointViewAnimation() {
		return AlphaAnimation.FADE_IN;
	}
}
