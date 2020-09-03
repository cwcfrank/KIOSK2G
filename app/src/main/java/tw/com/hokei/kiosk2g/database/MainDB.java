package tw.com.hokei.kiosk2g.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import tw.com.hokei.kiosk2g.Global;
import tw.com.hokei.kiosk2g.model.DbItemColor;
import tw.com.hokei.kiosk2g.model.DbItemDemo;
import tw.com.hokei.kiosk2g.model.DbItemType;
import tw.com.hokei.kiosk2g.model.DbTempMemberOrder;
import tw.com.hokei.kiosk2g.model.DbTempOrder;
import tw.com.hokei.kiosk2g.model.DbTempRegister;
import tw.com.hokei.kiosk2g.model.DbTextService;

public class MainDB {
	public static final Object lockObject = new Object();
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
	
	public synchronized static void closeDB(SQLiteDatabase sdb) {
		if (sdb != null) {
			//sdb.close();	
		}
	}
	
	public synchronized static void beginTransactionWithDB(SQLiteDatabase sdb) {
		try {
			sdb.execSQL("BEGIN TRANSACTION;");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void endTransactionWithDB(SQLiteDatabase sdb) {
		try {
			sdb.execSQL("END TRANSACTION;");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void startup(Context context) throws Exception {
		SQLiteDatabase db = getDb(context, true);
		synchronized(MainDbHelper.lockObject) {
			db.close();
		}
	}
	
	public static String sqlData(String data) {
		if (Global.isEmptyString(data)) {
			return "";
		} else {
			return MainDbHelper.sqlData(data);
		}
	}
	
	public static long getMaxID(Context context, String tableName) {
		long result = 0;
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor = sdb.rawQuery("SELECT MAX(_id) FROM " + tableName + ";", null);
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getLong(0);
			}
			cursor.close();
		}
		
		MainDB.closeDB(sdb);

		return result;
	}
	
	public static long getLastInsertRowID(SQLiteDatabase sdb) {
		long result = 0;
		//SELECT MAX(_id) FROM table...
		Cursor cursor = sdb.rawQuery("SELECT last_insert_rowid();", null);
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getLong(0);
			}
			cursor.close();
		}

		return result;
	}
	
