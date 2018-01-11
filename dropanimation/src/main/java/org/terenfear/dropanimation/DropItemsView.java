package org.terenfear.dropanimation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import org.terenfear.dropanimation.data.RendererData;
import org.terenfear.dropanimation.enums.AnimType;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

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

    public DropItemsView(Context context) {
        super(context);
        init();
    }

    public DropItemsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //==============================================================================================
    //---------------------------------------Override methods---------------------------------------
    //==============================================================================================

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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

    public DropItemsView setResourceIds(@DrawableRes int... resourceIds) {
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

    public DropItemsView setAColor(float aColor) {
        mRendererData.setAColor(aColor);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setRColor(float rColor) {
        mRendererData.setRColor(rColor);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setGColor(float gColor) {
        mRendererData.setGColor(gColor);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setBColor(float bColor) {
        mRendererData.setBColor(bColor);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setARGBColors(float a, float r, float g, float b) {
        mRendererData.setAColor(a);
        mRendererData.setRColor(r);
        mRendererData.setGColor(g);
        mRendererData.setBColor(b);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    public DropItemsView setARGBColors(@NonNull String colorString) {
        int color = Color.parseColor(colorString);
        mRendererData.setAColor(Color.alpha(color) / 255f);
        mRendererData.setRColor(Color.red(color) / 255f);
        mRendererData.setGColor(Color.green(color) / 255f);
        mRendererData.setBColor(Color.blue(color) / 255f);
        mRenderer.setRendererData(mRendererData);
        requestRender();
        return this;
    }

    //==============================================================================================
    //---------------------------------------Private methods----------------------------------------
    //==============================================================================================

    private void init() {
        mRenderer = new DropItemsRenderer(getResources());
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
