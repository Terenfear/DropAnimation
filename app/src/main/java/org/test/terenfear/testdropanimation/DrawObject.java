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
    private final long mDelayTime;
    private float mTraveledDistance;
    private long mStartTime = 0;

    public DrawObject(float objWH, float initOffsetY, long delayTime) {
        mDelayTime = delayTime;
        mOffsetY = (float) ThreadLocalRandom.current().nextDouble(-objWH / 4, objWH / 4) + initOffsetY;
        mAngle = ThreadLocalRandom.current().nextFloat() * 360;
    }

    public float getTraveledDistance(float maxDistance, float acceleration, long currentTime) {
        if (mStartTime == 0) {
            mStartTime = SystemClock.uptimeMillis();
            return 0;
        } else {
            long traveledTime = currentTime - mStartTime;
            if (traveledTime >= mDelayTime) {
                if (mTraveledDistance < maxDistance) {
                    mTraveledDistance = (float) (acceleration * Math.pow(traveledTime - mDelayTime, 2) / 2);
                }
                if (mTraveledDistance > maxDistance) {
                    mTraveledDistance = maxDistance;
                }
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
}
