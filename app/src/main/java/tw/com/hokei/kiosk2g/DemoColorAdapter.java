package tw.com.hokei.kiosk2g;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import tw.com.hokei.kiosk2g.database.MainDB;
import tw.com.hokei.kiosk2g.model.DbItemColor;

public class DemoColorAdapter extends ResourceCursorAdapter {
    public int selectedIndex = -1;

    public DemoColorAdapter(Context context, Cursor c) {
        super(context, R.layout.item_demo_color, c, 0);
    }

    @Override
    public void bindView(View view, final Context context, Cursor c) {
        ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(c, cv);
        DbItemColor db = MainDB.getItemColorWithPrimaryKey(context, cv.getAsInteger("_id"));

        if (db == null) return;

        ImageView itemColorImageView = (ImageView)view.findViewById(R.id.itemColorImageView);
        ImageView itemColorSelectedImageView = (ImageView)view.findViewById(R.id.itemColorSelectedImageView);
        String fileName = Global.getItemColorPathName(context) + File.separator + db.imageFileName;

        itemColorImageView.setImageBitmap(Global.loadBitmapFromFile(fileName));
        //itemColorSelectedImageView.setImageBitmap(Global.loadBitmapFromFile(fileName));

        if (selectedIndex == c.getPosition()) {
            itemColorSelectedImageView.setVisibility(View.VISIBLE);
        } else {
            itemColorSelectedImageView.setVisibility(View.GONE);
        }
    }
}
