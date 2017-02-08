package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
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
	private LinkedList<View> animationViews = new LinkedList<>();
	private HashMap<GroupNode, GroupView> groupsViews = new HashMap<>();

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
		GroupView recycledView = groupsViews.remove(removedGroup);
		groupsViews.put(a, recycledView);

		GroupView newView = new GroupView(getContext());
		addView(newView);
		groupsViews.put(b, newView);
	}

	private void updateGroupsViews() {
		Assert.assertEquals(getChildCount(), groupedList.getGroupsCount());

		for (GroupNode group : groupedList) {
			GroupView view = groupsViews.get(group);
			view.setY(group.getAbsolutePos(getHeight()));

			if (group.isLeaf()) {
				view.setModel(group.getData());
			} else {
				view.setModel(group.getData(), group.getItemsCount(), group.getAvgScore(rank));
			}
		}
	}
}
