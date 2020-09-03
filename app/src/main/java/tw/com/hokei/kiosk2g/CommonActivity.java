package tw.com.hokei.kiosk2g;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.airbnb.lottie.LottieAnimationView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.linphone.LinphoneManager;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCore;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.MainDB;
import tw.com.hokei.kiosk2g.database.UserConfig;
import tw.com.hokei.kiosk2g.model.DbArtist;
import tw.com.hokei.kiosk2g.model.DbTextService;
import tw.com.syscode.smartphone.SmartPhoneManager;
import tw.com.syscode.smartphone.SmartPhoneService;

public class CommonActivity extends AppCompatActivity implements OnKeyboardEventCallback {
    protected Context context;
    protected Activity activity;

    private View waitAnimationView;
    private LottieAnimationView waitLottieAnimationView;
    protected CustomKeyboard customKeyboard;

    private long startTime = 60 * 1000; // 15 MINS IDLE TIME
    private final long interval = 1 * 1000;

    private IdleCountDownTimer countDownTimer = null;

    private TextView idleCounterTextView;
    private ImageView sipStatusImageView;

    public final static String SIP_MESSAGE_RECEIVED_KEY = "KIOSK2G.SIP_MESSAGE_RECEIVED";

    private View textServiceView;
    private ListView textServiceListView;
    private TextServiceAdapter textServiceAdapter;
    private BadgeView  textServiceBadgeView;
    private View  textServiceInputView;

    private View exitView;
    private View idleView;
    private View sensorIdleView;

    protected GifImageView customerServiceImageView;
    protected GifImageView textServiceImageView;
    public boolean textServiceShow = false;
    public boolean countdownTimerChk = true;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();

            if (SIP_MESSAGE_RECEIVED_KEY.equals(action)) {
                final String textMessage = intent.getStringExtra("MESSAGE");

                new Thread() {
                    public void run () {
                        String finalMessage = textMessage;
                        String[] transMessage = new String[1];
                        String locale = UserConfig.getKioskArtistLocale(context);
                        if (!DbArtist.EN_US.equals(locale)) {
                            String lanCode = "";

                            if (DbArtist.FR_FR.equals(locale)) {
                                lanCode = "fr";
                            } else if (DbArtist.DE_DE.equals(locale)) {
                                lanCode = "de";
                            }else if (DbArtist.JA_JP.equals(locale)) {
                                lanCode = "ja";
                            }else if (DbArtist.ZH_TW.equals(locale)){
                                lanCode = "zh-TW";
                            }

                            if (WebService.googleTranslate(context, textMessage, transMessage, lanCode) == 0) {
                                finalMessage = transMessage[0];
                            }
                        }

                        finalMessage = Html.fromHtml(finalMessage).toString();

                        DbTextService db = new DbTextService();
                        db.content = finalMessage;
                        db.type = 1;
                        db.readed = 0;

                        MainDB.newItemWithTextService(context, db);
                        updateChatRoom();
                    }
                }.start();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //startService(new Intent(Intent.ACTION_MAIN).setClass(this, SmartPhoneService.class));

        context = this;
        activity = this;

        IntentFilter filter = new IntentFilter();
        filter.addAction(SIP_MESSAGE_RECEIVED_KEY);
        Global.registerReceiver(context, mReceiver, filter);

        screenSetting();
        Global.requestPermissions(this);

        startCountDownTimer();

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    protected int mainKeyboardLayoutID = 0;

    protected void initViews() {
        final String customerServiceType = UserConfig.getCustomerServiceType(context);
        final String locale = UserConfig.getKioskArtistLocale(context);
        final String filePath = Global.getArtistPathName(context) + File.separator + locale;
        final String filePath2 = Global.getArtistPathName(context);


        final KeyboardView keyboardView = findViewById(R.id.keyboardView);
        if (keyboardView != null) {
            keyboardView.setVisibility(View.GONE);
            customKeyboard = new CustomKeyboard(this, keyboardView, this);
            if (customKeyboard != null) {
                if (locale.equals(DbArtist.EN_US)) {
                    customKeyboard.setLayout(R.xml.eng_keyboard);
                    customKeyboard.setCustomKeyboardChk(true);
                } else if (locale.equals(DbArtist.FR_FR)) {
                    customKeyboard.setLayout(R.xml.frenc_keyboard);
                    customKeyboard.setCustomKeyboardChk(true);
                } else if (locale.equals(DbArtist.DE_DE)) {
                    customKeyboard.setLayout(R.xml.germ_keyboard);
                    customKeyboard.setCustomKeyboardChk(true);
                }else if (locale.equals(DbArtist.JA_JP)) {
                    customKeyboard.setCustomKeyboardChk(false);
                }else if (locale.equals(DbArtist.ZH_TW)){
                    customKeyboard.setCustomKeyboardChk(false);
                }

                mainKeyboardLayoutID = customKeyboard.getLayout();
            }
        }

        exitView = findViewById(R.id.exitView);
        if (exitView != null) {
            String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "exit_background");
            exitView.setBackground(Drawable.createFromPath(backgroundImageFile));
        }

        sensorIdleView = findViewById(R.id.sensorIdleView);

        idleView = findViewById(R.id.idleView);
        if (idleView != null) {
            String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "ldle_background");
            idleView.setBackground(Drawable.createFromPath(backgroundImageFile));
        }

