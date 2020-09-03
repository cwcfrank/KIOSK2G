package tw.com.hokei.kiosk2g.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import tw.com.hokei.kiosk2g.model.DbArtist;

public final class UserConfig {
	private static MainDbHelper writeDbHelper = null;
	private static MainDbHelper readDbHelper = null;

	public synchronized static SQLiteDatabase getDb(Context context, boolean writable) {
		SQLiteDatabase result = null;

		synchronized(MainDbHelper.lockObject) {
			if (writable) {
				if (writeDbHelper == null) {
					writeDbHelper = new MainDbHelper(context);
					SQLiteDatabase db = writeDbHelper.getWritableDatabase();
					writeDbHelper.onCreate(db);
					db.close();
				}

				result = writeDbHelper.getWritableDatabase();
			} else {
				if (readDbHelper == null) {
					readDbHelper = new MainDbHelper(context);
					SQLiteDatabase db = readDbHelper.getWritableDatabase();
					readDbHelper.onCreate(db);
					db.close();
				}

				result = readDbHelper.getReadableDatabase();
			}
		}

		return result;
	}

	public synchronized static void setValue(Context context, String id, String value) {
		SQLiteDatabase db = getDb(context, true);

		db.execSQL( "INSERT OR REPLACE INTO UserConfig VALUES('" + id + "', '" + MainDbHelper.sqlData(value) + "')");
		db.close();
	}

	public synchronized static String getValue(Context context, String id, String defaultValue) {
		String result = defaultValue;
		SQLiteDatabase db = getDb(context, false);
		Cursor cursor = db.rawQuery("SELECT VAL FROM UserConfig WHERE ID='" + id + "';", null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getString(0);
			}
			cursor.close();
		}

		db.close();

