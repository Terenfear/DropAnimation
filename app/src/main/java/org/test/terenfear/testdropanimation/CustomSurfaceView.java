package org.test.terenfear.testdropanimation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntlliJ IDEA<br>
 * User: Pavel Kozlovich <br>
 * E-mail: terenfear@gmail.com<br>
 * Skype: terenfear962<br>
 * Date  17.11.2017<br>
 * Time: 13:42<br>
 * Project name: TestDropAnimation<br>
 * ======================================================================================================================
 * //TODO: Add description
 * ======================================================================================================================
 */
public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private DrawThread drawThread;
    private final static String TAG = "CustomSurfaceView";

    public CustomSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
        drawThread = new DrawThread(getHolder(), getResources());
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        drawThread.setRecalc(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed: ");
        boolean retry = true;
        // завершаем работу потока
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // если не получилось, то будем пытаться еще и еще
            }
        }
    }

    class DrawThread extends Thread {
        private boolean mRunFlag = false;
        private boolean mRecalc = false;
        private final SurfaceHolder mSurfaceHolder;
        private List<Bitmap> mPictures;
        private List<Matrix> mMatrices;
        private long mPrevTime;

        public DrawThread(SurfaceHolder surfaceHolder, Resources resources){
            this.mSurfaceHolder = surfaceHolder;

            mPictures = new ArrayList<>();
            mPictures.add(BitmapFactory.decodeResource(resources, R.drawable.ic_drop_1));
            mPictures.add(BitmapFactory.decodeResource(resources, R.drawable.ic_drop_2));
            mPictures.add(BitmapFactory.decodeResource(resources, R.drawable.ic_drop_3));
            mPictures.add(BitmapFactory.decodeResource(resources, R.drawable.ic_drop_4));
            mPictures.add(BitmapFactory.decodeResource(resources, R.drawable.ic_drop_5));
            mPictures.add(BitmapFactory.decodeResource(resources, R.drawable.ic_drop_6));
            mPictures.add(BitmapFactory.decodeResource(resources, R.drawable.ic_drop_7));

            mMatrices = new ArrayList<>();
            float surfaceWidth = surfaceHolder.getSurfaceFrame().width();
            Random random = new Random();
            int angle = 0;
            for (int i = 0, offset = 0; i < mPictures.size(); i++) {
                angle = random.nextInt() % 360;
                int picWidth = mPictures.get(i).getWidth();
                int picHeight = mPictures.get(i).getHeight();
                float scaleFactor = surfaceWidth / (picWidth * mPictures.size());
                Matrix matrix = new Matrix();
                matrix.preRotate(angle, picWidth / 2, picHeight / 2);
                matrix.postScale(scaleFactor, scaleFactor);
                matrix.postTranslate(offset, -picHeight * scaleFactor);
                offset += picWidth * scaleFactor;
                mMatrices.add(matrix);
            }
            mPrevTime = System.currentTimeMillis();
        }

        public void setRunning(boolean run) {
            mRunFlag = run;
        }

        public void setRecalc(boolean recalc) {
            mRecalc = recalc;
        }

        private void transform(Bitmap picture,
                               Matrix matrix,
                               int surfaceHeight,
                               float pixelsPerFrame,
                               float[] values) {
            matrix.getValues(values);
//                    Log.d(TAG, "run: " + values[2] + ";" + values[5]);
            if (values[5] < surfaceHeight) {
                matrix.postTranslate(0, pixelsPerFrame);
            }
        }

        @Override
        public void run() {
            Canvas canvas;
            int framesPerSecond = 50;
            int frameLength = 1000 / 50;
            int surfaceHeight = mSurfaceHolder.getSurfaceFrame().height();
            float pixelsPerFrame = surfaceHeight / framesPerSecond;
            float values[] = new float[9];
            while (mRunFlag) {
                if (mRecalc) {
                    surfaceHeight = mSurfaceHolder.getSurfaceFrame().height();
                    pixelsPerFrame = surfaceHeight / framesPerSecond;
                    mRecalc = false;
                }
                long now = System.currentTimeMillis();
                long elapsedTime = now - mPrevTime;
                if (elapsedTime > frameLength){
                    mPrevTime = now;
                    for (int i = 0; i < mPictures.size(); i++) {
                        transform(mPictures.get(i),
                                mMatrices.get(i),
                                surfaceHeight,
                                pixelsPerFrame,
                                values);
                    }
                    if (pixelsPerFrame < surfaceHeight / 20) {
                        pixelsPerFrame *= 1.025;
                    }
                }
                canvas = null;
                try {
                    // получаем объект Canvas и выполняем отрисовку
                    synchronized (mSurfaceHolder) {
                        canvas = mSurfaceHolder.lockCanvas(null);
                        if (canvas != null) {
                            canvas.drawColor(Color.WHITE);
                            for (int i = 0; i < mPictures.size(); i++) {
                                canvas.drawBitmap(mPictures.get(i),
                                        mMatrices.get(i),
                                        null);
                            }
                        } else {
                            mRunFlag = false;
                        }
                    }
                } finally {
                    if (canvas != null) {
                        // отрисовка выполнена. выводим результат на экран
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
