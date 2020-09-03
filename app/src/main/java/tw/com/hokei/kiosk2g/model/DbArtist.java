package tw.com.hokei.kiosk2g.model;

import com.google.gson.annotations.SerializedName;

public class DbArtist {
    private static final long serialVersionUID = -2737065103251426668L;

    public static final String EN_US = "en_rUS";
    public static final String FR_FR = "fr_rFR";
    public static final String JA_JP = "ja_rJP";
    public static final String DE_DE = "de_rDE";
    public static final String ZH_TW = "zh_rTW";

    @SerializedName("returnCode")
    public Number returnCode;

    @SerializedName("versionCode")
    public Number versionCode;

    @SerializedName("en-rUS")
    public DbArtistItem en_rUS;

    @SerializedName("fr-rFR")
    public DbArtistItem fr_rFR;

    @SerializedName("ja-rJP")
    public DbArtistItem ja_rJP;

    @SerializedName("de-rDE")
    public DbArtistItem de_rDE;

    @SerializedName("zh-rTW")
    public DbArtistItem zh_rTW;

    @SerializedName("english_button_normal")
    public String english_button_normal;

    @SerializedName("english_button_press")
    public String english_button_press;

    @SerializedName("french_button_normal")
    public String french_button_normal;

    @SerializedName("french_button_press")
    public String french_button_press;

    @SerializedName("deutsch_button_normal")
    public String deutsch_button_normal;

    @SerializedName("deutsch_button_press")
    public String deutsch_button_press;

    @SerializedName("japan_button_normal")
    public String japan_button_normal;

    @SerializedName("japan_button_press")
    public String japan_button_press;

    @SerializedName("chinese_button_normal")
    public String chinese_button_normal;

    @SerializedName("chinese_button_press")
    public String chinese_button_press;

    @SerializedName("language_background")
    public String language_background;

    @SerializedName("exit_button_normal")
    public String exit_button_normal;

    @SerializedName("exit_button_press")
    public String exit_button_press;

    @SerializedName("service_call_left")
    public String service_call_left;

    @SerializedName("service_chat_right")
    public String service_chat_right;

    @SerializedName("service_chat_callcenter")
    public String service_chat_callcenter;

    @SerializedName("service_chat_guest")
    public String service_chat_guest;

    @SerializedName("service_chat_text")
    public String service_chat_text;

    @SerializedName("service_chat_sent_normal")
    public String service_chat_sent_normal;

    @SerializedName("service_chat_sent_press")
    public String service_chat_sent_press;

    @SerializedName("photo_target_left")
    public String photo_target_left;

    @SerializedName("photo_target_right")
    public String photo_target_right;

    @SerializedName("photo_target_back")
    public String photo_target_back;

    @SerializedName("photo_target_front")
    public String photo_target_front;

    @SerializedName("photo_target_up")
    public String photo_target_up;

    @SerializedName("photo_target_down")
    public String photo_target_down;

    @SerializedName("photo_countdown_1")
    public String photo_countdown_1;

    @SerializedName("photo_countdown_2")
    public String photo_countdown_2;

    @SerializedName("photo_countdown_3")
    public String photo_countdown_3;

    @SerializedName("photo_countdown_4")
    public String photo_countdown_4;

    @SerializedName("photo_countdown_5")
    public String photo_countdown_5;
}
