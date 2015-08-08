package net.cryptodirect.authenticator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

/**
 * <p>A visual indication of how many seconds are left in the
 * current timestep. For example, say the timestep is 30 seconds,
 * the initial epoch is the Unix epoch, and it is now 1438928300.
 * Then the circle will display:</p>
 * <br /><br />
 * 1438928300 / 30 = 47964276.7
 * Take fractional part: 0.7
 * 30 * 0.7 = 21
 * 21 seconds have elapsed in the current timestep interval, thus:
 * 30 - 21 = 9 seconds remain, so the TimestepIntervalWheel will be filled
 * (degree-wise) for 21/30ths of its diameter.
 */
public class TimestepIntervalWheel extends View
{
    private int trackWidth = 3;
    private int fillWidth = 12;
    private int trackRadius = 320;
    private int trackColor = Color.rgb(206, 208, 208);
    private int fillColor = Color.rgb(25, 90, 114);
    private int numberColor = Color.rgb(206, 208, 208);
    private Paint trackPaint;
    private Paint fillPaint;
    private Paint numberPaint;
    private int intervalInSeconds;
    private int secondsRemainingInInterval;
    private RectF enclosingSquare;
    private int viewWidth;
    private int viewHeight;
    private static final String TAG = TimestepIntervalWheel.class.getSimpleName();

    public TimestepIntervalWheel(Context context, int intervalInSeconds, int secondsRemainingInInterval)
    {
        super(context);
        this.intervalInSeconds = intervalInSeconds;
        this.secondsRemainingInInterval = secondsRemainingInInterval;
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
        numberPaint.setTextSize(60f);
        numberPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
    {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        viewWidth = width;
        viewHeight = height;
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
        Log.i(TAG, "Inside onDraw, secondsRemaining: " + secondsRemainingInInterval + " interval size: " + intervalInSeconds);
        canvas.drawArc(enclosingSquare, 0, 360, false, trackPaint);
        double chunkToRemovePercentage = 1d - ((double) secondsRemainingInInterval / (double) intervalInSeconds);
        Log.i(TAG, "Chunk to remove percentage: " + chunkToRemovePercentage);
        int chunkToRemoveInDegrees = (int) (360d * chunkToRemovePercentage);
        Log.i(TAG, "Drawing fill arc from angle: " + chunkToRemoveInDegrees + " to angle: " + 360);
        canvas.drawArc(enclosingSquare, 270, -360 + chunkToRemoveInDegrees, false, fillPaint);
        int textYPos = (int) ((canvas.getHeight() / 2) - ((numberPaint.descent() + numberPaint.ascent()) / 2)) ;

        canvas.drawText(String.valueOf(secondsRemainingInInterval), canvas.getWidth() / 2, textYPos, numberPaint);
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

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
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
}
