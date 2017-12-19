package org.test.terenfear.testdropanimation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private DropItemsView mDropItemsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDropItemsView = findViewById(R.id.vDropItemSurface);
        mDropItemsView
                .setDuration(5000)
                //.setObjectScale(1.9f)
                .setObjectScale(1f)
                .setRowLength(20)
                .setResourceIds(
                        R.drawable.sticker_batman,
                        R.drawable.sticker_crown,
                        R.drawable.sticker_lips,
                        R.drawable.sticker_glasses)
                .setStartListener(() -> Log.d(TAG, "anim start"))
                .setEndListener(() -> Log.d(TAG, "anim end"));

        findViewById(R.id.vBtnDropIn).setOnClickListener(view -> mDropItemsView.animDropIn());
        findViewById(R.id.vBtnDropOut).setOnClickListener(view ->mDropItemsView.animDropOut());
        findViewById(R.id.vPauseSwitch).setOnClickListener(view -> {
            if (((Switch) view).isChecked()) {
                mDropItemsView.onResume();
            } else {
                mDropItemsView.onPause();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDropItemsView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDropItemsView.onPause();
    }
}
