package tw.com.hokei.kiosk2g;

import android.content.Context;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import tw.com.hokei.kiosk2g.database.MainDB;
import tw.com.hokei.kiosk2g.database.UserConfig;
import tw.com.hokei.kiosk2g.model.DbItemColor;
import tw.com.hokei.kiosk2g.model.DbItemDemo;
import tw.com.hokei.kiosk2g.model.DbItemType;

public class DataProcess {

    public static void processItemData(Context context) {
        if (!Global.isNetworkAvailable(context)) return;

        int[] versionCode = new int[1];

        versionCode[0] = UserConfig.getVideoVersionCode(context);
        String videoUrl = WebService.videoSearch(context, versionCode);

        if (versionCode[0] != UserConfig.getVideoVersionCode(context)) {
            if (buildVideoData(context, videoUrl) >= 0) {
                UserConfig.setVideoVersionCode(context, versionCode[0]);
            }
        }

        versionCode[0] = UserConfig.getItemColorVersionCode(context);
        ArrayList<DbItemColor> itemColorArray = WebService.itemSearchColor(context, versionCode);

        if (versionCode[0] != UserConfig.getItemColorVersionCode(context)) {
            UserConfig.setItemColorVersionCode(context, versionCode[0]);
            buildItemColorData(context, itemColorArray);
        }

        versionCode[0] = UserConfig.getItemTypeVersionCode(context);
        ArrayList<DbItemType> itemTypeArray = WebService.itemSearchType(context, versionCode);

        if (versionCode[0] != UserConfig.getItemTypeVersionCode(context)) {
            UserConfig.setItemTypeVersionCode(context, versionCode[0]);
            buildItemTypeData(context, itemTypeArray);
        }

        versionCode[0] = UserConfig.getItemDemoVersionCode(context);
        ArrayList<DbItemDemo> itemDemoArray = WebService.itemSearchDemo(context, versionCode);

        if (versionCode[0] != UserConfig.getItemDemoVersionCode(context)) {
            UserConfig.setItemDemoVersionCode(context, versionCode[0]);
            buildItemDemoData(context, itemDemoArray, itemColorArray);

            try {
                Glide.get(context).clearDiskCache();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int buildVideoData(Context context, String url) {
        int result = 0;

        String videoPath = Global.getVideoPathName(context);
        String videoFileName = Global.extraFileNameFromURL(url);
/*
        try {
            FileUtils.cleanDirectory(new File(videoPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        Global.createPath(videoPath);
        result = Global.downloadFile(context, url, videoPath + File.separator + videoFileName);
        if (result >= 0) {
            UserConfig.setVideoFileName(context, videoFileName);
        }

        return result;
    }

    public static void buildItemColorData(Context context, ArrayList<DbItemColor> itemArray) {
        String itemPath = Global.getItemPathName(context);
        String itemColorPath = Global.getItemColorPathName(context);

        Global.createPath(itemPath);
        Global.createPath(itemColorPath);
/*
        try {
            FileUtils.cleanDirectory(new File(itemColorPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        MainDB.deleteItemColor(context);

        for (DbItemColor item : itemArray) {
            String fileName = Global.extraFileNameFromURL(item.image);
            String colorImageFile = itemColorPath + File.separator + fileName;

            //Global.deleteFile(colorImageFile);
            Global.downloadFile(context, item.image, colorImageFile);

            item.imageFileName = fileName;
            MainDB.newItemWithItemColor(context, item);
        }
    }

    public static void buildItemTypeData(Context context, ArrayList<DbItemType> itemArray) {
        String itemPath = Global.getItemPathName(context);
        String itemTypePath = Global.getItemTypePathName(context);

        Global.createPath(itemPath);
        Global.createPath(itemTypePath);
/*
        try {
            FileUtils.cleanDirectory(new File(itemTypePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        MainDB.deleteItemType(context);

        for (DbItemType item : itemArray) {
            String fileName = Global.extraFileNameFromURL(item.image);
            String typeImageFile = itemTypePath + File.separator + fileName;

            //Global.deleteFile(typeImageFile);
            Global.downloadFile(context, item.image, typeImageFile);

            item.imageFileName = fileName;
            MainDB.newItemWithItemType(context, item);
        }
    }

    public static void buildItemDemoData(
            Context context,
            ArrayList<DbItemDemo> itemDemoArray,
            ArrayList<DbItemColor> itemColorArray) {
        String itemPath = Global.getItemPathName(context);
        String itemDemoPath = Global.getItemDemoPathName(context);

        Global.createPath(itemPath);
        Global.createPath(itemDemoPath);
/*
        try {
            FileUtils.cleanDirectory(new File(itemDemoPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        MainDB.deleteItemDemo(context);

        for (DbItemDemo itemDemo : itemDemoArray) {
            String itemDemoPath2 = itemDemoPath + File.separator + itemDemo.path;

            Global.createPath(itemDemoPath2);

            for (DbItemColor itemColor : itemColorArray) {
                String itemDemoColorPath = itemDemoPath2 + File.separator + itemColor.color;

                Global.createPath(itemDemoColorPath);

                for (int i = 0 ; i <= Global.ITEM_DEMO_L1 ; i++) {
                    for (int j = 0 ; j <= Global.ITEM_DEMO_L2 ; j++) {
                        String fileName = i + "_" + j + ".png";
                        String demoImageFile = itemDemoColorPath + File.separator + fileName;

                        //Global.deleteFile(demoImageFile);
                        Global.downloadFile(context, itemDemo.image + itemColor.color + "/" + fileName, demoImageFile);
                    }
                }
            }

            MainDB.newItemWithItemDemo(context, itemDemo);
        }
    }
}
