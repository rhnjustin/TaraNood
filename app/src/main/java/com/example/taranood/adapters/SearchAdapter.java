package com.example.taranood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.taranood.R;
import com.example.taranood.api.TMDbModels;
import com.example.taranood.api.TMDbService;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<TMDbModels.SearchResult> results;
    private OnResultClickListener listener;

    public interface OnResultClickListener {
        void onResultClick(TMDbModels.SearchResult result);
    }

    public SearchAdapter(List<TMDbModels.SearchResult> results, OnResultClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    public void updateResults(List<TMDbModels.SearchResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TMDbModels.SearchResult result = results.get(position);
        
        String title = "tv".equals(result.mediaType) ? result.name : result.title;
        holder.titleText.setText(title != null ? title : "Unknown Title");
        
        String date = "tv".equals(result.mediaType) ? result.firstAirDate : result.releaseDate;
        String type = result.mediaType != null ? result.mediaType.toUpperCase() : "UNKNOWN";
        String info = type + (date != null && !date.isEmpty() ? " | " + date.substring(0, Math.min(date.length(), 4)) : "");
        holder.infoText.setText(info);

        if (result.posterPath != null) {
            Glide.with(holder.itemView.getContext())
                    .load(TMDbService.IMAGE_BASE_URL + result.posterPath)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.posterImage);
        } else {
            holder.posterImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> listener.onResultClick(result));
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;
        TextView titleText, infoText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.result_poster);
            titleText = itemView.findViewById(R.id.result_title);
            infoText = itemView.findViewById(R.id.result_info);
        }
    }
}
