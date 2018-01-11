package org.terenfear.dropanimation;

import android.opengl.Matrix;

import java.util.Random;

/**
 * Created with IntlliJ IDEA<br>
 * User: Pavel Kozlovich <br>
 * E-mail: terenfear@gmail.com<br>
 * Skype: terenfear962<br>
 * Date  06.12.2017<br>
 * Time: 18:28<br>
 * Project name: TestDropAnimation<br>
 * ======================================================================================================================
 */
public class DropObject {

    private float mOffsetY;
    private final float mAngle;
    private final float mOffsetZ;
    private final long mDelayTime;
    private final int mTextureId;
    private float mTraveledDistance;
    private long mStartTime = 0;
    private float[] mTranslationMat = new float[16];
    private boolean mInMotion = false;

    public DropObject(float objWH, int textureId, float initOffsetY, long delayTime) {
        mTextureId = textureId;
        mDelayTime = delayTime;
        Random random = new Random();
        float maxOffset = objWH / 8;
        mOffsetY = -maxOffset + random.nextFloat() * maxOffset * 2 + initOffsetY;
        mOffsetZ = random.nextFloat() + 1;
        mAngle = random.nextFloat() * 360;
    }

    public float travel(float maxDistance, float acceleration, long currentTime) {
        return this.travel(maxDistance, acceleration, currentTime, 0);
    }

    public float travel(float maxDistance, float acceleration, long currentTime, float startVelocity) {
        if (mInMotion) {
            if (mStartTime == 0) {
                mStartTime = currentTime;
                return 0;
            } else {
                long traveledTime = currentTime - mStartTime;
                if (traveledTime >= mDelayTime) {
                    if (mTraveledDistance < maxDistance) {
                        long tempTime = traveledTime - mDelayTime;
                        mTraveledDistance = (float) ( startVelocity * tempTime + acceleration * Math.pow(tempTime, 2) / 2);
                    } else {
                        mInMotion = false;
                    }
                    if (mTraveledDistance > maxDistance) {
                        mTraveledDistance = maxDistance;
                    }
                }
            }
        }
        return mTraveledDistance;
    }

    public float getAngle() {
        return mAngle;
    }

    public float[] getTranslationMat(float maxDistance, float acceleration, long currentTime, float startVelocity) {
        Matrix.setIdentityM(mTranslationMat, 0);
        Matrix.translateM(mTranslationMat, 0, 0, mOffsetY - travel(maxDistance, acceleration, currentTime, startVelocity), mOffsetZ);
        return mTranslationMat;
    }

    public int getTextureId() {
        return mTextureId;
    }

    public boolean isInMotion() {
        return mInMotion;
    }

    public void setInMotion(boolean inMotion) {
        mInMotion = inMotion;
    }

    public void resetTraveled() {
        mStartTime = 0;
        mOffsetY -= mTraveledDistance;
        mTraveledDistance = 0;
    }
}
