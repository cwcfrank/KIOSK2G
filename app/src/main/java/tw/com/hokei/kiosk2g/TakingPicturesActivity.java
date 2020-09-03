package tw.com.hokei.kiosk2g;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.serenegiant.dialog.MessageDialogFragment;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usbcameracommon.UVCCameraHandlerMultiSurface;
import com.serenegiant.utils.BuildCheck;
import com.serenegiant.utils.HandlerThreadHandler;
import com.serenegiant.utils.PermissionCheck;
import com.serenegiant.widget.UVCCameraTextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.UserConfig;

public class TakingPicturesActivity extends CommonSensorActivity implements MessageDialogFragment.MessageDialogListener  {
    private static final String TAG = TakingPicturesActivity.class.getSimpleName();

    private final Handler mUIHandler = new Handler(Looper.getMainLooper());
    private final Thread mUiThread = mUIHandler.getLooper().getThread();
    private Handler mWorkerHandler;
    private long mWorkerThreadID = -1;

    private static final boolean DEBUG = false;	// TODO set false on release
    private static final boolean USE_SURFACE_ENCODER = true;

    private static final int PREVIEW_WIDTH = 1280;
    private static final int PREVIEW_HEIGHT = 720;
    private static final int PREVIEW_MODE = 1;

    protected static final int SETTINGS_HIDE_DELAY_MS = 2500;

    private USBMonitor mUSBMonitor;

    private UVCCameraHandlerMultiSurface mCameraHandler1;
    private UVCCameraHandlerMultiSurface mCameraHandler2;
    private UVCCameraHandlerMultiSurface mCameraHandler3;

    private UVCCameraTextureView mUVCCameraView1;
    private UVCCameraTextureView mUVCCameraView2;
    private UVCCameraTextureView mUVCCameraView3;

    private ImageButton nextImageButton;
    private GifImageView customerServiceImageView;

    private GifImageView movingUpImageView;
    private GifImageView movingDownImageView;
    private GifImageView movingLeftImageView;
    private GifImageView movingRightImageView;
    private GifImageView movingFrontImageView;
    private GifImageView movingBackImageView;

    private ImageView goodPositionImageView;
    private ImageView badPositionImageView;
    private ImageView readyCountImageView;

    private TextView distanceTextView;

    private boolean redoOperation = false;

    private String leftImageFileName = "";
    private String frontImageFileName = "";
    private String rightImageFileName = "";

    private View flashView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taking_pictures);

        UserConfig.setLastVisitPage(context, "4");
        setIdleMaxTime(30000);

        if (mWorkerHandler == null) {
            mWorkerHandler = HandlerThreadHandler.createHandler(TAG);
            mWorkerThreadID = mWorkerHandler.getLooper().getThread().getId();
        }

        Intent intent = getIntent();

        if (intent != null) {
            String data = intent.getStringExtra("REDO");

            if (!Global.isEmptyString(data)) {
                redoOperation = data.equals("1");
            }
        }

        long unixTime = Global.unitTime();

        leftImageFileName = Global.getDeviceID() + "_left" + unixTime + ".jpg";
        frontImageFileName = Global.getDeviceID() + "_front" + unixTime + ".jpg";
        rightImageFileName = Global.getDeviceID() + "_right" + unixTime + ".jpg";

        UserConfig.setLeftImageFile(context, leftImageFileName);
        UserConfig.setFrontImageFile(context, frontImageFileName);
        UserConfig.setRightImageFile(context, rightImageFileName);

        initCamera();
        initViews();

        setIdleHome(false);
        startTakePictureTimer();
    }

    private void initCamera() {
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);

        Global.handlerPost(new Runnable() {
            @Override
            public void run() {
                showWaitAlertView();
            }
        });
    }

    private void showAllMovingImages(boolean show) {
        showView(readyCountImageView, show);
        showView(movingUpImageView, show);
        showView(movingDownImageView, show);
        showView(movingLeftImageView, show);
        showView(movingRightImageView, show);
        showView(movingDownImageView, show);
        showView(movingFrontImageView, show);
        showView(movingBackImageView, show);

        showView(goodPositionImageView, show);
        showView(badPositionImageView, show);
    }

    @Override
    protected void initViews() {
        super.initViews();

        View mainView = findViewById(R.id.mainView);

        final String locale = UserConfig.getKioskArtistLocale(context);
        final String filePath = Global.getArtistPathName(context) + File.separator + locale;

        String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "photo_background");
        mainView.setBackground(Drawable.createFromPath(backgroundImageFile));

        flashView = findViewById(R.id.flashView);

        //customerServiceImageView = findViewById(R.id.customerServiceImageView);
        if (customerServiceImageView != null) {
            customerServiceImageView.setVisibility(View.GONE);
        }