        waitAnimationView = findViewById(R.id.waitAnimationView);
        waitLottieAnimationView = findViewById(R.id.waitLottieAnimationView);

        View mainPlateView = findViewById(R.id.mainPlateView);

        if (mainPlateView != null) {
            mainPlateView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideCustomKeyboard();
                }
            });
        }

        GifImageButtonView exitImageButton = findViewById(R.id.exitImageButton);
//        ImageButton exitImageButton = findViewById(R.id.exitImageButton);
        if (exitImageButton != null) {
            Global.gifImageButtonViewLoadImage2(context, exitImageButton, "exit_button_normal", "exit_button_press");
            exitImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showExitView();
                }
            });
        }

        GifImageButtonView exitYesImageButton = findViewById(R.id.exitYesImageButton);
        if (exitYesImageButton != null) {
            Global.gifImageButtonViewLoadImage(context, exitYesImageButton, "exit_button_yes_normal", "exit_button_yes_press");
            exitYesImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDummyAlertView();

                    Global.handlerPostDelay(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                            Global.startMainActivity(context);
                            //dismissDummyAlertView();
                        }
                    }, 500);
                }
            });
        }

        GifImageButtonView exitNoImageButton = findViewById(R.id.exitNoImageButton);
        if (exitNoImageButton != null) {
            Global.gifImageButtonViewLoadImage(context, exitNoImageButton, "exit_button_no_normal", "exit_button_no_press");
            exitNoImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideExitView();
                }
            });
        }

        idleCounterTextView = findViewById(R.id.idleCounterTextView);
        if (idleCounterTextView != null) {
            idleCounterTextView.setText("");
        }

        //==sipStatusImageView = findViewById(R.id.sipStatusImageView);
        //SmartPhoneManager.getInstance().

        try {
            updateSipStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        GifImageButtonView idleContinueImageButton = findViewById(R.id.idleContinueImageButton);
        if (idleContinueImageButton != null) {
            Global.gifImageButtonViewLoadImage(context, idleContinueImageButton, "ldle_button_continue_normal", "ldle_button_continue_press");
            idleContinueImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View idleView = findViewById(R.id.idleView);
                    if (idleView != null) {
                        cancelOverIdleTimer();
                        hideIdleView();
                        startCountDownTimer();
                    }

                    hideSensorIdleView();
                    hideExitView();
                }
            });
        }

        GifImageButtonView idleExitImageButton = findViewById(R.id.idleExitImageButton);
        if (idleExitImageButton != null) {
            Global.gifImageButtonViewLoadImage(context, idleExitImageButton, "ldle_button_exit_normal", "ldle_button_exit_press");
            idleExitImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDummyAlertView();

                    Global.handlerPostDelay(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                            Global.startMainActivity(context);
                            //dismissDummyAlertView();
                        }
                    }, 500);
                }
            });
        }


        textServiceView = findViewById(R.id.textServiceView);


        if (textServiceView != null) {
            if (!"2".equals(customerServiceType)) {
                hideTextServiceView();
            }

            if (customKeyboard != null) {
                EditText textServiceEditText = findViewById(R.id.textServiceEditText);
                customKeyboard.registerEditText(R.id.textServiceEditText);
            }

            textServiceAdapter = new TextServiceAdapter(context, MainDB.getTextServiceList_Curosr(context));



            textServiceListView = findViewById(R.id.textServiceListView);
            textServiceListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    textServiceListView.setAdapter(textServiceAdapter);
                    textServiceListView.setSelection(textServiceAdapter.getCount() - 1);
                }
            });
            textServiceInputView = findViewById(R.id.textServiceInputView);
            String textServiceInputFile = filePath2 + File.separator + UserConfig.getKioskArtist(context, "service_chat_text");
            if(textServiceInputView!=null) {
                textServiceInputView.setBackground(Drawable.createFromPath(textServiceInputFile));
            }


            ImageButton textServiceCloseImageButton = findViewById(R.id.textServiceCloseImageButton);

            textServiceCloseImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (textServiceView != null) {
                        /*
                        textServiceView.animate()
                                .translationY(0)
                                .alpha(0.0f)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        textServiceView.setVisibility(View.GONE);
                                    }
                                });
                                */
                        hideTextService();
                    }
                }
            });

            customerServiceImageView = findViewById(R.id.customerServiceImageView);
            Global.gifImageViewLoadImage2(context,customerServiceImageView,"service_call_left");