	public synchronized static void resetSeq(Context context, String tableName) {
		SQLiteDatabase sdb = getDb(context, true);

		try {
			sdb.execSQL("DELETE FROM sqlite_sequence WHERE name='" + tableName + "';");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		MainDB.closeDB(sdb);
	}

	////////////////////////////////////////////////////////////////////////////////////

	public static List<DbItemColor> getItemColorList(Context context) {
		List<DbItemColor> result = new ArrayList<>();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM ItemColor " +
				"ORDER BY item_id;";

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			while (cursor.moveToNext()) {
				DbItemColor db = new DbItemColor();

				db._id = cursor.getLong(0);
				db.itemID = cursor.getLong(1);
				db.color = cursor.getString(2);
				db.image = cursor.getString(3);
				db.imageFileName = cursor.getString(4);

				result.add(db);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Cursor getItemColorList_Curosr(Context context) {
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor = null;
		final String sql =
				"SELECT * " +
				"FROM ItemColor " +
				"ORDER BY item_id;";

		try {
			cursor = sdb.rawQuery(sql, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cursor;
	}

	public static DbItemColor getItemColorWithPrimaryKey(Context context, Number primaryKey) {
		DbItemColor result = new DbItemColor();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM ItemColor " +
				"WHERE _id=" + primaryKey.longValue();

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			if (cursor.moveToNext()) {
				DbItemColor db = new DbItemColor();

				result._id = cursor.getLong(0);
				result.itemID = cursor.getLong(1);
				result.color = cursor.getString(2);
				result.image = cursor.getString(3);
				result.imageFileName = cursor.getString(4);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithItemColor(Context context, DbItemColor db) {
		Number result = null;
		SQLiteDatabase sdb = getDb(context, true);
		result = newItemWithItemColor(context, sdb, db);
		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithItemColor(Context context, SQLiteDatabase sdb, DbItemColor db) {
		Number result = null;

		try {
			sdb.execSQL(
					"INSERT INTO ItemColor (" +
					"item_id,color,image_url,image_file_name" +
					")VALUES(" +
					db.itemID + "," +
					"'" + sqlData(db.color) + "'," +
					"'" + sqlData(db.image) + "'," +
					"'" + sqlData(db.imageFileName) + "'" +
					");");

			result = getLastInsertRowID(sdb);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void deleteItemColor(Context context) {
		SQLiteDatabase sdb = getDb(context, true);

		try {
			resetSeq(context, "ItemColor");
			sdb.execSQL("DELETE FROM ItemColor;");
		} catch (Exception e) {
			e.printStackTrace();
		}

		MainDB.closeDB(sdb);
	}

	////////////////////////////////////////////////////////////////////////////////////

	public static List<DbItemType> getItemTypeList(Context context) {
		List<DbItemType> result = new ArrayList<>();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM ItemType " +
				"ORDER BY item_id;";

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			while (cursor.moveToNext()) {
				DbItemType db = new DbItemType();

				db._id = cursor.getLong(0);
				db.itemID = cursor.getLong(1);
				db.title = cursor.getString(2);
				db.image = cursor.getString(3);
				db.imageFileName = cursor.getString(4);

				result.add(db);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithItemType(Context context, DbItemType db) {
		Number result = null;
		SQLiteDatabase sdb = getDb(context, true);
		result = newItemWithItemType(context, sdb, db);
		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithItemType(Context context, SQLiteDatabase sdb, DbItemType db) {
		Number result = null;

		try {
			sdb.execSQL(
					"INSERT INTO ItemType (" +
					"item_id,title,image_url,image_file_name" +
					")VALUES(" +
					db.itemID + "," +
					"'" + sqlData(db.title) + "'," +
					"'" + sqlData(db.image) + "'," +
					"'" + sqlData(db.imageFileName) + "'" +
					");");

			result = getLastInsertRowID(sdb);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void deleteItemType(Context context) {
		SQLiteDatabase sdb = getDb(context, true);

		try {
			resetSeq(context, "ItemType");
			sdb.execSQL("DELETE FROM ItemType;");
		} catch (Exception e) {
			e.printStackTrace();
		}

		MainDB.closeDB(sdb);
	}

	////////////////////////////////////////////////////////////////////////////////////

	public static List<DbItemDemo> getItemDemoList(Context context) {
		List<DbItemDemo> result = new ArrayList<>();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM ItemDemo " +
				"ORDER BY item_id;";

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			while (cursor.moveToNext()) {
				DbItemDemo db = new DbItemDemo();

				db._id = cursor.getLong(0);
				db.itemID = cursor.getLong(1);
				db.path = cursor.getString(2);
				db.image = cursor.getString(3);

				result.add(db);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithItemDemo(Context context, DbItemDemo db) {
		Number result = null;
		SQLiteDatabase sdb = getDb(context, true);
		result = newItemWithItemDemo(context, sdb, db);
		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithItemDemo(Context context, SQLiteDatabase sdb, DbItemDemo db) {
		Number result = null;

		try {
			sdb.execSQL(
					"INSERT INTO ItemDemo (" +
					"item_id,path,image_url" +
					")VALUES(" +
					db.itemID + "," +
					"'" + sqlData(db.path) + "'," +
					"'" + sqlData(db.image) + "'" +
					");");

			result = getLastInsertRowID(sdb);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void deleteItemDemo(Context context) {
		SQLiteDatabase sdb = getDb(context, true);

		try {
			resetSeq(context, "ItemDemo");
			sdb.execSQL("DELETE FROM ItemDemo;");
		} catch (Exception e) {
			e.printStackTrace();
		}

		MainDB.closeDB(sdb);
	}

	////////////////////////////////////////////////////////////////////////////////////

	public static List<DbTempOrder> getTempOrderList(Context context) {
		List<DbTempOrder> result = new ArrayList<>();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM TempOrder;";

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			while (cursor.moveToNext()) {
				DbTempOrder db = new DbTempOrder();

				db._id = cursor.getLong(0);
				db.memberID = cursor.getString(1);
				db.ringType = cursor.getString(2);
				db.ringColor = cursor.getString(3);
				db.ringDiameter = cursor.getString(4);
				db.leftImageFileName = cursor.getString(5);
				db.frontImageFileName = cursor.getString(6);
				db.rightImageFileName = cursor.getString(7);

				result.add(db);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Cursor getTempOrderList_Curosr(Context context) {
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor = null;
		final String sql =
				"SELECT * " +
				"FROM TempOrder;";

		try {
			cursor = sdb.rawQuery(sql, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cursor;
	}

	public static DbTempOrder getTempOrderWithPrimaryKey(Context context, Number primaryKey) {
		DbTempOrder result = new DbTempOrder();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM TempOrder " +
				"WHERE _id=" + primaryKey.longValue();

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			if (cursor.moveToNext()) {
				DbTempOrder db = new DbTempOrder();

				result._id = cursor.getLong(0);
				result.memberID = cursor.getString(1);
				result.ringType = cursor.getString(2);
				result.ringColor = cursor.getString(3);
				result.ringDiameter = cursor.getString(4);
				result.leftImageFileName = cursor.getString(5);
				result.frontImageFileName = cursor.getString(6);
				result.rightImageFileName = cursor.getString(7);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithTempOrder(Context context, DbTempOrder db) {
		Number result = null;
		SQLiteDatabase sdb = getDb(context, true);
		result = newItemWithTempOrder(context, sdb, db);
		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithTempOrder(Context context, SQLiteDatabase sdb, DbTempOrder db) {
		Number result = null;

		try {
			sdb.execSQL(
					"INSERT INTO TempOrder (" +
							"member_id,ring_type,ring_color,ring_diameter," +
							"left_image_file_name,front_image_file_name,right_image_file_name" +
							")VALUES(" +
							"'" + sqlData(db.memberID) + "'," +
							"'" + sqlData(db.ringType) + "'," +
							"'" + sqlData(db.ringColor) + "'," +
							"'" + sqlData(db.ringDiameter) + "'," +
							"'" + sqlData(db.leftImageFileName) + "'," +
							"'" + sqlData(db.frontImageFileName) + "'," +
							"'" + sqlData(db.rightImageFileName) + "'" +
							");");

			result = getLastInsertRowID(sdb);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void deleteTempOrder(Context context, long _id) {
		SQLiteDatabase sdb = getDb(context, true);

		try {
			resetSeq(context, "TempOrder");
			sdb.execSQL("DELETE FROM TempOrder WHERE _id=" + _id + ";");
		} catch (Exception e) {
			e.printStackTrace();
		}

		MainDB.closeDB(sdb);
	}

	/////////////////////////////////////////////////////////////////////

	public static List<DbTempRegister> getTempRegisterList(Context context) {
		List<DbTempRegister> result = new ArrayList<>();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM TempRegister;";

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			while (cursor.moveToNext()) {
				DbTempRegister db = new DbTempRegister();

				db._id = cursor.getLong(0);
				db.firstName = cursor.getString(1);
				db.lastName = cursor.getString(2);
				db.phoneCountry = cursor.getString(3);
				db.phoneCode = cursor.getString(4);
				db.phone = cursor.getString(5);
				db.email = cursor.getString(6);

				result.add(db);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Cursor getTempRegisterList_Curosr(Context context) {
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor = null;
		final String sql =
				"SELECT * " +
				"FROM TempRegister;";

		try {
			cursor = sdb.rawQuery(sql, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cursor;
	}

	public static DbTempRegister getTempRegisterWithPrimaryKey(Context context, Number primaryKey) {
		DbTempRegister result = new DbTempRegister();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM TempRegister " +
				"WHERE _id=" + primaryKey.longValue();

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			if (cursor.moveToNext()) {
				DbTempRegister db = new DbTempRegister();

				result._id = cursor.getLong(0);
				result.firstName = cursor.getString(1);
				result.lastName = cursor.getString(2);
				result.phoneCountry = cursor.getString(3);
				result.phoneCode = cursor.getString(4);
				result.phone = cursor.getString(5);
				result.email = cursor.getString(6);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static DbTempRegister getTempRegisterWithEMail(Context context, String email) {
		DbTempRegister result = new DbTempRegister();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM TempRegister " +
				"WHERE email='" + sqlData(email) + "';";

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			if (cursor.moveToNext()) {
				DbTempRegister db = new DbTempRegister();

				result._id = cursor.getLong(0);
				result.firstName = cursor.getString(1);
				result.lastName = cursor.getString(2);
				result.phoneCountry = cursor.getString(3);
				result.phoneCode = cursor.getString(4);
				result.phone = cursor.getString(5);
				result.email = cursor.getString(6);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithTempRegister(Context context, DbTempRegister db) {
		Number result = null;
		SQLiteDatabase sdb = getDb(context, true);
		result = newItemWithTempRegister(context, sdb, db);
		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithTempRegister(Context context, SQLiteDatabase sdb, DbTempRegister db) {
		Number result = null;

		try {
			sdb.execSQL(
					"INSERT INTO TempRegister (" +
							"first_name,last_name,phone_country,phone_code,phone,email" +
							")VALUES(" +
							"'" + sqlData(db.firstName) + "'," +
							"'" + sqlData(db.lastName) + "'," +
							"'" + sqlData(db.phoneCountry) + "'," +
							"'" + sqlData(db.phoneCode) + "'," +
							"'" + sqlData(db.phone) + "'," +
							"'" + sqlData(db.email) + "'" +
							");");

			result = getLastInsertRowID(sdb);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static Number updateItemWithTempRegister(Context context, DbTempRegister db) {
		Number result = null;
		SQLiteDatabase sdb = getDb(context, true);

		try {
			sdb.execSQL(
					"UPDATE TempRegister SET " +
					"first_name='" + sqlData(db.firstName) + "'," +
					"last_name='" + sqlData(db.lastName) + "'," +
					"phone_country='" + sqlData(db.phoneCountry) + "'," +
					"phone_code='" + sqlData(db.phoneCode) + "'," +
					"phone='" + sqlData(db.phone) + "' " +
					"WHERE " +
					"email=" + sqlData(db.email) + "';");

			result = getLastInsertRowID(sdb);
		} catch (Exception e) {
			e.printStackTrace();
		}


		MainDB.closeDB(sdb);

		return result;
	}

	public static void deleteTempRegister(Context context, long _id) {
		SQLiteDatabase sdb = getDb(context, true);

		try {
			resetSeq(context, "TempRegister");
			sdb.execSQL("DELETE FROM TempRegister WHERE _id=" + _id + ";");
		} catch (Exception e) {
			e.printStackTrace();
		}

		MainDB.closeDB(sdb);
	}

/////////////////////////////////////////////////////////////////////

	public static List<DbTempMemberOrder> getTempMemberOrderList(Context context) {
		List<DbTempMemberOrder> result = new ArrayList<>();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
						"FROM TempMemberOrder;";

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			while (cursor.moveToNext()) {
				DbTempMemberOrder db = new DbTempMemberOrder();

				db._id = cursor.getLong(0);
				db.firstName = cursor.getString(1);
				db.lastName = cursor.getString(2);
				db.phoneCountry = cursor.getString(3);
				db.phoneCode = cursor.getString(4);
				db.phone = cursor.getString(5);
				db.email = cursor.getString(6);

				db.ringType = cursor.getString(7);
				db.ringColor = cursor.getString(8);
				db.ringDiameter = cursor.getString(9);
				db.leftImageFileName = cursor.getString(10);
				db.frontImageFileName = cursor.getString(11);
				db.rightImageFileName = cursor.getString(12);

				result.add(db);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Cursor getTempMemberOrderList_Curosr(Context context) {
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor = null;
		final String sql =
				"SELECT * " +
						"FROM TempMemberOrder;";

		try {
			cursor = sdb.rawQuery(sql, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cursor;
	}

	public static DbTempMemberOrder getTempMemberOrderWithPrimaryKey(Context context, Number primaryKey) {
		DbTempMemberOrder result = new DbTempMemberOrder();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
						"FROM TempMemberOrder " +
						"WHERE _id=" + primaryKey.longValue();

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			if (cursor.moveToNext()) {
				DbTempMemberOrder db = new DbTempMemberOrder();

				result._id = cursor.getLong(0);
				result.firstName = cursor.getString(1);
				result.lastName = cursor.getString(2);
				result.phoneCountry = cursor.getString(3);
				result.phoneCode = cursor.getString(4);
				result.phone = cursor.getString(5);
				result.email = cursor.getString(6);

				result.ringType = cursor.getString(7);
				result.ringColor = cursor.getString(8);
				result.ringDiameter = cursor.getString(9);
				result.leftImageFileName = cursor.getString(10);
				result.frontImageFileName = cursor.getString(11);
				result.rightImageFileName = cursor.getString(12);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static DbTempMemberOrder getTempMemberOrderWithEMail(Context context, String email) {
		DbTempMemberOrder result = new DbTempMemberOrder();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
						"FROM TempMemberOrder " +
						"WHERE email='" + sqlData(email) + "';";

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			if (cursor.moveToNext()) {
				DbTempMemberOrder db = new DbTempMemberOrder();

				result._id = cursor.getLong(0);
				result.firstName = cursor.getString(1);
				result.lastName = cursor.getString(2);
				result.phoneCountry = cursor.getString(3);
				result.phoneCode = cursor.getString(4);
				result.phone = cursor.getString(5);
				result.email = cursor.getString(6);

				result.ringType = cursor.getString(7);
				result.ringColor = cursor.getString(8);
				result.ringDiameter = cursor.getString(9);
				result.leftImageFileName = cursor.getString(10);
				result.frontImageFileName = cursor.getString(11);
				result.rightImageFileName = cursor.getString(12);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithTempMemberOrder(Context context, DbTempMemberOrder db) {
		Number result = null;
		SQLiteDatabase sdb = getDb(context, true);
		result = newItemWithTempMemberOrder(context, sdb, db);
		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithTempMemberOrder(Context context, SQLiteDatabase sdb, DbTempMemberOrder db) {
		Number result = null;

		try {
			sdb.execSQL(
					"INSERT INTO TempMemberOrder (" +
							"first_name,last_name,phone_country,phone_code,phone,email," +
							"ring_type,ring_color,ring_diameter," +
							"left_image_file_name,front_image_file_name,right_image_file_name" +
							")VALUES(" +
							"'" + sqlData(db.firstName) + "'," +
							"'" + sqlData(db.lastName) + "'," +
							"'" + sqlData(db.phoneCountry) + "'," +
							"'" + sqlData(db.phoneCode) + "'," +
							"'" + sqlData(db.phone) + "'," +
							"'" + sqlData(db.email) + "'," +
							"'" + sqlData(db.ringType) + "'," +
							"'" + sqlData(db.ringColor) + "'," +
							"'" + sqlData(db.ringDiameter) + "'," +
							"'" + sqlData(db.leftImageFileName) + "'," +
							"'" + sqlData(db.frontImageFileName) + "'," +
							"'" + sqlData(db.rightImageFileName) + "'" +
							");");

			result = getLastInsertRowID(sdb);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void deleteTempMemberOrder(Context context, long _id) {
		SQLiteDatabase sdb = getDb(context, true);

		try {
			resetSeq(context, "TempMemberOrder");
			sdb.execSQL("DELETE FROM TempMemberOrder WHERE _id=" + _id + ";");
		} catch (Exception e) {
			e.printStackTrace();
		}

		MainDB.closeDB(sdb);
	}

	////////////////////////////////////////////////////////////////////////////////////

	public static List<DbTextService> getTextServiceList(Context context) {
		List<DbTextService> result = new ArrayList<>();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM TextService " +
				"ORDER BY _id;";

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			while (cursor.moveToNext()) {
				DbTextService db = new DbTextService();

				db._id = cursor.getLong(0);
				db.content = cursor.getString(1);
				db.type = cursor.getLong(2);
				db.readed = cursor.getLong(3);
				db.op_time = cursor.getString(4);

				result.add(db);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static int getTextServiceUnreadCount(Context context) {
		int result = 0;
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT COUNT(*) " +
				"FROM TextService " +
				"WHERE type=1 AND readed=0;";

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			if (cursor.moveToNext()) {
				result = cursor.getInt(0);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Cursor getTextServiceList_Curosr(Context context) {
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor = null;
		final String sql =
				"SELECT * " +
				"FROM TextService " +
				"ORDER BY _id;";

		try {
			cursor = sdb.rawQuery(sql, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cursor;
	}

	public static DbTextService getTextServiceWithPrimaryKey(Context context, Number primaryKey) {
		DbTextService result = new DbTextService();
		SQLiteDatabase sdb = getDb(context, false);
		Cursor cursor;
		final String sql =
				"SELECT * " +
				"FROM TextService " +
				"WHERE _id=" + primaryKey.longValue();

		if ((cursor = sdb.rawQuery(sql, null)) != null) {
			if (cursor.moveToNext()) {
				DbTextService db = new DbTextService();

				result._id = cursor.getLong(0);
				result.content = cursor.getString(1);
				result.type = cursor.getLong(2);
				result.readed = cursor.getLong(3);
				result.op_time = cursor.getString(4);
			}
			cursor.close();
		}

		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithTextService(Context context, DbTextService db) {
		Number result = null;
		SQLiteDatabase sdb = getDb(context, true);
		result = newItemWithTextService(context, sdb, db);
		MainDB.closeDB(sdb);

		return result;
	}

	public static Number newItemWithTextService(Context context, SQLiteDatabase sdb, DbTextService db) {
		Number result = null;

		try {
			sdb.execSQL(
					"INSERT INTO TextService (" +
					"content,type,readed,op_time" +
					")VALUES(" +
					"'" + sqlData(db.content) + "'," +
					db.type + "," +
					db.readed + "," +
					"date('now')" +
					");");

			result = getLastInsertRowID(sdb);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void deleteTextService(Context context) {
		SQLiteDatabase sdb = getDb(context, true);

		try {
			resetSeq(context, "TextService");
			sdb.execSQL("DELETE FROM TextService;");
		} catch (Exception e) {
			e.printStackTrace();
		}

		MainDB.closeDB(sdb);
	}

	public static void updateTextServiceAllReaded(Context context) {
		SQLiteDatabase sdb = getDb(context, true);

		try {
			sdb.execSQL("UPDATE TextService SET readed=1;");
		} catch (Exception e) {
			e.printStackTrace();
		}

		MainDB.closeDB(sdb);
	}

}
