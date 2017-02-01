package alex.rankinglist.widget.drawable;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;


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
