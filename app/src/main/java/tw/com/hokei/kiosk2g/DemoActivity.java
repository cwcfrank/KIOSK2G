package tw.com.hokei.kiosk2g;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.MainDB;
import tw.com.hokei.kiosk2g.database.UserConfig;
import tw.com.hokei.kiosk2g.model.DbItemColor;
import tw.com.hokei.kiosk2g.model.DbItemDemo;
import tw.com.hokei.kiosk2g.model.DbItemType;

public class DemoActivity extends CommonSensorActivity implements DemoTypeAdapter.SelectItem2TypeAdapterDelegate {
    private List<DbItemColor> itemColorArray;
    private List<DbItemType> itemTypeArray;
    private List<DbItemDemo> itemDemoArray;

    private RecyclerView typeRecyclerView;
    private DemoTypeAdapter typeAdapter;

    private ListView colorListView;
    private DemoColorAdapter colorListAdapter;

    private GifImageButtonView returnImageButton;
    private GifImageView customerServiceImageView;

    private ImageButton typeLeftImageButton;
    private ImageButton typeRightImageButton;
    private ImageButton typeUpImageButton;
    private ImageButton typeDownImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        itemColorArray = MainDB.getItemColorList(context);
        itemTypeArray = MainDB.getItemTypeList(context);
        itemDemoArray = MainDB.getItemDemoList(context);

        UserConfig.setLastVisitPage(context, "3");
        initViews();
        setIdleHome(true);