//        customerServiceImageView = findViewById(R.id.customerServiceImageView);
//        Global.gifImageViewLoadImage2(context,customerServiceImageView,"service_call_left");

        readyCountImageView = findViewById(R.id.readyCountImageView);
        movingUpImageView = findViewById(R.id.movingUpImageView);
        movingDownImageView = findViewById(R.id.movingDownImageView);
        movingLeftImageView = findViewById(R.id.movingLeftImageView);
        movingRightImageView = findViewById(R.id.movingRightImageView);
        movingFrontImageView = findViewById(R.id.movingFrontImageView);
        movingBackImageView = findViewById(R.id.movingBackImageView);


        Global.gifImageViewLoadImage2(context,movingLeftImageView,"photo_target_left");
        Global.gifImageViewLoadImage2(context,movingRightImageView,"photo_target_right");
        Global.gifImageViewLoadImage2(context,movingBackImageView,"photo_target_back");
        Global.gifImageViewLoadImage2(context,movingFrontImageView,"photo_target_front");
        Global.gifImageViewLoadImage2(context,movingUpImageView,"photo_target_up");
        Global.gifImageViewLoadImage2(context,movingDownImageView,"photo_target_down");


        goodPositionImageView = findViewById(R.id.goodPositionImageView);
        backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "photo_face_green");
        goodPositionImageView.setImageDrawable(Drawable.createFromPath(backgroundImageFile));

        badPositionImageView = findViewById(R.id.badPositionImageView);
        backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "photo_face_red");
        badPositionImageView.setImageDrawable(Drawable.createFromPath(backgroundImageFile));

        readyCountImageView.setVisibility(View.GONE);
        movingUpImageView.setVisibility(View.GONE);
        movingDownImageView.setVisibility(View.GONE);
        movingLeftImageView.setVisibility(View.GONE);
        movingRightImageView.setVisibility(View.GONE);
        movingUpImageView.setVisibility(View.GONE);
        movingFrontImageView.setVisibility(View.GONE);
        movingBackImageView.setVisibility(View.GONE);
        goodPositionImageView.setVisibility(View.GONE);
        badPositionImageView.setVisibility(View.GONE);

        mUVCCameraView1 = (UVCCameraTextureView)findViewById(R.id.camera1_view);
        mUVCCameraView1.setRotation(90);
        mUVCCameraView1.setAspectRatio(PREVIEW_WIDTH / (float)PREVIEW_HEIGHT);
        mUVCCameraView1.setScaleX(2);
        mUVCCameraView1.setScaleY(2);
        mUVCCameraView1.setVisibility(View.GONE);

        mUVCCameraView2 = (UVCCameraTextureView)findViewById(R.id.camera2_view);
        mUVCCameraView2.setRotation(-90);
        mUVCCameraView2.setAspectRatio(PREVIEW_WIDTH / (float)PREVIEW_HEIGHT);
        mUVCCameraView2.setScaleX(-2); // mirror
        mUVCCameraView2.setScaleY(3);

        mUVCCameraView3 = (UVCCameraTextureView)findViewById(R.id.camera3_view);
        mUVCCameraView3.setRotation(90);
        mUVCCameraView3.setAspectRatio(PREVIEW_WIDTH / (float)PREVIEW_HEIGHT);
        mUVCCameraView3.setScaleX(2);
        mUVCCameraView3.setScaleY(2);
        mUVCCameraView3.setVisibility(View.GONE);

        mCameraHandler1 = UVCCameraHandlerMultiSurface.createHandler(this, mUVCCameraView1,
                USE_SURFACE_ENCODER ? 0 : 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);

        mCameraHandler2 = UVCCameraHandlerMultiSurface.createHandler(this, mUVCCameraView2,
                USE_SURFACE_ENCODER ? 0 : 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);

        mCameraHandler3 = UVCCameraHandlerMultiSurface.createHandler(this, mUVCCameraView3,
                USE_SURFACE_ENCODER ? 0 : 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);

        nextImageButton = findViewById(R.id.nextImageButton);
        nextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        if (customerServiceImageView != null) {
            customerServiceImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
    }

    @Override
    protected void onPause() {
        //clearToast();
        super.onPause();
    }

    @Override
    public synchronized void onDestroy() {
        stopTakePictureTimer();

        stopPreview1();
        stopPreview2();
        stopPreview3();

        if (usbDeviceMap != null) {
            for (Map.Entry<String, UsbDevice> entry : usbDeviceMap.entrySet()) {
                UsbDevice device = entry.getValue();
                mUSBMonitor.processDettach(device);
            }
        }

        if (mUVCCameraView1 != null) {
            mUVCCameraView1 = null;
        }

        if (mUVCCameraView2 != null) {
            mUVCCameraView2 = null;
        }

        if (mUVCCameraView3 != null) {
            mUVCCameraView3 = null;
        }

        if (mCameraHandler1 != null) {
            mCameraHandler1.close();
            mCameraHandler1.release();
            mCameraHandler1 = null;
        }

        if (mCameraHandler2 != null) {
            mCameraHandler2.close();
            mCameraHandler2.release();
            mCameraHandler2 = null;
        }

        if (mCameraHandler3 != null) {
            mCameraHandler3.close();
            mCameraHandler3.release();
            mCameraHandler3 = null;
        }

        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }

        mUVCCameraView1 = null;
        mUVCCameraView2 = null;
        mUVCCameraView3 = null;

        if (mWorkerHandler != null) {
            try {
                mWorkerHandler.getLooper().quit();
            } catch (final Exception e) {
                //
            }
            mWorkerHandler = null;
        }

        super.onDestroy();

        Runtime.getRuntime().gc();
    }

    //================================================================================================
    /**
     * UIスレッドでRunnableを実行するためのヘルパーメソッド
     * @param task
     * @param duration
     */
    public final void runOnUiThread(final Runnable task, final long duration) {
        if (task == null) return;
        mUIHandler.removeCallbacks(task);
        if ((duration > 0) || Thread.currentThread() != mUiThread) {
            mUIHandler.postDelayed(task, duration);
        } else {
            try {
                task.run();
            } catch (final Exception e) {
                Log.w(TAG, e);
            }
        }
    }

    /**
     * UIスレッド上で指定したRunnableが実行待ちしていれば実行待ちを解除する
     * @param task
     */
    public final void removeFromUiThread(final Runnable task) {
        if (task == null) return;
        mUIHandler.removeCallbacks(task);
    }

    /**
     * ワーカースレッド上で指定したRunnableを実行する
     * 未実行の同じRunnableがあればキャンセルされる(後から指定した方のみ実行される)
     * @param task
     * @param delayMillis
     */
    protected final synchronized void queueEvent(final Runnable task, final long delayMillis) {
        if ((task == null) || (mWorkerHandler == null)) return;

        try {
            mWorkerHandler.removeCallbacks(task);
            if (delayMillis > 0) {
                mWorkerHandler.postDelayed(task, delayMillis);
            } else if (mWorkerThreadID == Thread.currentThread().getId()) {
                task.run();
            } else {
                mWorkerHandler.post(task);
            }
        } catch (final Exception e) {
            // ignore
        }
    }

    /**
     * 指定したRunnableをワーカースレッド上で実行予定であればキャンセルする
     * @param task
     */
    protected final synchronized void removeEvent(final Runnable task) {
        if (task == null) return;
        try {
            mWorkerHandler.removeCallbacks(task);
        } catch (final Exception e) {
            // ignore
        }
    }

