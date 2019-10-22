package com.iita.akilimo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.iita.akilimo.R;
import com.iita.akilimo.models.Recommendations;
import com.iita.akilimo.utils.ItemAnimation;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdapterListAnimation extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Recommendations> items;

    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private int animation_type;
    private int lastPosition = -1;
    private int layoutId;
    private boolean on_attach = true;

    public interface OnItemClickListener {
        void onItemClick(View view, Recommendations obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListAnimation(Context context) {
        this.context = context;
        this.layoutId = R.layout.item_card_recommendation_arrow;
    }

    public AdapterListAnimation(Context context, int layoutId) {
        this.context = context;
        this.layoutId = layoutId;
    }

    public void setItems(List<Recommendations> items, int animation_type) {
        this.items = items;
        this.animation_type = animation_type;
        notifyDataSetChanged();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public View cardView;
        public View contentLayout;

        public OriginalViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            cardView = view.findViewById(R.id.lyt_parent);
            contentLayout = view.findViewById(R.id.contentLayout);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        viewHolder = new OriginalViewHolder(view);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            Recommendations rec = items.get(position);
            view.name.setText(rec.getRecommendationName());

            view.cardView.setOnClickListener(view1 -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view1, items.get(position), position);
                }
            });
            setAnimation(view.itemView, position);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, on_attach ? position : -1, animation_type);
            lastPosition = position;
        }
    }

}