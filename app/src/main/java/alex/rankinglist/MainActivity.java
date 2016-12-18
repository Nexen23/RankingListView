package alex.rankinglist;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import alex.rankinglist.widget.RankingView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
	@BindView(R.id.v_ranking) RankingView rankingView;

	ScaleGestureDetector scaleDetector;
	float scaleFactor = 1.f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		scaleDetector = new ScaleGestureDetector(this, new ScaleListener());
		rankingView.setData("Newbie", 15, 1, Color.DKGRAY);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		scaleDetector.onTouchEvent(ev);
		return true;
	}

	class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scaleFactor *= detector.getScaleFactor();
			rankingView.scale(scaleFactor);
			return true;
		}
	}
}
