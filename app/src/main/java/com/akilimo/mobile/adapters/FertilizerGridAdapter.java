package com.akilimo.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.akilimo.mobile.R;
import com.akilimo.mobile.entities.Fertilizer;
import com.akilimo.mobile.utils.CurrencyCode;
import com.akilimo.mobile.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class FertilizerGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Fertilizer> items = new ArrayList<>();

    private OnLoadMoreListener onLoadMoreListener;

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private int rowIndex = -1;

    public interface OnItemClickListener {
        void onItemClick(View view, Fertilizer obj, int position);
    }

    public FertilizerGridAdapter(@NonNull Context context) {
        ctx = context;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }


    public void setItems(@NonNull List<Fertilizer> fertilizerList) {
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
        Fertilizer obj = items.get(position);
        String fertilizerName = obj.getName();
        String bagPrice = obj.getPriceRange();

        boolean isSelected = obj.getSelected();

        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            view.fertilizerName.setText(fertilizerName);
            view.bagPrice.setText(isSelected ? bagPrice : null);

            view.lyt_parent.setOnClickListener(v -> clickListener(v, obj, position));
            Tools.displayImageOriginal(ctx, view.image, R.drawable.ic_fertilizer_bag);
            if (isSelected) {
                view.lyt_parent.setCardBackgroundColor(ctx.getResources().getColor(R.color.green_200));
            } else {
                view.lyt_parent.setCardBackgroundColor(ctx.getResources().getColor(R.color.grey_5));
            }

        }
    }

    private void clickListener(View view, Fertilizer fertilizer, int position) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, fertilizer, position);
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

    public List<Fertilizer> getAll() {
        return items;
    }

    public List<Fertilizer> getSelected() {
        List<Fertilizer> selected = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getSelected()) {
                selected.add(items.get(i));
            }
        }
        return selected;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView fertilizerName;
        public TextView bagPrice;
        public CardView lyt_parent;

        public OriginalViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.fertilizerImage);
            fertilizerName = view.findViewById(R.id.fertilizerName);
            bagPrice = view.findViewById(R.id.bagPrice);
            lyt_parent = view.findViewById(R.id.lyt_parent);
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

}
