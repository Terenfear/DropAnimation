package org.test.terenfear.testdropanimation;

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.ThreadLocalRandom;

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
public class DrawObject {
    private final float mOffsetY;
    private final float mAngle;
    private final float mBonusDistance;
    private float mTraveledDistance;

    public DrawObject(float objWH, float initOffsetY, float bonusDistance) {
        mBonusDistance = bonusDistance;
        mOffsetY = /*(float) ThreadLocalRandom.current().nextDouble(-objWH / 4, objWH / 4) +*/ initOffsetY + mBonusDistance;
        mAngle = /*ThreadLocalRandom.current().nextFloat() * 36*/0;
    }

    public float getTraveledDistance(float maxDistance, long maxTime, float acceleration, long currentTime) {
        float desiredDistance = maxDistance + mBonusDistance;
        long desiredTime =  Math.round((maxTime * desiredDistance / maxDistance));
        if (mTraveledDistance < desiredDistance) {
            long traveledTime = currentTime % desiredTime + 1;
            float tempTravDist = (float) (acceleration * Math.pow(traveledTime, 2) / 2);
            if (tempTravDist > mTraveledDistance) {
                mTraveledDistance = tempTravDist;
                if (mTraveledDistance > desiredDistance) {
                    mTraveledDistance = desiredDistance;
                }
            } else {
                mTraveledDistance = desiredDistance;
            }
        }
        return mTraveledDistance;
    }

    public float getOffsetY() {
        return mOffsetY;
    }

    public float getAngle() {
        return mAngle;
    }

    public float getBonusDistance() {
        return mBonusDistance;
    }
}
