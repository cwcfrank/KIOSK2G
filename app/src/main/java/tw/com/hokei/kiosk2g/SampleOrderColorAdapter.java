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

import tw.com.hokei.kiosk2g.model.DbItemColor;

public class SampleOrderColorAdapter extends RecyclerView.Adapter<SampleOrderColorAdapter.ViewHolder> {
    public int selectedIndex = -1;

    public interface SampleOrderColorAdapterDelegate {
        void onColorItemClick(int position);
    }

    private String itemColorPathName = null;

    public SampleOrderColorAdapterDelegate delegate = null;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView itemColorImageView;
        public ImageView itemColorSelectedImageView;
        public SampleOrderColorAdapterDelegate delegate = null;

        public ViewHolder(View itemView) {
            super(itemView);

            itemColorImageView = (ImageView) itemView.findViewById(R.id.itemColorImageView);
            itemColorSelectedImageView = (ImageView) itemView.findViewById(R.id.itemColorSelectedImageView);
            itemColorImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            delegate.onColorItemClick(getAdapterPosition());
        }
    }

    private List<DbItemColor> colorList = new ArrayList<>();

    public SampleOrderColorAdapter(List<DbItemColor> typeList) {
        this.colorList = typeList;
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View typeView = LayoutInflater.from(context).inflate(R.layout.item_sample_order_color, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(typeView);

        viewHolder.delegate = delegate;
        itemColorPathName = Global.getItemColorPathName(context);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        DbItemColor db = colorList.get(position);

        if (db == null) return;

        ImageView itemColorImageView = viewHolder.itemColorImageView;
        ImageView itemColorSelectedImageView = viewHolder.itemColorSelectedImageView;
        String fileName = itemColorPathName + File.separator + db.imageFileName;

        itemColorImageView.setImageBitmap(Global.loadBitmapFromFile(fileName));

        if (selectedIndex == position) {
            itemColorSelectedImageView.setVisibility(View.VISIBLE);
        } else {
            itemColorSelectedImageView.setVisibility(View.GONE);
        }
    }
}
