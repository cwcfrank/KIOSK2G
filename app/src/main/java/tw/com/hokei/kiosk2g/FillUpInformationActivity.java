package tw.com.hokei.kiosk2g;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;
import tw.com.hokei.kiosk2g.database.UserConfig;

public class FillUpInformationActivity extends CommonSensorActivity {
    private static final String TAG = FillUpInformationActivity.class.getSimpleName();

    private EditText surnameEditText;
    private EditText nameEditText;
    //private EditText phoneCodeEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private CountryCodePicker countryCodePicker;
    private ImageButton termsPrivacyImageButton;
    private ImageButton termsPrivacyCheckBoxImageButton;

    private TextView surnameRequiredTextView;
    private TextView nameRequiredTextView;
    private TextView phoneRequiredTextView;
    private TextView emailRequiredTextView;
    private TextView termsPrivacyRequiredTextView;

    private TextView emailMessageTextView;
    private TextView phoneMessageTextView;

    private View termsPrivacyView;
    private GifImageButtonView termsPrivacyCloseImageButton;

    private GifImageButtonView confirmImageButton;
    private GifImageView customerServiceImageView;

    private ScrollView inputScrollView;
    private View inputView;
    private boolean emailETChk = false;
    private boolean termsPrivacyChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_up_information);

        UserConfig.setLastVisitPage(context, "6");
        initViews();
        setIdleHome(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Runtime.getRuntime().gc();
    }

    @Override
    public void refreshView() {
        showDummyAlertView22();
        Global.handlerPostDelay(new Runnable() {
            @Override
            public void run() {
                dismissDummyAlertView22();
            }
        }, 100);
    }

    private void clickSpace() {
        Global.hideKeyboard(activity);
        hideCustomKeyboard();
        hideTextServiceView();

        if (!checkRequired()) {
            /*
            Global.hideKeyboard(activity);
            if (customKeyboard != null) {
                customKeyboard.hideCustomKeyboard();
            }
            */

            if (phoneEditText.isFocused()) {
                checkPhone();
            } else if (emailEditText.isFocused()) {
                checkEMail();
            }
        }
    }

    private final int SCROLL_LENGTH = 2000;

    @Override
    protected void initViews() {
        super.initViews();

        View mainView = findViewById(R.id.mainView);

        final String locale = UserConfig.getKioskArtistLocale(context);
        final String filePath = Global.getArtistPathName(context) + File.separator + locale;

        String backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "member_background");
        mainView.setBackground(Drawable.createFromPath(backgroundImageFile));
        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSpace();
            }
        });

        ImageView inputScrollImageView = findViewById(R.id.inputScrollImageView);
        inputScrollImageView.setBackground(Drawable.createFromPath(backgroundImageFile));

        inputScrollView = findViewById(R.id.inputScrollView);
        inputScrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSpace();
            }
        });

        inputView = findViewById(R.id.inputView);
        inputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSpace();

                inputView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                inputScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        inputScrollView.scrollTo(0, 0);
                    }
                });

                if (emailEditText != null) {
                    if (emailEditText.isFocused()) {
                        emailEditText.clearFocus();
                    }
                }
            }
        });
        surnameEditText = findViewById(R.id.surnameEditText);
        nameEditText = findViewById(R.id.nameEditText);
        //phoneCodeEditText = findViewById(R.id.phoneCodeEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
            if(customKeyboard.customKeyboardChk()) {
                registerCustomKeyboard(R.id.surnameEditText);
                registerCustomKeyboard(R.id.nameEditText);
                registerCustomKeyboard(R.id.phoneEditText);
                registerCustomKeyboard(R.id.emailEditText);
            }

        customKeyboard.setOnKeyPressListener(new CustomKeyboard.OnKeyPressListener() {
            @Override
            public void onKeyPress(int primaryCode, int[] ints) {
                int keycode=-4;
                if(primaryCode==keycode){
                    clickSpace();
                    if(emailETChk==true){
                        emailEditText.clearFocus();
                        emailETChk = false;
                    }
                }
            }
        });
        surnameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkRequired();
                }
            }
        });

        nameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkRequired();
                }
            }
        });

        phoneEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkRequired();
                    checkPhone();

                }

                if (hasFocus) {
                    if(customKeyboard.customKeyboardChk()) {
                        customKeyboard.setLayout(R.xml.numberic_keyboard);
                        customKeyboard.showKeyboard(v);
                    }
                } else {
                    if(customKeyboard.customKeyboardChk()) {
                        customKeyboard.setLayout(mainKeyboardLayoutID);
                    }
                }
            }
        });

        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, final boolean hasFocus) {
                if (!hasFocus) {
                    checkRequired();
                    checkEMail();
                }

                if (hasFocus) {
                    inputView.getLayoutParams().height = SCROLL_LENGTH;
                } else {
                    inputView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                }

                Global.handlerPostDelay(new Runnable() {
                    @Override
                    public void run() {
                        if (hasFocus) {
                            emailETChk = true;
                            inputScrollView.post(new Runnable() {
                                @Override
                                public void run() {
                                    inputScrollView.scrollTo(0, 100);
                                }
                            });
                        } else {
                            emailETChk = false;
                            inputScrollView.post(new Runnable() {
                                @Override
                                public void run() {
                                    inputScrollView.scrollTo(0, 0);
                                }
                            });
                        }
                    }
                }, 500);
            }
        });


        countryCodePicker = findViewById(R.id.countryCodePicker);
        countryCodePicker.setSearchAllowed(false);

        surnameRequiredTextView = findViewById(R.id.surnameRequiredTextView);
        nameRequiredTextView = findViewById(R.id.nameRequiredTextView);
        phoneRequiredTextView = findViewById(R.id.phoneRequiredTextView);
        emailRequiredTextView = findViewById(R.id.emailRequiredTextView);
        termsPrivacyRequiredTextView = findViewById(R.id.termsPrivacyRequiredTextView);

        phoneMessageTextView = findViewById(R.id.phoneMessageTextView);
        emailMessageTextView = findViewById(R.id.emailMessageTextView);

        termsPrivacyImageButton = findViewById(R.id.termsPrivacyImageButton);
        termsPrivacyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                termsPrivacyChecked = !termsPrivacyChecked;
                if (termsPrivacyChecked) {
                    termsPrivacyCheckBoxImageButton.setImageResource(R.drawable.member_terms_privacy_checked);
                    showView2(termsPrivacyView, true);
                } else {
                    termsPrivacyCheckBoxImageButton.setImageResource(R.drawable.member_terms_privacy_unchecked);
                    showView2(termsPrivacyView, false);
                }
            }
        });

        termsPrivacyCheckBoxImageButton = findViewById(R.id.termsPrivacyCheckBoxImageButton);
        termsPrivacyCheckBoxImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                termsPrivacyChecked = !termsPrivacyChecked;
                if (termsPrivacyChecked) {
                    termsPrivacyCheckBoxImageButton.setImageResource(R.drawable.member_terms_privacy_checked);
                    showView2(termsPrivacyView, true);
                } else {
                    termsPrivacyCheckBoxImageButton.setImageResource(R.drawable.member_terms_privacy_unchecked);
                    showView2(termsPrivacyView, false);
                }
            }
        });

        termsPrivacyView = findViewById(R.id.termsPrivacyView);
        backgroundImageFile = filePath + File.separator + UserConfig.getKioskArtistItem(context, locale, "member_terms_privacy");
        termsPrivacyView.setBackground(Drawable.createFromPath(backgroundImageFile));
        termsPrivacyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showView2(termsPrivacyView, false);
            }
        });

        termsPrivacyCloseImageButton = findViewById(R.id.termsPrivacyCloseImageButton);
        Global.gifImageButtonViewLoadImage(context, termsPrivacyCloseImageButton, "member_terms_button_close_normal", "member_terms_button_close_press");
        termsPrivacyCloseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showView2(termsPrivacyView, false);
            }
        });

        surnameRequiredTextView.setVisibility(View.GONE);
        nameRequiredTextView.setVisibility(View.GONE);
        phoneRequiredTextView.setVisibility(View.GONE);
        emailRequiredTextView.setVisibility(View.GONE);
        termsPrivacyRequiredTextView.setVisibility(View.GONE);

        phoneMessageTextView.setVisibility(View.GONE);
        emailMessageTextView.setVisibility(View.GONE);

