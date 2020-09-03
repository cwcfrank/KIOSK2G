package tw.com.hokei.kiosk2g;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import org.linphone.LinphonePreferences;
import org.linphone.core.LinphoneCore;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tw.com.hokei.kiosk2g.database.MainDB;
import tw.com.hokei.kiosk2g.database.UserConfig;
import tw.com.hokei.kiosk2g.model.DbTempMemberOrder;
import tw.com.hokei.kiosk2g.model.DbTempOrder;
import tw.com.hokei.kiosk2g.model.DbTempRegister;
import tw.com.syscode.smartphone.SmartPhoneManager;
import tw.com.syscode.smartphone.SmartPhoneService;

public class CommonApplication extends Application {
    private static CommonApplication instance;

    public CommonApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    static {
        new WebService.NukeSSLCerts().nuke();
        System.loadLibrary("native-lib");
    }

    public static boolean isOpenAppForTheFirstTime = true;

    public static Intent voipServiceIntent = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Global.currentContext = this;
        Thread.UncaughtExceptionHandler defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

        if (!(defaultUEH instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(defaultUEH));
        }

        ApplicationLifecycleHandler handler = new ApplicationLifecycleHandler();
        registerActivityLifecycleCallbacks(handler);
        registerComponentCallbacks(handler);

        voipServiceIntent = new Intent(Intent.ACTION_MAIN).setClass(this, SmartPhoneService.class);

        if (!SmartPhoneService.isReady()) {
            startService(voipServiceIntent);
        }

        startTempDataTimer();
        startKeepAliveTimer();
        startCheckVoIPTimer();
    }

    private Timer timerTempData = null;

    private void startTempDataTimer() {
        if (timerTempData == null) {
            timerTempData = new Timer();
            timerTempData.schedule(new TimerTask() {
                @Override
                public void run() {
                    //Global.toast(instance, "Check");
                    //MainDB.deleteTempMemberOrder(instance, 1);
                    if (!Global.isNetworkAvailable(instance)) return;

                    List<DbTempMemberOrder> dbMemberOrderList = MainDB.getTempMemberOrderList(instance);
                    String capturePath = Global.getAppCapturePathName(instance);

                    for (DbTempMemberOrder dbMemberOrder : dbMemberOrderList) {
                        if (!Global.isNetworkAvailable(instance)) continue;

                        String leftImage = "images/s_order/" + dbMemberOrder.leftImageFileName;
                        String frontImage = "images/s_order/" + dbMemberOrder.frontImageFileName;
                        String rightImage = "images/s_order/" + dbMemberOrder.rightImageFileName;

                        int result =
                                WebService.kioskOrderOffline(
                                        instance,
                                        dbMemberOrder.firstName,
                                        dbMemberOrder.lastName,
                                        dbMemberOrder.phoneCountry,
                                        dbMemberOrder.phoneCode,
                                        dbMemberOrder.phone,
                                        dbMemberOrder.email,
                                        dbMemberOrder.ringType,
                                        dbMemberOrder.ringColor,
                                        dbMemberOrder.ringDiameter,
                                        leftImage,
                                        frontImage,
                                        rightImage);

                        if (result == 0) {
                            ///////////////////////////////////////////////////////////////////////////////////////
                            String imagePath;

                            imagePath = capturePath + File.separator + dbMemberOrder.leftImageFileName;
                            Global.uploadFile(instance, imagePath, dbMemberOrder.leftImageFileName);
                            Global.sleep(1000);
                            Global.deleteFile(imagePath);
                            imagePath = capturePath + File.separator + dbMemberOrder.frontImageFileName;
                            Global.uploadFile(instance, imagePath, dbMemberOrder.frontImageFileName);
                            Global.sleep(1000);
                            Global.deleteFile(imagePath);
                            imagePath = capturePath + File.separator + dbMemberOrder.rightImageFileName;
                            Global.uploadFile(instance, imagePath, dbMemberOrder.rightImageFileName);
                            Global.sleep(1000);
                            Global.deleteFile(imagePath);
                            ///////////////////////////////////////////////////////////////////////////////////////
                            MainDB.deleteTempMemberOrder(instance, dbMemberOrder._id.longValue());
                        }
                    }
                }
            }, 3000, 30000);
        }
    }

    private void stopTakePictureTimer() {
        if (timerTempData != null) {
            timerTempData.cancel();
            timerTempData.purge();
            timerTempData = null;
        }
    }

    private Timer timerKeepAlive = null;

    private void startKeepAliveTimer() {
        if (timerKeepAlive == null) {
            timerKeepAlive = new Timer();
            timerKeepAlive.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (Global.isNetworkAvailable(instance)) {
                        new Thread() {
                            public void run() {
                                int[] response = new int[1];
                                WebService.kioskAlive(instance, response);
                            }
                        }.start();
                    }
                }
            }, 1000, 60 * 5 * 1000);
        }
    }

    private Timer timerCheckVoIP = null;

    private void startCheckVoIPTimer() {
        if (timerCheckVoIP == null) {
            timerCheckVoIP = new Timer();
            timerCheckVoIP.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String userName = UserConfig.getSipAccountUserID(instance);
                        String domain = UserConfig.getSipServerHost(instance);

                        if (!Global.isEmptyString(userName) && !Global.isEmptyString(domain)) {
                            sipAccountReg();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 60 * 5 * 1000, 60 * 5 * 1000);
        }
    }

    private void sipAccountReg() {
        String userName = UserConfig.getSipAccountUserID(instance);
        String password = UserConfig.getSipAccountPassword(instance);
        String domain = UserConfig.getSipServerHost(instance);

        try {
            SmartPhoneManager.initAccount(domain, userName, password);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