        UserConfig.setOrderItemType(context, 0);
        UserConfig.setOrderItemColor(context, 0);
    }

    @Override
    protected void onDestroy() {
        Glide.get(context).clearMemory();
        super.onDestroy();
        //Runtime.getRuntime().gc();
    }

    @SuppressLint("NewApi")
    @Override
    protected void initViews() {
        super.initViews();

        View mainView = findViewById(R.id.mainView);

        final String locale = UserConfig.getKioskArtistLocale(context);
        final String filePath = Global.getArtistPathName(context) + File.separator + locale;

        String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "demo_background");
        mainView.setBackground(Drawable.createFromPath(backgroundImageFile));

        ImageView faceCoverImageView = findViewById(R.id.faceCoverImageView);
        backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "demo_360touch");
        faceCoverImageView.setImageDrawable(Drawable.createFromPath(backgroundImageFile));

        typeLeftImageButton = findViewById(R.id.typeLeftImageButton);
        typeLeftImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                int x = typeRecyclerView.getScrollX() - 10;

                if (x < 0) x = 0;
                typeRecyclerView.scrollTo(x, typeRecyclerView.getScrollY());
                */
            }
        });

        typeRightImageButton = findViewById(R.id.typeRightImageButton);
        typeRightImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //typeRecyclerView.scrollTo(typeRecyclerView.getScrollX() + 200, typeRecyclerView.getScrollY());
            }
        });

        typeUpImageButton = findViewById(R.id.typeUpImageButton);
        typeUpImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        typeDownImageButton = findViewById(R.id.typeDownImageButton);
        typeDownImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        typeRecyclerView = findViewById(R.id.typeRecyclerView);
        typeAdapter = new DemoTypeAdapter(itemTypeArray);
        typeAdapter.selectedIndex = UserConfig.getOrderItemType(context);
        typeAdapter.delegate = this;
        typeRecyclerView.setAdapter(typeAdapter);
        typeRecyclerView.setHasFixedSize(true);
        typeRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                updateTypeArrows();
            }
        });

        colorListAdapter = new DemoColorAdapter(context, MainDB.getItemColorList_Curosr(context));
        colorListAdapter.selectedIndex = UserConfig.getOrderItemColor(context);
        colorListView = (ListView)findViewById(R.id.colorListView);
        Global.handlerPost(new Runnable() {
            @Override
            public void run() {
                colorListView.setAdapter(colorListAdapter);
            }
        });

        colorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                showDummyAlertView();
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(context).clearMemory();
                        Runtime.getRuntime().gc();
                        colorListAdapter.selectedIndex = position;
                        UserConfig.setOrderItemColor(context, position);
                        colorListAdapter.notifyDataSetChanged();
                        reloadRotateImages();
                        dismissDummyAlertView(1000);
                    }
                });
            }
        });

        colorListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                boolean toBottom = false;

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                        && (colorListView.getLastVisiblePosition() - colorListView.getHeaderViewsCount() -
                        colorListView.getFooterViewsCount()) >= (colorListAdapter.getCount() - 1)) {

                    toBottom = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                updateColorArrows();
            }
        });

        returnImageButton = findViewById(R.id.returnImageButton);
        Global.gifImageButtonViewLoadImage(context, returnImageButton, "demo_button_return_normal", "demo_button_return_press");
        returnImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDummyAlertView();
                Global.handlerPostDelay(new Runnable() {
                    @Override
                    public void run() {
                        releaseUsbSensor();
                        Glide.get(context).clearMemory();
                        Runtime.getRuntime().gc();
                        finish();
                        Global.startActivity(context, new Intent(context, BranchActivity.class));
                        //dismissDummyAlertView();
                    }
                }, 500);
            }
        });

        updateTypeArrows();
        updateColorArrows();

        //customerServiceImageView = findViewById(R.id.customerServiceImageView);
        if (customerServiceImageView != null) {
            customerServiceImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        faceRingImageView = findViewById(R.id.faceRingImageView);
        faceRingImageView.setOnTouchListener(onfaceRingImageViewTouchListener);

        loadImages();
    }

    private void updateColorArrows() {
        /*
        if (colorListView.canScrollVertically(-1)) {
            typeUpImageButton.setVisibility(View.VISIBLE);
        } else {
            typeUpImageButton.setVisibility(View.GONE);
        }

        if (colorListView.canScrollVertically(1)) {
            typeDownImageButton.setVisibility(View.VISIBLE);
        } else {
            typeDownImageButton.setVisibility(View.GONE);
        }
        */
    }

    private void updateTypeArrows() {
        /*
        if (typeRecyclerView.canScrollHorizontally(-1)) {
            typeLeftImageButton.setVisibility(View.VISIBLE);
        } else {
            typeLeftImageButton.setVisibility(View.GONE);
        }

        if (typeRecyclerView.canScrollHorizontally(1)) {
            typeRightImageButton.setVisibility(View.VISIBLE);
        } else {
            typeRightImageButton.setVisibility(View.GONE);
        }
        */
    }

    public void onTypeItemClick(final int position) {
        showDummyAlertView();
        Global.handlerPost(new Runnable() {
            @Override
            public void run() {
                Glide.get(context).clearMemory();
                Runtime.getRuntime().gc();

                UserConfig.setOrderItemType(context, position);
                typeAdapter.selectedIndex = position;
                typeAdapter.notifyDataSetChanged();

                reloadRotateImages();
                dismissDummyAlertView(1000);
            }
        });
    }

    /**
     * 只有在RingImageView的範圍內滑動, 才有旋轉圖片的效果
     */
    private View.OnTouchListener onfaceRingImageViewTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            /** 获取当前输入点的X Y坐标(视图坐标) */
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();

            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:                                                        // 处理按下事件
                    currentX = x;
                    currentY = y;
                    break;

                case MotionEvent.ACTION_MOVE:

                    /** 处理上下滑動事件 */
                    currentDistanceY = y - currentY;
                    if (currentDistanceY > 50) {
                        currentY = y;
                        currentFaceRingArrayNumber = currentFaceRingArrayNumber + 1;
                        switch (currentFaceRingArrayNumber % 5) {
                            case 0:
                                currentFaceRingArray = faceRingArray0;
                                break;
                            case 1:
                                currentFaceRingArray = faceRingArray1;
                                break;
                            case 2:
                                currentFaceRingArray = faceRingArray2;
                                break;
                            case 3:
                                currentFaceRingArray = faceRingArray3;
                                break;
                            case 4:
                                currentFaceRingArray = faceRingArray4;
                                break;
                        }

                        RequestOptions requestOptions1 = new RequestOptions();

                        requestOptions1.placeholder(faceRingImageView.getDrawable());
                        requestOptions1.dontAnimate();

                        Glide.with(context)
                                .load(currentFaceRingArray[distance])
                                .apply(requestOptions1)
                                .into(faceRingImageView);
                    }
                    if (currentDistanceY < -50) {
                        currentY = y;
                        currentFaceRingArrayNumber = currentFaceRingArrayNumber - 1;
                        switch (currentFaceRingArrayNumber % 5) {
                            case 0:
                                currentFaceRingArray = faceRingArray0;
                                break;
                            case 1:
                                currentFaceRingArray = faceRingArray1;
                                break;
                            case 2:
                                currentFaceRingArray = faceRingArray2;
                                break;
                            case 3:
                                currentFaceRingArray = faceRingArray3;
                                break;
                            case 4:
                                currentFaceRingArray = faceRingArray4;
                                break;
                        }

                        RequestOptions requestOptions1 = new RequestOptions();

                        requestOptions1.placeholder(faceRingImageView.getDrawable());
                        requestOptions1.dontAnimate();

                        Glide.with(context)
                                .load(currentFaceRingArray[distance])
                                .apply(requestOptions1)
                                .into(faceRingImageView);
                    }

                    /** 处理左右滑動事件 */
                    currentDistanceX = currentX - x;
                    if (currentDistanceX > 50) {
                        currentX = x;
                        distance = distance + 1;
                        distance = distance % 10;

                        RequestOptions requestOptions1 = new RequestOptions();

                        requestOptions1.placeholder(faceRingImageView.getDrawable());
                        requestOptions1.dontAnimate();

                        Glide.with(context)
                                .load(currentFaceRingArray[distance])
                                .apply(requestOptions1)
                                .into(faceRingImageView);
                    }
                    if (currentDistanceX < -50) {
                        currentX = x;
                        distance = distance - 1;
                        distance = distance % 10;
                        if (distance < 0) {
                            distance = distance + 10;
                        }

                        RequestOptions requestOptions1 = new RequestOptions();

                        requestOptions1.placeholder(faceRingImageView.getDrawable());
                        requestOptions1.dontAnimate();

                        Glide.with(context)
                                .load(currentFaceRingArray[distance])
                                .apply(requestOptions1)
                                .into(faceRingImageView);
                    }
                    break;

                case MotionEvent.ACTION_UP:                                                            // 处理离开事件
                    break;
            }
            return true;
        }
    };

    private void loadImages() {
        //////////////////////////////////////
        // Rotate Images
        //////////////////////////////////////

        reloadRotateImages();
    }

    private synchronized void reloadRotateImages() {
        int typeSelected = UserConfig.getOrderItemType(context);
        int colorSelected = UserConfig.getOrderItemColor(context);

        if (typeSelected >= itemDemoArray.size()) return;
        if (colorSelected >= itemColorArray.size()) return;

        DbItemDemo dbItemDemo = itemDemoArray.get(typeSelected);
        DbItemColor dbItemColor = itemColorArray.get(colorSelected);

        String itemDemoPath = Global.getItemDemoPathName(context);
        String itemDemoPath2 = itemDemoPath + File.separator + dbItemDemo.path;
        String itemDemoColorPath = itemDemoPath2 + File.separator + dbItemColor.color;

        File imgFile;
        Bitmap myBitmap;

        for (int i = 0 ; i <= Global.ITEM_DEMO_L1 ; i++) {
            for (int j = 0 ; j <= Global.ITEM_DEMO_L2 ; j++) {
                String fileName = i + "_" + j + ".png";
                String demoImageFile = itemDemoColorPath + File.separator + fileName;

                //Bitmap bmp = Global.loadBitmapFromFile(demoImageFile);

                if (i == 0) {
                    faceRingArray0[j] = demoImageFile;
                } else if (i == 1) {
                    faceRingArray1[j] = demoImageFile;
                } else if (i == 2) {
                    faceRingArray2[j] = demoImageFile;
                } else if (i == 3) {
                    faceRingArray3[j] = demoImageFile;
                } else if (i == 4) {
                    faceRingArray4[j] = demoImageFile;
                }

                Log.i("KIOSK", "ROTATE FILE : " + demoImageFile);

                /** 如果想知道圖片來源的解析度..等 */
                // imgFile = new File(demoImageFile);
                // if (imgFile.exists()) {
                //     myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                //     Log.v("more", "myBitmap.getConfig(): " + myBitmap.getConfig());
                //     Log.v("more", "myBitmap.getDensity(): " + myBitmap.getDensity());
                //     Log.v("more", "myBitmap.getHeight(): " + myBitmap.getHeight());
                //     Log.v("more", "myBitmap.getWidth(): " + myBitmap.getWidth());
                // }
            }
        }

        /** 圖片相關數據初始化, 讓人臉正中朝前 */
        currentFaceRingArray = faceRingArray2;
        distance = 6;
        currentFaceRingArrayNumber = 10002;
        CustomTouchEvent.setMoveToRight2(51, faceRingImageView);

        /** 如果想事先緩存 */
        isCachedInAdvance = false;
        if (isCachedInAdvance == true) {
            cachedInAdvance();
        } else {
            dismissWaitAlertView();
        }
    }

    private int distance;
    private ImageView faceRingImageView;
    private int currentFaceRingArrayNumber;
    private int currentX;
    private int currentDistanceX;
    private int currentY;
    private int currentDistanceY;
    private boolean isCachedInAdvance;

    private String[] currentFaceRingArray;
    private String[] faceRingArray0 = new String[10];
    private String[] faceRingArray1 = new String[10];
    private String[] faceRingArray2 = new String[10];
    private String[] faceRingArray3 = new String[10];
    private String[] faceRingArray4 = new String[10];

    /**
     * 提前緩存60張圖片
     */
    public void cachedInAdvance() {

        faceRingImageView.setVisibility(View.INVISIBLE);
        showWaitAlertView();

        /** faceRingArray3 */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 100);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 200);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 300);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 400);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 500);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 600);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 700);

        /** faceRingArray4 */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToBottom(51, (Activity) context);
            }
        }, 800);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 900);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 1000);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 1100);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 1200);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 1300);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 1400);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 1500);

        /** faceRingArray5 */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToBottom(51, (Activity) context);
            }
        }, 1600);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 1700);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 1800);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 1900);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 2000);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 2100);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 2200);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 2300);

        /** faceRingArray6 */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToBottom(51, (Activity) context);
            }
        }, 2400);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 2500);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 2600);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 2700);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 2800);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 2900);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 3000);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 3100);

        /** faceRingArray0 */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToBottom(51, (Activity) context);
            }
        }, 3200);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 3300);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 3400);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 3500);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 3600);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 3700);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 3800);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 3900);

        /** faceRingArray1 */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToBottom(51, (Activity) context);
            }
        }, 4000);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 4100);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 4200);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 4300);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 4400);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 4500);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 4600);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 4700);

        /** faceRingArray2 */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToBottom(51, (Activity) context);
            }
        }, 4800);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 4900);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 5000);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 5100);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 5200);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 5300);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 5400);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 5500);

        /** faceRingArray3 */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToBottom(51, (Activity) context);
            }
        }, 5600);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 5700);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 5800);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 5900);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 6000);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 6100);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 6200);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CustomTouchEvent.setMoveToRight(51, (Activity) context);
            }
        }, 6300);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                dismissWaitAlertView();
            }
        }, 6400);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                faceRingImageView.setVisibility(View.VISIBLE);
            }
        }, 6500);
    }
}