//        surnameEditText.setText("Benson");
//        nameEditText.setText("https://drive.google.com/open?id=1BiS_IB-p7-O5kr2yFIj7VeSIqjR5MO2O");
        countryCodePicker.setDefaultCountryUsingNameCode("TW");
        //phoneCodeEditText.setText("886");

//        phoneEditText.setText("0958201560");
//        emailEditText.setText("hunter1124@gmail.com");

        confirmImageButton = findViewById(R.id.confirmImageButton);
        Global.gifImageButtonViewLoadImage(context, confirmImageButton, "member_button_next_normal", "member_button_next_press");
        confirmImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDummyAlertView();
                Global.handlerPost(new Runnable() {
                    @Override
                    public void run() {
                        new Thread() {
                            public void run() {
                                register();
                                dismissDummyAlertView();
                            }
                        }.start();
                    }
                });
            }
        });

        //customerServiceImageView = findViewById(R.id.customerServiceImageView);
        if (customerServiceImageView != null) {
            customerServiceImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    private String[] emailMessage = new String[2];
    private  boolean processingCheckEMail = false;

    private void checkEMail() {
        if (!Global.isNetworkAvailable(context)) return;

        String email = emailEditText.getText().toString();

        email = Global.trimString(email);

        if (Global.isEmptyString(email)) {
            showView2(emailMessageTextView, false);
            return;
        }

        if (!processingCheckEMail) {
            new Thread() {
                public void run() {
                    processingCheckEMail = true;

                    String email = emailEditText.getText().toString();

                    email = Global.trimString(email);

                    int result =
                            WebService.kioskCheckemail(
                                    context,
                                    email,
                                    emailMessage);

                    if (result == 0) {
                        showView2(emailMessageTextView, false);
                    } else {
                        Global.handlerPost(new Runnable() {
                            @Override
                            public void run() {
                                showView2(emailMessageTextView, true);
                                emailMessageTextView.setText(emailMessage[0]);
                            }
                        });
                    }

                    processingCheckEMail = false;
                }
            }.start();
        }
    }

    private String[] phoneMessage = new String[2];
    private  boolean processingCheckPhone = false;

    private void checkPhone() {
        if (!Global.isNetworkAvailable(context)) return;

        String phone = phoneEditText.getText().toString();
        //String phoneCode = countryCodePicker.getSelectedCountryCode();

        phone = Global.trimString(phone);

        if (Global.isEmptyString(phone)) {
            showView2(phoneMessageTextView, false);
            return;
        }

        if (!processingCheckPhone) {
            new Thread() {
                public void run() {
                    processingCheckPhone = true;

                    String phone = phoneEditText.getText().toString();
                    String phoneCode = countryCodePicker.getSelectedCountryCode();

                    phone = Global.trimString(phone);

                    int result =
                            WebService.kioskCheckphone(
                                    context,
                                    phoneCode,
                                    phone,
                                    phoneMessage);

                    if (result == 0) {
                        showView2(phoneMessageTextView, false);
                    } else {
                        Global.handlerPost(new Runnable() {
                            @Override
                            public void run() {
                                showView2(phoneMessageTextView, true);
                                phoneMessageTextView.setText(phoneMessage[0]);
                            }
                        });
                    }

                    processingCheckPhone = false;
                }
            }.start();
        }
    }

    private boolean checkRequired() {
        showView2(surnameRequiredTextView, false);
        showView2(nameRequiredTextView, false);
        showView2(phoneRequiredTextView, false);
        showView2(emailRequiredTextView, false);
        showView2(termsPrivacyRequiredTextView, false);

        String surname = surnameEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String phoneCode = countryCodePicker.getSelectedCountryCode();
        String phone = phoneEditText.getText().toString();
        String email = emailEditText.getText().toString();

        surname = Global.trimString(surname);
        name = Global.trimString(name);
        phoneCode = Global.trimString(phoneCode);
        phone = Global.trimString(phone);
        email = Global.trimString(email);

        boolean required = false;

        if (Global.isEmptyString(surname)) {
            showView2(surnameRequiredTextView, true);
            required = true;
        }

        if (Global.isEmptyString(name)) {
            showView2(nameRequiredTextView, true);
            required = true;
        }

        if (Global.isEmptyString(phoneCode)) {
            if (Global.isEmptyString(email)) {
                showView2(phoneRequiredTextView, true);
                required = true;
            }
        }

        if (Global.isEmptyString(phone)) {
            if (Global.isEmptyString(email)) {
                showView2(phoneRequiredTextView, true);
                required = true;
            }
        }

        if (Global.isEmptyString(email)) {
            if (Global.isEmptyString(phone)) {
                showView2(emailRequiredTextView, true);
                required = true;
            }
        }

        if (!termsPrivacyChecked) {
            showView2(termsPrivacyRequiredTextView, true);
            required = true;
        }

        return required;
    }

    private String[] message = new String[2];

    private void register() {
        String surname = surnameEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String phoneCode = countryCodePicker.getSelectedCountryCode();
        String phoneCountry = countryCodePicker.getSelectedCountryEnglishName();
        String email = emailEditText.getText().toString();


        surname = Global.trimString(surname);
        name = Global.trimString(name);
        phoneCode = Global.trimString(phoneCode);
        phone = Global.trimString(phone);
        email = Global.trimString(email);
        Log.d("registerDataParam","surname = "+surname);
        Log.d("registerDataParam","name = "+name);
        Log.d("registerDataParam","phoneCode = "+phoneCode);
        Log.d("registerDataParam","phone = "+phone);
        Log.d("registerDataParam","phoneCountry = "+phoneCountry);
        Log.d("registerDataParam","email = "+email);

        if (checkRequired()) return;

        if (!Global.isNetworkAvailable(context)) {

            makeTempRegister(surname, name, phoneCountry, phoneCode, phone, email);
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    finish();
                    Global.startActivity(context, new Intent(context, SampleOrderActivity.class));
                }
            });
            return;
        }

        showView2(phoneMessageTextView, false);
        showView2(emailMessageTextView, false);
        cancelIdleTimer();
        if(Global.isEmptyString(phone)){
            phoneCode="";
            phoneCountry="";
        }

        String[] memberCode = new String[2];
        int result =
                WebService.kioskRegister(
                        context,
                        surname,
                        name,
                        phoneCountry,
                        phoneCode,
                        phone,
                        email,
                        message,
                        memberCode);

        startIdleTimer();

        if (result == 0) {
            UserConfig.setRegisterMemberCode(context, memberCode[0]);
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    finish();
                    Global.startActivity(context, new Intent(context, SampleOrderActivity.class));
                }
            });
        } else if (result == 1) {
            Global.alert(context, R.string.verification_failed);
        } else if (result == 2) {
            Global.alert(context, R.string.incomplete_parameters_passed_in);
        } else if (result == 3) {
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    showView2(phoneMessageTextView, true);
                    phoneMessageTextView.setText(message[0]);
                }
            });
        } else if (result == 4) {
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    showView2(emailMessageTextView, true);
                    emailMessageTextView.setText(message[0]);
                }
            });
        } else if (result == 5) {
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    showView2(phoneMessageTextView, true);
                    phoneMessageTextView.setText(message[0]);
                }
            });
        } else if (result == 6) {
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    showView2(emailMessageTextView, true);
                    emailMessageTextView.setText(message[0]);
                }
            });
        } else {
            makeTempRegister(surname, name, phoneCountry, phoneCode, phone, email);
            Global.handlerPost(new Runnable() {
                @Override
                public void run() {
                    releaseUsbSensor();
                    finish();
                    Global.startActivity(context, new Intent(context, SampleOrderActivity.class));
                }
            });
        }
    }

    private void makeTempRegister(String surname, String name, String phoneCountry, String phoneCode, String phone, String email) {
        UserConfig.setMemberOrderFirstName(context, surname);
        UserConfig.setMemberOrderLastName(context, name);
        UserConfig.setMemberOrderPhoneCounrty(context, phoneCountry);
        UserConfig.setMemberOrderPhoneCode(context, phoneCode);
        UserConfig.setMemberOrderPhone(context, phone);
        UserConfig.setMemberOrderEMail(context, email);

        /*
        DbTempRegister temp = MainDB.getTempRegisterWithEMail(context, email);
        if (!Global.isEmptyString(temp.email)) {
            temp.firstName = surname;
            temp.lastName = surname;
            temp.phoneCountry = phoneCountry;
            temp.phoneCode = phoneCode;
            temp.phone = phone;
            MainDB.updateItemWithTempRegister(context, temp);
        } else {
            DbTempRegister db = new DbTempRegister();

            db.firstName = surname;
            db.lastName = surname;
            db.phoneCountry = phoneCountry;
            db.phoneCode = phoneCode;
            db.phone = phone;
            db.email = email;
            MainDB.newItemWithTempRegister(context, db);
        }

        UserConfig.setRegisterMemberCode(context, email);
        */
    }

    public void closeAllDialog() {
        super.closeAllDialog();

        countryCodePicker.closeCountrySelectionDialog();
        //countryCodePicker.launchCountrySelectionDialog();
        //countryCodePicker.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        //countryCodePicker.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
    }

    @Override
    public void onKeyboardShow(int editTxtId) {
        super.onKeyboardShow(editTxtId);

        if (editTxtId == R.id.emailEditText) {

        }
    }
}
