package tw.com.hokei.kiosk2g;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.MainDB;
import tw.com.hokei.kiosk2g.database.UserConfig;
import tw.com.hokei.kiosk2g.model.DbItemColor;
import tw.com.hokei.kiosk2g.model.DbItemType;
import tw.com.hokei.kiosk2g.model.DbTempMemberOrder;

public class SampleOrderActivity extends CommonSensorActivity implements SampleOrderColorAdapter.SampleOrderColorAdapterDelegate{
    private static final String TAG = SampleOrderActivity.class.getSimpleName();

    private RecyclerView colorRecyclerView;
    private SampleOrderColorAdapter colorAdapter;

    private Spinner ringDiameterSpinner;
    private GifImageButtonView confirmImageButton;
    private GifImageView customerServiceImageView;

    private TextView colorRequiredTextView;
    private TextView ringDiameterRequiredTextView;

    private ImageButton colorLeftImageButton;
    private ImageButton colorRightImageButton;

    private List<DbItemColor> itemColorArray;

    private int ringDiameter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_order);

        itemColorArray = MainDB.getItemColorList(context);
        super.cancelCountDownTimer();
        super.disableCountDownTimer();
        UserConfig.setLastVisitPage(context, "7");
        initViews();
        setIdleHome(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Runtime.getRuntime().gc();
    }

    private boolean processing = false;

    @SuppressLint("NewApi")
    @Override
    protected void initViews() {
        super.initViews();

        View mainView = findViewById(R.id.mainView);

        final String locale = UserConfig.getKioskArtistLocale(context);
        final String filePath = Global.getArtistPathName(context) + File.separator + locale;

        String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "sample_background");
        mainView.setBackground(Drawable.createFromPath(backgroundImageFile));

        GifImageView videoImageView = findViewById(R.id.videoImageView);
        Global.gifImageViewLoadImage(context, videoImageView, "sample_left_gif");

        colorLeftImageButton = findViewById(R.id.colorLeftImageButton);
        colorLeftImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //typeRecyclerView.scrollTo(typeRecyclerView.getScrollX() + 200, typeRecyclerView.getScrollY());
            }
        });

        colorRightImageButton = findViewById(R.id.colorRightImageButton);
        colorRightImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //typeRecyclerView.scrollTo(typeRecyclerView.getScrollX() + 200, typeRecyclerView.getScrollY());
            }
        });

        colorRecyclerView = findViewById(R.id.colorRecyclerView);
        colorAdapter = new SampleOrderColorAdapter(itemColorArray);
        colorAdapter.selectedIndex = UserConfig.getOrderItemColor(context);
        colorAdapter.delegate = this;
        colorRecyclerView.setAdapter(colorAdapter);
        colorRecyclerView.setHasFixedSize(true);
        colorRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                updateColorArrows();
            }
        });

        colorRequiredTextView = findViewById(R.id.colorRequiredTextView);
        ringDiameterRequiredTextView = findViewById(R.id.ringDiameterRequiredTextView);

        colorRequiredTextView.setVisibility(View.GONE);
        ringDiameterRequiredTextView.setVisibility(View.GONE);

        ringDiameterSpinner = findViewById(R.id.ringDiameterSpinner);
        ArrayAdapter<CharSequence> ringDiameterAdapter = ArrayAdapter.createFromResource(this,
                R.array.ringdiameter_select_item,
                R.layout.ringdiameter_dropdown_item);
        ringDiameterSpinner.setAdapter(ringDiameterAdapter);
        ringDiameterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ringDiameter = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ImageView ringDiameterRightImageView = findViewById(R.id.ringDiameterRightImageView);
        ringDiameterRightImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        ringDiameterSpinner.performClick();
                    }
                });
            }
        });

        confirmImageButton = findViewById(R.id.confirmImageButton);
        Global.gifImageButtonViewLoadImage(context, confirmImageButton, "sample_button_next_normal", "sample_button_next_press");
        confirmImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWaitAlertView();
                confirmImageButton.setClickable(false);
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        new Thread() {
                            public void run() {
                                sendOrder();
                                dismissWaitAlertView();
                                confirmImageButton.setClickable(true);
                            }
                        }.start();
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

        updateColorArrows();
    }

    private void updateColorArrows() {
        /*
        if (colorRecyclerView.canScrollHorizontally(-1)) {
            colorLeftImageButton.setVisibility(View.VISIBLE);
        } else {
            colorLeftImageButton.setVisibility(View.GONE);
        }

        if (colorRecyclerView.canScrollHorizontally(1)) {
            colorRightImageButton.setVisibility(View.VISIBLE);
        } else {
            colorRightImageButton.setVisibility(View.GONE);
        }
        */
    }

    @Override
    public void onColorItemClick(int position) {
        UserConfig.setOrderItemColor(context, position);
        colorAdapter.selectedIndex = position;
        colorAdapter.notifyDataSetChanged();
    }

    private void sendOrder() {
        String  memberID = UserConfig.getRegisterMemberCode(context);//"M-171129000014";

        //showView(colorRequiredTextView, true);

        if (ringDiameter < 1) {
            showView(ringDiameterRequiredTextView, true);
            return;
        }

        showView(ringDiameterRequiredTextView, false);

        List<DbItemColor> itemColorArray = MainDB.getItemColorList(context);
        List<DbItemType> itemTypeArray = MainDB.getItemTypeList(context);

        long unixTime = Global.unitTime();

        String leftImageFileName = UserConfig.getLeftImageFile(context);//"kiosk_left" + unixTime + ".jpg";
        String frontImageFileName = UserConfig.getFrontImageFile(context);//"kiosk_front" + unixTime + ".jpg";
        String rightImageFileName = UserConfig.getRightImageFile(context);//"kiosk_right" + unixTime + ".jpg";

        String[] orderCode = new String[2];
        String ringType = String.valueOf(itemTypeArray.get(UserConfig.getOrderItemType(context)).itemID);
        String ringColor = String.valueOf(itemColorArray.get(UserConfig.getOrderItemColor(context)).itemID);
        String leftImage = "images/s_order/" + leftImageFileName;
        String frontImage = "images/s_order/" + frontImageFileName;
        String rightImage = "images/s_order/" + rightImageFileName;

        if (memberID.indexOf("@") != -1 || !Global.isNetworkAvailable(context)) {
            //UserConfig.setSampleOrderCode(context, "");
            makeTempOrder(
                    memberID,
                    ringType,
                    ringColor,
                    String.valueOf(ringDiameter - 1),
                    leftImageFileName,
                    frontImageFileName,
                    rightImageFileName);
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    finish();
                    Global.startActivity(context, new Intent(context, OrderCompletedActivity.class));
                }
            });
            return;
        }

        cancelIdleTimer();

        int result;

        if (Global.isNetworkAvailable(context)) {
            result =
                    WebService.kioskSampleOrder(
                            context,
                            memberID,
                            ringType,
                            ringColor,
                            String.valueOf(ringDiameter - 1),
                            leftImage,
                            frontImage,
                            rightImage,
                            orderCode);
        } else {
            result = -10;
        }

        startIdleTimer();

        if (result == 0) {
            ///////////////////////////////////////////////////////////////////////////////////////
            String capturePath = Global.getAppCapturePathName(context);
            String imagePath;

            imagePath = capturePath + File.separator + leftImageFileName;
            Global.uploadFile(context, imagePath, leftImageFileName);
            Global.sleep(1000);
            Global.deleteFile(imagePath);
            imagePath = capturePath + File.separator + frontImageFileName;
            Global.uploadFile(context, imagePath, frontImageFileName);
            Global.sleep(1000);
            Global.deleteFile(imagePath);
            imagePath = capturePath + File.separator + rightImageFileName;
            Global.uploadFile(context, imagePath, rightImageFileName);
            Global.sleep(1000);
            Global.deleteFile(imagePath);
            ///////////////////////////////////////////////////////////////////////////////////////

            UserConfig.setSampleOrderCode(context, orderCode[0]);
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    finish();
                    Global.startActivity(context, new Intent(context, OrderCompletedActivity.class));
                }
            });
        } else if (result == 1) {
            Global.alert(context, R.string.verification_failed);
        } else if (result == 2) {
            Global.alert(context, R.string.incomplete_parameters_passed_in);
        } else if (result == 3) {
            Global.alert(context, R.string.phone_has_been_registered);
        } else if (result == 4) {
            Global.alert(context, R.string.email_has_been_registered);
        } else {
            //UserConfig.setSampleOrderCode(context, "");
            makeTempOrder(
                    memberID,
                    ringType,
                    ringColor,
                    String.valueOf(ringDiameter - 1),
                    leftImageFileName,
                    frontImageFileName,
                    rightImageFileName);
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    finish();
                    Global.startActivity(context, new Intent(context, OrderCompletedActivity.class));
                }
            });
        }
    }

    private void makeTempOrder(
            String memberID,
            String ringType,
            String ringColor,
            String ringDiameter,
            String leftImageFileName,
            String frontImageFileName,
            String rightImageFileName) {

        DbTempMemberOrder db = new DbTempMemberOrder();

        db.firstName = UserConfig.getMemberOrderFirstName(context);
        db.lastName = UserConfig.getMemberOrderLastName(context);
        db.phoneCountry = UserConfig.getMemberOrderPhoneCounrty(context);
        db.phoneCode = UserConfig.getMemberOrderPhoneCode(context);
        db.phone = UserConfig.getMemberOrderPhone(context);
        db.email = UserConfig.getMemberOrderEMail(context);

        db.ringType = ringType;
        db.ringColor = ringColor;
        db.ringDiameter = ringDiameter;
        db.leftImageFileName = leftImageFileName;
        db.frontImageFileName = frontImageFileName;
        db.rightImageFileName = rightImageFileName;

        MainDB.newItemWithTempMemberOrder(context, db);
    }
}
