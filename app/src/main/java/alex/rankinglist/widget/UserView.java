package alex.rankinglist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import alex.rankinglist.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class UserView extends FrameLayout {
	@BindView(R.id.tv_name) TextView nameLabel;
	@BindView(R.id.tv_rank) TextView rankLabel;
	@BindView(R.id.iv_avatar) ImageView avatarImage;

	public UserView(Context context) {
		super(context);
		init();
	}

	public UserView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public UserView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.widget_user_view, this, true);
		ButterKnife.bind(this);
	}

	public void setData(String name, int rank) {
		nameLabel.setText(name);
		rankLabel.setText(String.format("%d%%", rank));
	}
}
