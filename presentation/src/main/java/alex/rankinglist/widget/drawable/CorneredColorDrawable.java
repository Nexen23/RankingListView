package alex.rankinglist.widget.drawable;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.RequiresApi;

/**
 * canvas.clipPath() is supported from >=18 API - http://stackoverflow.com/a/8895894/2653714
 */
@RequiresApi(18)
public class CorneredColorDrawable extends ColorDrawable {
	private final float[] radii;
	private Path roundedPath;

	public CorneredColorDrawable(int color, float[] radii) {
		super(color);
		this.radii = radii;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		roundedPath = new Path();
		roundedPath.addRoundRect(new RectF(0, 0, bounds.width(), bounds.height()),
				radii, Path.Direction.CW);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.clipPath(roundedPath);
		super.draw(canvas);
	}
}
