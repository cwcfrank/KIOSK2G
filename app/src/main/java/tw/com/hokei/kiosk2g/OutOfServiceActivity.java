package tw.com.hokei.kiosk2g;

import android.os.Bundle;
import android.widget.ImageView;

import pl.droidsonroids.gif.GifImageView;

public class OutOfServiceActivity extends CommonSensorActivity {
    private static final String TAG = OutOfServiceActivity.class.getSimpleName();

    private GifImageView customerServiceImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_of_service);

        initViews();
        setIdleHome(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Runtime.getRuntime().gc();
    }

    @Override
    protected void initViews() {
        super.initViews();

        ImageView backgroundImageView = findViewById(R.id.backgroundImageView);
        if (backgroundImageView != null) {
            try {
                backgroundImageView.setImageResource(R.drawable.out_of_service);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
