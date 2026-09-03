package com.example.taranood.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.taranood.MainActivity;
import com.example.taranood.R;
import com.example.taranood.models.WatchItem;
import com.example.taranood.utils.StorageHelper;

import java.util.List;

public class SettingsFragment extends Fragment {

    private ImageView profileImage;
    private TextView profileName, profileAge;
    private TextView tvTotalMovies, tvTotalSeries, tvTotalAnime, tvTotalOther, tvTotalCompleted, tvTotalFavorites, tvTotalWatchTime, tvEpisodesWatched;
    private android.widget.LinearLayout logsContainer;
    private TextView tvNoLogs;

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(View.VISIBLE);
        }
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        getContext().getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                    StorageHelper.saveUserProfile(getContext(), StorageHelper.getUserName(getContext()), StorageHelper.getUserAge(getContext()), imageUri.toString());
                    loadProfile();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileAge = view.findViewById(R.id.profile_age);

        tvTotalMovies = view.findViewById(R.id.tv_stat_total_movies);
        tvTotalSeries = view.findViewById(R.id.tv_stat_total_series);
        tvTotalAnime = view.findViewById(R.id.tv_stat_total_anime);
        tvTotalOther = view.findViewById(R.id.tv_stat_total_other);
        tvTotalCompleted = view.findViewById(R.id.tv_stat_total_completed);
        tvTotalFavorites = view.findViewById(R.id.tv_stat_total_favorites);
        tvTotalWatchTime = view.findViewById(R.id.tv_stat_total_watch_time);
        tvEpisodesWatched = view.findViewById(R.id.tv_stat_episodes_watched);

        logsContainer = view.findViewById(R.id.layout_logs_container);
        tvNoLogs = view.findViewById(R.id.tv_no_logs);

        loadProfile();
        updateStatistics();
        updateLogs();

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        Button btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Top Right Buttons Navigation
        ImageButton btnAboutTop = view.findViewById(R.id.btn_about_top);
        btnAboutTop.setOnClickListener(v -> navigateTo(new AboutFragment()));

        ImageButton btnSettingsTop = view.findViewById(R.id.btn_settings_top);
        btnSettingsTop.setOnClickListener(v -> navigateTo(new AppSettingsFragment()));

        view.findViewById(R.id.btn_see_all_logs).setOnClickListener(v -> navigateTo(new ActivityLogFragment()));

        return view;
    }

    private void updateLogs() {
        if (getContext() == null) return;
        logsContainer.removeAllViews();
        // Just add the empty state view back since we removed all views
        logsContainer.addView(tvNoLogs);

        List<com.example.taranood.models.LogEntry> allLogs = StorageHelper.getLogs(getContext());
        if (allLogs.isEmpty()) {
            tvNoLogs.setVisibility(View.VISIBLE);
        } else {
            tvNoLogs.setVisibility(View.GONE);
            int previewCount = StorageHelper.getLogPreviewCount(getContext());
            int count = Math.min(allLogs.size(), previewCount);
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy – h:mm a", java.util.Locale.getDefault());

            for (int i = 0; i < count; i++) {
                com.example.taranood.models.LogEntry log = allLogs.get(i);
                View logView = LayoutInflater.from(getContext()).inflate(R.layout.item_log, logsContainer, false);

                TextView tvDate = logView.findViewById(R.id.tv_log_date);
                TextView tvAction = logView.findViewById(R.id.tv_log_action);
                TextView tvDetails = logView.findViewById(R.id.tv_log_details);
                View iconContainer = logView.findViewById(R.id.log_icon_container);
                ImageView ivIcon = logView.findViewById(R.id.iv_log_icon);

                tvDate.setText(dateFormat.format(new java.util.Date(log.getTimestamp())));
                tvAction.setText(log.getAction());

                if (log.getDetails() == null || log.getDetails().isEmpty()) {
                    tvDetails.setVisibility(View.GONE);
                } else {
                    tvDetails.setVisibility(View.VISIBLE);
                    tvDetails.setText(log.getDetails());
                }

                // Setup icon and color (mirrors LogAdapter)
                int color = androidx.core.content.ContextCompat.getColor(getContext(), R.color.sky_blue);
                int iconRes;
                String action = log.getAction().toLowerCase();

                if (action.contains("added")) {
                    iconRes = android.R.drawable.ic_input_add;
                } else if (action.contains("updated") || action.contains("progress")) {
                    iconRes = android.R.drawable.ic_popup_sync;
                } else if (action.contains("status")) {
                    iconRes = android.R.drawable.ic_menu_manage;
                } else if (action.contains("completed") || action.contains("marked as completed")) {
                    iconRes = android.R.drawable.btn_star_big_on;
                } else if (action.contains("favorite") || action.contains("unfavorited")) {
                    iconRes = android.R.drawable.btn_star;
                } else if (action.contains("deleted") || action.contains("removed")) {
                    iconRes = android.R.drawable.ic_menu_delete;
                } else {
                    iconRes = android.R.drawable.ic_menu_info_details;
                }

                iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
                ivIcon.setImageResource(iconRes);

                logsContainer.addView(logView);
            }
        }
    }

    private void navigateTo(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText editName = dialogView.findViewById(R.id.edit_user_name);
        EditText editAge = dialogView.findViewById(R.id.edit_user_age);

        editName.setText(StorageHelper.getUserName(getContext()));
        editAge.setText(String.valueOf(StorageHelper.getUserAge(getContext())));

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = editName.getText().toString();
                    int age = 0;
                    try {
                        age = Integer.parseInt(editAge.getText().toString());
                    } catch (Exception ignored) {}
                    StorageHelper.saveUserProfile(getContext(), name, age, StorageHelper.getUserImage(getContext()));
                    loadProfile();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadProfile() {
        profileName.setText(StorageHelper.getUserName(getContext()));
        profileAge.setText("Age: " + StorageHelper.getUserAge(getContext()));
        String imgUri = StorageHelper.getUserImage(getContext());
        if (imgUri != null) {
            profileImage.setImageURI(Uri.parse(imgUri));
        } else {
            profileImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void updateStatistics() {
        List<WatchItem> items = StorageHelper.loadWatchItems(getContext());
        int movies = 0;
        int series = 0;
        int anime = 0;
        int other = 0;
        int completed = 0;
        int favorites = 0;
        int totalMinutes = 0;
        int episodes = 0;

        for (WatchItem item : items) {
            String type = item.getType();
            if ("Movie".equalsIgnoreCase(type)) {
                movies++;
            } else if ("Series".equalsIgnoreCase(type)) {
                series++;
            } else if ("Anime".equalsIgnoreCase(type)) {
                anime++;
            } else {
                other++;
            }

            if ("Completed".equalsIgnoreCase(item.getStatus())) {
                completed++;
            }

            if (item.isFavorite()) {
                favorites++;
            }

            totalMinutes += item.getMinutesWatched();
            episodes += item.getEpisodesWatched();
        }

        tvTotalMovies.setText(String.valueOf(movies));
        tvTotalSeries.setText(String.valueOf(series));
        tvTotalAnime.setText(String.valueOf(anime));
        tvTotalOther.setText(String.valueOf(other));
        tvTotalCompleted.setText(String.valueOf(completed));
        tvTotalFavorites.setText(String.valueOf(favorites));
        tvEpisodesWatched.setText(String.valueOf(episodes));

        int h = totalMinutes / 60;
        int m = totalMinutes % 60;
        tvTotalWatchTime.setText(getString(R.string.hours_minutes_suffix, h, m));
    }
}
