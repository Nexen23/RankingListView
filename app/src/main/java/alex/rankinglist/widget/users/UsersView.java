package alex.rankinglist.widget.users;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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
	private boolean didInitViews = false;


	private boolean composingAnimationEnabled = true, breakingAnimationEnabled = true;
	int animationsDuration;
	HashMap<GroupNode, GroupView> groupsViews = new HashMap<>(), animationsViews = new HashMap<>();
	LinkedList<GroupingAnimation> animations = new LinkedList<>();

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
		animationsDuration = getResources().getInteger(R.integer.animation_fast_duration);
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
			initViewsIfNeeded();
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

	@Override
	public void onBreak(GroupNode removedGroup, GroupNode a, GroupNode b) {
		final GroupView removedView = groupsViews.get(removedGroup);

		addGroupView(a);
		addGroupView(b);

		if (composingAnimationEnabled) {
			GroupingAnimation.Stop(removedView);
		}

		if (breakingAnimationEnabled) {
			BreakingAnimation.Start(this, removedGroup, a, b);
		} else {
			removeGroupView(removedGroup);
		}
	}

	@Override
	public void onGroup(GroupNode composedGroup, GroupNode a, GroupNode b) {
		addGroupView(composedGroup);

		if (breakingAnimationEnabled) {
			GroupingAnimation.Stop(groupsViews.get(a), groupsViews.get(b));
		}

		if (composingAnimationEnabled) {
			ComposingAnimation.Start(this, composedGroup, a, b);
		} else {
			removeGroupView(a);
			removeGroupView(b);
		}
	}

	void makeAnimationView(GroupNode group) {
		final GroupView usedView = groupsViews.remove(group);
		animationsViews.put(group, usedView);
	}

	void removeAnimationView(GroupNode group) {
		final GroupView removedView = animationsViews.remove(group);
		removeView(removedView);
	}

	private GroupView addGroupView(GroupNode group) {
		final GroupView view = new GroupView(getContext());

		if (group.isLeaf()) {
			view.setModel(group.getData());
		} else {
			view.setModel(group.getData(), group.getItemsCount(), group.getAvgScore(rank));
		}

		groupsViews.put(group, view);
		addView(view);
		return view;
	}

	private void removeGroupView(GroupNode group) {
		final GroupView removedView = groupsViews.remove(group);
		removeView(removedView);
	}

	private void updateChilds() {
		for (GroupNode group : groupedList) {
			GroupView view = groupsViews.get(group);
			if (view.getTag() == null) {
				view.setY(group.getAbsolutePos(getHeight()));
			}
		}
	}

	private void updateAnimations() {
		for (GroupingAnimation animation : animations) {
			animation.update();
		}
	}

	private void initViewsIfNeeded() {
		if (!didInitViews) {
			didInitViews = true; // skip first grouping events

			for (GroupNode group : groupedList) {
				addGroupView(group);
			}
			groupedList.addListener(this);
		}
	}
}
