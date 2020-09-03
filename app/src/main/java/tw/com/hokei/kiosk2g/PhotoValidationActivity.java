package tw.com.hokei.kiosk2g;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.UserConfig;

public class PhotoValidationActivity extends CommonSensorActivity {
    private static final String TAG = PhotoValidationActivity.class.getSimpleName();

    private ImageView leftImageView;
    private ImageView centerImageView;
    private ImageView rightImageView;

    private GifImageButtonView okImageButton;
    private GifImageButtonView redoImageButton;

    private GifImageView customerServiceImageView;

    private Bitmap bmpLeft = null;
    private Bitmap bmpFront = null;
    private Bitmap bmpRight = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_validation);

        UserConfig.setLastVisitPage(context, "5");
        initViews();
        setIdleHome(true);
    }

    @Override
    protected void initViews() {
        super.initViews();

        View mainView = findViewById(R.id.mainView);

        final String locale = UserConfig.getKioskArtistLocale(context);
        final String filePath = Global.getArtistPathName(context) + File.separator + locale;

        String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "verify_background");
        mainView.setBackground(Drawable.createFromPath(backgroundImageFile));

        leftImageView = findViewById(R.id.leftImageView);
        centerImageView = findViewById(R.id.centerImageView);
        rightImageView = findViewById(R.id.rightImageView);

        String capturePath = Global.getAppCapturePathName(context);
        String imagePath;

        Global.createPath(capturePath);

        imagePath = capturePath + File.separator + UserConfig.getLeftImageFile(context);
        bmpLeft = Global.loadBitmapFromFile(imagePath);
        leftImageView.setImageBitmap(bmpLeft);

        imagePath = capturePath + File.separator + UserConfig.getFrontImageFile(context);
        bmpFront = Global.loadBitmapFromFile(imagePath);
        centerImageView.setImageBitmap(bmpFront);

        imagePath = capturePath + File.separator + UserConfig.getRightImageFile(context);
        bmpRight = Global.loadBitmapFromFile(imagePath);
        rightImageView.setImageBitmap(bmpRight);

        okImageButton = findViewById(R.id.okImageButton);
        Global.gifImageButtonViewLoadImage(context, okImageButton, "verify_button_next_normal", "verify_button_next_press");
        okImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //redoImageButton.setEnabled(false);
                releaseUsbSensor();
                //leftImageView.setImageBitmap(null);
                //centerImageView.setImageBitmap(null);
                //rightImageView.setImageBitmap(null);
                System.gc();
                showDummyAlertView();
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        Global.startActivity(context, new Intent(context, FillUpInformationActivity.class));
                        finish();
                    }
                });
            }
        });

        redoImageButton = findViewById(R.id.redoImageButton);
        Global.gifImageButtonViewLoadImage(context, redoImageButton, "verify_button_redo_normal", "verify_button_redo_press");
        redoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDummyAlertView();
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        releaseUsbSensor();
                        finish();

                        Intent intent = new Intent(context, TakingPicturesActivity.class);
                        intent.putExtra("REDO", "1");
                        Global.startActivity(context, intent);
                        //overridePendingTransition(0, 0);
                    }
                });
            }
        });

        //customerServiceImageView = findViewById(R.id.customerServiceImageView);
        if (customerServiceImageView != null) {
            customerServiceImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bmpLeft != null) {
            bmpLeft.recycle();
            bmpLeft = null;
        }

        if (bmpFront != null) {
            bmpFront.recycle();
            bmpFront = null;
        }

        if (bmpRight != null) {
            bmpRight.recycle();
            bmpRight = null;
        }

        Runtime.getRuntime().gc();
    }
}
