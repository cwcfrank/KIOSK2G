package tw.com.hokei.kiosk2g;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import tw.com.hokei.kiosk2g.database.UserConfig;
import tw.com.hokei.kiosk2g.model.DbArtist;
import tw.com.hokei.kiosk2g.model.DbArtistItem;
import tw.com.hokei.kiosk2g.model.DbItemColor;
import tw.com.hokei.kiosk2g.model.DbItemDemo;
import tw.com.hokei.kiosk2g.model.DbItemType;
import tw.com.hokei.kiosk2g.model.DbLanguage;

public class WebService {
    public static class NukeSSLCerts {
        protected static final String TAG = "NukeSSLCerts";

        public static void nuke() {
            try {
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                                return myTrustedAnchors;
                            }

                            @Override
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                            @Override
                            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                        }
                };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final int CONNECT_TIMEOUT = 10;
    private static final int READ_TIMEOUT = 10 * 1000;
    //private static final String WEB_DEBUG_URL = "http://syscode.com.tw:1859/debug";
    private static final String WEB_DEBUG_URL = "http://106.104.165.121:1858/debug";
    //private static final String WEB_DEBUG_URL = "http://192.168.0.104:1858/debug";

    private static String performGetCall(Context context, String url) throws Exception {
        String result = "";

        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(url, future, null);
        queue.add(request);

        String response = future.get(CONNECT_TIMEOUT, TimeUnit.SECONDS); // Blocks for at most 10 seconds.

        if (!Global.isEmptyString(response)) {
            result = response.toString();
        }

        return result;
    }

