package tw.com.syscode.smartphone;

import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;

public final class Global {
	public static void handlerPost(Runnable runnable) {
		new Handler(Looper.getMainLooper()).post(runnable);
	}

	public static void handlerPostDelay(Runnable runnable, int delayMillis) {
		new Handler(Looper.getMainLooper()).postDelayed(runnable, delayMillis);
	}

	public static boolean isEmptyString(String value) {
		return (value == null || value.length() < 1);
	}
	
	public static String trimString(String value) {
		if (isEmptyString(value)) return "";
		
		value = value.trim();
		return value;
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void vibrate(Context context) {
		vibrate(context, 100);
	}

	public static void vibrate(Context context, long milliseconds) {
		Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);

		vibrator.vibrate(milliseconds);
	}
}
