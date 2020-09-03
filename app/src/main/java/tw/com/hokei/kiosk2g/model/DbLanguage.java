package tw.com.hokei.kiosk2g.model;

import com.google.gson.annotations.SerializedName;

public class DbLanguage {


    @SerializedName("returnCode")
    public Number returnCode;

    @SerializedName("versionCode")
    public Number versionCode;

    @SerializedName("English")
    public Number english;

    @SerializedName("French")
    public Number french;

    @SerializedName("Deutsch")
    public Number deutsch;

    @SerializedName("Japanese")
    public Number japanese;

    @SerializedName("Chinese")
    public Number chinese;
}
