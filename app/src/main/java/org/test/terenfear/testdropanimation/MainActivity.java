package org.test.terenfear.testdropanimation;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Switch;

import org.terenfear.dropanimation.DropItemsSupportDialogFragment;
import org.terenfear.dropanimation.DropItemsView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private DropItemsView mDropItemsView;
    private static final int[] RESOURCES_ID_ARRAY = new int[]{
            R.drawable.ic_drop_1,
            R.drawable.ic_drop_2,
            R.drawable.ic_drop_3,
            R.drawable.ic_drop_4,
            R.drawable.ic_drop_5,
            R.drawable.ic_drop_6,
            R.drawable.ic_drop_7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDropItemsView = findViewById(R.id.vDropItemSurface);
        mDropItemsView
                .setDuration(5000)
                .setObjectScale(1.9f)
                .setRowLength(20)
                .setResourceIds(RESOURCES_ID_ARRAY)
                .setStartListener(() -> Log.d(TAG, "anim start"))
                .setEndListener(() -> Log.d(TAG, "anim end"));

        findViewById(R.id.vBtnDropIn).setOnClickListener(view -> mDropItemsView.startDropIn());
        findViewById(R.id.vBtnDropOut).setOnClickListener(view ->mDropItemsView.startDropOut());
        findViewById(R.id.vBtnDialog).setOnClickListener(view ->startDialog());
        findViewById(R.id.vPauseSwitch).setOnClickListener(view -> {
            if (((Switch) view).isChecked()) {
                mDropItemsView.onResume();
            } else {
                mDropItemsView.onPause();
            }
        });
    }

    private void startDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putIntArray( DropItemsSupportDialogFragment.KEY_IMAGE_RESOURCES_ARRAY, RESOURCES_ID_ARRAY );
        DialogFragment dialogFragment = DropItemsSupportDialogFragment.newInstance(bundle);
        dialogFragment.show( ft, "DROPITEMS" );
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
