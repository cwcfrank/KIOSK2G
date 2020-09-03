package tw.com.hokei.kiosk2g;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.UserConfig;

public class OrderCompletedActivity extends CommonSensorActivity {
    private static final String TAG = OrderCompletedActivity.class.getSimpleName();

    private GifImageView leftImageView;
    private GifImageButtonView finishImageButton;
    private GifImageButtonView cancelImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_completed);

        UserConfig.setLastVisitPage(context, "8");
        initViews();
        setIdleHome(true);
/*
        Global.handlerPostDelay(new Runnable() {
            @Override
            public void run() {
                showView(idleImageView, true);

                Global.handlerPostDelay(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity();
                    }
                }, 10000);
            }
        }, 5000);
*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Runtime.getRuntime().gc();
    }

    @Override
    protected void initViews() {
        super.initViews();

        View mainView = findViewById(R.id.mainView);

        final String locale = UserConfig.getKioskArtistLocale(context);
        final String filePath = Global.getArtistPathName(context) + File.separator + locale;

        String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "finish_background");
        mainView.setBackground(Drawable.createFromPath(backgroundImageFile));

        leftImageView = findViewById(R.id.leftImageView);
        Global.gifImageViewLoadImage(context, leftImageView, "finish_left_gif");

        finishImageButton = findViewById(R.id.finishImageButton);
        Global.gifImageButtonViewLoadImage(context, finishImageButton, "finish_button_finish_normal", "finish_button_finish_press");
        finishImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity();
            }
        });

        cancelImageButton = findViewById(R.id.cancelImageButton);
        Global.gifImageButtonViewLoadImage(context, cancelImageButton, "finish_button_cancel_normal", "finish_button_cancel_press");
        cancelImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View exitView = findViewById(R.id.exitView);
                if (exitView != null) {
                    exitView.setVisibility(View.VISIBLE);
                }
            }
        });

        GifImageButtonView exitYesImageButton = findViewById(R.id.exitYesImageButton);
        if (exitYesImageButton != null) {
            exitYesImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDummyAlertView();
                    if (Global.isNetworkAvailable(context)) {
                        new Thread() {
                            public void run() {
                                WebService.kioskSampleOrderUpdate(context, UserConfig.getSampleOrderCode(context));
                                UserConfig.setSampleOrderCode(context, "");

                                Global.handlerPost(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissDummyAlertView();
                                        finish();
                                        Global.startMainActivity(context);
                                    }
                                });
                            }
                        }.start();
                    } else {
                        Global.handlerPost(new Runnable() {
                            @Override
                            public void run() {
                                dismissDummyAlertView();
                                finish();
                                Global.startMainActivity(context);
                            }
                        });
                    }
                }
            });
        }

        //customerServiceImageView = findViewById(R.id.customerServiceImageView);
        if (customerServiceImageView != null) {
            customerServiceImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainActivity();
                }
            });
        }

        //idleImageView = findViewById(R.id.idleImageView);
    }

    private void mainActivity() {
        showDummyAlertView();

        Global.handlerPostDelay(new Runnable() {
            @Override
            public void run() {
                releaseUsbSensor();
                finish();
                Global.startMainActivity(context);
                //dismissDummyAlertView();
            }
        }, 500);
    }
}
