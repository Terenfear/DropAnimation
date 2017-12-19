package org.terenfear.dropanimation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;

import org.intellij.lang.annotations.Language;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created with IntlliJ IDEA<br>
 * User: Pavel Kozlovich <br>
 * E-mail: terenfear@gmail.com<br>
 * Skype: terenfear962<br>
 * Date  07.12.2017<br>
 * Time: 13:40<br>
 * Project name: TestDropAnimation<br>
 * ======================================================================================================================
 */
public class DropItemsView extends GLSurfaceView {

    public interface AnimStartListener {
        void onAnimationStarted();
    }

    public interface AnimEndListener {
        void onAnimationEnded();
    }

    public static final String MVP_MATRIX = "uMVPMatrix";
    public static final String POSITION = "vPosition";
    public static final String TEXTURE_COORDINATE = "vTextureCoordinate";

    private static final float TEXTURE_COORDS[] = {
            0, 1, // X1,Y1
            1, 1, // X2,Y2
            0, 0, // X3,Y3
            1, 0, // X4,Y4
    };
    private FloatBuffer TEXTURE_COORDS_BUFFER = ByteBuffer
            .allocateDirect(TEXTURE_COORDS.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(TEXTURE_COORDS);

    @Language("GLSL")
    private static final String VERTEX_SHADER = ""+
            "precision mediump float;" +
            "uniform mat4 " + MVP_MATRIX + ";" +
            "attribute vec4 " + POSITION + ";" +
            "attribute vec4 " + TEXTURE_COORDINATE + ";" +
            "varying vec2 position;" +
            "void main(){" +
            " gl_Position = " + MVP_MATRIX + " * " + POSITION + ";" +
            " position = " + TEXTURE_COORDINATE + ".xy;" +
            "}";
    @Language("GLSL")
    private static final String FRAGMENT_SHADER = ""+
            "precision mediump float;" +
            "uniform sampler2D uTexture;" +
            "varying vec2 position;" +
            "void main() {" +
            "    gl_FragColor = texture2D(uTexture, position);" +
            "}";

    // region Variables
    private static final String TAG = "TEST";
    private FloatBuffer positionBuffer;
    private int vPosition;
    private int vTexturePosition;
    private int uMVPMatrix;
    private float[] mvpMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] scratch = new float[16];
    private volatile List<DropObject> mDropObjectList = new ArrayList<>();
    private float mMaxDistance = 2f;
    private float mAcceleration;
    private int mNumRows;
    private float mObjectWH;
    private boolean mDroppingOut = false;
    private Random mRandom = new Random();
    private int[] mBitmapArray;
    private int mWidth;
    private int mHeight;
    private int[] mResourceIds;
    private AnimStartListener mStartListener;
    private AnimEndListener mEndListener;
    private boolean mInMotion = false;
    private long mDuration = 2000;
    private int mRowLength = 10;
    private float mObjectScale = 1.9f;
    private boolean mIsStop;
    // endregion Variables

    public DropItemsView(Context context) {
        super(context);
        init();
    }

    public DropItemsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //==============================================================================================
    //---------------------------------------Public methods-----------------------------------------
    //==============================================================================================
    public void stopAnimation(){
        mDroppingOut = false;
        mDropObjectList.clear();
        initAnimationObjects();
        mIsStop = true;
        setObjectsInMotion(false);
    }

    public void startDropIn() {
        if (mInMotion) {
            return;
        }
        Log.d(TAG, "startDropIn: ");
        mIsStop = false;
        mDroppingOut = false;
        mDropObjectList.clear();
        initAnimationObjects();

        setObjectsInMotion(true);
    }

    public void startDropOut() {
        if (mInMotion) {
            return;
        }
        mIsStop = false;
        mDroppingOut = true;
        if (mDropObjectList.isEmpty()) {
            initAnimationObjects();
        } else {
            float excessiveHeight = (mNumRows * mObjectWH - 2) * mObjectScale;
            mMaxDistance = 2 + excessiveHeight * 1.1f;
        }

        setObjectsInMotion(true);
    }

    public DropItemsView setStartListener(AnimStartListener startListener) {
        mStartListener = startListener;
        return this;
    }

    public DropItemsView setEndListener(AnimEndListener endListener) {
        mEndListener = endListener;
        return this;
    }

    public DropItemsView setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    public DropItemsView setRowLength(int rowLength) {
        mRowLength = rowLength;
        return this;
    }

    public DropItemsView setObjectScale(float objectScale) {
        mObjectScale = objectScale;
        return this;
    }

    public DropItemsView setResourceIds(@DrawableRes int... resourceIds) {
        mResourceIds = resourceIds;
        return this;
    }

    //==============================================================================================
    //---------------------------------------Private methods----------------------------------------
    //==============================================================================================

