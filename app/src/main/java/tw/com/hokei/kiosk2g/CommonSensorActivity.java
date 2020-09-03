package tw.com.hokei.kiosk2g;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.Calendar;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.UserConfig;

public class CommonSensorActivity extends CommonActivity {
    protected static UsbService usbService = null;
    protected static UsbHandler mUsbHandler = null;
    protected static ServiceConnection usbConnection = null;

    protected TextView sensorIdleCounterTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUsbSensor();
    }

    protected void initViews() {
        super.initViews();
        final String locale = UserConfig.getKioskArtistLocale(context);
        final String filePath = Global.getArtistPathName(context) + File.separator + locale;

        GifImageButtonView sensorIdleContinueImageButton = findViewById(R.id.sensorIdleContinueImageButton);

        View sensorIdleView = findViewById(R.id.sensorIdleView);
        if (sensorIdleView != null) {
            String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "ldle_background");
            sensorIdleView.setBackground(Drawable.createFromPath(backgroundImageFile));
        }

        sensorIdleCounterTextView = findViewById(R.id.sensorIdleCounterTextView);
        if (sensorIdleCounterTextView != null) {
            sensorIdleCounterTextView.setText("");
        }



        if (sensorIdleContinueImageButton != null) {
            Global.gifImageButtonViewLoadImage(context, sensorIdleContinueImageButton, "ldle_button_continue_normal", "ldle_button_continue_press");
            sensorIdleContinueImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View sensorIdleView = findViewById(R.id.sensorIdleView);
                    if (sensorIdleView != null) {
                        cancelOverIdleTimer();
                        resetSensorTimeout();
                        showView(sensorIdleView, false);
                    }

                    View exitView = findViewById(R.id.exitView);
                    if (exitView != null) {
                        showView(exitView, false);
                    }
                }
            });
        }

        GifImageButtonView sensorIdleExitImageButton = findViewById(R.id.sensorIdleExitImageButton);
        if (sensorIdleExitImageButton != null) {
            Global.gifImageButtonViewLoadImage(context, sensorIdleExitImageButton, "ldle_button_exit_normal", "ldle_button_exit_press");
            sensorIdleExitImageButton.setOnClickListener(new View.OnClickListener() {
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
    }

    private static boolean initializedUsbSensor = false;

    protected void initUsbSensor() {
        if (initializedUsbSensor) return;

        initializedUsbSensor = true;

        if (mUsbHandler == null) {
            mUsbHandler = new UsbHandler();
        }

        if (usbConnection == null) {
            usbConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName arg0, IBinder arg1) {
                    usbService = ((UsbService.UsbBinder) arg1).getService();
                    usbService.setHandler(mUsbHandler);
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                    usbService.setHandler(null);
                    usbService = null;
                }
            };
        }

        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    protected synchronized void releaseUsbSensor() {
        /*
        if (!initializedUsbSensor) {
            return;
        }

        initializedUsbSensor = false;

        if (usbService != null) {
            usbService.setHandler(null);
            usbService = null;
        }

        if (usbConnection != null) {
            unbindService(usbConnection);
            usbConnection = null;
        }

        if (mUsbReceiver != null) {
            try {
                unregisterReceiver(mUsbReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mUsbReceiver = null;
        }
        */
    }

    @Override
    protected void onResume() {
        super.onResume();

        installReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();

        cancelOverIdleTimer();
        uninstallReceiver();
        releaseUsbSensor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setFilters() {
        /*
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
        */
    }

    protected void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private long hasBeenSensedTime = -1;
    private long MAX_SENSED_TIME = 60 * 1000;
    private boolean overIdleTime = false;

    protected void setIdleMaxTime(long ms) {
        MAX_SENSED_TIME = ms;
    }

    protected void resetSensorTimeout() {
        hasBeenSensedTime = -1;
        overIdleTime = false;
    }

    protected void sensorDataIncoming(int dataLeft, int dataRight, int dataTop, int dataUltrasnoic) {
        Log.i("SENSOR", "SENSOR : " + dataLeft + " , " + dataRight + " , " + dataTop + " , " + dataUltrasnoic);

        if (hasBeenSensedTime == -1) {
            hasBeenSensedTime = Calendar.getInstance().getTimeInMillis();
        }

        if (dataLeft > 1000 || dataRight > 1000 || dataTop > 1000 || (dataUltrasnoic < 100 && dataUltrasnoic != -1)) {
            hasBeenSensedTime = Calendar.getInstance().getTimeInMillis();
        } else {
            long now = Calendar.getInstance().getTimeInMillis();

            if ((now - hasBeenSensedTime) > MAX_SENSED_TIME) {
                if (!overIdleTime) {
                    closeAllDialog();
                    overIdleTime = true;

                    View sensorIdleView = findViewById(R.id.sensorIdleView);

                    showView(sensorIdleView, true);
                    //showDummyAlertView();
                    startOverIdleTimer();
                    //releaseUsbSensor();
                }
            }
        }
    }

    public static final String DidSensorDataUpdateNotification = "SensorDataUpdateNotification";

    private BroadcastReceiver didSensorUpdateNotification = null;

    protected void installReceiver() {
        if (didSensorUpdateNotification == null) {
            didSensorUpdateNotification = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int dataLeft = intent.getIntExtra("LEFT", -1);
                    int dataRight = intent.getIntExtra("RIGHT", -1);
                    int dataTop = intent.getIntExtra("TOP", -1);
                    int dataUltrasnoic = intent.getIntExtra("CM", -1);

                    sensorDataIncoming(dataLeft, dataRight, dataTop, dataUltrasnoic);
                }
            };
            Global.registerReceiver(context, didSensorUpdateNotification, new IntentFilter(DidSensorDataUpdateNotification));
        }
    }

    protected void uninstallReceiver() {
        if (didSensorUpdateNotification != null) {
            Global.unregisterReceiver(context, didSensorUpdateNotification);
            didSensorUpdateNotification = null;
        }
    }

    private class UsbHandler extends Handler {
        public UsbHandler() {
            //mActivity = new WeakReference<>(activity);
        }

        private void sendBroadcast(Context context, int dataLeft, int dataRight, int dataTop, int dataUltrasnoic) {
            Intent intent = new Intent(DidSensorDataUpdateNotification);
            intent.putExtra("LEFT", dataLeft);
            intent.putExtra("RIGHT", dataRight);
            intent.putExtra("TOP", dataTop);
            intent.putExtra("CM", dataUltrasnoic);

            Global.sendBroadcast(context, intent);
        }

        private StringBuffer dataLine = new StringBuffer();

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT: {
                    String data = (String) msg.obj;

                    if (data.contains("\n")) {
                        try {
                            dataLine.append(data);
                            String dataLine2 = dataLine.toString();

                            int lastEnter = dataLine2.lastIndexOf("\n");

                            dataLine2 = dataLine2.substring(0, lastEnter);

                            String line = dataLine2.toString();
                            String[] lineArray = line.split("\n");
                            String[] dataArray = lineArray[lineArray.length - 1].split(",");
                            if (!"@S".equalsIgnoreCase(dataArray[0])) return;
                            int dataLeft = Integer.parseInt(dataArray[1]);
                            int dataRight = Integer.parseInt(dataArray[2]);
                            int dataTop = Integer.parseInt(dataArray[3]);
                            int dataUltrasnoic = Integer.parseInt(dataArray[4]);

                            //Log.d("SENSOR", "dataUltrasnoic:" + dataUltrasnoic);

                            sendBroadcast(context, dataLeft, dataRight, dataTop, dataUltrasnoic);
                            dataLine.delete(0, dataLine.length());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        dataLine.append(data);
                    }
                    break;
                }
                case UsbService.CTS_CHANGE:
                    break;
                case UsbService.DSR_CHANGE:
                    break;
            }
        }
    }

    private Timer timerOverSensorIdleTime;
    private int sensorIdle2Counter = 20;

    private void updateSensorIdleCounterTextView() {
        if (sensorIdleCounterTextView != null) {
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    sensorIdleCounterTextView.setText(String.format("%d", sensorIdle2Counter));
                }
            });
        }
    }

    private void startOverIdleTimer() {
        if (timerOverSensorIdleTime != null) return;

        sensorIdle2Counter = 20;
        updateSensorIdleCounterTextView();
        timerOverSensorIdleTime = new Timer();
        timerOverSensorIdleTime.schedule(new TimerTask() {
            @Override
            public void run() {
                sensorIdle2Counter--;

                if (sensorIdle2Counter > 0) {
                    updateSensorIdleCounterTextView();
                } else {
                    //releaseUsbSensor();
                    finish();
                    Global.startMainActivity(context);
                }
            }
        }, 1000, 1000);
    }

    private void cancelOverIdleTimer() {
        if (timerOverSensorIdleTime != null) {
            timerOverSensorIdleTime.cancel();
            timerOverSensorIdleTime.purge();
            timerOverSensorIdleTime = null;
        }
    }
    public void cancelCountDownTimer(){
        super.cancelCountDownTimer();
    }
    public void disableCountDownTimer(){
        super.disableCountdownTimer();
    }
}
