package com.example.taranood.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.taranood.R;
import com.example.taranood.models.WatchItem;

import java.util.List;

public class WatchAdapter extends RecyclerView.Adapter<WatchAdapter.ViewHolder> {

    private List<WatchItem> items;
    private OnItemClickListener listener;
    private boolean selectionMode = false;
    private java.util.Set<String> selectedIds = new java.util.HashSet<>();

    public interface OnItemClickListener {
        void onItemClick(WatchItem item);
        void onFavoriteClick(WatchItem item, int position);
        void onLongClick(WatchItem item);
        void onRewatchClick(WatchItem item);
        void onDetailClick(WatchItem item);
        void onWatchLinkClick(WatchItem item);
    }

    public WatchAdapter(List<WatchItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setSelectionMode(boolean enabled) {
        this.selectionMode = enabled;
        if (!enabled) selectedIds.clear();
        notifyDataSetChanged();
    }

    public void toggleSelection(String id) {
        if (selectedIds.contains(id)) selectedIds.remove(id);
        else selectedIds.add(id);
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (WatchItem item : items) {
            selectedIds.add(item.getId());
        }
        notifyDataSetChanged();
    }

    public java.util.Set<String> getSelectedIds() {
        return selectedIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watch_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WatchItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.typeStatus.setText(item.getType() + " | " + item.getStatus());

        if (item.getImageUri() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUri())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.poster);
        } else {
            holder.poster.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Set Progress
        if ("Movie".equals(item.getType())) {
            holder.progressText.setText("Progress: " + item.getMinutesWatched() + "/" + item.getTotalRuntime() + " min");
            if (item.getTotalRuntime() > 0) {
                holder.progressBar.setProgress((item.getMinutesWatched() * 100) / item.getTotalRuntime());
            } else {
                holder.progressBar.setProgress(0);
            }
        } else if ("Series".equals(item.getType()) || "Anime".equals(item.getType())) {
            holder.progressText.setText("Eps: " + item.getEpisodesWatched() + "/" + item.getTotalEpisodes());
            if (item.getTotalEpisodes() > 0) {
                holder.progressBar.setProgress((item.getEpisodesWatched() * 100) / item.getTotalEpisodes());
            } else {
                holder.progressBar.setProgress(0);
            }
        } else {
            holder.progressText.setText(item.getCustomProgress());
            holder.progressBar.setVisibility(View.GONE);
        }

        holder.btnFavorite.setImageResource(item.isFavorite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

        // Rewatch button visibility
        if ("Completed".equals(item.getStatus())) {
            holder.btnRewatch.setVisibility(View.VISIBLE);
        } else {
            holder.btnRewatch.setVisibility(View.GONE);
        }

        // Watch link button: show only if a link is set and not completed
        String watchLink = item.getWatchLink();
        if (watchLink != null && !watchLink.trim().isEmpty() && !"Completed".equals(item.getStatus())) {
            holder.btnWatchLink.setVisibility(View.VISIBLE);
            holder.btnWatchLink.setOnClickListener(v -> listener.onWatchLinkClick(item));
        } else {
            holder.btnWatchLink.setVisibility(View.GONE);
        }

        // Selection overlay
        holder.itemView.setBackgroundColor(selectedIds.contains(item.getId()) ? 0x661E8EE6 : 0x00000000);

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(item.getId());
            } else {
                listener.onItemClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(item);
            return true;
        });

        holder.btnFavorite.setOnClickListener(v -> listener.onFavoriteClick(item, position));
        holder.btnRewatch.setOnClickListener(v -> listener.onRewatchClick(item));
        holder.btnDetails.setOnClickListener(v -> listener.onDetailClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<WatchItem> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, typeStatus, progressText;
        ProgressBar progressBar;
        ImageButton btnFavorite, btnRewatch, btnDetails, btnWatchLink;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.item_poster);
            title = itemView.findViewById(R.id.item_title);
            typeStatus = itemView.findViewById(R.id.item_type_status);
            progressText = itemView.findViewById(R.id.item_progress_text);
            progressBar = itemView.findViewById(R.id.item_progress_bar);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            btnRewatch = itemView.findViewById(R.id.btn_rewatch);
            btnDetails = itemView.findViewById(R.id.btn_details);
            btnWatchLink = itemView.findViewById(R.id.btn_watch_link);
        }
    }
}