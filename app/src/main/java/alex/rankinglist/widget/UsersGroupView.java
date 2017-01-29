package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import java.util.List;

import alex.rankinglist.databinding.WidgetUserViewBinding;
import alex.rankinglist.util.HtmlUtil;
import alex.rankinglist.widget.model.User;


public class UsersGroupView extends FrameLayout {
	private WidgetUserViewBinding binding;

	public UsersGroupView(Context context) {
		super(context);
		init();
	}

	public UsersGroupView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public UsersGroupView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		this.binding = WidgetUserViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
	}

	public void setModel(User mainUser, @Nullable List<? extends User> usersGroup, float groupScore) {
		binding.tvName.setText(mainUser.name);
		binding.tvRank.setText(String.format("%.1f%%", groupScore));
		if (usersGroup != null && usersGroup.size() > 1) {
			binding.tvGroupSize.setVisibility(VISIBLE);
			binding.tvGroupSize.setText(HtmlUtil.fromHtml(String.format("<sup>+%d</sup>", usersGroup.size())));
		} else {
			binding.tvGroupSize.setVisibility(GONE);
		}
	}

	public void setModel(User user) {
		setModel(user, null, user.score);
	}
}