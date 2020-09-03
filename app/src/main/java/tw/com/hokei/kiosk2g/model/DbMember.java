package tw.com.hokei.kiosk2g.model;

import com.google.gson.annotations.SerializedName;

public class DbMember {
    private static final long serialVersionUID = -2737065103251426637L;

    public Number _id;

    @SerializedName("itemID")
    public long itemID;

    @SerializedName("title")
    public String title;

    @SerializedName("image")
    public String image;

    public void finalInit() {
    }
}
