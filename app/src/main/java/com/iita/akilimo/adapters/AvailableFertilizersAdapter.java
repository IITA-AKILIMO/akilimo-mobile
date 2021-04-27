package com.iita.akilimo.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iita.akilimo.R;
import com.iita.akilimo.entities.Fertilizer;
import com.iita.akilimo.interfaces.IRecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;

public class AvailableFertilizersAdapter extends RecyclerView.Adapter<AvailableFertilizersAdapter.ViewHolder> {
    private static final String LOG_TAG = AvailableFertilizersAdapter.class.getSimpleName();

    private Context context;
    private List<Fertilizer> fertilizerList = new ArrayList<>();
    private LayoutInflater inflater;
    private SparseBooleanArray mSelectedItemsIds;
    private IRecyclerViewClickListener clickListener;

    public AvailableFertilizersAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public void setItems(List<Fertilizer> fertilizerList) {
        this.fertilizerList = fertilizerList;
    }

    @Override
    public int getItemCount() {
        return fertilizerList.size();
    }


    @Override
    public long getItemId(int itemId) {
        return itemId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = inflater.inflate(R.layout.list_custom_checkbox_row_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        Fertilizer fertilizer = fertilizerList.get(position);
        double price = fertilizer.getPricePerBag();
        String fertilizerName = fertilizer.getName();
        String bagPrice = fertilizer.getPriceRange();
        boolean isSelected = fertilizer.getSelected();

        viewHolder.label.setText(fertilizerName);
        viewHolder.bagPrice.setText(bagPrice != null ? bagPrice : "NA");

        if (isSelected) {
            viewHolder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.deep_orange_900));
            updateSelectedStatus(position, true);
        } else {
            viewHolder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.deep_orange_50));
        }
    }

    public void setClickListener(IRecyclerViewClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * Check the Checkbox if not checked
     **/
    private void updateSelectedStatus(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, true);
        } else {
            mSelectedItemsIds.delete(position);
        }
    }

    /**
     * Return the selected Checkbox IDs
     **/
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    public boolean isFertilizerAvailable(int position) {
        return mSelectedItemsIds.get(position);
    }


    @SuppressWarnings("WeakerAccess")
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView label;
        private TextView bagPrice;
        private LinearLayout linearLayout;

        private ViewHolder(@NonNull View view) {
            super(view);
            label = view.findViewById(R.id.label);
            bagPrice = view.findViewById(R.id.bagPrice);
            linearLayout = view.findViewById(R.id.labelCardLayout);

            view.setOnClickListener(holderView -> {
                if (clickListener != null) {
                    processClickAction(holderView);
                }
            });

            view.setOnLongClickListener(holderViewLongClick -> {
                if (clickListener != null) {
                    clickListener.itemClicked(holderViewLongClick, getAdapterPosition(), true);
                }

                return true;
            });
        }

        private void processClickAction(View view) {
            int adapterPosition = getAdapterPosition();
            boolean check = !isFertilizerAvailable(adapterPosition);
            updateSelectedStatus(adapterPosition, check);
            clickListener.itemClicked(view, adapterPosition, false);
        }
    }

}
