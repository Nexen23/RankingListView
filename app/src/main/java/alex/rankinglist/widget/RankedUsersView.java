package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import junit.framework.Assert;

import java.util.List;

import alex.rankinglist.R;
import alex.rankinglist.widget.model.RankedUser;
import alex.rankinglist.widget.model.User;
import butterknife.BindView;
import butterknife.ButterKnife;


public class RankedUsersView extends FrameLayout {
	@BindView(R.id.tv_name) TextView nameLabel;
	@BindView(R.id.tv_rank) TextView rankLabel;
	@BindView(R.id.iv_avatar) ImageView avatarImage;

	public RankedUsersView(Context context) {
		super(context);
		init();
	}

	public RankedUsersView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RankedUsersView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.widget_user_view, this, true);
		ButterKnife.bind(this);
	}

	public void setModel(User user) {
		nameLabel.setText(user.name);
		rankLabel.setText(String.format("%.1f%%", user.score));
	}

	public void setModel(List<User> usersGroup) {
		Assert.assertTrue(usersGroup.size() >= 2);
		setModel(usersGroup.get(0));
		nameLabel.setText(String.format("%s (+%d)", nameLabel.getText(), usersGroup.size()));
	}

	public void setModel(RankedUser user) {
		setModel(user.user);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		//LogUtil.log(this, "measured=%d, %s", getMeasuredHeight(), LogUtil.MeasureSpecToString(heightMeasureSpec));
	}
}
