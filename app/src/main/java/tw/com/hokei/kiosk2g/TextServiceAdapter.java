package tw.com.hokei.kiosk2g;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import tw.com.hokei.kiosk2g.database.MainDB;
import tw.com.hokei.kiosk2g.model.DbItemColor;
import tw.com.hokei.kiosk2g.model.DbTextService;

public class TextServiceAdapter extends ResourceCursorAdapter {
    public int selectedIndex = -1;

    public TextServiceAdapter(Context context, Cursor c) {
        super(context, R.layout.item_text_service, c, 0);
    }

    @Override
    public void bindView(View view, final Context context, Cursor c) {
        ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(c, cv);
        DbTextService db = MainDB.getTextServiceWithPrimaryKey(context, cv.getAsInteger("_id"));

        if (db == null) return;

        View customerCenterView = view.findViewById(R.id.customerCenterView);
        View customerView = view.findViewById(R.id.customerView);
        ImageView customerImageView = view.findViewById(R.id.text_service_customer);

        TextView contentTextView;
        int type = db.type.intValue();


        customerCenterView.setVisibility(type == 1 ? View.VISIBLE : View.GONE);
        customerView.setVisibility(type == 2 ? View.VISIBLE : View.GONE);

        if (type == 1) {
            contentTextView = view.findViewById(R.id.customerCenterTextView);
            contentTextView.setText(db.content);
        } else if (type == 2) {
            contentTextView = view.findViewById(R.id.customerTextView);
            contentTextView.setText(db.content);
        }
    }
}
