package tw.com.hokei.kiosk2g;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;

import tw.com.hokei.kiosk2g.database.UserConfig;
import tw.com.hokei.kiosk2g.model.DbArtist;
import tw.com.hokei.kiosk2g.model.DbLanguage;

public class SelectLanguageActivity extends CommonSensorActivity {
    private static final String TAG = SelectLanguageActivity.class.getSimpleName();
    int englishCode = -1;
    int frenchCode = -1;
    int deutschCode = -1;
    int japaneseCode = -1;
    int chineseCode = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_language);
        checkLanguage();
        initViews();
        setIdleHome(true);
    }

    public void checkLanguage(){



        englishCode = UserConfig.getKioskLanguage(context,"english");
        frenchCode = UserConfig.getKioskLanguage(context,"french");
        deutschCode = UserConfig.getKioskLanguage(context,"deutsch");
        japaneseCode = UserConfig.getKioskLanguage(context,"japanese");
        chineseCode = UserConfig.getKioskLanguage(context,"chinese");

//        englishCode = 0;
//        frenchCode = 0;
//        deutschCode = 0;
//        japaneseCode = 0;
//        chineseCode = 1;

        Log.d("Language","English = "+ englishCode);
        Log.d("Language","French = "+ frenchCode);
        Log.d("Language","Deutsch = "+ deutschCode);
        Log.d("Language","Japanese = "+ japaneseCode);
        Log.d("Language","Chinese = "+ chineseCode);

        if(checkOnlyLanguage(englishCode)){
            showDummyAlertView();
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    UserConfig.setKioskArtistLocale(context, DbArtist.EN_US);
                    finish();
                    Global.startActivity(context, new Intent(context, HomeActivity.class));
                }
            });

        }else if(checkOnlyLanguage(frenchCode)){
            showDummyAlertView();
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    UserConfig.setKioskArtistLocale(context, DbArtist.FR_FR);
                    finish();
                    Global.startActivity(context, new Intent(context, HomeActivity.class));
                }
            });

        }else if(checkOnlyLanguage(deutschCode)){
            showDummyAlertView();
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    UserConfig.setKioskArtistLocale(context, DbArtist.DE_DE);
                    finish();
                    Global.startActivity(context, new Intent(context, HomeActivity.class));
                }
            });

        }else if (checkOnlyLanguage(japaneseCode)){
            showDummyAlertView();
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    UserConfig.setKioskArtistLocale(context, DbArtist.JA_JP);
                    finish();
                    Global.startActivity(context, new Intent(context, HomeActivity.class));
                }
            });

        }else if(checkOnlyLanguage(chineseCode)){
            showDummyAlertView();
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    UserConfig.setKioskArtistLocale(context, DbArtist.ZH_TW);
                    finish();
                    Global.startActivity(context, new Intent(context, HomeActivity.class));
                }
            });
        }






    }

    public boolean checkOnlyLanguage(int languageCode){
        boolean onlyOne = false;
        int resultCode = englishCode+frenchCode+deutschCode+japaneseCode+chineseCode;
       if(resultCode == languageCode){
           onlyOne = true;

       }

        return  onlyOne;
    }
    @Override
    protected void initViews() {
        super.initViews();
        View mainView = findViewById(R.id.mainView);
        final String locale = UserConfig.getKioskArtistLocale(context);
        String filePath = Global.getArtistPathName(context)  ;

        String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtist(context ,"language_background");
        Drawable drawable = Drawable.createFromPath(backgroundImageFile);
        if(drawable!=null){
            mainView.setBackground(drawable);
        }

        GifImageButtonView englishImageButton = findViewById(R.id.englishImageButton);
        Global.gifImageButtonViewLoadImage2(context, englishImageButton, "english_button_normal", "english_button_press");
        if(englishCode == 1){
            englishImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDummyAlertView();
                    Global.handlerPost(new Runnable() {
                        @Override
                        public void run() {
                            releaseUsbSensor();
                            UserConfig.setKioskArtistLocale(context, DbArtist.EN_US);
                            finish();
                            Global.startActivity(context, new Intent(context, HomeActivity.class));
                        }
                    });
                }

            });

        }else {
            englishImageButton.setVisibility(View.GONE);
        }

        GifImageButtonView frenchImageButton = findViewById(R.id.frenchImageButton);
        Global.gifImageButtonViewLoadImage2(context, frenchImageButton, "french_button_normal", "french_button_press");
       if(frenchCode == 1) {
            if (frenchImageButton != null) {

                frenchImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDummyAlertView();
                        Global.handlerPost(new Runnable() {
                            @Override
                            public void run() {
                                releaseUsbSensor();
                                UserConfig.setKioskArtistLocale(context, DbArtist.FR_FR);
                                finish();
                                Global.startActivity(context, new Intent(context, HomeActivity.class));
                            }
                        });
                    }
                });
            }
        }else {
           frenchImageButton.setVisibility(View.GONE);
       }

        GifImageButtonView  germanImageButton = findViewById(R.id.germanImageButton);
        Global.gifImageButtonViewLoadImage2(context, germanImageButton, "deutsch_button_normal", "deutsch_button_press");
        if(deutschCode == 1) {
            germanImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDummyAlertView();
                    Global.handlerPost(new Runnable() {
                        @Override
                        public void run() {
                            releaseUsbSensor();
                            UserConfig.setKioskArtistLocale(context, DbArtist.DE_DE);
                            finish();
                            Global.startActivity(context, new Intent(context, HomeActivity.class));
                        }
                    });
                }
            });
        }else {
            germanImageButton.setVisibility(View.GONE);
        }


        GifImageButtonView japanImageButton = findViewById(R.id.japanImageButton);
        Global.gifImageButtonViewLoadImage2(context, japanImageButton, "japan_button_normal", "japan_button_press");
        if(japaneseCode == 1) {
            japanImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDummyAlertView();
                    Global.handlerPost(new Runnable() {
                        @Override
                        public void run() {
                            releaseUsbSensor();
                            UserConfig.setKioskArtistLocale(context, DbArtist.JA_JP);
                            finish();
                            Global.startActivity(context, new Intent(context, HomeActivity.class));
                        }
                    });
                }
            });
        }else {
            japanImageButton.setVisibility(View.GONE);
        }

        GifImageButtonView chineseImageButton = findViewById(R.id.chineseImageButton);
        Global.gifImageButtonViewLoadImage2(context, chineseImageButton, "chinese_button_normal", "chinese_button_press");
        if(chineseCode == 1){
            chineseImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDummyAlertView();
                    Global.handlerPost(new Runnable() {
                        @Override
                        public void run() {
                            releaseUsbSensor();
                            UserConfig.setKioskArtistLocale(context, DbArtist.ZH_TW);
                            finish();
                            Global.startActivity(context, new Intent(context, HomeActivity.class));
                        }
                    });
                }
            });
        }else {
            chineseImageButton.setVisibility(View.GONE);
        }
    }
}