//            String customerServiceImageFile = filePath2 + File.separator + UserConfig.getKioskArtist(context, "service_call_left");
//            customerServiceImageView.setBackground(Drawable.createFromPath(customerServiceImageFile));
            textServiceImageView = findViewById(R.id.textServiceImageView);
            Global.gifImageViewLoadImage2(context,textServiceImageView,"service_chat_right");
//            String textServiceImageFile = filePath2 + File.separator + UserConfig.getKioskArtist(context, "service_chat_right");
//            textServiceImageView.setBackground(Drawable.createFromPath(textServiceImageFile));
            if (textServiceImageView != null) {
                if (!"2".equals(customerServiceType)) {
                    textServiceImageView.setVisibility(View.GONE);
                }

                textServiceImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (textServiceView != null) {
                            if(!customKeyboard.customKeyboardChk()){
                                ViewGroup.LayoutParams params = textServiceView.getLayoutParams();
                                params.height = 525 ;
                                textServiceView.setLayoutParams(params);
                                ImageView customerCenterImageView = findViewById(R.id.text_service_customer_center);
                                String customerCenterImageFile = filePath2 + File.separator + UserConfig.getKioskArtist(context, "service_chat_callcenter");
                                if(customerCenterImageView!=null){
                                    customerCenterImageView.setBackground(Drawable.createFromPath(customerCenterImageFile));
                                }

                                ImageView customerImageView = findViewById(R.id.text_service_customer);
                                String customerImageFile = filePath2 + File.separator + UserConfig.getKioskArtist(context, "service_chat_guest");
                                if(customerImageView!=null){
                                    customerImageView.setBackground(Drawable.createFromPath(customerImageFile));
                                }

                            }
                            MainDB.updateTextServiceAllReaded(context);
                            setTextServiceBadgeCount(0);
                            textServiceView.setVisibility(View.VISIBLE);
                            textServiceShow=true;
                            screenSetting();
                            CustomEditText textServiceEditText =  findViewById(R.id.textServiceEditText);
                            if (textServiceEditText != null) {
                                textServiceEditText.requestFocus();
                                if(!customKeyboard.customKeyboardChk()) {
                                    textServiceEditText.setKeyImeChangeListener(new CustomEditText.KeyImeChange() {
                                        @Override
                                        public void onKeyIme(int keyCode, KeyEvent event) {
                                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                Log.d("backButton", "pressed1");
                                                hideTextService();
                                            }
                                        }
                                    });
                                }else{
                                    customKeyboard.setOnKeyPressListener(new CustomKeyboard.OnKeyPressListener() {
                                        @Override
                                        public void onKeyPress(int primaryCode, int[] ints) {
                                            int KEYCODE_DONE=-4;
                                            if(primaryCode == KEYCODE_DONE){
                                                Log.d("DoneButton", "pressed");
                                                hideTextService();
                                            }
                                        }
                                    });
                                }
                            }

                        /*
                        textServiceView.setAlpha(0.0f);
                        textServiceView.animate()
                                .translationY(textServiceView.getHeight())
                                .alpha(1.0f)
                                .setListener(null);
                                */
                        }
                    }
                });

                if ("2".equals(customerServiceType)) {
                    textServiceBadgeView = new BadgeView(this, textServiceImageView);
                    textServiceBadgeView.setTextSize(15);
                    textServiceBadgeView.setTextColor(Color.WHITE);
                    textServiceBadgeView.setBadgeBackgroundColor(Color.RED);
                    textServiceBadgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                }
            }
