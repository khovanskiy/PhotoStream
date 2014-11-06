package ru.example.PhotoStream.Camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SurfaceGridView extends View {
    private final int ROWS = 3;
    private final int COLUMNS = 3;

    private Paint paint = new Paint();
    private boolean visible = false;

    public SurfaceGridView(Context context) {
        super(context);
    }

    public SurfaceGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SurfaceGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (visible) {
            paint.setColor(Color.argb(128, 255, 255, 255));
            paint.setStrokeWidth(10);
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int rowWidth = height / ROWS;
            int columnWidth = width / COLUMNS;
            for (int i = 1; i < ROWS; i++) {
                canvas.drawLine(0, i * rowWidth, width, i * rowWidth, paint);
            }
            for (int i = 1; i < COLUMNS; i++) {
                canvas.drawLine(i * columnWidth, 0, i * columnWidth, height, paint);
            }
        }
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        invalidate();
    }

    public void setVisible() {
        visible = true;
        invalidate();
    }

    public void setInvisible() {
        visible = false;
        invalidate();
    }
}