		return result;
	}

	public static void reset(Context context) {
		setMachineID(context, "");
		setPushDeviceToken(context, "");
	}

	public static String getMachineID(Context context) {
		return getValue(context, "kMACHINE_ID", "");
	}

	public static void setMachineID(Context context, String value) {
		setValue(context, "kMACHINE_ID", value);
	}

	public static String getTokenId(Context context) {
		return getValue(context, "kTOKEN_ID", "");
	}

	public static void setTokenId(Context context, String value) {
		setValue(context, "kTOKEN_ID", value);
	}

	public static String getPushDeviceToken(Context context) {
		return getValue(context, "kPUSH_DEVICE_TOKEN", "");
	}

	public static void setPushDeviceToken(Context context, String value) {
		setValue(context, "kPUSH_DEVICE_TOKEN", value);
	}

	public static int getVersionCode(Context context, String tag) {
		String data = getValue(context, "kVERSION_CODE_" + tag, "0");

		return Integer.valueOf(data);
	}

	public static void setVersionCode(Context context, String tag, int value) {
		setValue(context, "kVERSION_CODE_" + tag, Integer.toString(value));
	}

	public static int getVideoVersionCode(Context context) {
		String data = getValue(context, "kVERSION_CODE_VIDEO", "0");

		return Integer.valueOf(data);
	}

	public static void setVideoVersionCode(Context context, int value) {
		setValue(context, "kVERSION_CODE_VIDEO", Integer.toString(value));
	}

	public static int getItemColorVersionCode(Context context) {
		String data = getValue(context, "kVERSION_CODE_ITEM_COLOR", "0");

		return Integer.valueOf(data);
	}

	public static void setItemColorVersionCode(Context context, int value) {
		setValue(context, "kVERSION_CODE_ITEM_COLOR", Integer.toString(value));
	}

	public static int getItemTypeVersionCode(Context context) {
		String data = getValue(context, "kVERSION_CODE_ITEM_TYPE", "0");

		return Integer.valueOf(data);
	}

	public static void setItemTypeVersionCode(Context context, int value) {
		setValue(context, "kVERSION_CODE_ITEM_TYPE", Integer.toString(value));
	}

	public static int getItemDemoVersionCode(Context context) {
		String data = getValue(context, "kVERSION_CODE_ITEM_DEMO", "0");

		return Integer.valueOf(data);
	}

	public static void setItemDemoVersionCode(Context context, int value) {
		setValue(context, "kVERSION_CODE_ITEM_DEMO", Integer.toString(value));
	}

	public static String getVideoFileName(Context context) {
		return getValue(context, "kVIDEO_FIEL_NAME", "");
	}

	public static void setVideoFileName(Context context, String value) {
		setValue(context, "kVIDEO_FIEL_NAME", value);
	}

	public static int getOrderItemType(Context context) {
		String data = getValue(context, "kORDER_ITEM_TYPE", "0");

		return Integer.valueOf(data);
	}

	public static void setOrderItemType(Context context, int value) {
		setValue(context, "kORDER_ITEM_TYPE", Integer.toString(value));
	}

	public static int getOrderItemColor(Context context) {
		String data = getValue(context, "kORDER_ITEM_COLOR", "0");

		return Integer.valueOf(data);
	}

	public static void setOrderItemColor(Context context, int value) {
		setValue(context, "kORDER_ITEM_COLOR", Integer.toString(value));
	}

	public static String getGuestTimeStart(Context context) {
		return getValue(context, "kGUEST_TIME_START", "");
	}

	public static void setGuestTimeStart(Context context, String value) {
		setValue(context, "kGUEST_TIME_START", value);
	}

	public static String getGuestTimeEnd(Context context) {
		return getValue(context, "kGUEST_TIME_END", "");
	}

	public static void setGuestTimeEnd(Context context, String value) {
		setValue(context, "kGUEST_TIME_END", value);
	}

	public static String getRegisterMemberCode(Context context) {
		return getValue(context, "kREGISTER_MEMBER_CODE", "");
	}

	public static void setRegisterMemberCode(Context context, String value) {
		setValue(context, "kREGISTER_MEMBER_CODE", value);
	}

	public static String getSampleOrderCode(Context context) {
		return getValue(context, "kSAMPLE_ORDER_CODE", "");
	}

	public static void setSampleOrderCode(Context context, String value) {
		setValue(context, "kSAMPLE_ORDER_CODE", value);
	}

	public static String getLeftImageFile(Context context) {
		return getValue(context, "kLEFT_IMAGE_FILE", "");
	}

	public static void setLeftImageFile(Context context, String value) {
		setValue(context, "kLEFT_IMAGE_FILE", value);
	}

	public static String getFrontImageFile(Context context) {
		return getValue(context, "kFRONT_IMAGE_FILE", "");
	}

	public static void setFrontImageFile(Context context, String value) {
		setValue(context, "kFRONT_IMAGE_FILE", value);
	}

	public static String getRightImageFile(Context context) {
		return getValue(context, "kRIGHT_IMAGE_FILE", "");
	}

	public static void setRightImageFile(Context context, String value) {
		setValue(context, "kRIGHT_IMAGE_FILE", value);
	}

	public static String getResolution(Context context) {
		return getValue(context, "kRESOLUTION", "1");
	}

	public static void setResolution(Context context, String value) {
		setValue(context, "kRESOLUTION", value);
	}

	public static String getCameraDeviceName(Context context, int no) {
		return getValue(context, "kCAMERA_DEVICE_NAME" + no, "");
	}

	public static void setCameraDeviceName(Context context, int no, String value) {
		setValue(context, "kCAMERA_DEVICE_NAME" + no, value);
	}

	public static String getUserOpBeginTime(Context context) {
		return getValue(context, "kUSER_OP_BEGIN_TIME", "");
	}

	public static void setUserOpBeginTime(Context context, String value) {
		setValue(context, "kUSER_OP_BEGIN_TIME", value);
	}


	public static String getSipServerHost(Context context) {
		return getValue(context, "kSIP_SERVER_HOST", "");
		//return "210.244.221.240";
	}

	public static void setSipServerHost(Context context, String value) {
		setValue(context, "kSIP_SERVER_HOST", value);
	}


	public static String getSipAccountUserID(Context context) {
		return getValue(context, "kSIP_ACCOUNT_USERID", "");
		//return "7654321";
	}

	public static void setSipAccountUserID(Context context, String value) {
		setValue(context, "kSIP_ACCOUNT_USERID", value);
	}

	public static String getSipAccountPassword(Context context) {
		return getValue(context, "kSIP_ACCOUNT_PASSWORD", "");
		//return "7654321";
	}

	public static void setSipAccountPassword(Context context, String value) {
		setValue(context, "kSIP_ACCOUNT_PASSWORD", value);
	}

	public static String getSipProxy(Context context) {
		return getValue(context, "kSIP_PROXY", "");
		//return "210.244.221.240";
	}

	public static void setSipProxy(Context context, String value) {
		setValue(context, "kSIP_PROXY", value);
	}

	public static String getSipCallNo(Context context) {
		return getValue(context, "kSIP_CALL_NO", "");
		//return "1234567";
	}

	public static void setSipCallNo(Context context, String value) {
		setValue(context, "kSIP_CALL_NO", value);
	}

	/////////////////////////////////////////////////////////////////////////////

	public static String getMemberOrderFirstName(Context context) {
		return getValue(context, "kMEMBER_ORDER_FIRST_NAME", "");
	}

	public static void setMemberOrderFirstName(Context context, String value) {
		setValue(context, "kMEMBER_ORDER_FIRST_NAME", value);
	}

	public static String getMemberOrderLastName(Context context) {
		return getValue(context, "kMEMBER_ORDER_LAST_NAME", "");
	}

	public static void setMemberOrderLastName(Context context, String value) {
		setValue(context, "kMEMBER_ORDER_LAST_NAME", value);
	}

	public static String getMemberOrderPhoneCounrty(Context context) {
		return getValue(context, "kMEMBER_ORDER_PHONE_COUNTRY", "");
	}

	public static void setMemberOrderPhoneCounrty(Context context, String value) {
		setValue(context, "kMEMBER_ORDER_PHONE_COUNTRY", value);
	}

	public static String getMemberOrderPhoneCode(Context context) {
		return getValue(context, "kMEMBER_ORDER_PHONE_CODE", "");
	}

	public static void setMemberOrderPhoneCode(Context context, String value) {
		setValue(context, "kMEMBER_ORDER_PHONE_CODE", value);
	}

	public static String getMemberOrderPhone(Context context) {
		return getValue(context, "kMEMBER_ORDER_PHONE", "");
	}

	public static void setMemberOrderPhone(Context context, String value) {
		setValue(context, "kMEMBER_ORDER_PHONE", value);
	}

	public static String getMemberOrderEMail(Context context) {
		return getValue(context, "kMEMBER_ORDER_EMAIL", "");
	}

	public static void setMemberOrderEMail(Context context, String value) {
		setValue(context, "kMEMBER_ORDER_EMAIL", value);
	}


	public static int getKioskArtistVersionCode(Context context) {
		return Integer.parseInt(getValue(context, "kKIOSK_ARTIST_VERSION_CODE", "-1"));
	}

	public static void setKioskArtistVersionCode(Context context, int value) {
		setValue(context, "kKIOSK_ARTIST_VERSION_CODE", Integer.toString(value));
	}
	public static int getKioskLanguageVersionCode(Context context) {
		return Integer.parseInt(getValue(context, "kKIOSK_LANGUAGE_VERSION_CODE", "-1"));
	}

	public static void setKioskLanguageVersionCode(Context context, int value) {
		setValue(context, "kKIOSK_LANGUAGE_VERSION_CODE", Integer.toString(value));
	}

	public static int getKioskLanguage(Context context, String item) {
		if ("english".equalsIgnoreCase(item)) {
			return Integer.parseInt(getValue(context, "kKIOSK_LANGUAGE_"  + item, "1"));
		} else {
			return Integer.parseInt(getValue(context, "kKIOSK_LANGUAGE_"  + item, "0"));
		}
	}

	public static void setKioskLanguage(Context context, String item, int value) {
		setValue(context, "kKIOSK_LANGUAGE_"  + item, Integer.toString(value));
	}
	public static String getKioskArtistItem(Context context, String prefix, String item) {
		return getValue(context, "kKIOSK_ARTIST_" + prefix + "_" + item, "");
	}

	public static void setKioskArtistItem(Context context, String prefix, String item, String value) {
		setValue(context, "kKIOSK_ARTIST_" + prefix + "_" + item, value);
	}
	//0528新增
	public static String getKioskArtist(Context context, String item) {
		return getValue(context, "kKIOSK_ARTIST_"  + item, "");
	}

	public static void setKioskArtist(Context context, String item, String value) {
		setValue(context, "kKIOSK_ARTIST_"  + item, value);
	}
	///

	public static String getKioskArtistLocale(Context context) {
		return getValue(context, "kKIOSK_ARTIST_LOCALE", DbArtist.EN_US);
	}

	public static void setKioskArtistLocale(Context context, String value) {
		setValue(context, "kKIOSK_ARTIST_LOCALE", value);
	}

	public static String getCustomerServiceType(Context context) {
		// 0 : None
		// 1 : voice
		// 2 : text
		return getValue(context, "kCUSTOMER_SERVICE_TYPE", "0");
	}

	public static void setCustomerServiceType(Context context, String value) {
		setValue(context, "kCUSTOMER_SERVICE_TYPE", value);
	}

	public static String getLastVisitPage(Context context) {
		//1-首頁 2-選擇頁 3-DEMO 頁 4-拍照頁 5-驗證頁 6-個資填寫頁 7-需求填寫頁 8-完成頁
		return getValue(context, "kLAST_VISIT_PAGE", "0");
	}

	public static void setLastVisitPage(Context context, String value) {
		setValue(context, "kLAST_VISIT_PAGE", value);
	}
}
