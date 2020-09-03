package tw.com.hokei.kiosk2g.model;

import com.google.gson.annotations.SerializedName;

public class DbTempOrder {
    private static final long serialVersionUID = -2737065103251426638L;

    public Number _id;

    public String memberID;
    public String ringType;
    public String ringColor;
    public String ringDiameter;
    public String leftImageFileName;
    public String frontImageFileName;
    public String rightImageFileName;
}
