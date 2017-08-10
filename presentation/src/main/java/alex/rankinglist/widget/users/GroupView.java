package alex.rankinglist.widget.users;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import alex.rankinglist.databinding.WidgetGroupViewBinding;
import alex.data.model.User;


public class GroupView extends FrameLayout {
	private WidgetGroupViewBinding binding;

	public GroupView(Context context) {
		super(context);
		init();
	}

	public GroupView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GroupView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		this.binding = WidgetGroupViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
	}

	public void setModel(User mainUser, int groupSize, float groupScore) {
		binding.tvName.setText(mainUser.name);
		binding.tvRank.setText(String.format("%.2f%%", groupScore));
		if (groupSize > 1) {
			binding.tvGroupSize.setVisibility(VISIBLE);
			groupSize = Math.min(groupSize, 99);
			binding.tvGroupSize.setText(String.format("+%d", groupSize));
		} else {
			binding.tvGroupSize.setVisibility(GONE);
		}
	}

	public void setModel(User user) {
		setModel(user, 1, user.score);
	}
}
