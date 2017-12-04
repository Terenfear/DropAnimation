package org.test.terenfear.testdropanimation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
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
public class CustomSurfaceView extends GLSurfaceView {
    private final static String TAG = "CustomSurfaceView";
//    private DrawThread drawThread;

    private CustomGLRenderer mRenderer;

    public CustomSurfaceView(Context context) {
        super(context);
        init();
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        mRenderer = new CustomGLRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        Log.d(TAG, "surfaceCreated: ");
//        drawThread = new DrawThread(getHolder(), getResources());
//        drawThread.setRunning(true);
//        drawThread.start();
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        drawThread.setRecalc(true);
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        Log.d(TAG, "surfaceDestroyed: ");
//        boolean retry = true;
//        // завершаем работу потока
//        drawThread.setRunning(false);
//        while (retry) {
//            try {
//                drawThread.join();
//                retry = false;
//            } catch (InterruptedException e) {
//                // если не получилось, то будем пытаться еще и еще
//            }
//        }
//    }
//
//    class DrawThread extends Thread {
//        private final SurfaceHolder mSurfaceHolder;
//        private boolean mRunFlag = false;
//        private boolean mRecalc = false;
//        private List<List<Bitmap>> mPictures;
//        private List<List<Matrix>> mMatrices;
//        private long mPrevTime;
//        private HashMap<Integer, Integer> mRes = new HashMap<>();
//
//        public DrawThread(SurfaceHolder surfaceHolder, Resources resources) {
//            this.mSurfaceHolder = surfaceHolder;
//
//            mRes.put(0, R.drawable.ic_drop_1);
//            mRes.put(1, R.drawable.ic_drop_2);
//            mRes.put(2, R.drawable.ic_drop_3);
//            mRes.put(3, R.drawable.ic_drop_4);
//            mRes.put(4, R.drawable.ic_drop_5);
//            mRes.put(5, R.drawable.ic_drop_6);
//            mRes.put(6, R.drawable.ic_drop_7);
//
//            float surfaceWidth = surfaceHolder.getSurfaceFrame().width();
//            float surfaceHeight = surfaceHolder.getSurfaceFrame().height();
//            Bitmap testBitmap = BitmapFactory.decodeResource(resources, mRes.get(0));
//            float overlapFactor = 0.99f;
//            int rowLength = mRes.size();
//            Random random = new Random();
//            float scaleFactor = surfaceWidth / (testBitmap.getWidth() * ((1 - overlapFactor / 2) * (rowLength - 1) + (1 - overlapFactor)));
//            int rowNumber = Math.round(surfaceHeight / (testBitmap.getHeight() * scaleFactor * (1 - overlapFactor)));
//
//            mPictures = new ArrayList<>();
//            for (int i = 0; i < rowNumber; i++) {
//                List<Bitmap> row = new ArrayList<>();
//                for (int j = 0; j < rowLength; j++) {
//                    int imageId = random.nextInt(mRes.size());
//                    row.add(BitmapFactory.decodeResource(resources, mRes.get(imageId)));
//                }
//                mPictures.add(row);
//            }
//
//            mMatrices = new ArrayList<>();
//            for (int rowId = 0, offsetY = 0; rowId < mPictures.size(); rowId++) {
//                List<Bitmap> row = mPictures.get(rowId);
//                List<Matrix> rowMatrices = new ArrayList<>();
//                for (int picId = 0, offsetX = 0; picId < row.size(); picId++) {
//                    int angle = random.nextInt() % 360;
//                    int picWidth = row.get(picId).getWidth();
//                    int picHeight = row.get(picId).getHeight();
//                    float overlap = picWidth * scaleFactor * overlapFactor / 2;
//                    offsetX -= overlap;
//                    if (picId == 0) {
//                        offsetY += overlap;
//                    }
//                    Matrix matrix = new Matrix();
//                    matrix.preRotate(angle, picWidth / 2, picHeight / 2);
//                    matrix.postScale(scaleFactor, scaleFactor);
//                    matrix.postTranslate(offsetX, offsetY - picHeight * scaleFactor + (random.nextInt() % 30));
//                    offsetX += picWidth * scaleFactor;
//                    if (picId == row.size() - 1) {
//                        offsetY -= picHeight * scaleFactor;
//                    }
//                    rowMatrices.add(matrix);
//                }
//                mMatrices.add(rowMatrices);
//            }
//            mPrevTime = System.currentTimeMillis();
//        }
//
//        public void setRunning(boolean run) {
//            mRunFlag = run;
//        }
//
//        public void setRecalc(boolean recalc) {
//            mRecalc = recalc;
//        }
//
//        private int transform(Bitmap picture,
//                              Matrix matrix,
//                              int traveledPath,
//                              float surfaceHeight,
//                              float pixelsPerFrame,
//                              float[] values) {
//
////            matrix.getValues(values);
////                    Log.d(TAG, "run: " + values[2] + ";" + values[5]);
////            if (values[5] < surfaceHeight - picture.getHeight() * values[4]) {
//            if (traveledPath < surfaceHeight) {
//                matrix.postTranslate(0, pixelsPerFrame);
//                traveledPath += pixelsPerFrame;
//            }
//            return traveledPath;
//        }
//
//        @Override
//        public void run() {
//            Canvas canvas;
//            int framesPerSecond = 50;
//            int frameLength = 1000 / 50;
//            float surfaceHeight = mSurfaceHolder.getSurfaceFrame().height();
//            List<Float> speeds = new ArrayList<>();
//            for (int i = 0; i < mPictures.size(); i++) {
//                speeds.add(surfaceHeight / framesPerSecond);
//            }
//            List<List<Integer>> paths = new ArrayList<>();
//            for (List<Bitmap> row : mPictures) {
//                List<Integer> pathRow = new ArrayList<>();
//                for (Bitmap bitmap : row) {
//                    pathRow.add(0);
//                }
//                paths.add(pathRow);
//            }
//            float values[] = new float[9];
//            int framesTillLastRow = 0;
//            int rowsOnScreen = 1;
//            while (mRunFlag) {
//                if (mRecalc) {
//                    surfaceHeight = mSurfaceHolder.getSurfaceFrame().height();
//                    speeds.clear();
//                    for (int i = 0; i < mPictures.size(); i++) {
//                        speeds.add(surfaceHeight / framesPerSecond);
//                    }
//                    mRecalc = false;
//                }
//                long now = System.currentTimeMillis();
//                long elapsedTime = now - mPrevTime;
//                if (elapsedTime > frameLength) {
//                    framesTillLastRow++;
//                    if (framesTillLastRow > framesPerSecond * 0.2 && rowsOnScreen < mPictures.size()) {
//                        rowsOnScreen++;
//                        framesTillLastRow = 0;
//                    }
//                    mPrevTime = now;
//                    for (int i = 0; i < rowsOnScreen; i++) {
//                        List<Bitmap> rowImages = mPictures.get(i);
//                        List<Matrix> rowMatrices = mMatrices.get(i);
//                        List<Integer> rowPaths = paths.get(i);
//                        float pixelsPerFrame = speeds.get(i);
//                        for (int j = 0; j < rowImages.size(); j++) {
//                            rowPaths.set(j, transform(rowImages.get(j),
//                                    rowMatrices.get(j),
//                                    rowPaths.get(j),
//                                    surfaceHeight,
//                                    pixelsPerFrame,
//                                    values));
//                        }
//                        if (pixelsPerFrame < surfaceHeight / 20) {
//                            pixelsPerFrame *= 1.025;
//                            speeds.set(i, pixelsPerFrame);
//                        }
//                    }
//                }
//                canvas = null;
//                try {
//                    // получаем объект Canvas и выполняем отрисовку
//                    synchronized (mSurfaceHolder) {
//                        canvas = mSurfaceHolder.lockCanvas(null);
//                        if (canvas != null) {
//                            canvas.drawColor(Color.WHITE);
//                            for (int i = 0; i < mPictures.size(); i++) {
//                                List<Bitmap> rowImages = mPictures.get(i);
//                                List<Matrix> rowMatrices = mMatrices.get(i);
//                                for (int j = 0; j < rowImages.size(); j++) {
//                                    canvas.drawBitmap(rowImages.get(j),
//                                            rowMatrices.get(j),
//                                            null);
//                                }
//                            }
//                        } else {
//                            mRunFlag = false;
//                        }
//                    }
//                } finally {
//                    if (canvas != null) {
//                        // отрисовка выполнена. выводим результат на экран
//                        mSurfaceHolder.unlockCanvasAndPost(canvas);
//                    }
//                }
//            }
//        }
//    }
}
