package com.iita.akilimo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.iita.akilimo.R;
import com.iita.akilimo.entities.FieldYield;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.Tools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FieldYieldAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FieldYield> items;

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private int animation_type;
    private int lastPosition = -1;
    private int rowIndex = -1;
    private boolean on_attach = true;
    private double selectedYieldAmount = 0.0;


    public interface OnItemClickListener {
        void onItemClick(View view, FieldYield fieldYield, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public FieldYieldAdapter(Context context, List<FieldYield> items, int animation_type) {
        this.items = items;
        ctx = context;
        this.animation_type = animation_type;
    }

    public void setItems(double selectedYieldAmount, @NonNull List<FieldYield> items) {
        this.items = items;
        this.selectedYieldAmount = selectedYieldAmount;
        notifyDataSetChanged();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView rootYieldImage;
        public TextView name;
        public View layoutView;
        public CardView mainCard;

        public OriginalViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            rootYieldImage = v.findViewById(R.id.rootYieldImage);
            mainCard = v.findViewById(R.id.mainCard);
            layoutView = v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_recommendation_image, parent, false);
        viewHolder = new OriginalViewHolder(view);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            FieldYield fieldYield = items.get(position);
            double currentYieldAmount = fieldYield.getYieldAmount();
            view.name.setText(fieldYield.getFieldYieldLabel());
            Tools.displayImageOriginal(ctx, view.rootYieldImage, fieldYield.getImageId());

            view.layoutView.setOnClickListener(view1 -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view1, items.get(position), position);
                }
            });

            if ((rowIndex == position) || (currentYieldAmount == selectedYieldAmount)) {
                view.mainCard.setCardBackgroundColor(ctx.getResources().getColor(R.color.green_100));
            } else {
                view.mainCard.setCardBackgroundColor(ctx.getResources().getColor(R.color.grey_3));
            }

            setAnimation(view.itemView, position);
        }
    }

    public void setActiveRowIndex(int position) {
        rowIndex = position;
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
