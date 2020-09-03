package tw.com.hokei.kiosk2g.model;

import com.google.gson.annotations.SerializedName;

public class DbItemDemo {
    private static final long serialVersionUID = -2737065103251426618L;

    public Number _id;

    @SerializedName("itemID")
    public long itemID;

    @SerializedName("path")
    public String path;

    @SerializedName("image")
    public String image;

    public void finalInit() {
    }
}
