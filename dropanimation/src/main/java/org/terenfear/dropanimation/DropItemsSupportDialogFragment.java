package org.terenfear.dropanimation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.TimeUnit;

/**
 * Create with Android Studio<br>
 * Created by Pavlovskii Ilya<br>
 * E-mail: pavlovskii_ilya@mail.ru, trane91666@gmail.com<br>
 * Skype: trane9119<br>
 * Date: 19.12.2017<br>
 * Time: 20:55<br>
 * Project name: DropAnimation-opengl<br>
 * ===================================================================================<br>
 */
public class DropItemsSupportDialogFragment extends DialogFragment {

    public static final String KEY_IMAGE_RESOURCES_ARRAY = "image_res_array";
    public static final String KEY_ANIMATION_DURATION = "animation_duration";
    public static final String KEY_ROW_LENGTH = "row_length";
    public static final String KEY_OBJECT_SCALE = "obj_scale";

    private static final int DEFAULT_ROW_LENGTH = 20;
    private static final long DEFAULT_DURATION = TimeUnit.SECONDS.toMillis(3);
    private static final float DEFAULT_OBJECT_SCALE = 1.9f;

    private static final String TAG = DropItemsSupportDialogFragment.class.getSimpleName();

    public static DropItemsSupportDialogFragment newInstance(){
        return new DropItemsSupportDialogFragment();
    }

    public static DropItemsSupportDialogFragment newInstance(Bundle arguments){
        DropItemsSupportDialogFragment dialogFragment = newInstance();
        dialogFragment.setArguments(arguments);
        return dialogFragment;
    }

    private DropItemsView mDropItemsView;
    private boolean mIsDropIn = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDropItemsView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mDropItemsView.onPause();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDropItemsView = new DropItemsView(getContext());
        return mDropItemsView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if( getArguments() != null ){
            int[] array = getArguments().getIntArray(KEY_IMAGE_RESOURCES_ARRAY);
            long duration = getArguments().getLong(KEY_ANIMATION_DURATION, DEFAULT_DURATION);
            int rowLength = getArguments().getInt(KEY_ROW_LENGTH, DEFAULT_ROW_LENGTH);
            float objectScale = getArguments().getFloat(KEY_OBJECT_SCALE, DEFAULT_OBJECT_SCALE);
            mDropItemsView
                    .setDuration(duration)
                    .setRowLength(rowLength)
                    .setObjectScale(objectScale)
                    .setResourceIds(array)
                    .setEndListener(type -> {
                        mIsDropIn = !mIsDropIn;
                        startAnimation();
                    });
            Log.d(TAG, "onViewCreated. pre start animation");
            startAnimation();
        }
    }

    private void startAnimation(){
        Log.d(TAG, "Animation start");
        if( mIsDropIn ){
            mDropItemsView.startDropIn();
        } else {
            mDropItemsView.startDropOut();
        }
    }
}
