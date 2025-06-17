package org.cmucreatelab.android.cameraandimageselect.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CircleCropper extends View {
    private float centerX = 500;
    private float centerY = 500;
    private float radius = 200;

    private Paint borderPaint;
    private Paint maskPaint;

    public CircleCropper(Context context, AttributeSet attrs) {
        super(context, attrs);

        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(4);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);

        maskPaint = new Paint();
        maskPaint.setColor(0xAA000000); // semi-transparent
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw dimmed background
        canvas.drawRect(0, 0, getWidth(), getHeight(), maskPaint);

        // Cut out the circle
        Path path = new Path();
        path.addCircle(centerX, centerY, radius, Path.Direction.CCW);
        canvas.save();
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        canvas.drawColor(0x88000000);
        canvas.restore();

        // Draw the circle border
        canvas.drawCircle(centerX, centerY, radius, borderPaint);
    }

    // Optional: drag to move circle
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE ||
                event.getAction() == MotionEvent.ACTION_DOWN) {
            centerX = event.getX();
            centerY = event.getY();
            invalidate();
            return true;
        }
        return false;
    }

    public float getCircleX() { return centerX; }
    public float getCircleY() { return centerY; }
    public float getRadius() { return radius; }
}
