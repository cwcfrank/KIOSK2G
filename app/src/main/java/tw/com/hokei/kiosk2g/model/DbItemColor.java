package tw.com.hokei.kiosk2g.model;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

public class DbItemColor {
    private static final long serialVersionUID = -2737065103251426619L;

    public Number _id;
    public String imageFileName;

    @SerializedName("itemID")
    public long itemID;

    @SerializedName("color")
    public String color;

    @SerializedName("image")
    public String image;

    public Bitmap bitmap;

    public void finalInit() {
    }
}
