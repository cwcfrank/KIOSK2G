package tw.com.hokei.kiosk2g.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MainDbHelper extends SQLiteOpenHelper {
	final private static int _DB_VERSION = 1;
	final private static String _DB_DATABASE_NAME = "kiosk.db";
	private static Context context = null;
    
    protected static Object lockObject = new Object();

	public MainDbHelper(Context context) {
		super(context, _DB_DATABASE_NAME, null, _DB_VERSION);
	}

    public MainDbHelper(Context context, String name, CursorFactory factory, int version) {
    	super(context, name, factory, version);
    	this.context = context;
    }
    
    private void execSQL(SQLiteDatabase db, String sql) {
    	try {
    		db.execSQL(sql);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	@Override
    public void onCreate(SQLiteDatabase db) {
		execSQL(db,
				"CREATE TABLE IF NOT EXISTS UserConfig (" +
						"ID TEXT PRIMARY KEY, " +
						"VAL TEXT NULL " +
						");");

		execSQL(db,
				"CREATE TABLE IF NOT EXISTS BUFFER (" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"NAME TEXT" +
						")");

		execSQL(db,
				"CREATE TABLE IF NOT EXISTS ItemColor (" +
					"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"item_id NUMERIC," +
					"color TEXT," +
					"image_url TEXT," +
					"image_file_name TEXT" +
					")");

		execSQL(db,
				"CREATE TABLE IF NOT EXISTS ItemType (" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"item_id NUMERIC," +
						"title TEXT," +
						"image_url TEXT," +
						"image_file_name TEXT" +
						")");

		execSQL(db,
				"CREATE TABLE IF NOT EXISTS ItemDemo (" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"item_id NUMERIC," +
						"path TEXT," +
						"image_url TEXT" +
						")");

		execSQL(db,
				"CREATE TABLE IF NOT EXISTS TempOrder (" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"member_id TEXT," +
						"ring_type TEXT," +
						"ring_color TEXT," +
						"ring_diameter TEXT," +
						"left_image_file_name TEXT," +
						"front_image_file_name TEXT," +
						"right_image_file_name TEXT" +
						")");

		execSQL(db,
				"CREATE TABLE IF NOT EXISTS TempRegister (" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"first_name TEXT," +
						"last_name TEXT," +
						"phone_country TEXT," +
						"phone_code TEXT," +
						"phone TEXT," +
						"email TEXT" +
						")");

		execSQL(db,
				"CREATE TABLE IF NOT EXISTS TempMemberOrder (" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"first_name TEXT," +
						"last_name TEXT," +
						"phone_country TEXT," +
						"phone_code TEXT," +
						"phone TEXT," +
						"email TEXT," +
						"ring_type TEXT," +
						"ring_color TEXT," +
						"ring_diameter TEXT," +
						"left_image_file_name TEXT," +
						"front_image_file_name TEXT," +
						"right_image_file_name TEXT" +
						")");

		execSQL(db,
				"CREATE TABLE IF NOT EXISTS TextService (" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"content TEXT," +
						"type NUMERIC," +
						"readed NUMERIC," +
						"op_time DATETIME" +
						")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

	public static String sqlData(String data) {
		return data.replace("'", "''");
	}
}
