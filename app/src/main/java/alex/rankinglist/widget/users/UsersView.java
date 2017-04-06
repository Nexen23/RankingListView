package alex.rankinglist.widget.users;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
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
	private boolean didInitViews = false, isVisibleOnScreen = true;
	private Rect visibleRect = new Rect();

	private boolean composingAnimationEnabled = true, breakingAnimationEnabled = true;
	int animationsDuration;
	HashMap<GroupNode, GroupView> groupsViews = new HashMap<>(), animationsViews = new HashMap<>();
	LinkedList<GroupingAnimation> animations = new LinkedList<>();
	LinkedList<GroupView> childsViews = new LinkedList<>();

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
			Assert.assertEquals(0, getChildCount());

			updateChilds();
			updateAnimations();

			measureChilds(w, h);
		}
		onVisibleFrameChanged();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		LogUtil.d(this, "onLayout()");
		super.onLayout(changed, left, top, right, bottom);
		layoutChilds(left, top, right, bottom);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (isVisibleOnScreen) {
			for (GroupView child : childsViews) {
				if (isChildVisible(child)) {
					drawChild(canvas, child, getDrawingTime());
				}
			}
		}
	}

	public void onVisibleFrameChanged() {
		isVisibleOnScreen = getLocalVisibleRect(visibleRect);
		if (isVisibleOnScreen) {
			invalidate();
		}
	}

	public void setModel(Rank rank, List<User> users) {
		childsViews.clear();
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
		childsViews.remove(removedView);
	}

	void bringChildToFront(GroupView child) {
		childsViews.remove(child);
		childsViews.add(child);
	}

	private GroupView addGroupView(GroupNode group) {
		final GroupView view = new GroupView(getContext());
		view.setLayoutParams(generateDefaultLayoutParams());

		if (group.isLeaf()) {
			view.setModel(group.getData());
		} else {
			view.setModel(group.getData(), group.getItemsCount(), group.getAvgScore(rank));
		}

		groupsViews.put(group, view);
		childsViews.add(view);
		return view;
	}

	private void removeGroupView(GroupNode group) {
		final GroupView removedView = groupsViews.remove(group);
		childsViews.remove(removedView);
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

	private void measureChilds(int w, int h) {
		int widthSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
		int heightSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);

		for (GroupView child : childsViews) {
			measureChildWithMargins(child, widthSpec, 0, heightSpec, 0);
		}
	}

	private void layoutChilds(int left, int top, int right, int bottom) {
		if (isVisibleOnScreen) {
			for (GroupView child : childsViews) {
				//if (isChildVisible(child)) {
					layoutChild(child, left, top, right, bottom);
				//}
			}
		}
	}

	private boolean isChildVisible(GroupView child) {
		return visibleRect.intersects(
				visibleRect.left, (int)child.getY(),
				visibleRect.right, (int)(child.getY() + child.getMeasuredHeight()));
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
