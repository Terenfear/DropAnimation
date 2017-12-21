package org.terenfear.dropanimation;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.terenfear.dropanimation.data.RendererData;
import org.terenfear.dropanimation.data.Shaders;
import org.terenfear.dropanimation.enums.AnimType;

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
 * Date  21.12.2017<br>
 * Time: 11:30<br>
 * Project name: TestDropAnimation<br>
 * ======================================================================================================================
 */
public class DropItemsRenderer implements GLSurfaceView.Renderer {

    //==============================================================================================
    //------------------------------------------Interfaces------------------------------------------
    //==============================================================================================
    public interface RendererCallback {
        void requestRedraw();
        void requestDirtyRendering();
        void requestContiniousRendering();
        void onAnimationStarted(AnimType animType);
        void onAnimationFinished(AnimType animType);
    }

    //==============================================================================================
    //-------------------------------------------Fields---------------------------------------------
    //==============================================================================================
    private static final String TAG = DropItemsRenderer.class.getSimpleName();

    private FloatBuffer mTextureCoordsBuffer = ByteBuffer
            .allocateDirect(Shaders.TEXTURE_COORDS.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(Shaders.TEXTURE_COORDS);

    private int mPositionAttr;
    private int mTexturePositionAttr;
    private int mMVPMatrixAttr;

    @Nullable
    private RendererCallback mCallback;
    private Resources mResources;
    private volatile RendererData mRendererData = new RendererData();
    private Random mRandom = new Random();

    private int mViewportWidth;
    private int mViewportHeight;

    private boolean mIsStop;
    private boolean mInMotion = false;
    private AnimType mCurrentAnimType = AnimType.DROP_IN;

    private float mMaxDistance = 2f;
    private float mAcceleration;

    private int mNumRows;
    private float mObjectWH;

    private FloatBuffer mPositionBuffer;
    private volatile List<DropObject> mDropObjectList = new ArrayList<>();
    private int[] mBitmapArray;

    private float[] mMVPMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mScratchMatrix = new float[16];

    //==============================================================================================
    //----------------------------------------Constructors------------------------------------------
    //==============================================================================================
    public DropItemsRenderer(Resources resources) {
        mResources = resources;
    }


    //==============================================================================================
    //---------------------------------------Override methods---------------------------------------
    //==============================================================================================
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(mRendererData.getRColor(),
                mRendererData.getGColor(),
                mRendererData.getBColor(),
                mRendererData.getAColor());

        GLES20.glEnable(GL10.GL_BLEND);
        GLES20.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

        int iVShader, iFShader, iProgId;
        int[] link = new int[1];
        iVShader = loadShader(Shaders.VERTEX_SHADER, GLES20.GL_VERTEX_SHADER);
        iFShader = loadShader(Shaders.FRAGMENT_SHADER, GLES20.GL_FRAGMENT_SHADER);

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

        mPositionAttr = GLES20.glGetAttribLocation(iProgId, Shaders.ATTR_POSITION);
        mTexturePositionAttr = GLES20.glGetAttribLocation(iProgId, Shaders.ATTR_TEXTURE_COORDINATE);
        mMVPMatrixAttr = GLES20.glGetUniformLocation(iProgId, Shaders.ATTR_MVP_MATRIX);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: ");

        mViewportWidth = width;
        mViewportHeight = height;
        GLES20.glViewport(0, 0, mViewportWidth, mViewportHeight);

        float screenRatio = (float) mViewportWidth / mViewportHeight;
        Matrix.orthoM(mProjectionMatrix, 0, -screenRatio, screenRatio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(mRendererData.getRColor(),
                mRendererData.getGColor(),
                mRendererData.getBColor(),
                mRendererData.getAColor());
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // TODO: 21.12.2017 maybe move it to view
        if (!mDropObjectList.isEmpty() && mPositionBuffer != null) {
            if (mBitmapArray == null && mRendererData.getResourceIds() != null) {
                mBitmapArray = loadTextureArrayFromRes(mRendererData.getResourceIds());
            }

            Matrix.setIdentityM(mMVPMatrix, 0);
            Matrix.setIdentityM(mScratchMatrix, 0);

            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7, 0, 0, -1, 0, 1, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

            Matrix.translateM(mMVPMatrix, 0,
                    mObjectWH - (mRendererData.getRowLength() * mObjectWH) / 2f,
                    1 + mObjectWH / 2,
                    0);

            mPositionBuffer.position(0);
            GLES20.glVertexAttribPointer(mPositionAttr, 3, GLES20.GL_FLOAT, false, 0, mPositionBuffer);
            GLES20.glEnableVertexAttribArray(mPositionAttr);

            mTextureCoordsBuffer.position(0);
            GLES20.glVertexAttribPointer(mTexturePositionAttr, 2, GLES20.GL_FLOAT, false, 0, mTextureCoordsBuffer);
            GLES20.glEnableVertexAttribArray(mTexturePositionAttr);

            if( !mIsStop ){
                try{
                    drawAllObjects();
                } catch (Throwable throwable){
                    throwable.printStackTrace();
                }
            }

            GLES20.glDisableVertexAttribArray(mPositionAttr);
            GLES20.glDisableVertexAttribArray(mTexturePositionAttr);
        }
    }

    //==============================================================================================
    //---------------------------------------Public methods-----------------------------------------
    //==============================================================================================
    public int[] loadTextureArrayFromRes(@DrawableRes int... resourceIds) {
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
            Bitmap bitmap = BitmapFactory.decodeResource(mResources, resourceIds[i], options);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[i]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
        return textureIds;
    }

    public void stopAnimation(){
        mCurrentAnimType = AnimType.DROP_IN;
        mDropObjectList.clear();
        mIsStop = true;
        setObjectsInMotion(false);
    }

    public void startDropIn() {
        if (mInMotion) {
            return;
        }
        mIsStop = false;
        mCurrentAnimType = AnimType.DROP_IN;
        mDropObjectList.clear();
        initObjects();
        mAcceleration = (float) (2 * mMaxDistance / Math.pow(mRendererData.getDuration() / 2, 2));

        setObjectsInMotion(true);
    }

    public void startDropOut() {
        if (mInMotion) {
            return;
        }
        mIsStop = false;
        mCurrentAnimType = AnimType.DROP_OUT;
        if (mDropObjectList.isEmpty()) {
            initObjects();
        } else {
            float excessiveHeight = (mNumRows * mObjectWH - 2) * mRendererData.getObjectScale();
            mMaxDistance = 2 + excessiveHeight * 1.1f;
        }
        mAcceleration = (float) (2 * mMaxDistance / Math.pow(mRendererData.getDuration() / 2, 2));

        setObjectsInMotion(true);
    }

    public void setStop(boolean stop) {
        mIsStop = stop;
    }

    public void setCallback(@Nullable RendererCallback callback) {
        mCallback = callback;
    }

    public void setRendererData(@NonNull RendererData rendererData) {
        mRendererData = rendererData;
    }

    //==============================================================================================
    //---------------------------------------Private methods----------------------------------------
    //==============================================================================================
    private FloatBuffer createVertexBuffer(float objWH) {
        float half = objWH / 2;
        float[] posMatrix = {
                -half, -half, 1,
                half, -half, 1,
                -half,  half, 1,
                half,  half, 1
        };
        return ByteBuffer.allocateDirect(posMatrix.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(posMatrix);
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

    private void drawAllObjects() {
        long time = SystemClock.uptimeMillis();
        if( mDropObjectList.isEmpty() ){
            return;
        }
        boolean inMotion = false;
        int rowLength = mRendererData.getRowLength();
        for (int i = 0; i < mNumRows; i++) {
            for (int j = 0; j < rowLength; j++) {
                DropObject obj = mDropObjectList.get(i * rowLength + j);
                drawOneObject(obj, time);
                inMotion = inMotion || obj.isInMotion();
                Matrix.translateM(mMVPMatrix, 0, mObjectWH, 0, 0);
            }
            Matrix.translateM(mMVPMatrix, 0, -mObjectWH * rowLength, 0, 0);
        }
        if (!inMotion) {
            setObjectsInMotion(false);
        }
    }

    private void drawOneObject(DropObject dropObject, long time) {
        if (mBitmapArray != null && mBitmapArray.length != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBitmapArray[dropObject.getTextureId()]);
        }

        float startVelocity = mCurrentAnimType == AnimType.DROP_IN ?
                mRendererData.getStartDropInVelocity() :
                mRendererData.getStartDropOutVelocity();

        float objectScale = mRendererData.getObjectScale();
        Matrix.setIdentityM(mScratchMatrix, 0);
        Matrix.multiplyMM(mScratchMatrix, 0, mMVPMatrix, 0,
                dropObject.getTranslationMat(mMaxDistance, mAcceleration, time, startVelocity), 0);
        Matrix.scaleM(mScratchMatrix, 0, objectScale, objectScale, 1);
        Matrix.rotateM(mScratchMatrix, 0, dropObject.getAngle(), 0, 0, -1);

        GLES20.glUniformMatrix4fv(mMVPMatrixAttr, 1, false, mScratchMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private void initObjects() {
        int rowLength = mRendererData.getRowLength();
        long duration = mRendererData.getDuration();
        mObjectWH = calcObjectWidthHeight(mViewportWidth, mViewportHeight, rowLength);
        mPositionBuffer = createVertexBuffer(mObjectWH);

        List<Long> delayTimeList = new ArrayList<>();
        mNumRows = Math.round(2 / mObjectWH) + 1;
        for (int i = 0; i < rowLength; i++) {
            delayTimeList.add(0L);
        }

        long minDelay = duration / mNumRows / 3;
        long maxDelay = duration / mNumRows / 2;

        mMaxDistance = 2f;
        float objectScale = mRendererData.getObjectScale();
        float animTypeOffsetY;
        if (mCurrentAnimType == AnimType.DROP_IN) {
            animTypeOffsetY = mObjectWH / 2 * objectScale;
            mMaxDistance +=animTypeOffsetY;
        } else {
            float excessiveHeight = (mNumRows * mObjectWH - 2) * objectScale;
            mMaxDistance += excessiveHeight * 1.1f;
            animTypeOffsetY = -2;
        }

        mDropObjectList = initObjectList(animTypeOffsetY, delayTimeList, minDelay, maxDelay);
    }

    private List<DropObject> initObjectList(float animTypeOffsetY, List<Long> delayTimeList,
                                            long minDelay, long maxDelay) {
        List<DropObject> dropObjects = new ArrayList<>();
        float offsetY;
        long delay;
        int[] resourceIds = mRendererData.getResourceIds();
        for (int i = 0; i < mNumRows; i++) {
            offsetY = mObjectWH * i + animTypeOffsetY;
            for (int j = 0; j < mRendererData.getRowLength(); j++) {
                delay = delayTimeList.get(j);
                delay += randomLong(minDelay, maxDelay);
                int textureId = 0;
                if (resourceIds != null) {
                    textureId = mRandom.nextInt(resourceIds.length);
                }
                dropObjects.add(new DropObject(mObjectWH, textureId, offsetY, delay));
                delayTimeList.set(j, delay);
            }
        }
        return dropObjects;
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
            prepareMotionStart();
        } else {
            prepareMotionFinish();
        }
    }

    private void prepareMotionStart() {
        if (mCurrentAnimType == AnimType.DROP_OUT) {
            for (DropObject obj : mDropObjectList) {
                obj.resetTraveled();
            }
        }
        if (mCallback != null) {
            mCallback.requestContiniousRendering();
            mCallback.onAnimationStarted(mCurrentAnimType);
        }
    }

    private void prepareMotionFinish() {
        if (mCurrentAnimType == AnimType.DROP_OUT) {
            mDropObjectList.clear();
        }
        if (mCallback != null) {
            mCallback.requestDirtyRendering();
            mCallback.onAnimationFinished(mCurrentAnimType);
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
}