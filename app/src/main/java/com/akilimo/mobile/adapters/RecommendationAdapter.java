package com.akilimo.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.akilimo.mobile.R;
import com.akilimo.mobile.mappers.ComputedResponse;

import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecViewHolder> {

    private List<ComputedResponse> computedResponseList;

    public RecommendationAdapter() {

    }

    public void setData(@NonNull List<ComputedResponse> computedResponseList) {
        this.computedResponseList = computedResponseList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_compute_card, viewGroup, false);
        RecViewHolder pvh = new RecViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecViewHolder recViewHolder, int i) {
        ComputedResponse cr = computedResponseList.get(i);
        recViewHolder.computedTitle.setText(cr.getComputedTitle());
        recViewHolder.computedBody.setText(cr.getComputedRecommendation());
    }

    @Override
    public int getItemCount() {
        return computedResponseList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @SuppressWarnings("WeakerAccess")
    public static class RecViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView computedTitle;
        TextView computedBody;

        RecViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cv);
            computedTitle = itemView.findViewById(R.id.recLabel);
            computedBody = itemView.findViewById(R.id.recSubLabel);
        }
    }
}
