package org.test.terenfear.testdropanimation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TestActivity extends AppCompatActivity
        implements GLSurfaceView.Renderer,
        View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {

    // region Constants
    public static final String MVP_MATRIX = "uMVPMatrix";
    public static final String POSITION = "vPosition";
    public static final String TEXTURE_COORDINATE = "vTextureCoordinate";
    // endregion

    // region Buffers
    private static final float[] POSITION_MATRIX_1 = {
            -1, 0.5f, 1,  // X1,Y1,Z1
            -0.5f, 0.5f, 1,  // X2,Y2,Z2
            -1, 1, 1,  // X3,Y3,Z3
            -0.5f, 1, 1,  // X4,Y4,Z4
    };
    private static final float[] POSITION_MATRIX_2 = {
            0, -1, 1,  // X1,Y1,Z1
            1, -1, 1,  // X2,Y2,Z2
            0, 0, 1,  // X3,Y3,Z3
            1, 0, 1  // X4,Y4,Z4
    };
    private FloatBuffer positionBuffer1 = ByteBuffer.allocateDirect(POSITION_MATRIX_1.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(POSITION_MATRIX_1);
    private FloatBuffer positionBuffer2 = ByteBuffer.allocateDirect(POSITION_MATRIX_2.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(POSITION_MATRIX_2);
    private static final float TEXTURE_COORDS[] = {
            0, 1, // X1,Y1
            1, 1, // X2,Y2
            0, 0, // X3,Y3
            1, 0, // X4,Y4
    };
    private FloatBuffer textureCoordsBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDS.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDS);
    // endregion Buffers

    // region Shaders

  /*
   * @Language("GLSL") may require you to install a plugin to support GLSL
   * but it's only for code highlighting and is not required
   */

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
    // endregion Shaders

    // region Variables
    private GLSurfaceView view;
    private int vPosition;
    private int vTexturePosition;
    private int uMVPMatrix;
    private ScaleGestureDetector detector;
    private float scale = 1;
    private float[] mvpMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] rotationMatrix = new float[16];
    private float[] translationMatrix = new float[16];
    private float[] scaleMatrix = new float[16];
    private float[] scratch = new float[16];
    private float[] repeat = new float[16];
    private float[] temp = new float[16];
    private List<DrawObject> mDrawObjectList;
    private Bitmap mBitmap1;
    private Bitmap mBitmap2;
    private long maxTime = 1000L;
    private long traveledTime;
    private float maxDistance = 2f;
    private float traveledDistance;
    private float acceleration = 0;
    private int numCols = 10;
    private int numRows;
    private float objWH;
    // endregion Variables

    private float calcObjectWidthHeight(int width, int height, int numItems) {
        float ratio = (float) width / height;
        if (ratio < 1) {
            ratio = 1 / ratio;
        }
        float numInPlane = numItems / ratio;
        return (float) 2 / numInPlane;
    }

    private FloatBuffer createFBO(float objWH) {
        float half = objWH / 2;
        float[] posMatrix = {
               -half, -half, 1,  // X1,Y1,Z1
                half, -half, 1,  // X2,Y2,Z2
               -half,  half, 1,  // X3,Y3,Z3
                half,  half, 1   // X4,Y4,Z4
        };
        return ByteBuffer.allocateDirect(posMatrix.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(posMatrix);
    }

    // region LifeCycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        detector = new ScaleGestureDetector(this, this);

        view = (GLSurfaceView) findViewById(R.id.surface);
//        view.setOnTouchListener(this);
        view.setPreserveEGLContextOnPause(true);
        view.setEGLContextClientVersion(2);
        view.setRenderer(this);
//        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//        view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    @Override
    protected void onResume() {
        super.onResume();
        view.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        view.onPause();
    }
    // endregion LifeCycle

    // region GLSurfaceView.Renderer
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        traveledTime = 0;
        traveledDistance = 0;
        acceleration = (float) (2 * maxDistance / Math.pow(maxTime, 2));
        scale = 1.9f;

        Matrix.setIdentityM(translationMatrix, 0);

        int width = getWindow().getDecorView().getWidth();
        int height = getWindow().getDecorView().getHeight();
        objWH = calcObjectWidthHeight(width, height, numCols);
        positionBuffer1 = createFBO(objWH);

        mDrawObjectList = new ArrayList<>();
        List<Long> delayTimeList = new ArrayList<>();
        numRows = Math.round(2 / objWH) + 1;
        for (int i = 0; i < numCols; i++) {
            delayTimeList.add(ThreadLocalRandom.current().nextLong(0, maxTime / numRows));
        }
        float offsetY;
        long delay;
        for (int i = 0; i < numRows; i++) {
            offsetY = objWH * i;
            for (int j = 0; j < numCols; j++) {
                delay = delayTimeList.get(j);
                delay += ThreadLocalRandom.current().nextLong(0, maxTime / numRows);
                Log.d("Test", "el " + i + "-" + j + ": delay = " + delay);
                mDrawObjectList.add(new DrawObject(objWH, offsetY, delay));
                delayTimeList.set(j, delay);
            }
        }

        Matrix.setIdentityM(repeat, 0);
        Matrix.translateM(repeat, 0, objWH, 0, 0);

        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, scale, scale, 1);

        // A little bit of initialization
        GLES20.glClearColor(1f, 1f, 1f, 0f);
        Matrix.setRotateM(rotationMatrix, 0, 0, 0, 0, 1.0f);

        // First, we load the picture into a texture that OpenGL will be able to use
        mBitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_drop_1);
        mBitmap2 = loadBitmapFromAssets(2);
        int texture = createFBOTexture(mBitmap1.getWidth(), mBitmap1.getHeight());
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mBitmap1);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

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
        GLES20.glViewport(0, 0, width, height);

        // OpenGL will stretch what we give it into a square. To avoid this, we have to send the ratio
        // information to the VERTEX_SHADER. In our case, we pass this information (with other) in the
        // MVP Matrix as can be seen in the onDrawFrame method.
        float ratio = (float) width / height;
        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

        // Since we requested our OpenGL thread to only render when dirty, we have to tell it to.