    private void init() {
        Log.d(TAG, "init: " + Thread.currentThread().getId());
        setZOrderOnTop(true);
        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8,8,8,8,0,0);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mWidth = getWidth();
                mHeight = getHeight();
            }
        });

    }

    private void initAnimationObjects() {
        mObjectWH = calcObjectWidthHeight(mWidth, mHeight, mRowLength);
        positionBuffer = createVertexBuffer(mObjectWH);

        List<Long> delayTimeList = new ArrayList<>();
        mNumRows = Math.round(2 / mObjectWH) + 1;
        long minDelay = mDuration / mNumRows / 3;
        long maxDelay = mDuration / mNumRows / 2;
        for (int i = 0; i < mRowLength; i++) {
            delayTimeList.add(randomLong(minDelay, maxDelay));
        }
        mMaxDistance = 2f;
        float animTypeOffsetY;
        if (mDroppingOut) {
            float excessiveHeight = (mNumRows * mObjectWH - 2) * mObjectScale;
            mMaxDistance += excessiveHeight * 1.1f;
            animTypeOffsetY = -2;
        } else {
            animTypeOffsetY = mObjectWH / 2 * mObjectScale;
            mMaxDistance +=animTypeOffsetY;
        }
        float offsetY;
        long delay;
        for (int i = 0; i < mNumRows; i++) {
            offsetY = mObjectWH * i + animTypeOffsetY;
            for (int j = 0; j < mRowLength; j++) {
                delay = delayTimeList.get(j);
                delay += randomLong(minDelay, maxDelay);
                int textureId = mRandom.nextInt(mResourceIds.length);
                mDropObjectList.add(new DropObject(mObjectWH, textureId, offsetY, delay));
                delayTimeList.set(j, delay);
            }
        }
        mAcceleration = (float) (2 * mMaxDistance / Math.pow(mDuration / 2, 2));
    }

    private long randomLong(long min, long max) {
        return (long) (min + mRandom.nextDouble() * (max - min));
    }

    private void setObjectsInMotion(boolean inMotion) {
        mInMotion = inMotion;
        for (DropObject obj : mDropObjectList) {
            obj.setInMotion(inMotion);
        }
        if (inMotion) {
            if (mDroppingOut) {
                for (DropObject obj : mDropObjectList) {
                    obj.resetTraveled();
                }
            }
            setRenderMode(RENDERMODE_CONTINUOUSLY);
            new Handler(Looper.getMainLooper())
                    .post(() -> {
                        if (mStartListener != null) {
                            mStartListener.onAnimationStarted();
                        }
                    });
        } else {
            Log.d(TAG, "setObjectsInMotion: acc = " + mAcceleration);
            if (mDroppingOut) {
                mDropObjectList.clear();
            }
            setRenderMode(RENDERMODE_WHEN_DIRTY);
            new Handler(Looper.getMainLooper())
                    .post(() -> {
                        if (mEndListener != null) {
                            mEndListener.onAnimationEnded();
                        }
                    });
        }
    }

    private float calcObjectWidthHeight(int width, int height, int numItems) {
        float ratio = (float) width / height;
        if (ratio < 1) {
            ratio = 1 / ratio;
        }
        float numInPlane = numItems / ratio;
        return (float) 2 / numInPlane;
    }

    private FloatBuffer createVertexBuffer(float objWH) {
        float half = objWH / 2;
        float[] posMatrix = {
                -half, -half, 1,  // X1,Y1,Z1
                half, -half, 1,   // X2,Y2,Z2
                -half,  half, 1,  // X3,Y3,Z3
                half,  half, 1    // X4,Y4,Z4
        };
        return ByteBuffer.allocateDirect(posMatrix.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(posMatrix);
    }

    private void drawAllObjects() {
        long time = SystemClock.uptimeMillis();
        if( mDropObjectList.isEmpty() ){
            return;
        }
        boolean inMotion = false;
        for (int i = 0; i < mNumRows; i++) {
            for (int j = 0; j < mRowLength; j++) {
                DropObject obj = mDropObjectList.get(i * mRowLength + j);
                drawOneObject(obj, time);
                inMotion = inMotion || obj.isInMotion();
                Matrix.translateM(mvpMatrix, 0, mObjectWH, 0, 0);
            }
            Matrix.translateM(mvpMatrix, 0, -mObjectWH * mRowLength, 0, 0);
        }
        if (!inMotion) {
            setObjectsInMotion(false);
        }
    }

    private void drawOneObject(DropObject dropObject, long time) {
        if (mBitmapArray != null && mBitmapArray.length != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBitmapArray[dropObject.getTextureId()]);
        }
        Matrix.setIdentityM(scratch, 0);
        Matrix.multiplyMM(scratch, 0, mvpMatrix, 0, dropObject.getTranslationMat(mMaxDistance, mAcceleration, time), 0);
        Matrix.scaleM(scratch, 0, mObjectScale, mObjectScale, 1);
        Matrix.rotateM(scratch, 0, dropObject.getAngle(), 0, 0, -1);
        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, scratch, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private int[] loadTextureArrayFromRes(@DrawableRes int... resourceIds) {
        Log.d(TAG, "loadTextureArrayFromRes: " + Thread.currentThread().getId());
        final int[] textureIds = new int[resourceIds.length];
        GLES20.glGenTextures(resourceIds.length, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.d("Test", "loadTextureArrayFromRes: " +
                    GLUtils.getEGLErrorString(GLES20.glGetError()));
            return new int[] {0};
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        for (int i = 0; i < resourceIds.length; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceIds[i], options);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[i]);

            GLES20.glEnable(GL10.GL_BLEND);
            GLES20.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
        return textureIds;
    }

    private int loadShader(final String strSource, final int iType) {
        int[] compiled = new int[1];
        int iShader = GLES20.glCreateShader(iType);
        GLES20.glShaderSource(iShader, strSource);
        GLES20.glCompileShader(iShader);
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            throw new RuntimeException("Compilation failed : " + GLES20.glGetShaderInfoLog(iShader));
        }
        return iShader;
    }

    //==============================================================================================
    //---------------------------------------Inner classes------------------------------------------
    //==============================================================================================

    private GLSurfaceView.Renderer mRenderer = new Renderer() {

    //==============================================================================================
    //---------------------------------------Override methods---------------------------------------
    //==============================================================================================

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            Log.d(TAG, "onSurfaceCreated: ");
            //GLES20.glClearColor(1f, 1f, 1f, 0f);
            gl10.glDisable(GL10.GL_DITHER);
            gl10.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

            gl10.glClearColor(0,0,0,0);
            gl10.glEnable(GL10.GL_CULL_FACE);
            gl10.glShadeModel(GL10.GL_SMOOTH);
            gl10.glEnable(GL10.GL_DEPTH_TEST);

            // Then, we load the shaders into a program
            int iVShader, iFShader, iProgId;
            int[] link = new int[1];
            iVShader = loadShader(VERTEX_SHADER, GLES20.GL_VERTEX_SHADER);
            iFShader = loadShader(FRAGMENT_SHADER, GLES20.GL_FRAGMENT_SHADER);

            iProgId = GLES20.glCreateProgram();
            GLES20.glAttachShader(iProgId, iVShader);
            GLES20.glAttachShader(iProgId, iFShader);
            GLES20.glLinkProgram(iProgId);

            GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0);
            if (link[0] <= 0) {
                throw new RuntimeException("Program couldn't be loaded");
            }
            GLES20.glDeleteShader(iVShader);
            GLES20.glDeleteShader(iFShader);
            GLES20.glUseProgram(iProgId);

            // Now that our program is loaded and in use, we'll retrieve the handles of the parameters
            // we pass to our shaders
            vPosition = GLES20.glGetAttribLocation(iProgId, POSITION);
            vTexturePosition = GLES20.glGetAttribLocation(iProgId, TEXTURE_COORDINATE);
            uMVPMatrix = GLES20.glGetUniformLocation(iProgId, MVP_MATRIX);
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            Log.d(TAG, "onSurfaceChanged: ");
            GLES20.glViewport(0, 0, width, height);
            float screenRatio = (float) width / height;
            Matrix.orthoM(projectionMatrix, 0, -screenRatio, screenRatio, -1, 1, 3, 7);
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            if (!mDropObjectList.isEmpty() && positionBuffer != null) {

                if (mBitmapArray == null && mResourceIds != null) {
                    mBitmapArray = loadTextureArrayFromRes(mResourceIds);
                }

                Matrix.setIdentityM(mvpMatrix, 0);
                Matrix.setIdentityM(scratch, 0);

                Matrix.setLookAtM(viewMatrix, 0, 0, 0, 7, 0, 0, -1, 0, 1, 0);
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

                Matrix.translateM(mvpMatrix, 0, (mObjectWH - (mRowLength * mObjectWH)) / 2f, 1 + mObjectWH / 2, 0);

                positionBuffer.position(0);
                GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 0, positionBuffer);
                GLES20.glEnableVertexAttribArray(vPosition);

                // We pass the buffer for the texture position
                TEXTURE_COORDS_BUFFER.position(0);
                GLES20.glVertexAttribPointer(vTexturePosition, 2, GLES20.GL_FLOAT, false, 0, TEXTURE_COORDS_BUFFER);
                GLES20.glEnableVertexAttribArray(vTexturePosition);

                if( !mIsStop ){
                    try{
                        drawAllObjects();
                    } catch (Throwable throwable){
                        throwable.printStackTrace();
                    }
                }

                GLES20.glDisableVertexAttribArray(vPosition);
                GLES20.glDisableVertexAttribArray(vTexturePosition);
            }
        }
    };

}
