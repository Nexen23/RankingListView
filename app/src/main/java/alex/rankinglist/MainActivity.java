package alex.rankinglist;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import android.widget.ScrollView;

import alex.rankinglist.widget.RankingView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
	public static final String TAG = MainActivity.class.getName();

	@BindView(R.id.v_ranking) RankingView rankingView;
	@BindView(R.id.v_ranking2) RankingView rankingView2;
	@BindView(R.id.activity_main) ViewGroup rootViewGroup;
	@BindView(R.id.scroll_view) ScrollView rootScrollView;

	ScaleGestureDetector scaleDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		scaleDetector = new ScaleGestureDetector(this, new ScaleListener());
		rankingView2.setData("Newbie", 15, Color.DKGRAY, R.drawable.icon_smile_3);
		rankingView.setData("Good", 45, Color.BLUE, R.drawable.icon_smile_5);
	}

	boolean x = false;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean b = super.dispatchTouchEvent(ev);
		boolean b1 = scaleDetector.onTouchEvent(ev);

		/*if (!x) {
			View stretchingView = rootViewGroup;
			ViewGroup.LayoutParams params = stretchingView.getLayoutParams();
			Log.d(TAG, String.format("dispatchTouchEvent: params.height=%d, getHeight()=%d", params.height, stretchingView.getHeight()));
			params.height = (int) (stretchingView.getHeight() * 1.3f);
			stretchingView.setLayoutParams(params);
			x = true;
		}*/


		return true;
	}

	class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			ViewGroup.LayoutParams params = rootViewGroup.getLayoutParams();
			float scaleFactor = detector.getScaleFactor();
			params.height = (int) (rootViewGroup.getHeight() * scaleFactor);
			rootViewGroup.setLayoutParams(params);
			//rankingView.scale(detector.getScaleFactor());
			//rankingView2.scale(detector.getScaleFactor());
			return true;
		}
	}
}
