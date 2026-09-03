package com.example.taranood.utils;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.taranood.R;
import com.example.taranood.models.WatchItem;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogUtils {

    public static void showWatchItemDetails(Context context, WatchItem item) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_watch_details, null);

        ImageView poster = view.findViewById(R.id.detail_poster);
        TextView title = view.findViewById(R.id.detail_title);
        TextView typeStatus = view.findViewById(R.id.detail_type_status);
        TextView progressValue = view.findViewById(R.id.detail_progress_value);
        TextView notesValue = view.findViewById(R.id.detail_notes_value);

        title.setText(item.getTitle());
        typeStatus.setText(item.getType() + " | " + item.getStatus());

        if (item.getImageUri() != null) {
            Glide.with(context)
                    .load(item.getImageUri())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(poster);
        } else {
            poster.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        if ("Movie".equals(item.getType())) {
            progressValue.setText(context.getString(R.string.movie_progress, item.getMinutesWatched(), item.getTotalRuntime()));
        } else if ("Series".equals(item.getType()) || "Anime".equals(item.getType())) {
            progressValue.setText(context.getString(R.string.series_progress, item.getEpisodesWatched(), item.getTotalEpisodes()));
        } else {
            progressValue.setText(item.getCustomProgress());
        }

        if (item.getNotes() != null && !item.getNotes().isEmpty()) {
            notesValue.setText(item.getNotes());
        } else {
            notesValue.setText(R.string.no_notes);
        }

        new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setPositiveButton(R.string.close, null)
                .show();
    }
}