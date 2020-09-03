package tw.com.hokei.kiosk2g.model;

import com.google.gson.annotations.SerializedName;

public class DbItemType {
    private static final long serialVersionUID = -2737065103251426617L;

    public Number _id;
    public String imageFileName;

    @SerializedName("itemID")
    public long itemID;

    @SerializedName("title")
    public String title;

    @SerializedName("image")
    public String image;

    public void finalInit() {
    }
}
