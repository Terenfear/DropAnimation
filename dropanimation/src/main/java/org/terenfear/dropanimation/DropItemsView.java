package org.terenfear.dropanimation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;

import org.terenfear.dropanimation.data.RendererData;
import org.terenfear.dropanimation.enums.AnimType;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

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
        void onAnimationStarted(AnimType type);
    }

    public interface AnimFinishListener {
        void onAnimationFinished(AnimType type);
    }

    private static final String TAG = DropItemsView.class.getSimpleName();

    private AnimStartListener mStartListener;
    private AnimFinishListener mFinishListener;

    @NonNull
    private DropItemsRenderer mRenderer;
    @NonNull
    private RendererData mRendererData = new RendererData();

    public DropItemsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRenderer = new DropItemsRenderer(getResources());

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DropItemsView);
            mRendererData.setDuration(a.getInteger(R.styleable.DropItemsView_duration, RendererData.DEFAULT_DURATION));
            mRendererData.setObjectScale(a.getFloat(R.styleable.DropItemsView_objectScale, RendererData.DEFAULT_OBJECT_SCALE));
            mRendererData.setRowLength(a.getInteger(R.styleable.DropItemsView_rowLength, RendererData.DEFAULT_ROW_LENGTH));
            mRendererData.setBackgroundColor(a.getColor(R.styleable.DropItemsView_backgroundColor, RendererData.DEFAULT_COLOR));
            int arrayId = a.getResourceId(R.styleable.DropItemsView_imageResArray, 0);
            if (arrayId != 0) {
                TypedArray typedDrawableArray = getResources().obtainTypedArray(arrayId);
                List<Integer> idList = new ArrayList<>();
                int drawableId;
                for (int i = 0; i < typedDrawableArray.length(); i++) {
                    drawableId = typedDrawableArray.getResourceId(i, -1);
                    if (drawableId != -1) {
                        idList.add(drawableId);
                    } else {
                        Log.d(TAG, "DropItemsView: drawable array item #" + i + " is not defined or is not a resource");
                    }
                }
                mRendererData.setResourceIds(idList);
                typedDrawableArray.recycle();
            }
            mRendererData.setStartDropInVelocity(a.getFloat(R.styleable.DropItemsView_startDropInVelocity, RendererData.DEFAULT_VELOCITY));
            mRendererData.setStartDropOutVelocity(a.getFloat(R.styleable.DropItemsView_startDropOutVelocity, RendererData.DEFAULT_VELOCITY));
            mRendererData.setAlphaValue(a.getFloat(R.styleable.DropItemsView_backgroundAlpha, RendererData.DEFAULT_COLOR_VALUE));
            mRendererData.setRedValue(a.getFloat(R.styleable.DropItemsView_backgroundRed, RendererData.DEFAULT_COLOR_VALUE));
            mRendererData.setGreenValue(a.getFloat(R.styleable.DropItemsView_backgroundGreen, RendererData.DEFAULT_COLOR_VALUE));
            mRendererData.setBlueValue(a.getFloat(R.styleable.DropItemsView_backgroundBlue, RendererData.DEFAULT_COLOR_VALUE));

            mRenderer.setRendererData(mRendererData);
            a.recycle();
        }
        init();
    }

    public DropItemsView(Context context) {
        super(context);
        mRenderer = new DropItemsRenderer(getResources());
        init();
    }

    //==============================================================================================
    //---------------------------------------Public methods-----------------------------------------
    //==============================================================================================
    public void stopAnimation(){
        queueEvent(() -> mRenderer.stopAnimation());
    }

    public void startDropIn() {
        queueEvent(() -> mRenderer.startDropIn());
    }

    public void startDropOut() {
        queueEvent(() -> mRenderer.startDropOut());
    }

    public DropItemsView setStartListener(AnimStartListener startListener) {
        mStartListener = startListener;
        return this;
    }

    public DropItemsView setFinishListener(AnimFinishListener finishListener) {
        mFinishListener = finishListener;
        return this;
    }

    public DropItemsView setDuration(long duration) {
        mRendererData.setDuration(duration);
        mRenderer.setRendererData(mRendererData);
        return this;
    }

    public DropItemsView setRowLength(int rowLength) {
        mRendererData.setRowLength(rowLength);
        mRenderer.setRendererData(mRendererData);
        return this;
    }

    public DropItemsView setObjectScale(float objectScale) {
        mRendererData.setObjectScale(objectScale);
        mRenderer.setRendererData(mRendererData);
        return this;
    }

    public DropItemsView setImageResArray(@DrawableRes int... resourceIds) {
        mRendererData.setResourceIds(resourceIds);
        mRenderer.setRendererData(mRendererData);
        return this;
    }

    public DropItemsView setStartDropInVelocity(float velocity) {
        mRendererData.setStartDropInVelocity(velocity);
        mRenderer.setRendererData(mRendererData);
        return this;
    }

    public DropItemsView setStartDropOutVelocity(float velocity) {
        mRendererData.setStartDropOutVelocity(velocity);
        mRenderer.setRendererData(mRendererData);
        return this;
    }

    public DropItemsView setBackgroundAlphaValue(float aColor) {
        mRendererData.setAlphaValue(aColor);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setBackgroundRedValue(float rColor) {
        mRendererData.setRedValue(rColor);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setBackgroundGreenValue(float gColor) {
        mRendererData.setGreenValue(gColor);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setBackgroundBlueValue(float bColor) {
        mRendererData.setBlueValue(bColor);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setBackgroundColor(float a, float r, float g, float b) {
        mRendererData.setAlphaValue(a);
        mRendererData.setRedValue(r);
        mRendererData.setGreenValue(g);
        mRendererData.setBlueValue(b);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setBackgroundColor(@NonNull String colorString) {
        mRendererData.setBackgroundColor(colorString);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setBackgroundColorInt(int color) {
        mRendererData.setBackgroundColor(color);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    //==============================================================================================
    //---------------------------------------Private methods----------------------------------------
    //==============================================================================================

    private void init() {
        mRenderer.setCallback(mRendererCallback);

        setZOrderOnTop(true);
        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8,8,8,8,0,0);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    //==============================================================================================
    //---------------------------------------Inner classes------------------------------------------
    //==============================================================================================

    private DropItemsRenderer.RendererCallback mRendererCallback = new DropItemsRenderer.RendererCallback() {
        @Override
        public void requestRedraw() {
            requestRender();
        }

        @Override
        public void requestDirtyRendering() {
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }

        @Override
        public void requestContiniousRendering() {
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        }

        @Override
        public void onAnimationStarted(AnimType animType) {
            Observable.just(animType)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(type -> {
                        if (mStartListener != null) {
                            mStartListener.onAnimationStarted(type);
                        }
                    });
        }

        @Override
        public void onAnimationFinished(AnimType animType) {
            Observable.just(animType)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(type -> {
                        if (mFinishListener != null) {
                            mFinishListener.onAnimationFinished(type);
                        }
                    });
        }
    };
}
