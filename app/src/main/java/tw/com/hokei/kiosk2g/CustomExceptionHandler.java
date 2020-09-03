package tw.com.hokei.kiosk2g;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by hsu on 2017/4/5.
 */

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Thread.UncaughtExceptionHandler defaultUEH;

    public CustomExceptionHandler(Thread.UncaughtExceptionHandler defaultUEH) {
        this.defaultUEH = defaultUEH;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StackTraceElement[] arr = e.getStackTrace();
        String Raghav = t.toString();
        String report = e.toString()+"\n\n";
        report += "--------- Stack trace ---------\n\n"+Raghav;
        for (int i=0; i<arr.length; i++)
        {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "-------------------------------\n\n";

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i=0; i<arr.length; i++)
            {
                report += "    "+arr[i].toString()+"\n";
            }
        }
        report += "-------------------------------\n\n";

        final String errMsg = report;

        if (Global.currentContext != null) {
            if (Global.isNetworkAvailable(Global.currentContext)) {
                new Thread() {
                    public void run() {
                        WebService.debug(Global.currentContext, errMsg);
                        WebService.debugHOKEI(Global.currentContext, errMsg);
                        autoRestart(Global.currentContext);
                    }
                }.start();
            }

            //autoRestart(Global.currentContext);
        }

        defaultUEH.uncaughtException(t, e);
    }

    private void autoRestart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        context.startActivity(intent);

        AlarmManager mgr = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        //activity.finish();
        System.exit(2);
    }
}
