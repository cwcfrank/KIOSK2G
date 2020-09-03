package tw.com.hokei.kiosk2g;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.UserConfig;

public class BranchActivity extends CommonSensorActivity {
    private static final String TAG = BranchActivity.class.getSimpleName();

    private View maskView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch);

        UserConfig.setLastVisitPage(context, "2");

        initViews();
        setIdleHome(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Runtime.getRuntime().gc();
    }

    @Override
    protected void initViews() {
        super.initViews();

        View mainView = findViewById(R.id.mainView);

        final String locale = UserConfig.getKioskArtistLocale(context);
        final String filePath = Global.getArtistPathName(context) + File.separator + locale;

        String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "branch_background");
        mainView.setBackground(Drawable.createFromPath(backgroundImageFile));

        GifImageButtonView exitImageButton = findViewById(R.id.exitImageButton);
        Global.gifImageButtonViewLoadImage2(context, exitImageButton, "exit_button_normal", "exit_button_press");


        customerServiceImageView = findViewById(R.id.customerServiceImageView);

        GifImageView branchDemoImageView = findViewById(R.id.branchDemoImageView);
        Global.gifImageViewLoadImage(context, branchDemoImageView, "branch_demo_gif");

        GifImageView branchStartImageView = findViewById(R.id.branchStartImageView);
        Global.gifImageViewLoadImage(context, branchStartImageView, "branch_start_gif");

        GifImageButtonView branchDemoButton = findViewById(R.id.branchDemoButton);
        Global.gifImageButtonViewLoadImage(context, branchDemoButton, "branch_button_demo_normal", "branch_button_demo_press");
        branchDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDummyAlertView();
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        releaseUsbSensor();
                        finish();
                        Global.startActivity(context, new Intent(context, DemoActivity.class));
                    }
                });
            }
        });

        GifImageButtonView branchStartButton = findViewById(R.id.branchStartButton);
        Global.gifImageButtonViewLoadImage(context, branchStartButton, "branch_button_start_normal", "branch_button_start_press");
        branchStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDummyAlertView();
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        releaseUsbSensor();
                        finish();
                        Global.startActivity(context, new Intent(context, TakingPicturesActivity.class));
                    }
                });
            }
        });
    }
}