//        view.requestRender();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // We have setup that the background color will be black with GLES20.glClearColor in
        // onSurfaceCreated, now is the time to ask OpenGL to clear the screen with this color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setIdentityM(mvpMatrix, 0);
        Matrix.setIdentityM(scratch, 0);
        // Using matrices, we set the camera at the center, advanced of 7 looking to the center back
        // of -1
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 7, 0, 0, -1, 0, 1, 0);
        // We combine the scene setup we have done in onSurfaceChanged with the camera setup
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        // We combile that with the applied rotation
        Matrix.translateM(mvpMatrix,0,  (objWH - (numCols * objWH)) / 2f, 1 + objWH / 2, 0);
        // We attach the float array containing our Matrix to the correct handle

        // We pass the buffer for the position
        positionBuffer1.position(0);
        GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 0, positionBuffer1);
        GLES20.glEnableVertexAttribArray(vPosition);

        // We pass the buffer for the texture position
        textureCoordsBuffer.position(0);
        GLES20.glVertexAttribPointer(vTexturePosition, 2, GLES20.GL_FLOAT, false, 0, textureCoordsBuffer);
        GLES20.glEnableVertexAttribArray(vTexturePosition);

//        if (traveledDistance < maxDistance) {
//            Matrix.setIdentityM(translationMatrix, 0);
//            Log.d("Test", "traveledDistance: " + traveledDistance);
//            traveledTime = SystemClock.uptimeMillis() % maxTime + 1;
//            tempTravDist = (float) (acceleration * Math.pow(traveledTime, 2) / 2);
//            if (tempTravDist > traveledDistance) {
//                traveledDistance = tempTravDist;
//            } else {
//                traveledDistance = maxDistance;
//            }
//            Matrix.translateM(translationMatrix, 0, 0, -traveledDistance, 0);
//        }
        drawAll();
//        drawStuff();

        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(vTexturePosition);
    }

    private void drawAll() {
        long time = SystemClock.uptimeMillis();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                DrawObject obj = mDrawObjectList.get(i * numCols + j);
                drawStuff(obj, time);
                Matrix.translateM(mvpMatrix, 0, objWH, 0, 0);
            }
            Matrix.translateM(mvpMatrix, 0, -objWH * numCols, 0, 0);
        }
    }

    private void drawStuff(DrawObject drawObject, long time) {
        Matrix.setIdentityM(scratch, 0);
        Matrix.translateM(scratch, 0, 0, drawObject.getOffsetY() - drawObject.getTraveledDistance(maxDistance, acceleration, time), 0);
//        Matrix.multiplyMM(scratch, 0, scratch, 0, translationMatrix, 0);
        Matrix.multiplyMM(scratch, 0, scratch, 0, mvpMatrix, 0);
        Matrix.multiplyMM(scratch, 0, scratch, 0, scaleMatrix, 0);
        Matrix.rotateM(scratch, 0, drawObject.getAngle(), 0, 0, -1);
        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, scratch, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
    // endregion GLSurfaceView.Renderer

    // region Listener
    private float previousX, previousY;
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        detector.onTouchEvent(motionEvent);
        if (motionEvent.getPointerCount() == 1) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    previousX = motionEvent.getX();
                    previousY = motionEvent.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (previousX != motionEvent.getX()) {
                        Matrix.rotateM(rotationMatrix, 0, motionEvent.getX() - previousX, 0, 1, 0);
                    }
                    if (previousY != motionEvent.getY()) {
                        Matrix.rotateM(rotationMatrix, 0, motionEvent.getY() - previousY, 1, 0, 0);
                    }
                    this.view.requestRender();
                    previousX = motionEvent.getX();
                    previousY = motionEvent.getY();
                    break;
            }
        }

        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        if (scaleGestureDetector.getScaleFactor() != 0) {
            scale *= scaleGestureDetector.getScaleFactor();
            view.requestRender();
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }
    // endregion

    // region Utils
    private Bitmap loadBitmapFromAssets(int postfix) {
        InputStream is = null;
        try {
            is = getAssets().open("logo" + postfix + ".png");
            return BitmapFactory.decodeStream(is);
        } catch (IOException ex) {
            throw new RuntimeException();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                    //
                }
            }
        }
    }

    private int createFBOTexture(int width, int height) {
        int[] temp = new int[1];
        GLES20.glGenFramebuffers(1, temp, 0);
        int handleID = temp[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, handleID);

        int fboTex = createTexture(width, height);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTex, 0);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("GL_FRAMEBUFFER status incomplete");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return handleID;
    }

    private int createTexture(int width, int height) {
        int[] mTextureHandles = new int[1];
        GLES20.glGenTextures(1, mTextureHandles, 0);
        int textureID = mTextureHandles[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        return textureID;
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
    // endregion Utils
}