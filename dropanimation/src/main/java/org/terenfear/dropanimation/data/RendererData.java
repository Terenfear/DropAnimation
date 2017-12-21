package org.terenfear.dropanimation.data;

import android.support.annotation.Nullable;

import org.terenfear.dropanimation.DropObject;

import java.util.ArrayList;
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
    private float mAColor = 0f;
    private float mRColor = 0f;
    private float mGColor = 0f;
    private float mBColor = 0f;

    @Nullable
    private int[] mResourceIds;

    private long mDuration = 2000;
    private int mRowLength = 10;
    private float mObjectScale = 1.9f;
    private float mStartDropInVelocity = 0;
    private float mStartDropOutVelocity = 0;

    public synchronized float getAColor() {
        return mAColor;
    }

    public synchronized void setAColor(float AColor) {
        mAColor = AColor;
    }

    public synchronized float getRColor() {
        return mRColor;
    }

    public synchronized void setRColor(float RColor) {
        mRColor = RColor;
    }

    public synchronized float getGColor() {
        return mGColor;
    }

    public synchronized void setGColor(float GColor) {
        mGColor = GColor;
    }

    public synchronized float getBColor() {
        return mBColor;
    }

    public synchronized void setBColor(float BColor) {
        mBColor = BColor;
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
        setAColor(a);
        setRColor(r);
        setGColor(g);
        setBColor(b);
    }
}