//================================================================================
    /**
     * MessageDialogFragmentメッセージダイアログからのコールバックリスナー
     * @param dialog
     * @param requestCode
     * @param permissions
     * @param result
     */
    @SuppressLint("NewApi")
    @Override
    public void onMessageDialogResult(final MessageDialogFragment dialog, final int requestCode, final String[] permissions, final boolean result) {
        if (result) {
            // メッセージダイアログでOKを押された時はパーミッション要求する
            if (BuildCheck.isMarshmallow()) {
                requestPermissions(permissions, requestCode);
                return;
            }
        }
        // メッセージダイアログでキャンセルされた時とAndroid6でない時は自前でチェックして#checkPermissionResultを呼び出す
        for (final String permission: permissions) {
            checkPermissionResult(requestCode, permission, PermissionCheck.hasPermission(this, permission));
        }
    }

    /**
     * パーミッション要求結果を受け取るためのメソッド
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);	// 何もしてないけど一応呼んどく
        final int n = Math.min(permissions.length, grantResults.length);
        for (int i = 0; i < n; i++) {
            //====checkPermissionResult(requestCode, permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
        }
    }

    /**
     * パーミッション要求の結果をチェック
     * ここではパーミッションを取得できなかった時にToastでメッセージ表示するだけ
     * @param requestCode
     * @param permission
     * @param result
     */
    protected void checkPermissionResult(final int requestCode, final String permission, final boolean result) {
        // パーミッションがないときにはメッセージを表示する
        if (!result && (permission != null)) {
            if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
                //Global.toast(context, com.serenegiant.common.R.string.permission_audio);
            }
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                //Global.toast(context, com.serenegiant.common.R.string.permission_ext_storage);
            }
            if (Manifest.permission.INTERNET.equals(permission)) {
                //Global.toast(context, com.serenegiant.common.R.string.permission_network);
            }
        }
    }

    // 動的パーミッション要求時の要求コード
    protected static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 0x12345;
    protected static final int REQUEST_PERMISSION_AUDIO_RECORDING = 0x234567;
    protected static final int REQUEST_PERMISSION_NETWORK = 0x345678;
    protected static final int REQUEST_PERMISSION_CAMERA = 0x537642;

    /**
     * 外部ストレージへの書き込みパーミッションが有るかどうかをチェック
     * なければ説明ダイアログを表示する
     * @return true 外部ストレージへの書き込みパーミッションが有る
     */
    protected boolean checkPermissionWriteExternalStorage() {
        if (!PermissionCheck.hasWriteExternalStorage(this)) {
            MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE,
                    com.serenegiant.common.R.string.permission_title, com.serenegiant.common.R.string.permission_ext_storage_request,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
            return false;
        }
        return true;
    }
    //================================================================================================

    private int mPreviewSurfaceId1;
    private int mPreviewSurfaceId2;
    private int mPreviewSurfaceId3;

    private void startPreview1() {
        if (!mCameraHandler1.isPreviewing()) {
            mUVCCameraView1.resetFps();
            mCameraHandler1.startPreview();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final SurfaceTexture st = mUVCCameraView1.getSurfaceTexture();
                        if (st != null) {
                            final Surface surface = new Surface(st);
                            mPreviewSurfaceId1 = surface.hashCode();
                            mCameraHandler1.addSurface(mPreviewSurfaceId1, surface, false);
                        }
                        //startImageProcessor1(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                    } catch (final Exception e) {
                        Log.w(TAG, e);
                    }
                }
            });
        }

        updateItems();
    }

    private void startPreview2() {
        if (!mCameraHandler2.isPreviewing()) {
            mUVCCameraView2.resetFps();
            mCameraHandler2.startPreview();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final SurfaceTexture st = mUVCCameraView2.getSurfaceTexture();
                        if (st != null) {
                            final Surface surface = new Surface(st);
                            mPreviewSurfaceId2 = surface.hashCode();
                            mCameraHandler2.addSurface(mPreviewSurfaceId2, surface, false);
                        }
                        //startImageProcessor1(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                    } catch (final Exception e) {
                        Log.w(TAG, e);
                    }
                }
            });
        }

        updateItems();
    }

    private void startPreview3() {
        if (!mCameraHandler3.isPreviewing()) {
            mUVCCameraView3.resetFps();
            mCameraHandler3.startPreview();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final SurfaceTexture st = mUVCCameraView3.getSurfaceTexture();
                        if (st != null) {
                            final Surface surface = new Surface(st);
                            mPreviewSurfaceId3 = surface.hashCode();
                            mCameraHandler3.addSurface(mPreviewSurfaceId3, surface, false);
                        }
                        //startImageProcessor1(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                    } catch (final Exception e) {
                        Log.w(TAG, e);
                    }
                }
            });
        }

        updateItems();
    }

    private void stopPreview1() {
        try {
            if (mPreviewSurfaceId1 != 0) {
                mCameraHandler1.removeSurface(mPreviewSurfaceId1);
                mPreviewSurfaceId1 = 0;
            }
            mCameraHandler1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPreview2() {
        try {
            if (mPreviewSurfaceId2 != 0) {
                mCameraHandler2.removeSurface(mPreviewSurfaceId2);
                mPreviewSurfaceId2 = 0;
            }
            mCameraHandler2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPreview3() {
        try {
            if (mPreviewSurfaceId3 != 0) {
                mCameraHandler3.removeSurface(mPreviewSurfaceId3);
                mPreviewSurfaceId3 = 0;
            }
            mCameraHandler3.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final int VENDOR_ID1 = 1423;
    private final int PROD_ID1 = 14374;

    private final int VENDOR_ID2 = 7119;//1133;
    private final int PROD_ID2 = 11434;//2140;

    private int[] connect_status = new int[] {0, 0, 0};

    private Map<String, UsbDevice> usbPermisionMap = new HashMap<>();
    private Map<String, UsbDevice> usbDeviceMap = new HashMap<>();

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener
            = new USBMonitor.OnDeviceConnectListener() {

        //private Map<String, UsbDevice> usbPermisionMap = new HashMap<>();

        @SuppressLint("NewApi")
        @Override
        public void onAttach(final UsbDevice device) {
            //Global.toast(context, "USB_DEVICE_ATTACHED");

            Log.i("VENDOR", "device:"  + device.getDeviceName() + "," + device.getProductName() + "," + device.getVendorId() + "," + device.getProductId());

            if ((device.getVendorId() == VENDOR_ID1 && device.getProductId() == PROD_ID1) ||
                (device.getVendorId() == VENDOR_ID2 && device.getProductId() == PROD_ID2)) {

                int delayTime = 500;

                if (!mUSBMonitor.hasPermission(device)) {
                    delayTime = 5000;
                }

                Global.handlerPostDelay(new Runnable() {
                    @Override
                    public void run() {
                        boolean toDoOpen = false;

                        if (device.getVendorId() == VENDOR_ID2 && device.getProductId() == PROD_ID2) {
                            if (mCameraHandler2 != null && !mCameraHandler2.isPreviewing() && Global.isEmptyString(mCameraHandler2.deviceName)) {
                                toDoOpen = true;
                                connect_status[1] = 1;
                                mCameraHandler2.deviceName = device.getDeviceName();
                            }
                        } else {
                            if (mCameraHandler1 != null && !mCameraHandler1.isPreviewing() && Global.isEmptyString(mCameraHandler1.deviceName)) {
                                toDoOpen = true;
                                connect_status[0] = 1;
                                mCameraHandler1.deviceName = device.getDeviceName();
                            } else if (mCameraHandler3 != null && !mCameraHandler3.isPreviewing() && Global.isEmptyString(mCameraHandler3.deviceName)) {
                                toDoOpen = true;
                                connect_status[2] = 1;
                                mCameraHandler3.deviceName = device.getDeviceName();
                            }
                        }

                        if (toDoOpen) {
                            int orgCount = usbPermisionMap.size();

                            usbPermisionMap.put(device.getDeviceName(), device);
                            usbDeviceMap.put(device.getDeviceName(), device);

                            if (mUSBMonitor.hasPermission(device)) {
                                mUSBMonitor.processConnect(device);
                            } else if (orgCount < 1) {
                                mUSBMonitor.requestPermission(device);
                            }
                        }
                    }
                }, delayTime);
            }
        }

        @Override
        public void onConnect(final UsbDevice device,
                              final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {

            if (DEBUG) Log.v(TAG, "onConnect:");

            if (mCameraHandler1.deviceName.equalsIgnoreCase(device.getDeviceName())) {
                if (connect_status[0] == 1) {
                    stopPreview1();
                    mCameraHandler1.open(ctrlBlock);
                    startPreview1();
                    connect_status[0] = 2;
                }
            } else if (mCameraHandler2.deviceName.equalsIgnoreCase(device.getDeviceName())) {
                if (connect_status[1] == 1) {
                    stopPreview2();
                    mCameraHandler2.open(ctrlBlock);
                    startPreview2();
                    connect_status[1] = 2;
                }
            } else if (mCameraHandler3.deviceName.equalsIgnoreCase(device.getDeviceName())) {
                if (connect_status[2] == 1) {
                    stopPreview3();
                    mCameraHandler3.open(ctrlBlock);
                    startPreview3();
                    connect_status[2] = 2;
                }
            }

            if (connect_status[0] == 2 && connect_status[1] == 2 && connect_status[2] == 2) {
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        dismissWaitAlertView();
                    }
                });
            }

            updateItems();

            usbPermisionMap.remove(device.getDeviceName());
            int count = usbPermisionMap.size();

            if (count > 0) {
                for (Map.Entry<String, UsbDevice> entry : usbPermisionMap.entrySet()) {
                    //Object key = entry.getKey();
                    UsbDevice deviceOther = entry.getValue();
                    mUSBMonitor.requestPermission(deviceOther);
                    break;
                }
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device,
                                 final USBMonitor.UsbControlBlock ctrlBlock) {

            if (DEBUG) Log.v(TAG, "onDisconnect:");

            try {
                if (mCameraHandler1 != null && !Global.isEmptyString(mCameraHandler1.deviceName) && mCameraHandler1.deviceName.equalsIgnoreCase(device.getDeviceName())) {
                    connect_status[0] = 0;
                    mCameraHandler1.deviceName = "";
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            stopPreview1();
                        }
                    }, 0);
                } else if (mCameraHandler2 != null && !Global.isEmptyString(mCameraHandler2.deviceName) && mCameraHandler2.deviceName.equalsIgnoreCase(device.getDeviceName())) {
                    connect_status[1] = 0;
                    mCameraHandler2.deviceName = "";
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            stopPreview2();
                        }
                    }, 0);
                } else if (mCameraHandler3 != null && !Global.isEmptyString(mCameraHandler3.deviceName) &&  mCameraHandler3.deviceName.equalsIgnoreCase(device.getDeviceName())) {
                    connect_status[2] = 0;
                    mCameraHandler3.deviceName = "";
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            stopPreview3();
                        }
                    }, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            updateItems();
        }
        @Override
        public void onDettach(final UsbDevice device) {
            //Global.toast(context, "USB_DEVICE_DETACHED");
        }

        @Override
        public void onCancel(final UsbDevice device) {
            //Global.toast(context, "USB_DEVICE_CANCEL");
        }
    };

    private boolean isAllCamerasReady() {
        if (connect_status[0] != 2) return false;
        if (connect_status[1] != 2) return false;
        if (connect_status[2] != 2) return false;

        return true;
    }

    //================================================================================
    private boolean isActive() {
        return mCameraHandler1 != null && mCameraHandler1.isOpened();
    }

    private boolean checkSupportFlag(final int flag) {
        return mCameraHandler1 != null && mCameraHandler1.checkSupportFlag(flag);
    }

    private int getValue(final int flag) {
        return mCameraHandler1 != null ? mCameraHandler1.getValue(flag) : 0;
    }

    private int setValue(final int flag, final int value) {
        return mCameraHandler1 != null ? mCameraHandler1.setValue(flag, value) : 0;
    }

    private int resetValue(final int flag) {
        return mCameraHandler1 != null ? mCameraHandler1.resetValue(flag) : 0;
    }

    private void updateItems() {
        runOnUiThread(mUpdateItemsOnUITask, 100);
    }

    private final Runnable mUpdateItemsOnUITask = new Runnable() {
        @Override
        public void run() {
            if (isFinishing()) return;
        }
    };


    private void savePicture() {
        savePicture(1);
        savePicture(2);
        savePicture(3);
    }

    private void saveTempPicture(int index) {
        if (!checkPermissionWriteExternalStorage()) return;

        String capturePath = Global.getAppCapturePathName(context);
        String imagePath;

        Global.createPath(capturePath);

        switch(index) {
            case 1 :
                if (mCameraHandler1 != null && mCameraHandler1.isOpened()) {
                    imagePath = capturePath + File.separator + "LTEMP.jpg";
                    mCameraHandler1.captureStill(imagePath);
                }
                break;

            case 2 :
                if (mCameraHandler2 != null && mCameraHandler2.isOpened()) {
                    imagePath = capturePath + File.separator + "FTEMP.jpg";
                    mCameraHandler2.captureStill(imagePath);
                }
                break;

            case 3 :
                if (mCameraHandler3 != null && mCameraHandler3.isOpened()) {
                    imagePath = capturePath + File.separator + "RTEMP.jpg";
                    mCameraHandler3.captureStill(imagePath);
                }
                break;
        }
    }

    private void savePicture(int index) {
        if (!checkPermissionWriteExternalStorage()) return;

        String capturePath = Global.getAppCapturePathName(context);
        String imagePath;

        Global.createPath(capturePath);

        switch(index) {
            case 1 :
                if (mCameraHandler1 != null && mCameraHandler1.isOpened()) {
                    imagePath = capturePath + File.separator + leftImageFileName;
                    mCameraHandler1.captureStill(imagePath);
                    Global.sleep(1000);
                    rotateImage(imagePath);
                }
                break;

            case 2 :
                if (mCameraHandler2 != null && mCameraHandler2.isOpened()) {
                    imagePath = capturePath + File.separator + frontImageFileName;
                    mCameraHandler2.captureStill(imagePath);
                    Global.sleep(1000);
                    rotateImage(imagePath);
                }
                break;

            case 3 :
                if (mCameraHandler3 != null && mCameraHandler3.isOpened()) {
                    imagePath = capturePath + File.separator + rightImageFileName;
                    mCameraHandler3.captureStill(imagePath);
                    Global.sleep(1000);
                    rotateImage(imagePath);
                }
                break;
        }
    }

    private void rotateImage(String filePath) {
        Bitmap originBmp= BitmapFactory.decodeFile(filePath);
        if (originBmp == null) return;

        String newFilePath = filePath;
        newFilePath = newFilePath.replace(".png", ".jpg");

        Matrix m = new Matrix();
        int width = originBmp.getWidth();
        int height = originBmp.getHeight();

        m.postRotate(90);
        Bitmap rotateBmp = Bitmap.createBitmap(originBmp, 0, 0, width, height, m, true);

        try {
            File file = new File(newFilePath);
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file, false);
            rotateBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    //Global.toast(context, "USB Ready");
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    //Global.toast(context, "USB Permission not granted");
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    //Global.toast(context, "No USB connected");
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    //Global.toast(context, "USB disconnected");
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    //Global.toast(context, "USB device not supported");
                    break;
            }
        }
    };

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */

    private final int FINAL_FACE_READY_TIME = 5;
    private final int FINAL_FACE_IDLE_TIME = 1500;
    private int faceReadyTime = -1;
    private int faceIdleTime = -1;
    private Timer timerTakePicture = null;
    private boolean pictureIsSaving = false;

    private synchronized void setReadyCountImageView(final String imagePath) {
        Global.handlerPost(new Runnable() {
            @Override
            public void run() {
//                readyCountImageView.setImageResource(resID);
                readyCountImageView.setImageDrawable(Drawable.createFromPath(imagePath));
            }
        });
    }

    private void startTakePictureTimer() {
        if (timerTakePicture == null) {
            timerTakePicture = new Timer();
            timerTakePicture.schedule(new TimerTask() {
                @Override
                public void run() {
                    faceIdleTime++;
                    if (faceIdleTime > FINAL_FACE_IDLE_TIME) {
                        showDummyAlertView();
                        Global.handlerPostDelay(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                                Global.startMainActivity(context);
                                //dismissDummyAlertView();
                            }
                        }, 500);
                        return;
                    }

                    if (faceReadyTime < 0) return;

                    faceReadyTime++;
                    showView(readyCountImageView, (faceReadyTime > 0));

                    if (faceReadyTime > FINAL_FACE_READY_TIME) {
                        showDummyAlertView();
                        showView(flashView, true);
                        Global.handlerPostDelay(new Runnable() {
                            @Override
                            public void run() {
                                showView(flashView, false);
                            }
                        }, 300);

                        stopPreview2();
                        stopTakePictureTimer();
                        releaseUsbSensor();
                        pictureIsSaving = true;
                        Runtime.getRuntime().gc();
                        showAllMovingImages(false);
                        //savePicture();
                        stopPreview2();
                        //stopPreview3();
//                        savePicture(1);
                        Runtime.getRuntime().gc();

                        //Global.sleep(1000);
                        faceReadyTime = -1;
                        //stopPreview1();
                        //stopPreview2();
                        //stopPreview3();

                        Global.handlerPostDelay(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                                Global.startActivity(context, new Intent(context, PhotoValidationActivity.class));
                                //overridePendingTransition(0, 0);
                                //dismissDummyAlertView();
                            }
                        },2000);
                    } else {
                        String filePath = Global.getArtistPathName(context);
                        switch(faceReadyTime) {
                            case 1 :
                                String countDown5FilePath = filePath + File.separator + UserConfig.getKioskArtist(context, "photo_countdown_5");
                                setReadyCountImageView(countDown5FilePath);
                                break;
                            case 2 :
                                String countDown4FilePath = filePath + File.separator + UserConfig.getKioskArtist(context, "photo_countdown_4");
                                setReadyCountImageView(countDown4FilePath);
                                break;
                            case 3 :
                                String countDown3FilePath = filePath + File.separator + UserConfig.getKioskArtist(context, "photo_countdown_3");
                                setReadyCountImageView(countDown3FilePath);
                                break;
                            case 4 :
                                String countDown2FilePath = filePath + File.separator + UserConfig.getKioskArtist(context, "photo_countdown_2");
                                setReadyCountImageView(countDown2FilePath);
                                break;
                            case 5 :
                                String countDown1FilePath = filePath + File.separator + UserConfig.getKioskArtist(context, "photo_countdown_1");
                                setReadyCountImageView(countDown1FilePath);
                                break;
                        }
                        //
                        if (faceReadyTime == 3) {
                            Log.d("faceReadyTime3","savePic1");
                            savePicture(1);
                        }
                        if (faceReadyTime == 4) {
                            Log.d("faceReadyTime4","savePic3");
                            savePicture(3);
                        }
                        if (faceReadyTime == 5) {
                            Log.d("faceReadyTime5","savePic2");
                            savePicture(2);
                        }
                    }
                }
            }, 3000, 1000);
        }
    }

    private void stopTakePictureTimer() {
        if (timerTakePicture != null) {
            timerTakePicture.cancel();
            timerTakePicture.purge();
            timerTakePicture = null;
        }
    }

    private int dataUltrasnoicGlobal;
    private int dataUltrasnoicGlobal2;
    private int greaterThen100Count = 0;
    private int dataUltrasnoicPrev = -999;
    private int greaterThen100BeforeValue = 0;

    private final int ultrasnoicMax = 55;//70;
    private final int ultrasnoicMin = 35;//40;

    protected void sensorDataIncoming(int dataLeft, int dataRight, int dataTop, int dataUltrasnoic) {
        if (pictureIsSaving || !isAllCamerasReady()) return;

        super.sensorDataIncoming(dataLeft, dataRight, dataTop, dataUltrasnoic);

        if (dataUltrasnoic < ultrasnoicMax) {
            greaterThen100Count = 0;
        } else {
            greaterThen100Count++;
            if (greaterThen100Count < 5/*10*/) {
                if (greaterThen100Count == 1) {
                    if (dataUltrasnoicPrev == -999) {
                        dataUltrasnoicPrev = dataUltrasnoic;
                    }
                    greaterThen100BeforeValue = dataUltrasnoicPrev;
                }
                dataUltrasnoic = greaterThen100BeforeValue;
            }
        }

        dataUltrasnoicPrev = dataUltrasnoic;
        dataUltrasnoicGlobal2 = dataUltrasnoic;

        boolean ready = true;

        if (dataLeft >= 1000) {
            showView(movingRightImageView, true);
            faceReadyTime = -1;
            ready = false;
        } else {
            showView(movingRightImageView, false);
        }

        if (dataRight >= 1000) {
            showView(movingLeftImageView, true);
            faceReadyTime = -1;
            ready = false;
        } else {
            showView(movingLeftImageView, false);
        }

        if (dataTop >= 1000) {
            showView(movingDownImageView, true);
            faceReadyTime = -1;
            ready = false;
        } else {
            showView(movingDownImageView, false);
        }

        if (faceReadyTime <= 1) {
            if (faceReadyTime == -1 && dataUltrasnoic == -1) {
                showView(movingFrontImageView, false);
                showView(movingBackImageView, false);
                faceReadyTime = -1;
                ready = false;
            } else if (dataUltrasnoic != -1) {
                if (dataUltrasnoic > ultrasnoicMax) {
                    showView(movingFrontImageView, true);
                    faceReadyTime = -1;
                    ready = false;
                } else if (ready) {
                    showView(movingFrontImageView, false);
                    if (dataUltrasnoic < ultrasnoicMin) {
                        showView(movingBackImageView, true);
                        faceReadyTime = -1;
                        ready = false;
                    } else {
                        showView(movingBackImageView, false);
                    }
                }

                if (dataUltrasnoic < 100) {
                    faceIdleTime = 0;
                }
            }
        }

        if (ready && faceReadyTime < 0) {
            faceReadyTime = 0;
        }

        if (!ready) {
            showView(readyCountImageView, false);
        }

        showView(goodPositionImageView, ready);
        showView(badPositionImageView, !ready);

        dataUltrasnoicGlobal = dataUltrasnoic;


    }
}