//            View textServiceInputView = findViewById(R.id.textServiceInputView);
//            if(textServiceInputView!=null){
//                String textServiceInputImageFile = filePath + File.separator + UserConfig.getKioskArtist(context, "service_chat_text");
//                textServiceInputView.setBackground(Drawable.createFromPath(textServiceInputImageFile));
//            }
            GifImageButtonView textServiceSendImageButton = findViewById(R.id.textServiceSendImageButton);
            if (textServiceSendImageButton != null) {
                Global.gifImageButtonViewLoadImage2(context, textServiceSendImageButton, "service_chat_sent_normal", "service_chat_sent_press");
            }
            textServiceSendImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                  EditText textServiceEditText = findViewById(R.id.textServiceEditText);

                    if (!textServiceEditText.getText().toString().matches("")) {
                        if (Global.isNetworkAvailable(context)) {
                            String message = textServiceEditText.getText().toString();
                            final String message2 = Global.trimString(message);

                            showWaitAlertView();
                            new Thread() {
                                public void run() {
                                    String[] translatedText = new String[1];

                                    if (!"en_rUS".equals(UserConfig.getKioskArtistLocale(context))) {
                                        //if (WebService.googleTranslateToEn(context, message2, translatedText) != 0) {
                                        if (WebService.googleTranslate(context, message2, translatedText, "en") != 0) {
                                            translatedText[0] = message2;
                                        }
                                    } else {
                                        translatedText[0] = message2;
                                    }

                                    if (sendSipTextMessageToCustomerCenter(message2, translatedText[0], true) == 1) {
                                        dismissWaitAlertView();
                                        Global.handlerPost(new Runnable() {
                                            @Override
                                            public void run() {
                                                EditText textServiceEditText = findViewById(R.id.textServiceEditText);
                                                textServiceEditText.setText("");
                                                textServiceEditText.requestFocus();
                                            }
                                        });
                                    }
                                }
                            }.start();
                        }
                    }
                }
            });
        }
    }
    public void hideTextService(){
        MainDB.updateTextServiceAllReaded(context);
        setTextServiceBadgeCount(0);
        textServiceView.setVisibility(View.GONE);
        textServiceShow=false;
        hideCustomKeyboard();
    }
    public void immersiveMode() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (customerServiceImageView != null) {
            final String customerServiceType = UserConfig.getCustomerServiceType(context);
            if ("1".equals(customerServiceType)) {
                customerServiceImageView.setVisibility(Global.isNetworkAvailable(context) ? View.VISIBLE : View.GONE);
            } else {
                customerServiceImageView.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Global.PERMS_REQUEST_CODE) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                // Permission Denied
                //Toast.makeText(this, getString(R.string.please_grant_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void refreshView() {

    }

    private boolean keyboardShowing = false;

    public boolean isKeyboardShowing() {
        return keyboardShowing;
    }

    @Override
    protected void onStart() {
        super.onStart();

        KeyboardVisibilityEvent.setEventListener(
                this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        keyboardShowing = isOpen;

                        if (!isOpen) {
                            Global.handlerPostDelay(new Runnable() {
                                @Override
                                public void run() {
                                    //getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
                                    refreshView();
                                }
                            }, 1000);
                        }
                    }
                });
        screenSetting();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        screenSetting();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        screenSetting();
                    }
                });
        if (hasFocus) {
            screenSetting();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        screenSetting();
    }

    private long userInteractionTime = 0;

    @Override
    public void onUserInteraction(){
        super.onUserInteraction();

        if (needIdleHome) {
            long nowTime = System.currentTimeMillis();

            if ((nowTime - userInteractionTime) > 500) {
                cancelCountDownTimer();
                startCountDownTimer();
            }
        }
    }
    public void disableCountdownTimer(){
        countdownTimerChk=false;
    }
    protected void startCountDownTimer() {
        if(countdownTimerChk) {
            if (countDownTimer == null) {
                countDownTimer = new IdleCountDownTimer(startTime, interval);
                countDownTimer.start();
                userInteractionTime = System.currentTimeMillis();
            }
        }
    }

    protected void cancelCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public void startIdleTimer() {
        Global.handlerPost(new Runnable() {
            @Override
            public void run() {
                startCountDownTimer();
            }
        });
    }

    public void cancelIdleTimer() {
        Global.handlerPost(new Runnable() {
            @Override
            public void run() {
                cancelCountDownTimer();
            }
        });
    }

    private Timer timerOverIdleTime;
    private int idle2Counter = 20;

    private void updateIdleCounterTextView() {
        if (idleCounterTextView != null) {
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    idleCounterTextView.setText(String.format("%d", idle2Counter));
                }
            });
        }
    }

    private void startOverIdleTimer() {
        if (timerOverIdleTime != null) return;

        idle2Counter = 20;
        updateIdleCounterTextView();
        timerOverIdleTime = new Timer();
        timerOverIdleTime.schedule(new TimerTask() {
            @Override
            public void run() {
                idle2Counter--;

                if (idle2Counter > 0) {
                    updateIdleCounterTextView();
                } else {
                    //releaseUsbSensor();
                    finish();
                    Global.startMainActivity(context);
                }
            }
        }, 1000, 1000);
    }

    private void cancelOverIdleTimer() {
        if (timerOverIdleTime != null) {
            timerOverIdleTime.cancel();
            timerOverIdleTime.purge();
            timerOverIdleTime = null;
            idle2Counter = 20;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Global.unregisterReceiver(context, mReceiver);
        cancelCountDownTimer();
        cancelOverIdleTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void screenSetting() {
        if (Build.VERSION.SDK_INT < 19) return;

        final View decorView = getWindow().getDecorView();

        if (decorView != null) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private Dialog alertDialog;

    protected void showWaitAlertView() {
        showWaitAlertView("");
    }

    protected void showWaitAlertView(int message) {
        showWaitAlertView(getString(message));
    }

    protected void showWaitAlertView(final String message) {
        showWaitAnimationView();
    }

    protected void dismissWaitAlertView() {
        dismissWaitAnimationView();
    }

    public void showView(final View view, final boolean show) {

        if (view != null) {
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    try {
                            Global.hideKeyboard2(activity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    view.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    private boolean needIdleHome = false;

    public void setIdleHome(boolean value) {
        needIdleHome = value;
    }

    public class IdleCountDownTimer extends CountDownTimer {
        public IdleCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            if (needIdleHome) {
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        closeAllDialog();
                        cancelCountDownTimer();
                        showIdleView();
                        startOverIdleTimer();
                        //finish();
                        //Global.startActivity(context, new Intent(context, MainActivity.class));
                    }
                });
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }

    //mostTopView
    private View mostTopView = null;

    protected void showDummyAlertView() {
        if (mostTopView == null) {
            mostTopView = findViewById(R.id.mostTopView);
            /*
            if (mostTopView != null) {
                mostTopView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
            */
        }

        showView(mostTopView, true);
    }
    protected void showDummyAlertView22() {
        if (mostTopView == null) {
            mostTopView = findViewById(R.id.mostTopView);
            /*
            if (mostTopView != null) {
                mostTopView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
            */
        }

        showView2(mostTopView, true);
    }

    protected void dismissDummyAlertView(int delayTime) {
        if (mostTopView != null) {
            Global.handlerPostDelay(new Runnable() {
                @Override
                public void run() {
                    mostTopView.setVisibility(View.GONE);
                }
            }, delayTime);
        }
    }

    protected void dismissDummyAlertView() {
        showView(mostTopView, false);
    }
    protected void dismissDummyAlertView22() {
        showView2(mostTopView, false);
    }
    private Dialog alertDummyDialog;

    protected void showDummyAlertView2() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (alertDummyDialog == null) {
                    //alertDummyDialog = new Dialog(activity,android.R.style.Theme_Translucent_NoTitleBar);
                    alertDummyDialog = new Dialog(activity);
                    alertDummyDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    alertDummyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    alertDummyDialog.setContentView(R.layout.dialog_dummy_alert);
                    alertDummyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDummyDialog.setCancelable(false);
                    alertDummyDialog.setCanceledOnTouchOutside(false);
                    View waitView = (View)alertDummyDialog.findViewById(R.id.dummyView);
                    waitView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //dismissDummyAlertView();
                        }
                    });
                }

                try {
                    if (alertDummyDialog.isShowing()) alertDummyDialog.dismiss();
                    alertDummyDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void dismissDummyAlertView2() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (alertDummyDialog != null && alertDummyDialog.isShowing()) {
                    try {
                        alertDummyDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    protected void dismissDummyAlertView2(int delayTime) {
        Global.handlerPostDelay(new Runnable() {
            @Override
            public void run() {
                if (alertDummyDialog != null && alertDummyDialog.isShowing()) {
                    try {
                        alertDummyDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, delayTime);
    }

    protected void showWaitAnimationView() {
        if (waitAnimationView != null) {
            showView2(waitAnimationView, true);
            if (waitLottieAnimationView != null) {
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        //waitLottieAnimationView.cancelAnimation();
                        waitLottieAnimationView.animate();
                    }
                });
            }
        }
    }

    protected void dismissWaitAnimationView() {
        if (waitAnimationView != null) {
            showView2(waitAnimationView, false);
            if (waitLottieAnimationView != null) {
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        waitLottieAnimationView.cancelAnimation();
                    }
                });
            }
        }
    }
    public void showView2(final View view, final boolean show){
        if (view != null) {
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Global.hideKeyboard2(activity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    view.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }
    public void closeAllDialog() {

    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    private final static boolean SIP_FUN = true;


    protected void callCustomer() {
        if (!SIP_FUN) return;
        if (!SmartPhoneService.isReady()) return;
        if (SmartPhoneManager.getLc() == null) return;

        new Thread() {
            public void run() {
                SmartPhoneManager.invite(UserConfig.getSipCallNo(context), UserConfig.getSipAccountUserID(context));
                //SmartPhoneManager.invite("305", "506");
            }
        }.start();
    }

    protected void hangupCusromer() {
        if (!Global.isNetworkAvailable(context)) return;
        if (!SIP_FUN) return;
        if (!SmartPhoneService.isReady()) return;
        if (SmartPhoneManager.getLc() == null) return;

        new Thread() {
            public void run() {
                SmartPhoneManager.declineCurrentCall();
            }
        }.start();
    }

    protected void updateSipStatus() {
        if (!SIP_FUN) return;
        if (!SmartPhoneService.isReady()) return;
        if (SmartPhoneManager.getLc() == null) return;
        if (SmartPhoneManager.getLc().getDefaultProxyConfig() == null) return;


        final LinphoneCore.RegistrationState state = SmartPhoneManager.getLc().getDefaultProxyConfig().getState();

        if (state == LinphoneCore.RegistrationState.RegistrationOk) {
            LinphoneCallLog[] callLog = SmartPhoneManager.getCallLog();
            if (callLog != null && callLog.length > 0) {
                //sipStatusImageView.setImageResource(R.drawable.sip_calling);
            } else {
                //sipStatusImageView.setImageResource(R.drawable.sip_reg_ok);
            }
        } else if (state == LinphoneCore.RegistrationState.RegistrationFailed) {
            //sipStatusImageView.setImageResource(R.drawable.sip_reg_failed);
        }
        //sipStatusTextView.setText(SmartPhoneManager.getLc().getDefaultProxyConfig().getState().toString());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void registerCustomKeyboard(int edit_id) {
        if (customKeyboard != null) {
            customKeyboard.registerEditText(edit_id);
        }
    }

    public int sendSipTextMessageToCustomerCenter(String orgMessage, String transMessage, boolean writeToDb) {
        int result = 0;
        LinphoneManager manager = SmartPhoneManager.getInstance();
        if (manager != null) {
            LinphoneCore lc = manager.getLc();

            try {
                String to = "sip:" + UserConfig.getSipCallNo(context) + "@" + UserConfig.getSipProxy(context);
                //String to = "sip:" + "315" + "@" + UserConfig.getSipProxy(context);
                LinphoneAddress toAddress = lc.interpretUrl(to);
                LinphoneChatRoom cr = lc.getChatRoom(toAddress);
                LinphoneChatMessage msg = cr.createLinphoneChatMessage(transMessage);
                cr.sendChatMessage(msg);

                if (writeToDb) {
                    DbTextService db = new DbTextService();
                    db.content = orgMessage;
                    db.type = 2;
                    db.readed = 1;
                    MainDB.newItemWithTextService(context, db);
                }

                updateChatRoom();
                result = 1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public void updateChatRoom() {
        if (textServiceListView != null) {
            textServiceAdapter = new TextServiceAdapter(context, MainDB.getTextServiceList_Curosr(context));
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    textServiceListView.setAdapter(textServiceAdapter);
                    textServiceListView.setSelection(textServiceAdapter.getCount() - 1);
                    setTextServiceBadgeCount(MainDB.getTextServiceUnreadCount(context));
                }
            });
        }
    }

    public void setTextServiceBadgeCount(int count) {
        if (textServiceBadgeView != null) {
            if (count > 0) {
                textServiceBadgeView.setText(String.valueOf(count));
                textServiceBadgeView.show();
            } else {
                textServiceBadgeView.hide();
            }
        }
    }


    public void onKeyboardShow(int editTxtId) {
        Log.i("", "");
    }

    protected void hideCustomKeyboard() {
        if (customKeyboard != null) {
            customKeyboard.hideCustomKeyboard();
        }
    }

    protected void showCustomKeyboard(View v) {
        if (customKeyboard != null) {
            customKeyboard.showCustomKeyboard();
        }
    }

    protected void showExitView() {
        showView(exitView, true);
    }

    protected void hideExitView() {
        showView(exitView, false);
    }

    protected void showIdleView() {
        showView(idleView, true);
    }

    protected void hideIdleView() {
        showView(idleView, false);
    }

    protected void showSensorIdleView() {
        showView(sensorIdleView, true);
    }


    protected void hideSensorIdleView() {
        showView(sensorIdleView, false);
    }

    protected void showTextServiceView() {
        showView(textServiceView, true);
    }

    protected void hideTextServiceView() {
        showView(textServiceView, false);
        textServiceShow=false;
    }
    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}
