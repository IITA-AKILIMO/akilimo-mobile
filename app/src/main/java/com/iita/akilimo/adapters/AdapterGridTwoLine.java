package com.iita.akilimo.adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.iita.akilimo.R;
import com.iita.akilimo.models.CurrentFieldYield;
import com.iita.akilimo.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class AdapterGridTwoLine extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CurrentFieldYield> items = new ArrayList<>();

    private OnLoadMoreListener onLoadMoreListener;

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private int rowIndex = -1;

    public interface OnItemClickListener {
        void onItemClick(View view, CurrentFieldYield obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterGridTwoLine(Context context, List<CurrentFieldYield> items) {
        this.items = items;
        ctx = context;
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
        CurrentFieldYield obj = items.get(position);
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            view.name.setText(obj.getFieldYieldLabel());
            Tools.displayImageOriginal(ctx, view.image, obj.getImageId());
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });
            if (rowIndex == position) {
//            holder.itemView.setBackgroundColor(ctx.getResources().getColor(R.color.colorAccent));
                view.selectionIndicator.setImageResource(R.drawable.ic_done);
                view.selectionIndicator.setColorFilter(ctx.getResources().getColor(R.color.colorAccent));

            } else {
//            holder.itemView.setBackgroundColor(ctx.getResources().getColor(R.color.grey_5));
                view.selectionIndicator.setImageResource(R.drawable.ic_info);
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

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

}