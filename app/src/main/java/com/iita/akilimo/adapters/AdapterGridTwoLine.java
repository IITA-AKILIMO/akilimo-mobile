package com.iita.akilimo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.iita.akilimo.R;
import com.iita.akilimo.entities.FieldYield;
import com.iita.akilimo.utils.Tools;

import java.util.ArrayList;
import java.util.List;


public class AdapterGridTwoLine extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FieldYield> items = new ArrayList<>();


    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private int rowIndex = -1;
    private double selectedYieldAmount = 0.0;

    public interface OnItemClickListener {
        void onItemClick(View view, FieldYield obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterGridTwoLine(Context context) {
        ctx = context;
    }

    public void setItems(double selectedYieldAmount, @NonNull List<FieldYield> items) {
        this.items = items;
        this.selectedYieldAmount = selectedYieldAmount;
        notifyDataSetChanged();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public AppCompatImageButton selectionIndicator;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            selectionIndicator = (AppCompatImageButton) v.findViewById(R.id.selectionIndicator);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_image_two_line, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        FieldYield fieldYield =items.get(position);
        String yieldLabel = fieldYield.getFieldYieldLabel();
        double currentYieldAmount = fieldYield.getYieldAmount();
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            view.name.setText(yieldLabel);
            Tools.displayImageOriginal(ctx, view.image, fieldYield.getImageId());

            view.lyt_parent.setOnClickListener(view1 -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view1, items.get(position), position);
                }
            });
            if ((rowIndex == position) || (currentYieldAmount == selectedYieldAmount)) {
                view.selectionIndicator.setImageResource(R.drawable.ic_radio_button_checked);
                view.selectionIndicator.setColorFilter(ctx.getResources().getColor(R.color.colorAccent));
            } else {
                view.selectionIndicator.setImageResource(R.drawable.ic_radio_button_unchecked);
                view.selectionIndicator.setColorFilter(ctx.getResources().getColor(R.color.grey_5));
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
}
