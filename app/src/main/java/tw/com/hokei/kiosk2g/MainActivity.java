package tw.com.hokei.kiosk2g;

import android.content.Intent;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;

import tw.com.hokei.kiosk2g.database.MainDB;
import tw.com.hokei.kiosk2g.model.DbTextService;

public class MainActivity extends CommonActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        showWaitAnimationView();

        new Thread() {
            public void run() {


                try {
                    Global.apiNetworkChk(context);
                    DataProcess.processItemData(context);
                    WebService.kioskLanguage(context);
                    WebService.kioskArtist(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                dismissWaitAlertView();
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(context, PlayVideoActivity.class);
                        //Intent intent = new Intent(context, SelectLanguageActivity.class);
                        //Intent intent = new Intent(context, HomeActivity.class);
                        //Intent intent = new Intent(context, DemoActivity.class);
                        //Intent intent = new Intent(context, FillUpInformationActivity.class);
                        //Intent intent = new Intent(context, PhotoValidationActivity.class);
                        //Intent intent = new Intent(context, OrderCompletedActivity.class);
                        //Intent intent = new Intent(context, SampleOrderActivity.class);
                        //Intent intent = new Intent(context, BranchActivity.class);
                        //Intent intent = new Intent(context, TakingPicturesActivity.class);
                        finish();
                        Global.startActivity(context, intent);
                    }
                });
            }
        }.start();
/*
        DbTextService db = new DbTextService();
        db.content = "User " + System.currentTimeMillis();
        db.type = 2;
        db.readed = 1;
        MainDB.newItemWithTextService(context, db);

        db.content = "Center " + System.currentTimeMillis();
        db.type = 1;
        db.readed = 0;
        MainDB.newItemWithTextService(context, db);
*/
        MainDB.deleteTextService(context);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
