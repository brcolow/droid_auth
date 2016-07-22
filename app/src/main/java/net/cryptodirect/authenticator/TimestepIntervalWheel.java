package net.cryptodirect.authenticator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/**
 * A visual indication of how many seconds are left in the
 * current timestep. For example, say the timestep is 30 seconds,
 * the initial epoch is the Unix epoch, and it is now 1438928300.
 * Then the circle will display:
 * <p/>
 * 1438928300 / 30 = 47964276.7
 * Take fractional part: 0.7
 * 30 * 0.7 = 21
 * <p/>
 * 21 seconds have elapsed in the current timestep interval, thus:
 * 30 - 21 = 9 seconds remain, so the TimestepIntervalWheel will be filled
 * (degree-wise) for 21/30ths of its diameter.
 */
public class TimestepIntervalWheel extends View
{
    private final int trackWidth;
    private final int fillWidth;
    private final float numberSize;
    private final int trackColor = Color.rgb(206, 208, 208);
    private final int fillColor = Color.rgb(25, 90, 114);
    private final int numberColor = Color.rgb(206, 208, 208);
    private Paint trackPaint;
    private Paint fillPaint;
    private Paint numberPaint;
    private final int intervalInSeconds;
    private int secondsRemainingInInterval;
    private RectF enclosingSquare;
    private final int displayWidth;

    public TimestepIntervalWheel(Context context, int intervalInSeconds, int secondsRemainingInInterval, int displayWidth)
    {
        super(context);
        this.displayWidth = displayWidth;
        this.intervalInSeconds = intervalInSeconds;
        this.secondsRemainingInInterval = secondsRemainingInInterval;
        this.trackWidth = (int) (displayWidth * 0.01f);
        this.fillWidth = (int) (displayWidth * 0.03f);
        numberSize = displayWidth * 0.08f;
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        trackPaint = new Paint();
        trackPaint.setColor(trackColor);
        trackPaint.setAntiAlias(true);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(trackWidth);

        fillPaint = new Paint();
        fillPaint.setColor(fillColor);
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.STROKE);
        fillPaint.setStrokeWidth(fillWidth);

        numberPaint = new Paint();
        numberPaint.setColor(numberColor);
        numberPaint.setAntiAlias(true);
        numberPaint.setTextSize(numberSize);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
    {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        int trackRadius = (int) ((displayWidth * 0.64f));
        enclosingSquare = new RectF(
                (Math.max(trackWidth, fillWidth) / 2) + (width / 2) - trackRadius / 2,
                (Math.max(trackWidth, fillWidth) / 2) + (height / 2) - trackRadius / 2,
                (Math.max(trackWidth, fillWidth) / 2) + (width / 2) + trackRadius / 2,
                (Math.max(trackWidth, fillWidth) / 2) + (height / 2) + trackRadius / 2
        );
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawArc(enclosingSquare, 0, 360, false, trackPaint);
        double chunkToRemovePercentage = 1d - ((double) secondsRemainingInInterval /
                (double) intervalInSeconds);
        int chunkToRemoveInDegrees = (int) (360d * chunkToRemovePercentage);
        canvas.drawArc(enclosingSquare, 270, -360 + chunkToRemoveInDegrees, false, fillPaint);
        float textWidth = numberPaint.measureText(String.valueOf(secondsRemainingInInterval));
        canvas.drawText(String.valueOf(secondsRemainingInInterval),
                (canvas.getWidth() / 2) - (textWidth / 4),
                (canvas.getHeight() / 2) + (numberSize / 2), numberPaint);
    }

    /**
     * Forces the View to be square.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        // set the dimensions
        if (widthWithoutPadding > heightWithoutPadding)
        {
            size = heightWithoutPadding;
        }
        else
        {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(),
                size + getPaddingTop() + getPaddingBottom());
    }

    public boolean decrementSecondsRemaining()
    {
        boolean newCycle = false;
        if (secondsRemainingInInterval == 0)
        {
            secondsRemainingInInterval = intervalInSeconds;
            newCycle = true;
        }
        else
        {
            secondsRemainingInInterval -= 1;
        }
        postInvalidate();
        return newCycle;
    }

    public int getSecondsRemainingInInterval()
    {
        return secondsRemainingInInterval;
    }
}
