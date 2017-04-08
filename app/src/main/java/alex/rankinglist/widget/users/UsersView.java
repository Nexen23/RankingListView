package alex.rankinglist.widget.users;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import alex.rankinglist.R;
import alex.rankinglist.misc.grouping.GroupNode;
import alex.rankinglist.misc.grouping.GroupedList;
import alex.rankinglist.util.LogUtil;
import alex.rankinglist.widget.model.Rank;
import alex.rankinglist.widget.model.User;


public class UsersView extends FrameLayout implements GroupedList.EventsListener {
	private Rank rank;
	private GroupedList groupedList;
	private boolean didSkipFirstGroupingEvents = false;
	private Rect visibleRect = new Rect();

	private boolean composingAnimationEnabled = true, breakingAnimationEnabled = true;
	int animationsDuration;
	@Px int viewHeight;
	private HashMap<GroupNode, GroupView> groupsViews = new HashMap<>(),
			animationsViewsBack = new HashMap<>(), animationsViewsFront = new HashMap<>();
	LinkedList<GroupingAnimation> animations = new LinkedList<>();
	GroupNode firstVisibleGroup = null, lastVisibleGroup = null;
	final LinkedList<GroupView> viewsPool = new LinkedList<>();

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
		animationsDuration = 1000;//getResources().getInteger(R.integer.animation_fast_duration);
		viewHeight = getResources().getDimensionPixelSize(R.dimen.group_view_height);
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
			registerGroupingEventsListenerIfNeeded();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		LogUtil.d(this, "onLayout()");
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (isVisibleOnScreen()) {
			for (Map.Entry<GroupNode, GroupView> entry : animationsViewsBack.entrySet()) {
				drawChild(canvas, entry.getValue(), getDrawingTime());
			}

			GroupNode group = firstVisibleGroup;
			while (group != lastVisibleGroup.getNext()) {
				drawChild(canvas, getGroupView(group), getDrawingTime());
				group = group.getNext();
			}

			for (Map.Entry<GroupNode, GroupView> entry : animationsViewsFront.entrySet()) {
				drawChild(canvas, entry.getValue(), getDrawingTime());
			}
		}
	}

	public void setModel(Rank rank, List<User> users) {
		didSkipFirstGroupingEvents = false;
		firstVisibleGroup = null;
		lastVisibleGroup = null;
		viewsPool.clear();
		this.rank = rank;
		groupedList.setData(rank, users);
	}

	public void onVisibleFrameChanged() {
		final boolean isVisible = getLocalVisibleRect(visibleRect);

		if (isVisible) {
			updateVisibleGroups();
			if (isVisibleOnScreen()) {
				updateChilds();
				updateAnimations();
			}
			invalidate();
		} else {
			firstVisibleGroup = null;
			lastVisibleGroup = null;
			groupsViews.clear();
		}

		LogUtil.i(this, "TEST: rank[%d; %d] first=%s, last=%s \t:: \tvisible=%s, pool=%d, views=%d, animations=%d",
				rank.scoreMin, rank.scoreMax,
				firstVisibleGroup == null ? "null" : firstVisibleGroup.getData().name,
				lastVisibleGroup == null ? "null" : lastVisibleGroup.getData().name,
				isVisible, viewsPool.size(), groupsViews.size(), animationsViewsBack.size());
	}

	@Override
	public void onBreak(GroupNode removedGroup, GroupNode a, GroupNode b) {
		if (firstVisibleGroup == removedGroup) {
			firstVisibleGroup = a;
		}

		if (lastVisibleGroup == removedGroup) {
			lastVisibleGroup = b;
		}

		if (breakingAnimationEnabled) {// && isGroupVisible(removedGroup, getHeight())) {
			BreakingAnimation.Start(this, removedGroup, a, b);
		} else {
			releaseGroupView(removedGroup);
		}
	}

	@Override
	public void onGroup(GroupNode composedGroup, GroupNode a, GroupNode b) {
		if (firstVisibleGroup == a || firstVisibleGroup == b) {
			firstVisibleGroup = composedGroup;
		}

		if (lastVisibleGroup == a || lastVisibleGroup == b) {
			lastVisibleGroup = composedGroup;
		}

		if (composingAnimationEnabled) {// && isGroupVisible(composedGroup, getHeight())) {
			ComposingAnimation.Start(this, composedGroup, a, b);
		} else {
			releaseGroupView(a);
			releaseGroupView(b);
		}
	}

	void makeBackAnimationView(GroupNode group) {
		final GroupView usedView = groupsViews.remove(group);
		animationsViewsBack.put(group, usedView);
	}

	void makeFrontAnimationView(GroupNode group) {
		final GroupView usedView = groupsViews.remove(group);
		animationsViewsFront.put(group, usedView);
	}

	void removeAnimationView(GroupNode group) {
		GroupView removedView = animationsViewsBack.remove(group);
		if (removedView == null) {
			removedView = animationsViewsFront.remove(group);
		}
		addGroupViewToPool(removedView);
	}

	private void updateVisibleGroups() {
		final int height = getHeight();
		if (!isVisibleOnScreen()) {
			for (GroupNode group : groupedList) {
				if (firstVisibleGroup == null) {
					if (isGroupVisible(group, height)) {
						firstVisibleGroup = group;
						lastVisibleGroup = group;
					}
				} else {
					if (!isGroupVisible(group, height)) {
						break;
					}
					lastVisibleGroup = group;
				}
			}
		} else {
			// Expand
			while (firstVisibleGroup.getPrev() != null && isGroupVisible(firstVisibleGroup.getPrev(), height)) {
				firstVisibleGroup = firstVisibleGroup.getPrev();
			}

			while (lastVisibleGroup.getNext() != null && isGroupVisible(lastVisibleGroup.getNext(), height)) {
				lastVisibleGroup = lastVisibleGroup.getNext();
			}

			// Shrink
			while (firstVisibleGroup != null && !isGroupVisible(firstVisibleGroup, height)) {
				releaseGroupView(firstVisibleGroup);
				firstVisibleGroup = firstVisibleGroup.getNext();
			}

			while (lastVisibleGroup != null && !isGroupVisible(lastVisibleGroup, height)) {
				releaseGroupView(lastVisibleGroup);
				lastVisibleGroup = lastVisibleGroup.getPrev();
			}

			if (firstVisibleGroup == null || lastVisibleGroup == null) {
				firstVisibleGroup = null;
				lastVisibleGroup = null;
			}
		}
	}

	private void updateChilds() {
		GroupNode group = firstVisibleGroup;
		while (group != lastVisibleGroup.getNext()) {
			GroupView view = getGroupView(group);
			if (view.getTag() == null) {
				view.setY(group.getAbsolutePos(getHeight()));
			}
			group = group.getNext();
		}
	}

	private void updateAnimations() {
		for (GroupingAnimation animation : animations) {
			animation.update();
		}
	}

	private void registerGroupingEventsListenerIfNeeded() {
		if (!didSkipFirstGroupingEvents) {
			didSkipFirstGroupingEvents = true;
			groupedList.addListener(this);
		}
	}

	private boolean isVisibleOnScreen() {
		return firstVisibleGroup != null;
	}

	private boolean isGroupVisible(@Nullable GroupNode group, int height) {
		final Float groupPos = group.getAbsolutePos(height);
		return (groupPos + viewHeight) >= visibleRect.top &&
				groupPos <= visibleRect.bottom;
	}

	private void setChildData(GroupView view, GroupNode group) {
		if (group.isLeaf()) {
			view.setModel(group.getData());
		} else {
			view.setModel(group.getData(), group.getItemsCount(), group.getAvgScore(rank));
		}
		measureAndLayoutChild(view);
	}

	GroupView getGroupView(GroupNode group) {
		GroupView view = groupsViews.get(group);
		if (view == null) {
			view = getSpareGroupView();
			groupsViews.put(group, view);
			setChildData(view, group);
			view.setAlpha(1.0f);
		}
		return view;
	}

	private GroupView getSpareGroupView() {
		if (viewsPool.isEmpty()) {
			final GroupView view = new GroupView(getContext());
			view.setLayoutParams(generateDefaultLayoutParams());
			return view;
		} else {
			return viewsPool.pop();
		}
	}

	private void releaseGroupView(GroupNode group) {
		GroupView view = groupsViews.remove(group);
		addGroupViewToPool(view);
	}

	private void addGroupViewToPool(@Nullable GroupView view) {
		if (view != null) {
			if (composingAnimationEnabled || breakingAnimationEnabled) {
				GroupingAnimation.Stop(view);
			}
			viewsPool.add(view);
		}
	}

	private void measureAndLayoutChild(GroupView child) {
		int widthSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
		int heightSpec = MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST);
		measureChildWithMargins(child, widthSpec, 0, heightSpec, 0);

		layoutChild(child, 0, 0, getWidth(), getHeight());
	}

	private void layoutChild(GroupView child, int left, int top, int right, int bottom) {
		final boolean forceLeftGravity = false;

		final int parentLeft = 0;//getPaddingLeftWithForeground();
		final int parentRight = right - left;// - getPaddingRightWithForeground();

		final int parentTop = 0;//getPaddingTopWithForeground();
		final int parentBottom = bottom - top;// - getPaddingBottomWithForeground();

		if (child.getVisibility() != GONE) {
			final LayoutParams lp = (LayoutParams) child.getLayoutParams();

			final int width = child.getMeasuredWidth();
			final int height = child.getMeasuredHeight();

			int childLeft;
			int childTop;

			int gravity = lp.gravity;
			if (gravity == -1) {
				gravity = Gravity.LEFT;
			}

			final int absoluteGravity;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
				final int layoutDirection = getLayoutDirection();
				absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
			} else {
				absoluteGravity = gravity;
			}

			final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

			switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
				case Gravity.CENTER_HORIZONTAL:
					childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
							lp.leftMargin - lp.rightMargin;
					break;
				case Gravity.RIGHT:
					if (!forceLeftGravity) {
						childLeft = parentRight - width - lp.rightMargin;
						break;
					}
				case Gravity.LEFT:
				default:
					childLeft = parentLeft + lp.leftMargin;
			}

			switch (verticalGravity) {
				case Gravity.TOP:
					childTop = parentTop + lp.topMargin;
					break;
				case Gravity.CENTER_VERTICAL:
					childTop = parentTop + (parentBottom - parentTop - height) / 2 +
							lp.topMargin - lp.bottomMargin;
					break;
				case Gravity.BOTTOM:
					childTop = parentBottom - height - lp.bottomMargin;
					break;
				default:
					childTop = parentTop + lp.topMargin;
			}

			child.layout(childLeft, childTop, childLeft + width, childTop + height);
		}
	}
}
