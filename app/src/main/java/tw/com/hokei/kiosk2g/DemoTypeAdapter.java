package tw.com.hokei.kiosk2g;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tw.com.hokei.kiosk2g.model.DbItemType;

public class DemoTypeAdapter extends RecyclerView.Adapter<DemoTypeAdapter.ViewHolder> {
    public int selectedIndex = -1;

    public interface SelectItem2TypeAdapterDelegate {
        void onTypeItemClick(int position);
    }

    private String itemTypePathName = null;

    public SelectItem2TypeAdapterDelegate delegate = null;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView itemTypeImageView;
        public ImageView itemTypeSelectedImageView;
        public SelectItem2TypeAdapterDelegate delegate = null;

        public ViewHolder(View itemView) {
            super(itemView);

            itemTypeImageView = (ImageView) itemView.findViewById(R.id.itemTypeImageView);
            itemTypeSelectedImageView = (ImageView) itemView.findViewById(R.id.itemTypeSelectedImageView);
            itemTypeImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            delegate.onTypeItemClick(getAdapterPosition());
        }
    }

    private List<DbItemType> typeList = new ArrayList<>();

    public DemoTypeAdapter(List<DbItemType> typeList) {
        this.typeList = typeList;
    }

    @Override
    public int getItemCount() {
        return typeList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View typeView = LayoutInflater.from(context).inflate(R.layout.item_demo_type, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(typeView);

        viewHolder.delegate = delegate;
        itemTypePathName = Global.getItemTypePathName(context);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        DbItemType db = typeList.get(position);

        if (db == null) return;

        ImageView itemTypeImageView = viewHolder.itemTypeImageView;
        ImageView itemTypeSelectedImageView = viewHolder.itemTypeSelectedImageView;
        String fileName = itemTypePathName + File.separator + db.imageFileName;

        itemTypeImageView.setImageBitmap(Global.loadBitmapFromFile(fileName));
        itemTypeSelectedImageView.setImageBitmap(Global.loadBitmapFromFile(fileName));

        if (selectedIndex == position) {
            itemTypeSelectedImageView.setVisibility(View.VISIBLE);
        } else {
            itemTypeSelectedImageView.setVisibility(View.GONE);
        }
    }
}
