package org.terenfear.dropanimation.data;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created with IntlliJ IDEA<br>
 * User: Pavel Kozlovich <br>
 * E-mail: terenfear@gmail.com<br>
 * Skype: terenfear962<br>
 * Date  21.12.2017<br>
 * Time: 11:36<br>
 * Project name: TestDropAnimation<br>
 * ======================================================================================================================
 */
public class RendererData {
    public static final float DEFAULT_COLOR_VALUE = 0f;
    public static final int DEFAULT_COLOR = Color.BLACK;
    public static final int DEFAULT_DURATION = 2000;
    public static final int DEFAULT_ROW_LENGTH = 10;
    public static final float DEFAULT_OBJECT_SCALE = 1.9f;
    public static final int DEFAULT_VELOCITY = 0;

    private float mAlphaValue;
    private float mRedValue;
    private float mGreenValue;
    private float mBlueValue;

    @Nullable
    private int[] mResourceIds;

    private long mDuration;
    private int mRowLength;
    private float mObjectScale;
    private float mStartDropInVelocity;
    private float mStartDropOutVelocity;

    public RendererData() {
       mAlphaValue = DEFAULT_COLOR_VALUE;
       mRedValue = DEFAULT_COLOR_VALUE;
       mGreenValue = DEFAULT_COLOR_VALUE;
       mBlueValue = DEFAULT_COLOR_VALUE;

       mDuration = DEFAULT_DURATION;
       mRowLength = DEFAULT_ROW_LENGTH;
       mObjectScale = DEFAULT_OBJECT_SCALE;
       mStartDropInVelocity = DEFAULT_VELOCITY;
       mStartDropOutVelocity = DEFAULT_VELOCITY;
    }

    public synchronized float getAlphaValue() {
        return mAlphaValue;
    }

    public synchronized void setAlphaValue(float alphaValue) {
        mAlphaValue = alphaValue;
    }

    public synchronized float getRedValue() {
        return mRedValue;
    }

    public synchronized void setRedValue(float redValue) {
        mRedValue = redValue;
    }

    public synchronized float getGreenValue() {
        return mGreenValue;
    }

    public synchronized void setGreenValue(float greenValue) {
        mGreenValue = greenValue;
    }

    public synchronized float getBlueValue() {
        return mBlueValue;
    }

    public synchronized void setBlueValue(float blueValue) {
        mBlueValue = blueValue;
    }

    @Nullable
    public synchronized int[] getResourceIds() {
        return mResourceIds;
    }

    public synchronized void setResourceIds(@Nullable int[] resourceIds) {
        mResourceIds = resourceIds;
    }

    public synchronized void setResourceIds(@Nullable List<Integer> resourceIds) {
        if (resourceIds != null) {
            mResourceIds = new int[resourceIds.size()];
            Integer id;
            for (int i = 0; i < resourceIds.size(); i++) {
                id = resourceIds.get(i);
                mResourceIds[i] = id;
            }
        }
    }

    public synchronized long getDuration() {
        return mDuration;
    }

    public synchronized void setDuration(long duration) {
        mDuration = duration;
    }

    public synchronized int getRowLength() {
        return mRowLength;
    }

    public synchronized void setRowLength(int rowLength) {
        mRowLength = rowLength;
    }

    public synchronized float getObjectScale() {
        return mObjectScale;
    }

    public synchronized void setObjectScale(float objectScale) {
        mObjectScale = objectScale;
    }

    public synchronized float getStartDropInVelocity() {
        return mStartDropInVelocity;
    }

    public synchronized void setStartDropInVelocity(float startDropInVelocity) {
        mStartDropInVelocity = startDropInVelocity;
    }

    public synchronized float getStartDropOutVelocity() {
        return mStartDropOutVelocity;
    }

    public synchronized void setStartDropOutVelocity(float startDropOutVelocity) {
        mStartDropOutVelocity = startDropOutVelocity;
    }

    public synchronized void setBackgroundColor(float a, float r, float g, float b) {
        setAlphaValue(a);
        setRedValue(r);
        setGreenValue(g);
        setBlueValue(b);
    }

    public synchronized void setBackgroundColor(@NonNull String backgroundColor) {
        int color = Color.parseColor(backgroundColor);
        setBackgroundColor(color);
    }

    public synchronized void setBackgroundColor(int color) {
        setAlphaValue(Color.alpha(color) / 255f);
        setRedValue(Color.red(color) / 255f);
        setGreenValue(Color.green(color) / 255f);
        setBlueValue(Color.blue(color) / 255f);

    }
}
