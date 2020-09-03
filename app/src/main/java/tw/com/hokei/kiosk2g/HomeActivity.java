package tw.com.hokei.kiosk2g;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import org.linphone.LinphonePreferences;
import org.linphone.core.LinphoneCore;

import java.util.Date;
import java.io.File;
import java.util.*;

import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.UserConfig;
import tw.com.hokei.kiosk2g.model.DbArtist;
import tw.com.syscode.smartphone.SmartPhoneManager;

public class HomeActivity extends CommonSensorActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private VideoView videoView;
    private View callMessageView;
    private View maskView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        UserConfig.setLastVisitPage(context, "1");
        UserConfig.setUserOpBeginTime(context, Global.getTodayDateTimeString());
        initViews();
        setIdleHome(true);

        hangupCusromer();

        new Thread() {
            public void run() {
                processSIP(CommonApplication.getContext());
            }
        }.start();

        if (Global.isNetworkAvailable(context)) {
            new Thread() {
                public void run() {
                    WebService.debugHOKEI(context, "Running...");
                }
            }.start();
        }
    }

    @Override
    protected void initViews() {
        super.initViews();

        final String locale = UserConfig.getKioskArtistLocale(context);
        final String filePath = Global.getArtistPathName(context) + File.separator + locale;

        maskView = findViewById(R.id.maskView);
        maskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Global.isNetworkAvailable(context)) {
                    releaseUsbSensor();
                    Global.handlerPostDelay(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                            Global.startActivity(context, new Intent(context, BranchActivity.class));
                        }
                    }, 500);
                    return;
                }
                showView(callMessageView, true);
            }
        });

        callMessageView = findViewById(R.id.callMessageView);
        String callMessageImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "call_message");
        callMessageView.setBackground(Drawable.createFromPath(callMessageImageFile));

        GifImageButtonView callMessageVoiceImageButton = findViewById(R.id.callMessageVoiceImageButton);
        if(callMessageVoiceImageButton!=null) {
            Global.gifImageButtonViewLoadImage(context, callMessageVoiceImageButton, "call_button_voix_normal", "call_button_voix_press");
        }
        callMessageVoiceImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserConfig.setCustomerServiceType(context, "1");
                goNext();
            }
        });

        GifImageButtonView callMessageTextImageButton = findViewById(R.id.callMessageTextImageButton);
        if(callMessageTextImageButton!=null) {
            Global.gifImageButtonViewLoadImage(context, callMessageTextImageButton, "call_button_text_normal", "call_button_text_press");
        }
        callMessageTextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserConfig.setCustomerServiceType(context, "2");
                goNext();

                if (Global.isNetworkAvailable(context)) {
                    new Thread() {
                        public void run() {
                            String locale = UserConfig.getKioskArtistLocale(context);
                            String msg = "Our customer is online and mother language is ";

                            if (DbArtist.DE_DE.equals(locale)) {
                                msg += "Deutsch";
                            } else if (DbArtist.FR_FR.equals(locale)) {
                                msg += "French";
                            } else if (DbArtist.EN_US.equals(locale)) {
                                msg += "English";
                            }else if (DbArtist.JA_JP.equals(locale)) {
                                msg += "Japan";
                            }else if(DbArtist.ZH_TW.equals(locale)){
                                msg += "Chinese";
                            }

                            sendSipTextMessageToCustomerCenter(msg, msg, false);
                        }
                    }.start();
                }
            }
        });

        GifImageButtonView callMessagePassImageButton = findViewById(R.id.callMessagePassImageButton);
        Global.gifImageButtonViewLoadImage(context, callMessagePassImageButton, "call_button_pass_normal", "call_button_pass_press");
        callMessagePassImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserConfig.setCustomerServiceType(context, "0");
                goNext();
            }
        });

        videoView = findViewById(R.id.videoView);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(false);
            }
        });

        MediaController mediaController = new MediaController(this);

        mediaController.setAnchorView(videoView);
        mediaController.setVisibility(View.GONE);

        String videoFileName = UserConfig.getKioskArtistItem(context, locale, "index_video");
        Uri uri = null;
        if (!Global.isEmptyString(videoFileName)) {
            String videoFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "index_video");
            uri = Uri.parse(videoFile);
        }

        videoView.setMediaController(mediaController);
        if (uri != null) {
            videoView.setVideoURI(uri);
        }
        videoView.requestFocus();
        //videoView.start();
    }

    private void nextPage(long startTime) {
        showDummyAlertView();
        //showView(callMessageView, false);
        //showView(connectingToCallCenterImageView, false);
        finish();
        Global.startActivity(context, new Intent(context, BranchActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!videoView.isPlaying()) {
            Global.handlerPostDelay(new Runnable() {
                @Override
                public void run() {
                    videoView.start();
                }
            }, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Runtime.getRuntime().gc();
    }

    private void processSIP(Context context) {
        if (Global.isNetworkAvailable(context)) {
            cancelIdleTimer();
            if (WebService.kioskSIP(context) == 0) {

            }
            startIdleTimer();
        }

        try {
            String userName = UserConfig.getSipAccountUserID(context);
            String password = UserConfig.getSipAccountPassword(context);
            String domain = UserConfig.getSipServerHost(context);
            String proxy = UserConfig.getSipProxy(context);

            if (!Global.isEmptyString(userName) && !Global.isEmptyString(domain)) {
                if (SmartPhoneManager.getLc() != null && SmartPhoneManager.getLc().getDefaultProxyConfig() != null) {
                    LinphoneCore.RegistrationState state = SmartPhoneManager.getLc().getDefaultProxyConfig().getState();
                    int expireTime = SmartPhoneManager.getLc().getDefaultProxyConfig().getExpires();
                    int publishExpireTime = SmartPhoneManager.getLc().getDefaultProxyConfig().getPublishExpires();

                    Log.d("sipDebug0509","SIP Expire time = "+Integer.toString(expireTime));
                    Log.d("sipDebug0509","SIP Publish Expire time = "+Integer.toString(publishExpireTime));

                    if (state != LinphoneCore.RegistrationState.RegistrationOk) {
                        sipAccountReg();
                        //紀錄當前時間
                        Global.SIP_REGISTRY_TIME = System.currentTimeMillis();
                        Log.d("sipDebug0508","sipAcc is not registered, sipAcc registered");
                    }
                } else {
                    Global.SIP_REGISTRY_TIME = System.currentTimeMillis() - (11 * 60 * 1000) ;
                }

                //如離上次註冊時間超過10分鐘（600秒）則再進行註冊
                if (Global.SIP_REGISTRY_TIME != -1) {
                    long systemCurrentTime = System.currentTimeMillis();
                    long diff = systemCurrentTime - Global.SIP_REGISTRY_TIME;
                    Log.d("sipDebug0508","diff = " + diff);
                    if (diff > (10 * 60 * 1000)) {
                        sipAccountReg();
                        Global.SIP_REGISTRY_TIME = System.currentTimeMillis();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sipAccountReg() {
        String userName = UserConfig.getSipAccountUserID(context);
        String password = UserConfig.getSipAccountPassword(context);
        String domain = UserConfig.getSipServerHost(context);
        String proxy = UserConfig.getSipProxy(context);
        int count = LinphonePreferences.instance().getAccountCount();

        if (SmartPhoneManager.isExitingAccount(userName, domain)) {
            for (int i = count - 1 ; i >= 0 ; i--) {
                SmartPhoneManager.deleteAccount(i);
            }
        } else {
            if (count > 0) {
                for (int i = count - 1 ; i >= 0 ; i--) {
                    SmartPhoneManager.deleteAccount(i);
                }
            }
        }

        SmartPhoneManager.saveCreatedAccount(userName, password, domain, proxy);

        new Thread() {
            public void run() {
                String userName = UserConfig.getSipAccountUserID(context);
                String proxy = UserConfig.getSipProxy(context);
                WebService.debug(context, "SIP_REG : " + userName + "," + proxy);
            }
        }.start();
        Log.d("sipDebug0509","SIP Register called");
    }

    private void goNext() {
        String customerType = UserConfig.getCustomerServiceType(context);

        if ("1".equals(customerType)) {
            callCustomer();
        }

        releaseUsbSensor();
        showView(callMessageView, true);
        cancelIdleTimer();
        showWaitAlertView();

        if (Global.isNetworkAvailable(context)) {
            new Thread() {
                public void run() {
                    long startTime = System.currentTimeMillis();
                    int[] status = new int[1];
                    int returnCode = WebService.kioskStatus(context, status);

                    startIdleTimer();
                    //dismissWaitAlertView();

                    if (returnCode == 0 && status[0] == 1) {
                        nextPage(startTime);
                    } else {
                        showView(callMessageView, false);
                    }
                }
            }.start();
        }
    }
}
