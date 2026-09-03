package com.example.taranood.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.taranood.R;
import com.example.taranood.models.WatchItem;
import com.example.taranood.utils.StorageHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class AddEditDialogFragment extends BottomSheetDialogFragment {

    private static final int PICK_IMAGE = 101;
    private String selectedImageUri = null;

    // Common views
    private ImageView posterImage;
    private TextInputEditText inputTitle, inputNotes, inputWatchLink;
    private Spinner spinnerType, spinnerStatus;
    private LinearLayout layoutMovie, layoutSeries;

    // ── Movie: Total Runtime NumberPickers ──────────────────────────
    private NumberPicker pickerTotalHours, pickerTotalMinutes;
    private TextView textRuntimeDisplay;

    // ── Movie: Watched Runtime NumberPickers (no +/- buttons) ───────
    private NumberPicker pickerWatchedHours, pickerWatchedMinutes;
    private TextView textWatchedProgress, textMoviePercent;
    private ProgressBar progressBarMovie;

    // ── Series: Total episodes EditText ───────────────────────────
    private TextInputEditText inputTotalEpisodes;
    private int totalEpisodesCount = 0;

    // ── Series: Watched episode steppers ─────────────────────────
    private MaterialButton btnEpMinus, btnEpPlus;
    private TextView textEpisodes, textEpWatchedCount, textEpPercent;
    private ProgressBar progressBarSeries;
    private int currentEpisodes = 0;

    // State
    private WatchItem currentItem;
    private List<WatchItem> allItems;
    private String itemId;

    private int totalRuntimeMins = 0;
    private int watchedMinutes = 0;

    private boolean isAutoChangingStatus = false;

    // ─────────────────────────────────────────────────────────────

    public static AddEditDialogFragment newInstance(String itemId) {
        AddEditDialogFragment fragment = new AddEditDialogFragment();
        Bundle args = new Bundle();
        args.putString("item_id", itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupTotalRuntimePickers();
        setupWatchedRuntimePickers();
        setupSpinners();
        setupSeriesControls();

        // Load item if editing
        if (getArguments() != null) {
            itemId = getArguments().getString("item_id");
        }
        allItems = StorageHelper.loadWatchItems(getContext());

        if (itemId != null) {
            ((TextView) view.findViewById(R.id.text_title_page)).setText("Edit Item");
            for (WatchItem item : allItems) {
                if (item.getId().equals(itemId)) {
                    currentItem = item;
                    break;
                }
            }
            if (currentItem != null) fillData();
        } else {
            currentItem = new WatchItem();
            currentItem.setStatus("Planned");
            String defaultType = StorageHelper.getDefaultAddType(getContext());
            currentItem.setType(defaultType);
            
            // Set initial spinner selection for new items based on preference
            setSpinnerValue(spinnerType, defaultType);
        }

        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveItem());
        view.findViewById(R.id.btn_select_image).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });
    }

    // ──────────────────────────────────────────────────────────────
    //  View binding
    // ──────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        posterImage        = view.findViewById(R.id.edit_poster);
        inputTitle         = view.findViewById(R.id.input_title);
        inputNotes         = view.findViewById(R.id.input_notes);
        inputWatchLink     = view.findViewById(R.id.input_watch_link);
        spinnerType        = view.findViewById(R.id.spinner_type);
        spinnerStatus      = view.findViewById(R.id.spinner_status);
        layoutMovie        = view.findViewById(R.id.layout_progress_movie);
        layoutSeries       = view.findViewById(R.id.layout_progress_series);

        // Total runtime
        pickerTotalHours   = view.findViewById(R.id.picker_hours);
        pickerTotalMinutes = view.findViewById(R.id.picker_minutes);
        textRuntimeDisplay = view.findViewById(R.id.text_runtime_display);

        // Watched runtime
        pickerWatchedHours   = view.findViewById(R.id.picker_watched_hours);
        pickerWatchedMinutes = view.findViewById(R.id.picker_watched_minutes);
        textWatchedProgress  = view.findViewById(R.id.text_watched_progress);
        textMoviePercent     = view.findViewById(R.id.text_movie_percent);
        progressBarMovie     = view.findViewById(R.id.progress_bar_movie);

        // Series total episodes
        inputTotalEpisodes = view.findViewById(R.id.input_total_episodes);

        // Series watched
        btnEpMinus         = view.findViewById(R.id.btn_ep_minus);
        btnEpPlus          = view.findViewById(R.id.btn_ep_plus);
        textEpisodes       = view.findViewById(R.id.text_episodes);
        textEpWatchedCount = view.findViewById(R.id.text_ep_watched_count);
        textEpPercent      = view.findViewById(R.id.text_ep_percent);
        progressBarSeries  = view.findViewById(R.id.progress_bar_series);
    }

    // ──────────────────────────────────────────────────────────────
    //  Total Runtime NumberPickers (Hours 0-10, Minutes 0-59)
    // ──────────────────────────────────────────────────────────────

    private void setupTotalRuntimePickers() {
        pickerTotalHours.setMinValue(0);
        pickerTotalHours.setMaxValue(10);
        pickerTotalHours.setValue(0);
        pickerTotalHours.setWrapSelectorWheel(false);

        pickerTotalMinutes.setMinValue(0);
        pickerTotalMinutes.setMaxValue(59);
        pickerTotalMinutes.setValue(0);
        pickerTotalMinutes.setWrapSelectorWheel(true);
        pickerTotalMinutes.setFormatter(value -> String.format("%02d", value));

        NumberPicker.OnValueChangeListener runtimeListener = (picker, oldVal, newVal) -> {
            totalRuntimeMins = pickerTotalHours.getValue() * 60 + pickerTotalMinutes.getValue();
            updateRuntimeDisplay();
            updateWatchedRuntimePickerRange();
        };

        pickerTotalHours.setOnValueChangedListener(runtimeListener);
        pickerTotalMinutes.setOnValueChangedListener(runtimeListener);
    }

    private void updateRuntimeDisplay() {
        int h = pickerTotalHours.getValue();
        int m = pickerTotalMinutes.getValue();
        textRuntimeDisplay.setText(h + " hr " + m + " min  (" + totalRuntimeMins + " min total)");
    }

    // ──────────────────────────────────────────────────────────────
    //  Watched Runtime NumberPickers (no slider, no +/- buttons)
    // ──────────────────────────────────────────────────────────────

    private void setupWatchedRuntimePickers() {
        pickerWatchedHours.setMinValue(0);
        pickerWatchedHours.setMaxValue(0);
        pickerWatchedHours.setValue(0);
        pickerWatchedHours.setWrapSelectorWheel(false);

        pickerWatchedMinutes.setMinValue(0);
        pickerWatchedMinutes.setMaxValue(0);
        pickerWatchedMinutes.setValue(0);
        pickerWatchedMinutes.setWrapSelectorWheel(false);
        pickerWatchedMinutes.setFormatter(value -> String.format("%02d", value));

        NumberPicker.OnValueChangeListener watchedListener = (picker, oldVal, newVal) -> {
            watchedMinutes = pickerWatchedHours.getValue() * 60 + pickerWatchedMinutes.getValue();
            if (watchedMinutes > totalRuntimeMins && totalRuntimeMins > 0) {
                watchedMinutes = totalRuntimeMins;
                updateWatchedRuntimePickersFromMinutes();
            }
            updateMovieWatchedUI();
        };

        pickerWatchedHours.setOnValueChangedListener(watchedListener);
        pickerWatchedMinutes.setOnValueChangedListener(watchedListener);
    }

    private void updateWatchedRuntimePickerRange() {
        boolean hasRuntime = totalRuntimeMins > 0;
        pickerWatchedHours.setEnabled(hasRuntime);
        pickerWatchedMinutes.setEnabled(hasRuntime);

        if (hasRuntime) {
            int maxHours = totalRuntimeMins / 60;
            int maxMinutes = totalRuntimeMins % 60;

            pickerWatchedHours.setMaxValue(maxHours);
            pickerWatchedMinutes.setMaxValue(59);

            if (watchedMinutes > totalRuntimeMins) {
                watchedMinutes = totalRuntimeMins;
            }
            updateWatchedRuntimePickersFromMinutes();
        } else {
            pickerWatchedHours.setMaxValue(0);
            pickerWatchedMinutes.setMaxValue(0);
            watchedMinutes = 0;
            pickerWatchedHours.setValue(0);
            pickerWatchedMinutes.setValue(0);
        }
        updateMovieWatchedUI();
    }

    private void updateWatchedRuntimePickersFromMinutes() {
        int hours = watchedMinutes / 60;
        int minutes = watchedMinutes % 60;
        pickerWatchedHours.setValue(hours);
        pickerWatchedMinutes.setValue(minutes);
    }

    private void updateMovieWatchedUI() {
        int hours = watchedMinutes / 60;
        int minutes = watchedMinutes % 60;
        textWatchedProgress.setText(hours + " hr " + minutes + " min");

        int pct = totalRuntimeMins > 0 ? (watchedMinutes * 100) / totalRuntimeMins : 0;
        textMoviePercent.setText(pct + "%");
        progressBarMovie.setProgress(pct);

        // Sync status with progress
        if (!isAutoChangingStatus) {
            isAutoChangingStatus = true;
            if (totalRuntimeMins > 0 && watchedMinutes >= totalRuntimeMins) {
                if (currentItem != null && currentItem.getMinutesWatched() > 0 && currentItem.getMinutesWatched() < totalRuntimeMins) {
                    currentItem.setLastMinutesWatched(currentItem.getMinutesWatched());
                }
                setSpinnerValue(spinnerStatus, "Completed");
            } else if (watchedMinutes > 0) {
                setSpinnerValue(spinnerStatus, "Watching");
            } else {
                if (currentItem != null && currentItem.getMinutesWatched() >= 2) {
                    currentItem.setLastMinutesWatched(currentItem.getMinutesWatched());
                }
                setSpinnerValue(spinnerStatus, "Planned");
            }
            isAutoChangingStatus = false;
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Series: Total episodes (editable text) + watched steppers
    // ──────────────────────────────────────────────────────────────

    private void setupSeriesControls() {
        // Total episodes - TextWatcher for manual input
        inputTotalEpisodes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!value.isEmpty()) {
                    try {
                        int newTotal = Integer.parseInt(value);
                        if (newTotal >= 0) {
                            totalEpisodesCount = newTotal;
                            if (currentEpisodes > totalEpisodesCount) {
                                currentEpisodes = totalEpisodesCount;
                            }
                            updateSeriesUI();
                        }
                    } catch (NumberFormatException ignored) {}
                } else {
                    totalEpisodesCount = 0;
                    updateSeriesUI();
                }
            }
        });

        // Watched episodes steppers
        btnEpMinus.setOnClickListener(v -> {
            if (currentEpisodes > 0) {
                currentEpisodes--;
                updateSeriesWatchedUI();
            }
        });

        btnEpPlus.setOnClickListener(v -> {
            if (currentEpisodes < totalEpisodesCount) {
                currentEpisodes++;
                updateSeriesWatchedUI();
            }
        });
    }

    private void updateSeriesUI() {
        boolean hasEps = totalEpisodesCount > 0;
        btnEpMinus.setEnabled(hasEps);
        btnEpPlus.setEnabled(hasEps);
        updateSeriesWatchedUI();
    }

    private void updateSeriesWatchedUI() {
        textEpWatchedCount.setText(String.valueOf(currentEpisodes));
        textEpisodes.setText(currentEpisodes + " / " + totalEpisodesCount + " Episodes");
        int pct = totalEpisodesCount > 0 ? (currentEpisodes * 100) / totalEpisodesCount : 0;
        textEpPercent.setText(pct + "%");
        progressBarSeries.setProgress(pct);

        // Sync status with progress
        if (!isAutoChangingStatus) {
            isAutoChangingStatus = true;
            if (totalEpisodesCount > 0 && currentEpisodes >= totalEpisodesCount) {
                if (currentItem != null && currentItem.getEpisodesWatched() > 0 && currentItem.getEpisodesWatched() < totalEpisodesCount) {
                    currentItem.setLastEpisodesWatched(currentItem.getEpisodesWatched());
                }
                setSpinnerValue(spinnerStatus, "Completed");
            } else if (currentEpisodes > 0) {
                setSpinnerValue(spinnerStatus, "Watching");
            } else {
                if (currentItem != null && currentItem.getEpisodesWatched() >= 2) {
                    currentItem.setLastEpisodesWatched(currentItem.getEpisodesWatched());
                }
                setSpinnerValue(spinnerStatus, "Planned");
            }
            isAutoChangingStatus = false;
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Spinners
    // ──────────────────────────────────────────────────────────────

    private void setupSpinners() {
        String[] types = {"Movie", "Series", "Anime", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        String[] statuses = {"Planned", "Watching", "Completed"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isAutoChangingStatus) return;
                handleManualStatusChange(statuses[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = types[pos];
                if ("Movie".equals(selected)) {
                    layoutMovie.setVisibility(View.VISIBLE);
                    layoutSeries.setVisibility(View.GONE);
                } else if ("Series".equals(selected) || "Anime".equals(selected)) {
                    layoutMovie.setVisibility(View.GONE);
                    layoutSeries.setVisibility(View.VISIBLE);
                } else {
                    layoutMovie.setVisibility(View.GONE);
                    layoutSeries.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void handleManualStatusChange(String newStatus) {
        String type = spinnerType.getSelectedItem().toString();
        if ("Movie".equals(type)) {
            if ("Watching".equals(newStatus)) {
                if (watchedMinutes == 0 || watchedMinutes >= totalRuntimeMins) {
                    if (currentItem != null && currentItem.getLastMinutesWatched() > 0 && currentItem.getLastMinutesWatched() < totalRuntimeMins) {
                        watchedMinutes = currentItem.getLastMinutesWatched();
                    } else {
                        watchedMinutes = 1;
                    }
                    updateWatchedRuntimePickersFromMinutes();
                    updateMovieWatchedUI();
                }
            } else if ("Planned".equals(newStatus)) {
                if (watchedMinutes >= 2) {
                    if (currentItem != null) currentItem.setLastMinutesWatched(watchedMinutes);
                }
                watchedMinutes = 0;
                updateWatchedRuntimePickersFromMinutes();
                updateMovieWatchedUI();
            } else if ("Completed".equals(newStatus)) {
                if (watchedMinutes < totalRuntimeMins && totalRuntimeMins > 0) {
                    if (currentItem != null) currentItem.setLastMinutesWatched(watchedMinutes);
                    watchedMinutes = totalRuntimeMins;
                    updateWatchedRuntimePickersFromMinutes();
                    updateMovieWatchedUI();
                }
            }
        } else if ("Series".equals(type) || "Anime".equals(type)) {
            if ("Watching".equals(newStatus)) {
                if (currentEpisodes == 0 || currentEpisodes >= totalEpisodesCount) {
                    if (currentItem != null && currentItem.getLastEpisodesWatched() > 0 && currentItem.getLastEpisodesWatched() < totalEpisodesCount) {
                        currentEpisodes = currentItem.getLastEpisodesWatched();
                    } else {
                        currentEpisodes = 1;
                    }
                    updateSeriesWatchedUI();
                }
            } else if ("Planned".equals(newStatus)) {
                if (currentEpisodes >= 2) {
                    if (currentItem != null) currentItem.setLastEpisodesWatched(currentEpisodes);
                }
                currentEpisodes = 0;
                updateSeriesWatchedUI();
            } else if ("Completed".equals(newStatus)) {
                if (currentEpisodes < totalEpisodesCount && totalEpisodesCount > 0) {
                    if (currentItem != null) currentItem.setLastEpisodesWatched(currentEpisodes);
                    currentEpisodes = totalEpisodesCount;
                    updateSeriesWatchedUI();
                }
            }
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Fill existing data when editing
    // ──────────────────────────────────────────────────────────────

    private void fillData() {
        isAutoChangingStatus = true;
        inputTitle.setText(currentItem.getTitle());
        inputNotes.setText(currentItem.getNotes());
        if (currentItem.getWatchLink() != null) {
            inputWatchLink.setText(currentItem.getWatchLink());
        }
        selectedImageUri = currentItem.getImageUri();
        if (selectedImageUri != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(posterImage);
        }

        setSpinnerValue(spinnerType, currentItem.getType());
        setSpinnerValue(spinnerStatus, currentItem.getStatus());

        // ── Movie ──
        totalRuntimeMins = currentItem.getTotalRuntime();
        int totalH = totalRuntimeMins / 60;
        int totalM = totalRuntimeMins % 60;
        pickerTotalHours.setValue(Math.min(totalH, 10));
        pickerTotalMinutes.setValue(totalM);
        updateRuntimeDisplay();

        watchedMinutes = Math.min(currentItem.getMinutesWatched(), totalRuntimeMins);
        updateWatchedRuntimePickerRange();

        // ── Series ──
        totalEpisodesCount = currentItem.getTotalEpisodes();
        inputTotalEpisodes.setText(String.valueOf(totalEpisodesCount));
        currentEpisodes = Math.min(currentItem.getEpisodesWatched(), totalEpisodesCount);
        updateSeriesUI();
        isAutoChangingStatus = false;
    }

    // ──────────────────────────────────────────────────────────────
    //  Image picker result
    // ──────────────────────────────────────────────────────────────

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            selectedImageUri = imageUri.toString();
            Glide.with(this).load(imageUri).into(posterImage);
            try {
                getContext().getContentResolver()
                        .takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Save
    // ──────────────────────────────────────────────────────────────

    private void saveItem() {
        String title = inputTitle.getText() != null ? inputTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for duplicate title and image (case-insensitive title)
        for (WatchItem item : allItems) {
            if (item.getTitle() != null && item.getTitle().equalsIgnoreCase(title) && !item.getId().equals(currentItem.getId())) {
                String existingUri = item.getImageUri();
                boolean imagesMatch = (existingUri == null && selectedImageUri == null) || 
                                     (existingUri != null && existingUri.equals(selectedImageUri));
                if (imagesMatch) {
                    Toast.makeText(getContext(), "This item already exists in your watchlist", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        boolean isNew = true;
        for (WatchItem item : allItems) {
            if (item.getId().equals(currentItem.getId())) {
                isNew = false;
                break;
            }
        }
        
        int oldWatched = isNew ? 0 : ("Movie".equals(currentItem.getType()) ? currentItem.getMinutesWatched() : currentItem.getEpisodesWatched());
        String oldStatus = isNew ? null : currentItem.getStatus();

        if (isNew) {
            allItems.add(currentItem);
        }

        currentItem.setTitle(title);
        currentItem.setNotes(inputNotes.getText() != null ? inputNotes.getText().toString() : "");
        String watchLink = inputWatchLink.getText() != null ? inputWatchLink.getText().toString().trim() : "";
        currentItem.setWatchLink(watchLink.isEmpty() ? null : watchLink);
        currentItem.setImageUri(selectedImageUri);
        String newType = spinnerType.getSelectedItem().toString();
        currentItem.setType(newType);
        String newStatus = spinnerStatus.getSelectedItem().toString();
        currentItem.setStatus(newStatus);
        currentItem.setLastUpdated(System.currentTimeMillis());

        if ("Movie".equals(newType)) {
            currentItem.setTotalRuntime(totalRuntimeMins);
            currentItem.setMinutesWatched(watchedMinutes);
        } else if ("Series".equals(newType) || "Anime".equals(newType)) {
            currentItem.setTotalEpisodes(totalEpisodesCount);
            currentItem.setEpisodesWatched(currentEpisodes);
        }

        StorageHelper.saveWatchItems(getContext(), allItems);

        String message = isNew ? currentItem.getTitle() + " added to watchlist" : currentItem.getTitle() + " updated";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        // Logging
        if (isNew) {
            StorageHelper.saveLogEntry(getContext(), new com.example.taranood.models.LogEntry(
                    "Added \"" + currentItem.getTitle() + "\" to watchlist",
                    currentItem.getTitle(),
                    "Type: " + currentItem.getType()
            ));
        } else {
            // Check for progress change
            int newWatched = ("Movie".equals(newType) ? currentItem.getMinutesWatched() : currentItem.getEpisodesWatched());
            if (newWatched != oldWatched) {
                String unit = "Movie".equals(newType) ? "Minutes" : "Episodes";
                StorageHelper.saveLogEntry(getContext(), new com.example.taranood.models.LogEntry(
                        "Updated progress of \"" + currentItem.getTitle() + "\"",
                        currentItem.getTitle(),
                        unit + ": " + oldWatched + " → " + newWatched
                ));
            }

            // Check for status change
            if (!newStatus.equals(oldStatus)) {
                if ("Completed".equals(newStatus)) {
                    StorageHelper.saveLogEntry(getContext(), new com.example.taranood.models.LogEntry(
                            "Marked \"" + currentItem.getTitle() + "\" as Completed",
                            currentItem.getTitle(),
                            ""
                    ));
                } else {
                    StorageHelper.saveLogEntry(getContext(), new com.example.taranood.models.LogEntry(
                            "Status changed for \"" + currentItem.getTitle() + "\"",
                            currentItem.getTitle(),
                            oldStatus + " → " + newStatus
                    ));
                }
            }
        }

        // Refresh parent fragment if it implements refreshData
        if (getParentFragment() instanceof HomeFragment) {
            ((HomeFragment) getParentFragment()).refreshData();
        } else if (getParentFragment() instanceof HistoryFragment) {
            ((HistoryFragment) getParentFragment()).refreshData();
        } else if (getParentFragment() instanceof FavoritesFragment) {
            ((FavoritesFragment) getParentFragment()).refreshData();
        }

        dismiss();
    }
}