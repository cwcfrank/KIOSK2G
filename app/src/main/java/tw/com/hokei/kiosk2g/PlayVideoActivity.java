package tw.com.hokei.kiosk2g;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

import tw.com.hokei.kiosk2g.database.UserConfig;
import tw.com.hokei.kiosk2g.model.DbArtist;

public class PlayVideoActivity extends CommonSensorActivity {
    private static final String TAG = PlayVideoActivity.class.getSimpleName();

    private VideoView videoView;
    private View maskView;

    private static boolean visible = false;

    public static boolean isVisible() {
        return visible;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        initViews();

        if (Global.isNetworkAvailable(context)) {
            new Thread() {
                public void run() {
                    String beginTime = UserConfig.getUserOpBeginTime(context);

                    if (!Global.isEmptyString(beginTime)) {
                        String endTime = Global.getTodayDateTimeString();

                        UserConfig.setUserOpBeginTime(context, "");
                        cancelIdleTimer();
                        WebService.kioskSpread(context, beginTime, endTime, UserConfig.getLastVisitPage(context));
                        startIdleTimer();
                    }
                }
            }.start();
        }

        hangupCusromer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        visible = true;

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
        visible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        visible = false;
        //Runtime.getRuntime().gc();
    }

    public void process() {
        if (!Global.isNetworkAvailable(context)) {
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    videoView.stopPlayback();
                    finish();
                    Global.startActivity(context, new Intent(context, SelectLanguageActivity.class));
                }
            });
            return;
        }

        showDummyAlertView();
        cancelIdleTimer();

        new Thread() {
            public void run() {
                int[] status = new int[1];
                int returnCode = WebService.kioskStatus(context, status);

                startIdleTimer();
                dismissDummyAlertView();

                if (returnCode == 0 && status[0] == 1) {
                    Log.d("kioskStatus","returnCode = "+returnCode);
                    Log.d("kioskStatus","status = "+status[0]);
                    Global.handlerPost(new Runnable() {
                        @Override
                        public void run() {
                            videoView.stopPlayback();
                            finish();
                            Global.startActivity(context, new Intent(context, SelectLanguageActivity.class));
                        }
                    });
                } else {

                        Log.d("kioskStatus", "returnCode = " + returnCode);
                        Log.d("kioskStatus", "status = " + status[0]);
                        Global.handlerPost(new Runnable() {
                            @Override
                            public void run() {
                                videoView.stopPlayback();
                                finish();
                                Global.startActivity(context, new Intent(context, OutOfServiceActivity.class));
                            }
                        });

                }
            }
        }.start();
    }

    @Override
    protected void initViews() {
        super.initViews();

        maskView = findViewById(R.id.maskView);
        maskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                process();
            }
        });

        videoView = findViewById(R.id.videoView);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        MediaController mediaController = new MediaController(this);

        mediaController.setAnchorView(videoView);
        mediaController.setVisibility(View.GONE);

        final String locale = DbArtist.EN_US;

        String videoFileName = UserConfig.getVideoFileName(context);
        Uri uri = null;
        if (!Global.isEmptyString(videoFileName)) {
            String videoFile = Global.getVideoPathName(context) + File.separator + UserConfig.getVideoFileName(context);
            uri = Uri.parse(videoFile);
        }

        videoView.setMediaController(mediaController);
        if (uri != null) {
            videoView.setVideoURI(uri);
        }
        videoView.requestFocus();
        //videoView.start();
    }

    private int nearCount = 0;

    @Override
    protected void sensorDataIncoming(int dataLeft, int dataRight, int dataTop, int dataUltrasnoic) {
        Log.i("SENSOR", "SENSOR : " + dataLeft + " , " + dataRight + " , " + dataTop + " , " + dataUltrasnoic);

        boolean nextActivity = false;

        if (dataLeft >= 1000) {
            nextActivity = true;
        }

        if (!nextActivity && dataRight >= 1000) {
            nextActivity = true;
        }

        if (!nextActivity && dataTop >= 1000) {
            nextActivity = true;
        }

        if (!nextActivity) {
            if (dataUltrasnoic != -1 && dataUltrasnoic < 80) {
                nearCount++;
                if (nearCount > 3) {
                    nextActivity = true;
                }
            } else {
                nearCount = 0;
            }
        }

        if (nextActivity) {
            final int dataLeftX = dataLeft;
            final int dataRightX = dataRight;
            final int dataTopX = dataTop;
            final int dataUltrasnoicX = dataUltrasnoic;
/*
            if (Global.isNetworkAvailable(context)) {
                new Thread() {
                    public void run() {
                        WebService.debug(context, "SENSOR : " + dataLeftX + " , " + dataRightX + " , " + dataTopX + " , " + dataUltrasnoicX);
                    }
                }.start();
            }
*/
            process();
        }
    }
}
