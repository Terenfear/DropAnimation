package org.terenfear.dropanimation.data;

import android.support.annotation.Nullable;

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
    private float mAlphaValue = 0f;
    private float mRedValue = 0f;
    private float mGreenValue = 0f;
    private float mBlueValue = 0f;

    @Nullable
    private int[] mResourceIds;

    private long mDuration = 2000;
    private int mRowLength = 10;
    private float mObjectScale = 1.9f;
    private float mStartDropInVelocity = 0;
    private float mStartDropOutVelocity = 0;

    public synchronized float getAlphaValue() {
        return mAlphaValue;
    }

    public synchronized void setAlphaColor(float alphaColor) {
        mAlphaValue = alphaColor;
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

    public synchronized void setARGB(float a, float r, float g, float b) {
        setAlphaColor(a);
        setRedValue(r);
        setGreenValue(g);
        setBlueValue(b);
    }
}
