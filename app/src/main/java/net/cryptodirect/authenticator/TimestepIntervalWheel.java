package net.cryptodirect.authenticator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import net.cryptodirect.authenticator.crypto.TOTP;

import java.util.Timer;
import java.util.TimerTask;

import static net.cryptodirect.authenticator.crypto.TOTP.generateTOTP;
import static net.cryptodirect.authenticator.crypto.TOTP.getTC;

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
    private int secondsRemainingInInterval;
    private RectF enclosingSquare;
    private final Account account;
    private final int displayWidth;
    private final Handler handler;
    private final Timer timer;
    private final WheelTask currentTask;
    private final TextView codeTextView;
    private volatile boolean tickingSoundPlaying = false;

    public TimestepIntervalWheel(Context context, Account account, TextView codeTextView,
                                 int displayWidth)
    {
        super(context);
        this.displayWidth = displayWidth;
        this.account = account;
        this.codeTextView = codeTextView;
        this.trackWidth = Math.max(5, (int) (displayWidth * 0.01f));
        this.fillWidth = Math.max(10, (int) (displayWidth * 0.03f));
        handler = new Handler();
        timer = new Timer("Wheel Timer (" +  account.getLabel() + ")", true);
        numberSize = Math.max(26, displayWidth * 0.08f);
        currentTask = new WheelTask();
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
        secondsRemainingInInterval = account.getCodeParams().getTotpPeriod() -
                TOTP.getCurrentSpotInTSInterval(account);
        timer.schedule(currentTask, 0, 1000);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        currentTask.cancel();
        timer.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
    {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        int trackRadius = (int) ((displayWidth * 0.64f));
        enclosingSquare = new RectF(
                (Math.max(trackWidth, fillWidth) / 2) + (width / 2) - trackRadius / 2,
                (Math.max(trackWidth, fillWidth) / 2) + (height / 2) - trackRadius / 2,
                (Math.max(trackWidth, fillWidth) / 2) + (width / 2) + (trackRadius / 2) + 2,
                (Math.max(trackWidth, fillWidth) / 2) + (height / 2) + (trackRadius / 2) + 2
        );
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawArc(enclosingSquare, 0, 360, false, trackPaint);
        double chunkToRemovePercentage = 1d - ((double) secondsRemainingInInterval /
                (double) account.getCodeParams().getTotpPeriod());
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

    private boolean decrementSecondsRemaining()
    {
        boolean newCycle = false;
        if (secondsRemainingInInterval == 0)
        {
            secondsRemainingInInterval = account.getCodeParams().getTotpPeriod();
            newCycle = true;
        }
        else
        {
            secondsRemainingInInterval -= 1;
        }
        postInvalidate();
        return newCycle;
    }

    private class WheelTask extends TimerTask
    {
        @Override
        public void run()
        {
            if (decrementSecondsRemaining())
            {
                handler.post(new SetNewCodeTask(codeTextView, generateTOTP(account.getSecretKey(),
                        getTC(account.getCodeParams().getTotpPeriod()),
                        account.getCodeParams().getDigits(),
                        account.getCodeParams().getAlgorithm())));

                // SoundPoolManager.getInstance().stopSound("TICKTOCK");
                // tickingSoundPlaying = false;
            }
            else
            {
                if (!tickingSoundPlaying)
                {
                    // TODO might need to cache this value and implement a prefchangedlistener
                    boolean playTimeRunningOutSound = false;
                    if (playTimeRunningOutSound)
                    {
                        if (secondsRemainingInInterval <= -1)
                        {
                            tickingSoundPlaying = true;
                            // SoundPoolManager.getInstance().playSound("TICKTOCK", false);
                        }
                    }
                }
            }
        }
    }
}
