package com.iita.akilimo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.iita.akilimo.R;
import com.iita.akilimo.models.Fertilizer;
import com.iita.akilimo.models.InterCropFertilizer;
import com.iita.akilimo.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class IntercropFertilizerGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<InterCropFertilizer> items = new ArrayList<>();

    private OnLoadMoreListener onLoadMoreListener;

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private int rowIndex = -1;

    public interface OnItemClickListener {
        void onItemClick(View view, Fertilizer obj, int position);
    }

    public IntercropFertilizerGridAdapter(@NonNull Context context) {
        ctx = context;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }


    public void setItems(@NonNull List<InterCropFertilizer> fertilizerList) {
        this.items = fertilizerList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_fertilizer_grid_row, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        InterCropFertilizer obj = items.get(position);
        String fertilizerName = obj.getName();
        String bagPrice = obj.getPriceRange();
        boolean isSelected = obj.isSelected();

        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            view.fertilizerName.setText(fertilizerName);
            view.bagPrice.setText(isSelected ? bagPrice : fertilizerName);
            view.lyt_parent.setOnClickListener(view1 -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view1, obj, position);
                }
            });
            if (isSelected) {
                view.selectionIndicator.setImageResource(R.drawable.ic_check_box_checked);
                view.selectionIndicator.setColorFilter(ctx.getResources().getColor(R.color.colorAccent));
                Tools.displayImageOriginal(ctx, view.image, R.drawable.ic_sack_solid);
            } else {
                view.selectionIndicator.setImageResource(R.drawable.ic_check_box_unchecked);
                view.selectionIndicator.setColorFilter(ctx.getResources().getColor(R.color.grey_5));
                Tools.displayImageOriginal(ctx, view.image, R.drawable.ic_sack_outline);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setActiveRowIndex(int position) {
        rowIndex = position;
    }


    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public List<InterCropFertilizer> getAll() {
        return items;
    }

    public List<InterCropFertilizer> getSelected() {
        List<InterCropFertilizer> selected = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isSelected()) {
                selected.add(items.get(i));
            }
        }
        return selected;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView fertilizerName;
        public TextView bagPrice;
        public AppCompatImageButton selectionIndicator;
        public CardView lyt_parent;
        public View bagPricePanel;

        public OriginalViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.fertilizerImage);
            fertilizerName = view.findViewById(R.id.fertilizerName);
            bagPrice = view.findViewById(R.id.bagPrice);
            lyt_parent = view.findViewById(R.id.lyt_parent);
            bagPricePanel = view.findViewById(R.id.bagPricePanel);
            selectionIndicator = view.findViewById(R.id.selectionIndicator);
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

}