    private static String performPostCall(Context context, String url, final HashMap<String, String> params) {
        String result = "";

        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            RequestFuture<String> future = RequestFuture.newFuture();
            StringRequest request = new StringRequest(Request.Method.POST, url, future, future) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return params;
                }
            };
            queue.add(request);

            result = future.get(CONNECT_TIMEOUT, TimeUnit.SECONDS); // Blocks for at most 10 seconds.
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static int debug(Context context, String message) {
        int result = 0;
        String url = WEB_DEBUG_URL;

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("platform", "ANDROID");
            params.put("model", Build.MANUFACTURER + "," + Build.MODEL + "," + Build.VERSION.RELEASE);
            params.put("package", context.getPackageName() + "(" + version + ")");
            params.put("content", message);

            try {
                String response = performPostCall(context, url, params);

                if (!Global.isEmptyString(response)) {
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            result = -1;
            e.printStackTrace();
        }

        return result;
    }

    public static int debugHOKEI(Context context, String message) {
        int result = 0;
        String url = "https://www.id-yours.com/token/KioskNetwork.php";

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("machineID", Global.getDeviceID());
            params.put("hash", makeHash());
            params.put("platform", "ANDROID");
            params.put("model", Build.MANUFACTURER + "," + Build.MODEL + "," + Build.VERSION.RELEASE);
            params.put("package", context.getPackageName() + "(" + version + ")");
            params.put("content", message);

            try {
                String response = performPostCall(context, url, params);

                if (!Global.isEmptyString(response)) {
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            result = -1;
            e.printStackTrace();
        }

        return result;
    }

    public static String extractJsonString(String json) {
        if (json == null || json.isEmpty()) return "";
        String result = "";
        int p_index = -1;
        int p_index1 = json.indexOf("[");
        int p_index2 = json.indexOf("{");

        if (p_index1 == -1 && p_index2 != -1) {
            p_index = p_index2;
        } else if (p_index2 == -1 && p_index1 != -1) {
            p_index = p_index1;
        } else if (p_index1 != -1 && p_index2 != -1) {
            if (p_index1 < p_index2) {
                p_index = p_index1;
            } else if (p_index2 < p_index1) {
                p_index = p_index2;
            }
        }

        if (p_index > -1) {
            result = json.substring(p_index);
        } else {
            result = json;
        }

        return result;
    }


    public static final String MD5_KEY1 = "hk";
    public static final String MD5_KEY2 = "idyours";
    //public static String machineID = null;

    private static String makeHash() {
        String md5Base = MD5_KEY1 + Global.getDeviceID() + MD5_KEY2;
        String result = "";
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("MD5");
            md.update(md5Base.getBytes());
            byte[] md5Digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b:md5Digest) {
                sb.append(String.format("%02x", b));
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
        }

        return result;
    }

    static final String WS_URL = "https://www.id-yours.com/token";
    //static final String WS_URL = "https://59.120.223.150/token";

    public static synchronized String videoSearch(Context context, int[] versionCode) {
        String result = null;

        String url =
                String.format("%s/VideoSearch.php?machineID=%s&hash=%s&item=%s",
                        WS_URL,
                        urlEncode(Global.getDeviceID()),
                        urlEncode(makeHash()),
                        urlEncode("url")
                );

        try {
            String response = performGetCall(context, url);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                int returnCode = jsonObject.get("returnCode").getAsInt();

                if (returnCode == 0) {
                    versionCode[0] = jsonObject.get("versionCode").getAsInt();
                    result = jsonObject.get("url").getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public enum ItemSearch {
        ItemSearchDemo,
        ItemSearchType,
        ItemSearchColor,
    }

    public static String urlEncode(String data) {
        String result = "";

        try {
            result = URLEncoder.encode(data, "utf8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized ArrayList<DbItemDemo> itemSearchDemo(Context context, int[] versionCode) {
        final String itemCmd = "demo";
        ArrayList<DbItemDemo> result = new ArrayList<>();

        String url =
                String.format("%s/ItemSearch.php?machineID=%s&hash=%s&item=%s",
                        WS_URL,
                        urlEncode(Global.getDeviceID()),
                        urlEncode(makeHash()),
                        urlEncode(itemCmd)
                );

        try {
            String response = performGetCall(context, url);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                int returnCode = jsonObject.get("returnCode").getAsInt();

                versionCode[0] = jsonObject.get("versionCode").getAsInt();

                if (returnCode == 0) {
                    JsonArray jsonArray1 = jsonObject.getAsJsonArray("log").getAsJsonArray();

                    if (jsonArray1 != null) {
                        Gson gson = new Gson();
                        for (JsonElement json1 : jsonArray1) {
                            DbItemDemo db = gson.fromJson(json1, DbItemDemo.class);
                            db.finalInit();
                            result.add(db);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized ArrayList<DbItemType> itemSearchType(Context context, int[] versionCode) {
        final String itemCmd = "type";
        ArrayList<DbItemType> result = new ArrayList<>();

        String url =
                String.format("%s/ItemSearch.php?machineID=%s&hash=%s&item=%s",
                        WS_URL,
                        urlEncode(Global.getDeviceID()),
                        urlEncode(makeHash()),
                        urlEncode(itemCmd)
                );

        try {
            String response = performGetCall(context, url);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                int returnCode = jsonObject.get("returnCode").getAsInt();

                versionCode[0] = jsonObject.get("versionCode").getAsInt();

                if (returnCode == 0) {
                    JsonArray jsonArray1 = jsonObject.getAsJsonArray("log").getAsJsonArray();

                    if (jsonArray1 != null) {
                        Gson gson = new Gson();
                        for (JsonElement json1 : jsonArray1) {
                            DbItemType db = gson.fromJson(json1, DbItemType.class);
                            db.finalInit();
                            result.add(db);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized ArrayList<DbItemColor> itemSearchColor(Context context, int[] versionCode) {
        final String itemCmd = "color";
        ArrayList<DbItemColor> result = new ArrayList<>();

        String url =
                String.format("%s/ItemSearch.php?machineID=%s&hash=%s&item=%s",
                        WS_URL,
                        urlEncode(Global.getDeviceID()),
                        urlEncode(makeHash()),
                        urlEncode(itemCmd)
                );

        try {
            String response = performGetCall(context, url);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                int returnCode = jsonObject.get("returnCode").getAsInt();

                versionCode[0] = jsonObject.get("versionCode").getAsInt();

                if (returnCode == 0) {
                    JsonArray jsonArray1 = jsonObject.getAsJsonArray("log").getAsJsonArray();

                    if (jsonArray1 != null) {
                        Gson gson = new Gson();
                        for (JsonElement json1 : jsonArray1) {
                            DbItemColor db = gson.fromJson(json1, DbItemColor.class);
                            db.finalInit();
                            result.add(db);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskStatus(Context context, int[] status) {
        int result = -1;

        String url =
                String.format("%s/KioskStatus.php?machineID=%s&hash=%s&system=%s",
                        WS_URL,
                        urlEncode(Global.getDeviceID()),
                        urlEncode(makeHash()),
                        urlEncode("status")
                );

        try {
            String response = performGetCall(context, url);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();
                status[0] = jsonObject.get("status").getAsInt();
            }
        } catch (Exception e) {
            result = -2;
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskAlive(Context context, int[] responseCode) {
        int result = -1;

        String url =
                String.format("%s/KioskAlive.php?machineID=%s&hash=%s&status=1",
                        WS_URL,
                        urlEncode(Global.getDeviceID()),
                        urlEncode(makeHash())
                );

        try {
            String response = performGetCall(context, url);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();
                responseCode[0] = jsonObject.get("response").getAsInt();
            }
        } catch (Exception e) {
            result = -2;
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskIP(Context context) {
        int result = -1;

        String url =
                String.format("%s/KioskIP.php?machineID=%s&hash=%s",
                        WS_URL,
                        urlEncode(Global.getDeviceID()),
                        urlEncode(makeHash())
                );

        try {
            String response = performGetCall(context, url);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskSpread(Context context, String timeStart, String timeEnd, String page) {
        int result = -1;

        String url =
                String.format("%s/KioskSpread.php",
                        WS_URL
                );

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("machineID", Global.getDeviceID());
        params.put("hash", makeHash());
        params.put("time_start", timeStart);
        params.put("time_end", timeEnd);
        params.put("page", page);

        try {
            String response = performPostCall(context, url, params);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskRegister(
            Context context,
            String fName,
            String lname,
            String phoneCountry,
            String phoneCode,
            String phone,
            String email,
            String[] message,
            String[] memberCode) {
        int result = -1;

        String url =
                String.format("%s/KioskRegister.php",
                        WS_URL
                );

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("machineID", Global.getDeviceID());
        params.put("hash", makeHash());
        params.put("fname", fName);
        params.put("lname", lname);
        params.put("phonecountry", phoneCountry);
        params.put("phonecode", phoneCode);
        params.put("phone", phone);
        params.put("email", email);

        try {
            String response = performPostCall(context, url, params);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();
                if (result != 0) {
                    message[0] = jsonObject.get("message").getAsString();
                } else {
                    memberCode[0] = jsonObject.get("memberCode").getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskSampleOrder(
            Context context,
            String memberID,
            String ringType,
            String ringColor,
            String ringDiameter,
            String leftImage,
            String frontImage,
            String rightImage,
            String[] orderCode) {
        int result = -1;

        String url =
                String.format("%s/KioskSampleOrder.php",
                        WS_URL
                );

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("machineID", Global.getDeviceID());
        params.put("hash", makeHash());
        params.put("memberID", memberID);
        params.put("ringtype", ringType);
        params.put("ringcolor", ringColor);
        params.put("ringdiameter", ringDiameter);
        params.put("leftimage", leftImage);
        params.put("frontimage", frontImage);
        params.put("rightimage", rightImage);

        try {
            String response = performPostCall(context, url, params);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();
                orderCode[0] = jsonObject.get("orderCode").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskCheckphone(Context context, String phoneCode, String phone, String[] message) {
        int result = 0;

        String url =
                String.format("%s/KioskCheckphone.php",
                        WS_URL
                );

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("machineID", Global.getDeviceID());
        params.put("hash", makeHash());
        params.put("phonecode", phoneCode);
        params.put("phone", phone);

        try {
            String response = performPostCall(context, url, params);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();
                message[0] = jsonObject.get("message").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskCheckemail(Context context, String email, String[] message) {
        int result = 0;

        String url =
                String.format("%s/KioskCheckemail.php",
                        WS_URL
                );

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("machineID", Global.getDeviceID());
        params.put("hash", makeHash());
        params.put("email", email);

        try {
            String response = performPostCall(context, url, params);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();
                message[0] = jsonObject.get("message").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskSIP(final Context context) {
        int result = -1;

        String url =
                String.format("%s/KioskSIP.php?machineID=%s&hash=%s",
                        WS_URL,
                        urlEncode(Global.getDeviceID()),
                        urlEncode(makeHash())
                );

        try {
            String response = performGetCall(context, url);
            final String response2 = response;

            if (!Global.isEmptyString(response)) {
                new Thread() {
                    public void run() {
                        WebService.debug(context, "SIP==>" + response2);
                    }
                }.start();

                response = response.replace("\"proxy\":203.73.70.78:60138", "\"proxy\":\"203.73.70.78:60138\"");
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();

                if (result == 0) {
                    final String domain = jsonObject.get("domain").getAsString();
                    final String proxy = jsonObject.get("proxy").getAsString();
                    final String userName = jsonObject.get("username").getAsString();
                    final String password = jsonObject.get("password").getAsString();
                    final String call_no = jsonObject.get("call_no").getAsString();

                    UserConfig.setSipServerHost(context, domain);
                    UserConfig.setSipAccountUserID(context, userName);
                    UserConfig.setSipAccountPassword(context, password);
                    UserConfig.setSipProxy(context, proxy);
                    UserConfig.setSipCallNo(context, call_no);
                }
            }
        } catch (Exception e) {
            result = -1;
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskOrderOffline(
            Context context,
            String firstName,
            String lastName,
            String phoneCountry,
            String phoneCode,
            String phone,
            String email,
            String ringType,
            String ringColor,
            String ringDiameter,
            String leftImage,
            String frontImage,
            String rightImage) {
        int result = -1;

        String url =
                String.format("%s/KioskOrderOffline.php",
                        WS_URL
                );

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("machineID", Global.getDeviceID());
        params.put("hash", makeHash());
        params.put("fname", firstName);
        params.put("lname", lastName);
        params.put("phonecountry", phoneCountry);
        params.put("phonecode", phoneCode);
        params.put("phone", phone);
        params.put("email", email);
        params.put("ringtype", ringType);
        params.put("ringcolor", ringColor);
        params.put("ringdiameter", ringDiameter);
        params.put("leftimage", leftImage);
        params.put("frontimage", frontImage);
        params.put("rightimage", rightImage);

        String s = params.toString();

        try {
            String response = performPostCall(context, url, params);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskArtist(Context context) {
        if (!Global.isNetworkAvailable(context)) return -1;

        int result = -1;

        String url =
                String.format("%s/KioskArtist.php?machineID=%s&hash=%s",
                        WS_URL,
                        urlEncode(Global.getDeviceID()),
                        urlEncode(makeHash())
                );
        Log.d("url=",url);

        try {
            String response = performGetCall(context, url);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                Gson gson = new Gson();
                DbArtist dbArtist = gson.fromJson(jsonObject, DbArtist.class);

                if (dbArtist != null) {
                    if (dbArtist.returnCode.intValue() == 0) {
                        int nowVersionCode = UserConfig.getKioskArtistVersionCode(context);
                        if (nowVersionCode != dbArtist.versionCode.intValue()) {
                            UserConfig.setKioskArtistVersionCode(context, dbArtist.versionCode.intValue());
                            processArtist(context,dbArtist);
                            processArtistItem(context,dbArtist.EN_US, dbArtist.en_rUS);
                            processArtistItem(context,dbArtist.FR_FR, dbArtist.fr_rFR);
                            processArtistItem(context,dbArtist.DE_DE, dbArtist.de_rDE);
                            processArtistItem(context,dbArtist.JA_JP, dbArtist.ja_rJP);
                            processArtistItem(context,dbArtist.ZH_TW, dbArtist.zh_rTW);
                        }
                    }
                }
            }
        } catch (Exception e) {
            result = -2;
            e.printStackTrace();
        }

        return result;
    }

    public static synchronized int kioskSampleOrderUpdate(Context context, String orderCode) {
        int result = -1;

        String url =
                String.format("%s/KioskSampleOrderUpdate.php",
                        WS_URL
                );

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("machineID", Global.getDeviceID());
        params.put("hash", makeHash());
        params.put("orderCode", orderCode);
        params.put("status", "0");

        try {
            String response = performPostCall(context, url, params);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                result = jsonObject.get("returnCode").getAsInt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    public static synchronized int kioskLanguage(Context context) {
        if (!Global.isNetworkAvailable(context)) return -1;

        int result = -1;

        String url =
                String.format("%s/KioskLanguage.php?machineID=%s&hash=%s",
                        WS_URL,
                        urlEncode(Global.getDeviceID()),
                        urlEncode(makeHash())
                );
        Log.d("url=",url);

        try {
            String response = performGetCall(context, url);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                Gson gson = new Gson();
                DbLanguage dbLanguage = gson.fromJson(jsonObject, DbLanguage.class);

                if (dbLanguage != null) {
                    if (dbLanguage.returnCode.intValue() == 0) {
                            Log.d("dbLanguage","return code 0");
                        int nowVersionCode = UserConfig.getKioskArtistVersionCode(context);
                        if (nowVersionCode != dbLanguage.versionCode.intValue()) {
                            UserConfig.setKioskLanguageVersionCode(context,dbLanguage.versionCode.intValue());
                            UserConfig.setKioskLanguage(context, "english", dbLanguage.english.intValue());
                            UserConfig.setKioskLanguage(context, "french", dbLanguage.french.intValue());
                            UserConfig.setKioskLanguage(context, "deutsch", dbLanguage.deutsch.intValue());
                            UserConfig.setKioskLanguage(context, "japanese", dbLanguage.japanese.intValue());
                            UserConfig.setKioskLanguage(context, "chinese", dbLanguage.chinese.intValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            result = -2;
            e.printStackTrace();
        }

        return result;
    }

    private static String splitOutFileName(String data) {
        return Global.extraFileNameFromURL(data);
    }

    private static void processArtist(Context context,DbArtist dbArtist ){
        String artistPath = Global.getArtistPathName(context) ;
        Global.createPath(artistPath);

        UserConfig.setKioskArtist(context, "english_button_normal", splitOutFileName(dbArtist.english_button_normal));
        UserConfig.setKioskArtist(context, "english_button_press", splitOutFileName(dbArtist.english_button_press));
        UserConfig.setKioskArtist(context, "french_button_normal", splitOutFileName(dbArtist.french_button_normal));
        UserConfig.setKioskArtist(context, "french_button_press", splitOutFileName(dbArtist.french_button_press));
        UserConfig.setKioskArtist(context, "deutsch_button_normal", splitOutFileName(dbArtist.deutsch_button_normal));
        UserConfig.setKioskArtist(context, "deutsch_button_press", splitOutFileName(dbArtist.deutsch_button_press));
        UserConfig.setKioskArtist(context, "japan_button_normal", splitOutFileName(dbArtist.japan_button_normal));
        UserConfig.setKioskArtist(context, "japan_button_press", splitOutFileName(dbArtist.japan_button_press));
        UserConfig.setKioskArtist(context, "chinese_button_normal", splitOutFileName(dbArtist.chinese_button_normal));
        UserConfig.setKioskArtist(context, "chinese_button_press", splitOutFileName(dbArtist.chinese_button_press));
        UserConfig.setKioskArtist(context, "language_background", splitOutFileName(dbArtist.language_background));
        UserConfig.setKioskArtist(context, "exit_button_normal", splitOutFileName(dbArtist.exit_button_normal));
        UserConfig.setKioskArtist(context, "exit_button_press", splitOutFileName(dbArtist.exit_button_press));
        UserConfig.setKioskArtist(context, "service_call_left", splitOutFileName(dbArtist.service_call_left));
        UserConfig.setKioskArtist(context, "service_chat_right", splitOutFileName(dbArtist.service_chat_right));
        UserConfig.setKioskArtist(context, "service_chat_callcenter", splitOutFileName(dbArtist.service_chat_callcenter));
        UserConfig.setKioskArtist(context, "service_chat_guest", splitOutFileName(dbArtist.service_chat_guest));
        UserConfig.setKioskArtist(context, "service_chat_text", splitOutFileName(dbArtist.service_chat_text));
        UserConfig.setKioskArtist(context, "service_chat_sent_normal", splitOutFileName(dbArtist.service_chat_sent_normal));
        UserConfig.setKioskArtist(context, "service_chat_sent_press", splitOutFileName(dbArtist.service_chat_sent_press));
        UserConfig.setKioskArtist(context, "photo_target_left", splitOutFileName(dbArtist.photo_target_left));
        UserConfig.setKioskArtist(context, "photo_target_right", splitOutFileName(dbArtist.photo_target_right));
        UserConfig.setKioskArtist(context, "photo_target_back", splitOutFileName(dbArtist.photo_target_back));
        UserConfig.setKioskArtist(context, "photo_target_front", splitOutFileName(dbArtist.photo_target_front));
        UserConfig.setKioskArtist(context, "photo_target_up", splitOutFileName(dbArtist.photo_target_up));
        UserConfig.setKioskArtist(context, "photo_target_down", splitOutFileName(dbArtist.photo_target_down));
        UserConfig.setKioskArtist(context, "photo_countdown_1", splitOutFileName(dbArtist.photo_countdown_1));
        UserConfig.setKioskArtist(context, "photo_countdown_2", splitOutFileName(dbArtist.photo_countdown_2));
        UserConfig.setKioskArtist(context, "photo_countdown_3", splitOutFileName(dbArtist.photo_countdown_3));
        UserConfig.setKioskArtist(context, "photo_countdown_4", splitOutFileName(dbArtist.photo_countdown_4));
        UserConfig.setKioskArtist(context, "photo_countdown_5", splitOutFileName(dbArtist.photo_countdown_5));

        Global.downloadFile(context, dbArtist.english_button_normal, artistPath + File.separator + UserConfig.getKioskArtist(context, "english_button_normal"));
        Global.downloadFile(context, dbArtist.english_button_press, artistPath + File.separator + UserConfig.getKioskArtist(context, "english_button_press"));
        Global.downloadFile(context, dbArtist.french_button_normal, artistPath + File.separator + UserConfig.getKioskArtist(context, "french_button_normal"));
        Global.downloadFile(context, dbArtist.french_button_press, artistPath + File.separator + UserConfig.getKioskArtist(context, "french_button_press"));
        Global.downloadFile(context, dbArtist.deutsch_button_normal, artistPath+ File.separator + UserConfig.getKioskArtist(context, "deutsch_button_normal"));
        Global.downloadFile(context, dbArtist.deutsch_button_press, artistPath + File.separator + UserConfig.getKioskArtist(context, "deutsch_button_press"));
        Global.downloadFile(context, dbArtist.japan_button_normal, artistPath + File.separator + UserConfig.getKioskArtist(context, "japan_button_normal"));
        Global.downloadFile(context, dbArtist.japan_button_press, artistPath + File.separator + UserConfig.getKioskArtist(context, "japan_button_press"));
        Global.downloadFile(context, dbArtist.chinese_button_normal, artistPath + File.separator + UserConfig.getKioskArtist(context, "chinese_button_normal"));
        Global.downloadFile(context, dbArtist.chinese_button_press, artistPath + File.separator + UserConfig.getKioskArtist(context, "chinese_button_press"));
        Global.downloadFile(context, dbArtist.language_background, artistPath + File.separator + UserConfig.getKioskArtist(context, "language_background"));
        Global.downloadFile(context, dbArtist.exit_button_normal, artistPath + File.separator + UserConfig.getKioskArtist(context, "exit_button_normal"));
        Global.downloadFile(context, dbArtist.exit_button_press, artistPath + File.separator + UserConfig.getKioskArtist(context, "exit_button_press"));
        Global.downloadFile(context, dbArtist.service_call_left, artistPath + File.separator + UserConfig.getKioskArtist(context, "service_call_left"));
        Global.downloadFile(context, dbArtist.service_chat_right, artistPath + File.separator + UserConfig.getKioskArtist(context, "service_chat_right"));
        Global.downloadFile(context, dbArtist.service_chat_callcenter, artistPath + File.separator + UserConfig.getKioskArtist(context, "service_chat_callcenter"));
        Global.downloadFile(context, dbArtist.service_chat_guest, artistPath + File.separator + UserConfig.getKioskArtist(context, "service_chat_guest"));
        Global.downloadFile(context, dbArtist.service_chat_text, artistPath + File.separator + UserConfig.getKioskArtist(context, "service_chat_text"));
        Global.downloadFile(context, dbArtist.service_chat_sent_normal, artistPath + File.separator + UserConfig.getKioskArtist(context, "service_chat_sent_normal"));
        Global.downloadFile(context, dbArtist.service_chat_sent_press, artistPath + File.separator + UserConfig.getKioskArtist(context, "service_chat_sent_press"));
        Global.downloadFile(context, dbArtist.photo_target_left, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_target_left"));
        Global.downloadFile(context, dbArtist.photo_target_right, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_target_right"));
        Global.downloadFile(context, dbArtist.photo_target_back, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_target_back"));
        Global.downloadFile(context, dbArtist.photo_target_front, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_target_front"));
        Global.downloadFile(context, dbArtist.photo_target_up, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_target_up"));
        Global.downloadFile(context, dbArtist.photo_target_down, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_target_down"));
        Global.downloadFile(context, dbArtist.photo_countdown_1, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_countdown_1"));
        Global.downloadFile(context, dbArtist.photo_countdown_2, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_countdown_2"));
        Global.downloadFile(context, dbArtist.photo_countdown_3, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_countdown_3"));
        Global.downloadFile(context, dbArtist.photo_countdown_4, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_countdown_4"));
        Global.downloadFile(context, dbArtist.photo_countdown_5, artistPath + File.separator + UserConfig.getKioskArtist(context, "photo_countdown_5"));
    }

    private static void processArtistItem(Context context, String prefix, DbArtistItem dbItem) {
        String artistFilePath = Global.getArtistPathName(context) + File.separator + prefix;
        Global.createPath(artistFilePath);

        UserConfig.setKioskArtistItem(context, prefix, "index_button_normal", splitOutFileName(dbItem.index_button_normal));
        UserConfig.setKioskArtistItem(context, prefix, "index_button_press", splitOutFileName(dbItem.index_button_press));
        UserConfig.setKioskArtistItem(context, prefix, "index_video", splitOutFileName(dbItem.index_video));
        UserConfig.setKioskArtistItem(context, prefix, "call_message", splitOutFileName(dbItem.call_message));
        UserConfig.setKioskArtistItem(context, prefix, "call_button_voix_normal", splitOutFileName(dbItem.call_button_voix_normal));
        UserConfig.setKioskArtistItem(context, prefix, "call_button_voix_press", splitOutFileName(dbItem.call_button_voix_press));
        UserConfig.setKioskArtistItem(context, prefix, "call_button_text_normal", splitOutFileName(dbItem.call_button_text_normal));
        UserConfig.setKioskArtistItem(context, prefix, "call_button_text_press", splitOutFileName(dbItem.call_button_text_press));
        UserConfig.setKioskArtistItem(context, prefix, "call_button_pass_normal", splitOutFileName(dbItem.call_button_pass_normal));
        UserConfig.setKioskArtistItem(context, prefix, "call_button_pass_press", splitOutFileName(dbItem.call_button_pass_press));
        UserConfig.setKioskArtistItem(context, prefix, "branch_background", splitOutFileName(dbItem.branch_background));
        UserConfig.setKioskArtistItem(context, prefix, "branch_demo_gif", splitOutFileName(dbItem.branch_demo_gif));
        UserConfig.setKioskArtistItem(context, prefix, "branch_start_gif", splitOutFileName(dbItem.branch_start_gif));
        UserConfig.setKioskArtistItem(context, prefix, "branch_button_demo_normal", splitOutFileName(dbItem.branch_button_demo_normal));
        UserConfig.setKioskArtistItem(context, prefix, "branch_button_demo_press", splitOutFileName(dbItem.branch_button_demo_press));
        UserConfig.setKioskArtistItem(context, prefix, "branch_button_start_normal", splitOutFileName(dbItem.branch_button_start_normal));
        UserConfig.setKioskArtistItem(context, prefix, "branch_button_start_press", splitOutFileName(dbItem.branch_button_start_press));
        UserConfig.setKioskArtistItem(context, prefix, "demo_background", splitOutFileName(dbItem.demo_background));
        UserConfig.setKioskArtistItem(context, prefix, "demo_360touch", splitOutFileName(dbItem.demo_360touch));
        UserConfig.setKioskArtistItem(context, prefix, "demo_button_return_normal", splitOutFileName(dbItem.demo_button_return_normal));
        UserConfig.setKioskArtistItem(context, prefix, "demo_button_return_press", splitOutFileName(dbItem.demo_button_return_press));
        UserConfig.setKioskArtistItem(context, prefix, "photo_background", splitOutFileName(dbItem.photo_background));
        UserConfig.setKioskArtistItem(context, prefix, "photo_face_red", splitOutFileName(dbItem.photo_face_red));
        UserConfig.setKioskArtistItem(context, prefix, "photo_face_green", splitOutFileName(dbItem.photo_face_green));
        UserConfig.setKioskArtistItem(context, prefix, "verify_background", splitOutFileName(dbItem.verify_background));
        UserConfig.setKioskArtistItem(context, prefix, "verify_button_redo_normal", splitOutFileName(dbItem.verify_button_redo_normal));
        UserConfig.setKioskArtistItem(context, prefix, "verify_button_redo_press", splitOutFileName(dbItem.verify_button_redo_press));
        UserConfig.setKioskArtistItem(context, prefix, "verify_button_next_normal", splitOutFileName(dbItem.verify_button_next_normal));
        UserConfig.setKioskArtistItem(context, prefix, "verify_button_next_press", splitOutFileName(dbItem.verify_button_next_press));
        UserConfig.setKioskArtistItem(context, prefix, "member_background", splitOutFileName(dbItem.member_background));
        UserConfig.setKioskArtistItem(context, prefix, "member_terms_privacy", splitOutFileName(dbItem.member_terms_privacy));
        UserConfig.setKioskArtistItem(context, prefix, "member_terms_button_close_normal", splitOutFileName(dbItem.member_terms_button_close_normal));
        UserConfig.setKioskArtistItem(context, prefix, "member_terms_button_close_press", splitOutFileName(dbItem.member_terms_button_close_press));
        UserConfig.setKioskArtistItem(context, prefix, "member_button_next_normal", splitOutFileName(dbItem.member_button_next_normal));
        UserConfig.setKioskArtistItem(context, prefix, "member_button_next_press", splitOutFileName(dbItem.member_button_next_press));
        UserConfig.setKioskArtistItem(context, prefix, "sample_background", splitOutFileName(dbItem.sample_background));
        UserConfig.setKioskArtistItem(context, prefix, "sample_button_next_normal", splitOutFileName(dbItem.sample_button_next_normal));
        UserConfig.setKioskArtistItem(context, prefix, "sample_button_next_press", splitOutFileName(dbItem.sample_button_next_press));
        UserConfig.setKioskArtistItem(context, prefix, "sample_left_gif", splitOutFileName(dbItem.sample_left_gif));
        UserConfig.setKioskArtistItem(context, prefix, "finish_background", splitOutFileName(dbItem.finish_background));
        UserConfig.setKioskArtistItem(context, prefix, "finish_left_gif", splitOutFileName(dbItem.finish_left_gif));
        UserConfig.setKioskArtistItem(context, prefix, "finish_button_finish_normal", splitOutFileName(dbItem.finish_button_finish_normal));
        UserConfig.setKioskArtistItem(context, prefix, "finish_button_finish_press", splitOutFileName(dbItem.finish_button_finish_press));
        UserConfig.setKioskArtistItem(context, prefix, "finish_button_cancel_normal", splitOutFileName(dbItem.finish_button_cancel_normal));
        UserConfig.setKioskArtistItem(context, prefix, "finish_button_cancel_press", splitOutFileName(dbItem.finish_button_cancel_press));
        UserConfig.setKioskArtistItem(context, prefix, "ldle_background", splitOutFileName(dbItem.ldle_background));
        UserConfig.setKioskArtistItem(context, prefix, "ldle_button_exit_normal", splitOutFileName(dbItem.ldle_button_exit_normal));
        UserConfig.setKioskArtistItem(context, prefix, "ldle_button_exit_press", splitOutFileName(dbItem.ldle_button_exit_press));
        UserConfig.setKioskArtistItem(context, prefix, "ldle_button_continue_normal", splitOutFileName(dbItem.ldle_button_continue_normal));
        UserConfig.setKioskArtistItem(context, prefix, "ldle_button_continue_press", splitOutFileName(dbItem.ldle_button_continue_press));
        UserConfig.setKioskArtistItem(context, prefix, "exit_background", splitOutFileName(dbItem.exit_background));
        UserConfig.setKioskArtistItem(context, prefix, "exit_button_yes_normal", splitOutFileName(dbItem.exit_button_yes_normal));
        UserConfig.setKioskArtistItem(context, prefix, "exit_button_yes_press", splitOutFileName(dbItem.exit_button_yes_press));
        UserConfig.setKioskArtistItem(context, prefix, "exit_button_no_normal", splitOutFileName(dbItem.exit_button_no_normal));
        UserConfig.setKioskArtistItem(context, prefix, "exit_button_no_press", splitOutFileName(dbItem.exit_button_no_press));


        Global.downloadFile(context, dbItem.index_button_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "index_button_normal"));
        Global.downloadFile(context, dbItem.index_button_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "index_button_press"));
        Global.downloadFile(context, dbItem.index_video, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "index_video"));
        Global.downloadFile(context, dbItem.call_message, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "call_message"));
        Global.downloadFile(context, dbItem.call_button_voix_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "call_button_voix_normal"));
        Global.downloadFile(context, dbItem.call_button_voix_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "call_button_voix_press"));
        Global.downloadFile(context, dbItem.call_button_text_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "call_button_text_normal"));
        Global.downloadFile(context, dbItem.call_button_text_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "call_button_text_press"));
        Global.downloadFile(context, dbItem.call_button_pass_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "call_button_pass_normal"));
        Global.downloadFile(context, dbItem.call_button_pass_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "call_button_pass_press"));
        Global.downloadFile(context, dbItem.branch_background, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "branch_background"));
        Global.downloadFile(context, dbItem.branch_demo_gif, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "branch_demo_gif"));
        Global.downloadFile(context, dbItem.branch_start_gif, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "branch_start_gif"));
        Global.downloadFile(context, dbItem.branch_button_demo_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "branch_button_demo_normal"));
        Global.downloadFile(context, dbItem.branch_button_demo_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "branch_button_demo_press"));
        Global.downloadFile(context, dbItem.branch_button_start_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "branch_button_start_normal"));
        Global.downloadFile(context, dbItem.branch_button_start_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "branch_button_start_press"));
        Global.downloadFile(context, dbItem.demo_background, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "demo_background"));
        Global.downloadFile(context, dbItem.demo_360touch, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "demo_360touch"));
        Global.downloadFile(context, dbItem.demo_button_return_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "demo_button_return_normal"));
        Global.downloadFile(context, dbItem.demo_button_return_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "demo_button_return_press"));
        Global.downloadFile(context, dbItem.photo_background, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "photo_background"));
        Global.downloadFile(context, dbItem.photo_face_red, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "photo_face_red"));
        Global.downloadFile(context, dbItem.photo_face_green, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "photo_face_green"));
        Global.downloadFile(context, dbItem.verify_background, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "verify_background"));
        Global.downloadFile(context, dbItem.verify_button_redo_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "verify_button_redo_normal"));
        Global.downloadFile(context, dbItem.verify_button_redo_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "verify_button_redo_press"));
        Global.downloadFile(context, dbItem.verify_button_next_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "verify_button_next_normal"));
        Global.downloadFile(context, dbItem.verify_button_next_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "verify_button_next_press"));
        Global.downloadFile(context, dbItem.member_background, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "member_background"));
        Global.downloadFile(context, dbItem.member_terms_privacy, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "member_terms_privacy"));
        Global.downloadFile(context, dbItem.member_terms_button_close_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "member_terms_button_close_normal"));
        Global.downloadFile(context, dbItem.member_terms_button_close_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "member_terms_button_close_press"));
        Global.downloadFile(context, dbItem.member_button_next_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "member_button_next_normal"));
        Global.downloadFile(context, dbItem.member_button_next_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "member_button_next_press"));
        Global.downloadFile(context, dbItem.sample_background, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "sample_background"));
        Global.downloadFile(context, dbItem.sample_button_next_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "sample_button_next_normal"));
        Global.downloadFile(context, dbItem.sample_button_next_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "sample_button_next_press"));
        Global.downloadFile(context, dbItem.sample_left_gif, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "sample_left_gif"));
        Global.downloadFile(context, dbItem.finish_background, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "finish_background"));
        Global.downloadFile(context, dbItem.finish_left_gif, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "finish_left_gif"));
        Global.downloadFile(context, dbItem.finish_button_finish_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "finish_button_finish_normal"));
        Global.downloadFile(context, dbItem.finish_button_finish_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "finish_button_finish_press"));
        Global.downloadFile(context, dbItem.finish_button_cancel_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "finish_button_cancel_normal"));
        Global.downloadFile(context, dbItem.finish_button_cancel_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "finish_button_cancel_press"));
        Global.downloadFile(context, dbItem.ldle_background, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "ldle_background"));
        Global.downloadFile(context, dbItem.ldle_button_exit_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "ldle_button_exit_normal"));
        Global.downloadFile(context, dbItem.ldle_button_exit_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "ldle_button_exit_press"));
        Global.downloadFile(context, dbItem.ldle_button_continue_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "ldle_button_continue_normal"));
        Global.downloadFile(context, dbItem.ldle_button_continue_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "ldle_button_continue_press"));
        Global.downloadFile(context, dbItem.exit_background, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "exit_background"));
        Global.downloadFile(context, dbItem.exit_button_yes_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "exit_button_yes_normal"));
        Global.downloadFile(context, dbItem.exit_button_yes_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "exit_button_yes_press"));
        Global.downloadFile(context, dbItem.exit_button_no_normal, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "exit_button_no_normal"));
        Global.downloadFile(context, dbItem.exit_button_no_press, artistFilePath + File.separator + UserConfig.getKioskArtistItem(context, prefix, "exit_button_no_press"));
    }

    private static final String google_key = "AIzaSyCNIb1O_bpknPY7rgTbjKH6vENX-i9V1XQ";

    public static synchronized int googleTranslate(Context context, String fromText, String[] retText, String langCode) {
        int result = -1;

        String url =
                String.format("https://translation.googleapis.com/language/translate/v2?key=%s&q=%s&target=%s",
                        urlEncode(google_key),
                        urlEncode(fromText),
                        langCode
                );

        Log.d("TranslateURL",url);
        try {
            String response = performGetCall(context, url);

            if (!Global.isEmptyString(response)) {
                JsonObject jsonObject =  new JsonParser().parse(response).getAsJsonObject();
                JsonObject dataJsonObject = jsonObject.getAsJsonObject("data");

                if (dataJsonObject != null) {
                    JsonArray translationsArrayObject = dataJsonObject.getAsJsonArray("translations");

                    if (translationsArrayObject != null && translationsArrayObject.size() > 0) {
                        for (JsonElement json1 : translationsArrayObject) {
                            JsonObject obj = json1.getAsJsonObject();
                            retText[0] = obj.get("translatedText").getAsString();
                        }
                        result = 0;
                    } else {
                        result = -3;
                    }
                } else {
                    result = -2;
                }
            }
        } catch (Exception e) {
            result = -1;
            e.printStackTrace();
        }
        if(retText[0]!=null) {
            retText[0] = Html.fromHtml(retText[0]).toString();
        }
        return result;
    }
}
