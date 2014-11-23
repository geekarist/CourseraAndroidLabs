package course.labs.graphicslab;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.util.FloatMath.sqrt;

public class BubbleActivity extends Activity {

    // These variables are for testing purposes, do not modify
    private final static int RANDOM = 0;
    private final static int SINGLE = 1;
    private final static int STILL = 2;
    private static int speedMode = RANDOM;

    private static final String TAG = "Lab-Graphics";

    // The Main view
    private RelativeLayout mFrame;

    // Bubble image's bitmap
    private Bitmap mBitmap;

    // Display dimensions
    private int mDisplayWidth, mDisplayHeight;

    // Sound variables

    // AudioManager
    private AudioManager mAudioManager;
    // SoundPool
    private SoundPool mSoundPool;
    // ID for the bubble popping sound
    private int mSoundID;
    // Audio volume
    private float mStreamVolume;

    // Gesture Detector
    private GestureDetector mGestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // Set up user interface
        mFrame = (RelativeLayout) findViewById(R.id.frame);

        // Load basic bubble Bitmap
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b64);

        setupGestureDetector();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Manage bubble popping sound
        // Use AudioManager.STREAM_MUSIC as stream type

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mStreamVolume = (float) mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC)
                / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // TODO - make a new SoundPool, allowing up to 10 streams
        mSoundPool = null;

        // TODO - set a SoundPool OnLoadCompletedListener that calls
        // setupGestureDetector()


        // TODO - load the sound from res/raw/bubble_pop.wav


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {

            // Get the size of the display so this View knows where borders are
            mDisplayWidth = mFrame.getWidth();
            mDisplayHeight = mFrame.getHeight();

        }
    }

    // Set up GestureDetector
    private void setupGestureDetector() {

        mGestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {

                    // If a fling gesture starts on a BubbleView then change the
                    // BubbleView's velocity

                    @Override
                    public boolean onFling(MotionEvent event1, MotionEvent event2,
                                           float velocityX, float velocityY) {

                        // TODO - Implement onFling actions.
                        // You can get all Views in mFrame one at a time
                        // using the ViewGroup.getChildAt() method


                        return true;
                    }

                    // If a single tap intersects a BubbleView, then pop the BubbleView
                    // Otherwise, create a new BubbleView at the tap's location and add
                    // it to mFrame. You can get all views from mFrame with
                    // ViewGroup.getChildAt()

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent event) {
                        // DONE - Implement onSingleTapConfirmed actions.
                        // You can get all Views in mFrame using the
                        // ViewGroup.getChildCount() method
                        float x = event.getX();
                        float y = event.getY();

                        BubbleView existingBubble;
                        if (null != (existingBubble = getBubbleIntersecting(x, y))) {
                            mFrame.removeView(existingBubble);
                        } else {
                            BubbleView bubble = new BubbleView(getBaseContext(), x, y);
                            mFrame.addView(bubble);
                            bubble.startMovement();
                        }
                        return true;
                    }
                });
    }

    private BubbleView getBubbleIntersecting(float x, float y) {
        for (int i = 0; i < mFrame.getChildCount(); i++) {
            View childView = mFrame.getChildAt(i);
            if (childView instanceof BubbleView) {
                BubbleView bubbleChildView = (BubbleView) childView;
                if (bubbleChildView.intersects(x, y)) {
                    return bubbleChildView;
                }
            }
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // DONE - Delegate the touch to the gestureDetector

        return mGestureDetector.onTouchEvent(event);

    }

    @Override
    protected void onPause() {

        // TODO - Release all SoundPool resources


        super.onPause();
    }

    // BubbleView is a View that displays a bubble.
    // This class handles animating, drawing, and popping amongst other actions.
    // A new BubbleView is created for each bubble on the display

    public class BubbleView extends View {

        private static final int BITMAP_SIZE = 64;
        private static final int REFRESH_RATE = 40;
        private final Paint mPainter = new Paint();
        private ScheduledFuture<?> mMoverFuture;
        private int mScaledBitmapWidth;
        private Bitmap mScaledBitmap;

        // location, speed and direction of the bubble
        private float mXPos, mYPos, mDx, mDy, mRadius, mRadiusSquared;
        private long mRotate, mDRotate;

        BubbleView(Context context, float x, float y) {
            super(context);

            // Create a new random number generator to
            // randomize size, rotation, speed and direction
            Random r = new Random();

            // Creates the bubble bitmap for this BubbleView
            createScaledBitmap(r);

            // Radius of the Bitmap
            mRadius = mScaledBitmapWidth / 2;
            mRadiusSquared = mRadius * mRadius;

            // Adjust position to center the bubble under user's finger
            mXPos = x - mRadius;
            mYPos = y - mRadius;

            // Set the BubbleView's speed and direction
            setSpeedAndDirection(r);

            // Set the BubbleView's rotation
            setRotation(r);

            mPainter.setAntiAlias(true);

        }

        private void setRotation(Random r) {
            if (speedMode == RANDOM) {

                // DONE - set rotation in range [1..3]
                mDRotate = 1 + r.nextInt(3);

            } else {
                mDRotate = 0;
            }
        }

        private void setSpeedAndDirection(Random r) {

            // Used by test cases
            switch (speedMode) {

                case SINGLE:

                    mDx = 20;
                    mDy = 20;
                    break;

                case STILL:

                    // No speed
                    mDx = 0;
                    mDy = 0;
                    break;

                default:

                    // DONE - Set mDx and mDy to indicate movement direction and speed
                    // Limit speed in the x and y direction to [-3..3] pixels per movement.
                    mDx = r.nextInt(7) - 3;
                    mDy = r.nextInt(7) - 3;

            }
        }

        private void createScaledBitmap(Random r) {

            if (speedMode != RANDOM) {
                mScaledBitmapWidth = BITMAP_SIZE * 3;
            } else {

                // DONE - set scaled bitmap size in range [1..3] * BITMAP_SIZE
                mScaledBitmapWidth = (r.nextInt(3) + 1) * BITMAP_SIZE;

            }

            // DONE - create the scaled bitmap using size set above
            mScaledBitmap = Bitmap.createScaledBitmap(mBitmap, mScaledBitmapWidth, mScaledBitmapWidth, false);

        }

        // Start moving the BubbleView & updating the display
        private void startMovement() {

            // Creates a WorkerThread
            ScheduledExecutorService executor = Executors
                    .newScheduledThreadPool(1);

            // Execute the run() in Worker Thread every REFRESH_RATE
            // milliseconds
            // Save reference to this job in mMoverFuture
            mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    // DONE - implement movement logic.
                    // Each time this method is run the BubbleView should
                    // move one step. If the BubbleView exits the display,
                    // stop the BubbleView's Worker Thread.
                    // Otherwise, request that the BubbleView be redrawn.
                    if (moveWhileOnScreen()) {
                        postInvalidate();
                    } else {
                        stopMovement(false);
                    }
                }
            }, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
        }

        // Returns true if the BubbleView intersects position (x,y)
        private synchronized boolean intersects(float x, float y) {
            // DONE - Return true if the BubbleView intersects position (x,y)
            float dx = x - (mXPos + mRadius)    ;
            float dy = y - (mYPos + mRadius);
            float distance = sqrt(dx * dx + dy * dy);
            return distance < mRadius;
        }

        // Cancel the Bubble's movement
        // Remove Bubble from mFrame
        // Play pop sound if the BubbleView was popped

        private void stopMovement(final boolean wasPopped) {

            if (null != mMoverFuture) {

                if (!mMoverFuture.isDone()) {
                    mMoverFuture.cancel(true);
                }

                final BubbleView bubbleView = this;
                // This work will be performed on the UI Thread
                mFrame.post(new Runnable() {
                    @Override
                    public void run() {

                        // DONE - Remove the BubbleView from mFrame
                        mFrame.removeView(bubbleView);

                        // TODO - If the bubble was popped by user,
                        // play the popping sound
                        if (wasPopped) {


                        }
                    }
                });
            }
        }

        // Change the Bubble's speed and direction
        private synchronized void deflect(float velocityX, float velocityY) {
            mDx = velocityX / REFRESH_RATE;
            mDy = velocityY / REFRESH_RATE;
        }

        // Draw the Bubble at its current location
        @Override
        protected synchronized void onDraw(Canvas canvas) {

            // DONE - save the canvas
            canvas.save();

            // DONE - increase the rotation of the original image by mDRotate
            mRotate += mDRotate;

            // DONE Rotate the canvas by current rotation
            // Hint - Rotate around the bubble's center, not its position
            canvas.rotate(mRotate, mXPos + mRadius, mYPos + mRadius);

            // DONE - draw the bitmap at it's new location
            canvas.drawBitmap(mScaledBitmap, mXPos, mYPos, mPainter);

            // DONE - restore the canvas
            canvas.restore();

        }

        // Returns true if the BubbleView is still on the screen after the move
        // operation
        private synchronized boolean moveWhileOnScreen() {
            // DONE - Move the BubbleView
            mXPos += mDx;
            mYPos += mDy;

            return isInsideView();
        }

        // Return true if the BubbleView is still on the screen after the move
        // operation
        private boolean isInsideView() {
            // DONE - Return true if the BubbleView is still on the screen after
            // the move operation
            DisplayMetrics metrics = new DisplayMetrics();
            mFrame.getDisplay().getMetrics(metrics);
            float bubbleCenterX = mXPos + mRadius;
            float bubbleCenterY = mYPos + mRadius;
            boolean bubbleIsInsideHoriz = bubbleCenterX < metrics.widthPixels && bubbleCenterX > 0;
            boolean bubbleIsInsideVert = bubbleCenterY < metrics.heightPixels && bubbleCenterY > 0;
            return bubbleIsInsideHoriz && bubbleIsInsideVert;
        }
    }

    // Do not modify below here

    @Override
    public void onBackPressed() {
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_still_mode:
                speedMode = STILL;
                return true;
            case R.id.menu_single_speed:
                speedMode = SINGLE;
                return true;
            case R.id.menu_random_mode:
                speedMode = RANDOM;
                return true;
            case R.id.quit:
                exitRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void exitRequested() {
        super.onBackPressed();
    }
}