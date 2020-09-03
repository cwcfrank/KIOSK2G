package tw.com.hokei.kiosk2g;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.UserConfig;
import tw.com.hokei.kiosk2g.model.DbArtist;

public final class Global {
	public static final String ACTION_RECEIVED_GCM_MESSAGE = Global.class.getPackage() + ".action.RECEIVED_GCM_MESSAGE";

	public static Context currentContext = null;

	public static void handlerPost(Runnable runnable) {
		new Handler(Looper.getMainLooper()).post(runnable);
	}

	public static void handlerPostDelay(Runnable runnable, int delayMillis) {
		new Handler(Looper.getMainLooper()).postDelayed(runnable, delayMillis);
	}

	public static void startActivity(Context context, Intent intent) {
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	public static void startActivity(Context context, Intent intent, int flag) {
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_SINGLE_TOP | flag);
		context.startActivity(intent);
	}
	
	public static boolean isEmptyString(String value) {
		return (value == null || value.length() < 1);
	}
	
	public static String trimString(String value) {
		if (isEmptyString(value)) return "";
		
		value = value.trim();
		return value;
	}

	public static void alert(final Context context, final int message) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.app_name);
				builder.setMessage(message);
				builder.setNegativeButton(R.string.i_see, null);
				builder.show();
			}
		});
	}

	public static void alert(final Context context, final String message) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.app_name);
				builder.setMessage(message);
				builder.setNegativeButton(R.string.i_see, null);
				builder.show();
			}
		});
	}

	public static void alertNetworkNotAvailableView(final Context context) {
		alert(context, R.string.no_network_services);
	}

	public static void toastNetworkNotAvailableView(final Context context) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, R.string.no_network_services, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public static void toast(final Context context, final String message) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public static void toast(final Context context, final int message) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public static String getTodayDateTimeString() {
		Calendar now = Calendar.getInstance();

		return String.format(Locale.getDefault(),
				"%04d-%02d-%02d %02d:%02d:%02d",
				now.get(Calendar.YEAR),
				now.get(Calendar.MONTH) + 1,
				now.get(Calendar.DAY_OF_MONTH),
				now.get(Calendar.HOUR_OF_DAY),
				now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));
	}

	/*
	To provide users with greater data protection, starting in this release, Android removes programmatic access to the device’s local hardware identifier for apps using the Wi-Fi and Bluetooth APIs. The WifiInfo.getMacAddress() and the BluetoothAdapter.getAddress() methods now return a constant value of 02:00:00:00:00:00.
	To access the hardware identifiers of nearby external devices via Bluetooth and Wi-Fi scans, your app must now have the ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permissions.
	 */
	public static boolean apiNetworkChkFlag = true;
	public static void apiNetworkChk(Context context){
		apiNetworkChkFlag = true;
		ConnectivityManager connectivityManager  = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		boolean chk = activeNetworkInfo != null && activeNetworkInfo.isConnected();
		if(chk) {
			int[] status = new int[1];
			int returnCode = WebService.kioskStatus(context, status);
			if (returnCode == -2) {
				apiNetworkChkFlag = false;
			}
		}
	}


	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager  = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		boolean chk = activeNetworkInfo != null && activeNetworkInfo.isConnected();
		if(chk) {
			if(apiNetworkChkFlag == false){
				chk = false;
				Log.d("isNetworkAvailable = ", "false");
			}else {
				Log.d("isNetworkAvailable = ", "true");
			}
		}else {
			Log.d("isNetworkAvailable = ", "false");
		}

		return chk;
	}

	private static String deviceID = null;

	public static String getDeviceID() {
		if (deviceID == null) {
			deviceID = getMacAddr();
			//deviceID = "146B9C20A67C";
		}
		return deviceID;
	}

	public static String getMacAddr() {
		try {
			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
				if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

				byte[] macBytes = nif.getHardwareAddress();
				if (macBytes == null) {
					return "";
				}

				StringBuilder res1 = new StringBuilder();
				for (byte b : macBytes) {
					//res1.append(Integer.toHexString(b & 0xFF));
					res1.append(String.format("%02X", (b & 0xFF)));
				}
/*
				if (res1.length() > 0) {
					res1.deleteCharAt(res1.length() - 1);
				}
*/
				return res1.toString().toUpperCase();
			}
		} catch (Exception ex) {
			//handle exception
		}
		return "";
	}

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

	private static String[] perms = {
			"android.permission.WRITE_SECURE_SETTINGS",
			"android.permission.INTERNET",
			"android.permission.ACCESS_NETWORK_STATE",
			"android.permission.VIBRATE",
			"Manifest.permission.READ_PHONE_STATE",
			"android.permission.WRITE_EXTERNAL_STORAGE",
			"android.permission.READ_PHONE_STATE",
			"Manifest.permission.READ_PHONE_STATE",
			"android.permission.BIND_INPUT_METHOD",
			// linphone
			"android.permission.RECORD_AUDIO",
			"android.permission.READ_CONTACTS",
			"android.permission.WRITE_CONTACTS",
			"android.permission.MODIFY_AUDIO_SETTINGS",
			"android.permission.WAKE_LOCK",
			"android.permission.PROCESS_OUTGOING_CALLS",
			"android.permission.CALL_PHONE",
			"android.permission.RECEIVE_BOOT_COMPLETED",
			"android.permission.VIBRATE",
			"android.permission.CAMERA",
			"android.permission.BLUETOOTH",
			"android.permission.BROADCAST_STICKY",
			"android.permission.GET_ACCOUNTS",
			"android.permission.CHANGE_WIFI_MULTICAST_STATE",
			"android.permission.READ_SYNC_SETTINGS",
			"android.permission.WRITE_SYNC_SETTINGS",
			"android.permission.AUTHENTICATE_ACCOUNTS",
	};

	public static final int PERMS_REQUEST_CODE = 200;

	public static void requestPermissions(Activity activity) {
		boolean permission = true;

		for (String perm : perms) {
			permission = (ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED);
			if (!permission) {
				ActivityCompat.requestPermissions(activity, perms, PERMS_REQUEST_CODE);
			}
		}
	}

	public static void createPath(String path) {
		File filePath = new File(path);

		if (!filePath.exists()) {
			try {
				filePath.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void deleteFile(String fileName) {
		File file = new File(fileName);

		if (file.exists()) {
			try {
				file.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String extraFileNameFromURL(String url) {
		Log.d("fileUrl = ",url);
		String result = "";
		int index = url.lastIndexOf("/");
		if (index >= 0) {
			result = url.substring(index + 1);
		}
		Log.d("urlResult = ",result);
		return result;
	}

	public static int downloadFile(Context context, String fileURL, String filePath) {
		int result = 0;

		try {
			URL url = new URL(fileURL);
			URLConnection con = url.openConnection();

			con.setDefaultUseCaches(false);
			con.setUseCaches(false);
			InputStream is = con.getInputStream();//url.openStream();

			DataInputStream dis = new DataInputStream(is);
			FileOutputStream fos = new FileOutputStream(new File(filePath), false);

			byte[] buffer = new byte[1024 * 10];
			int length;
			int totalLen = 0;

			while ((length = dis.read(buffer)) > 0) {
				fos.write(buffer, 0, length);
				totalLen += length;
			}

			result = totalLen;

            dis.close();
            fos.close();
		} catch (Exception e) {
			result = -1;
			e.printStackTrace();
			WebService.debug(context, "404 - " + fileURL);
			Log.d("downloadFile","downloadError"+fileURL);
		}
		if(result!=-1&&result!=0){
			Log.d("downloadFile","downloadSuccess"+fileURL);
		}
		return result;
	}

	public static Bitmap loadBitmapFromFile(String fileName) {
		Bitmap bitmap = null;

		try {
			// Get the dimensions of the bitmap
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(fileName, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;

			// Determine how much to scale down the image
			int scaleFactor = 1;//Math.min(photoW/targetW, photoH/targetH);

			// Decode the image file into a Bitmap sized to fill the View
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor << 1;
			bmOptions.inPurgeable = true;

			bitmap = BitmapFactory.decodeFile(fileName, bmOptions);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
		/*
		Bitmap result = null;

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inDither = true;

			result = BitmapFactory.decodeFile(fileName, options);
		} catch(Exception e) {
			e.printStackTrace();
		}

		return result;
		*/
	}
	public static long SIP_REGISTRY_TIME = -1;
	public static final String ITEM_PATH_NAME = "item";
	public static final int ITEM_DEMO_L1= 4;
	public static final int ITEM_DEMO_L2= 9;

	public static final String VIDEO_PATH_NAME = "video";
	public static final String ARTIST_PATH_NAME = "artist";

	public static String getFileBasePath(Context context) {
		return context.getFilesDir().getPath();
	}

	public static String getArtistPathName(Context context) {
		return getFileBasePath(context) + File.separator + ARTIST_PATH_NAME;
	}

	public static String getVideoPathName(Context context) {
		return getFileBasePath(context) + File.separator + VIDEO_PATH_NAME;
	}

	public static String getItemPathName(Context context) {
		return getFileBasePath(context) + File.separator + ITEM_PATH_NAME;
	}

	public static String getItemColorPathName(Context context) {
		return getFileBasePath(context) + File.separator + ITEM_PATH_NAME + File.separator + "color";
	}

	public static String getItemTypePathName(Context context) {
		return getFileBasePath(context) + File.separator + ITEM_PATH_NAME + File.separator + "type";
	}

	public static String getItemDemoPathName(Context context) {
		return getFileBasePath(context) + File.separator + ITEM_PATH_NAME + File.separator + "demo";
	}

	public static final String CAPTURE_PATH_NAME = "capture";

	public static String getAppCapturePathName(Context context) {
		return getFileBasePath(context) + File.separator + CAPTURE_PATH_NAME;
	}

	public static long unitTime() {
		long unixTime = System.currentTimeMillis() / 1000L;

		return unixTime;
	}

	private static final String FTP_SERVER_HOST = "59.120.223.150";
	private static final String FTP_USERNAME = "kiosk";
	private static final String FTP_PASSWORD = "HOKEIidyours";

	public static int uploadFile(Context context, String srcFile, String destFile) {
		int result = 0;

		FTPClient ftp = null;
		FTPClientConfig config = null;

		final boolean useEpsvWithIPv4 = false;

		try {
			ftp = new FTPClient();
			config = new FTPClientConfig();
			int reply;

			config.setUnparseableEntries(false);
			ftp.configure(config);
			ftp.connect(FTP_SERVER_HOST);
			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.err.println("FTP server refused connection.");
				return -2;
			}

			if (!ftp.login(FTP_USERNAME, FTP_PASSWORD)) {
				ftp.logout();
				return -3;
			}

			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.setCopyStreamListener(createListener());
			//ftp.enterLocalPassiveMode();
			ftp.setUseEPSVwithIPv4(useEpsvWithIPv4);

			InputStream input = new FileInputStream(srcFile);

			ftp.storeFile(destFile, input);
			input.close();

			ftp.logout();
			ftp.disconnect();
		} catch (Exception e) {
			if (ftp != null && ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			result = -1;
		}

		return result;
	}

	private static CopyStreamListener createListener(){
		return new CopyStreamListener(){
			private long megsTotal = 0;

			@Override
			public void bytesTransferred(CopyStreamEvent event) {
				bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
			}

			@Override
			public void bytesTransferred(long totalBytesTransferred,
										 int bytesTransferred, long streamSize) {
				long megs = totalBytesTransferred / 1000000;
				for (long l = megsTotal; l < megs; l++) {
					System.err.print("#");
				}
				megsTotal = megs;
			}
		};
	}

	public static void hideKeyboard(Activity activity) {
		if (activity == null) return;

		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && activity.getCurrentFocus() != null) {
			imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
		}

	}

	public static void hideKeyboard2(Activity activity) {
		if (activity == null) return;

		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && activity.getCurrentFocus() != null) {
			imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
			Log.d("KeyboardHide","FunctionCall");
		}
	}

	public static void sendBroadcast(Context context, String name) {
		if (Build.VERSION.SDK_INT >= 7) {
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(name));
		} else {
			context.sendBroadcast(new Intent(name));
		}
	}

	public static void sendBroadcast(Context context, Intent intent) {
		if (Build.VERSION.SDK_INT >= 7) {
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		} else {
			context.sendBroadcast(intent);
		}
	}

	public static void registerReceiver(Context context, BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
		if (Build.VERSION.SDK_INT >= 7) {
			LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
		} else {
			context.registerReceiver(broadcastReceiver, intentFilter);
		}
	}

	public static void unregisterReceiver(Context context, BroadcastReceiver broadcastReceiver) {
		if (Build.VERSION.SDK_INT >= 7) {
			LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
		} else {
			context.unregisterReceiver(broadcastReceiver);
		}
	}

	public static void startMainActivity(Context context) {
		startActivity(context, new Intent(context, MainActivity.class));
	}

	public static void gifImageButtonViewLoadImage(Context context, GifImageButtonView gif, String normalFileKey, String pressFileKey) {
		String locale = UserConfig.getKioskArtistLocale(context);
		String filePath = Global.getArtistPathName(context) + File.separator + locale;
		String filePath1 = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, normalFileKey);
		String filePath2 = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, pressFileKey);

		try {
			Drawable drawable1 = null;
			if (filePath1.lastIndexOf(".gif") >= 0) {
				drawable1 = new GifDrawable(new File(filePath1));
			} else {
				drawable1 = Drawable.createFromPath(filePath1);
			}
			gif.saveImageDrawable(drawable1);
			gif.setImageDrawable(drawable1);


			Drawable drawable2 = Drawable.createFromPath(filePath2);
			gif.saveImageBackroundDrawable(drawable2);
			gif.setBackground(null);
			//gif.setBackground(drawable2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void gifImageButtonViewLoadImage2(Context context, GifImageButtonView gif, String normalFileKey, String pressFileKey) {
		String filePath = Global.getArtistPathName(context) ;
		String filePath1 = filePath + File.separator + UserConfig.getKioskArtist(context, normalFileKey);
		String filePath2 = filePath + File.separator + UserConfig.getKioskArtist(context, pressFileKey);


		try {
			Drawable drawable1 = null;
			if (filePath1.lastIndexOf(".gif") >= 0) {
				drawable1 = new GifDrawable(new File(filePath1));
			} else {
				drawable1 = Drawable.createFromPath(filePath1);
			}
			gif.saveImageDrawable(drawable1);
			gif.setImageDrawable(drawable1);


			Drawable drawable2 = Drawable.createFromPath(filePath2);
			gif.saveImageBackroundDrawable(drawable2);
			gif.setBackground(null);
			//gif.setBackground(drawable2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void gifImageViewLoadImage(Context context, GifImageView gif, String fileKey) {
		String locale = UserConfig.getKioskArtistLocale(context);
		String filePath = Global.getArtistPathName(context) + File.separator + locale;
		String filePath1 = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, fileKey);

		try {
			Drawable drawable = new GifDrawable(new File(filePath1));
			gif.setImageDrawable(drawable);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//無需locale的圖片檔案
	public static void gifImageViewLoadImage2(Context context, GifImageView gif, String fileKey) {

		String filePath = Global.getArtistPathName(context) ;
		String filePath1 = filePath + File.separator + UserConfig.getKioskArtist(context, fileKey);

		try {
			Drawable drawable = new GifDrawable(new File(filePath1));
			gif.setImageDrawable(drawable);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void buttonBackgroundImage(Context context, Button button, String normalFileKey,String pressFileKey){
		String locale = UserConfig.getKioskArtistLocale(context);
		String filePath = Global.getArtistPathName(context) + File.separator + locale;
		String filePath1 = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, normalFileKey);
		String filePath2 = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, pressFileKey);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});
	}
	public static void gifImageButtonLoadImage(Context context, ImageButton button, String fileKey) {
		String locale = UserConfig.getKioskArtistLocale(context);
		String filePath = Global.getArtistPathName(context) + File.separator + locale;
		String filePath1 = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, fileKey);

		try {
			Drawable drawable1 = null;
			if (filePath1.lastIndexOf(".gif") >= 0) {
				drawable1 = new GifDrawable(new File(filePath1));
			} else {
				drawable1 = Drawable.createFromPath(filePath1);
			}
			button.setImageDrawable(drawable1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
