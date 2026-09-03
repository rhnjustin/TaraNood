package com.example.taranood.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taranood.R;
import com.example.taranood.adapters.WatchAdapter;
import com.example.taranood.models.WatchItem;
import com.example.taranood.utils.StorageHelper;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment implements WatchAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private View emptyState;
    private WatchAdapter adapter;
    private List<WatchItem> allItems;
    private String currentFilter = "All";
    private String searchQuery = "";
    private String currentSort;

    private FloatingActionButton fab;
    private View selectionControls;
    private boolean isSelectionMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        emptyState = view.findViewById(R.id.empty_state_home);

        fab = view.findViewById(R.id.fab_add);
        fab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1E8EE6));
        fab.setOnClickListener(v -> {
            if (isSelectionMode) {
                showDeleteConfirmationDialog();
            } else {
                showAddOptions(v);
            }
        });

        selectionControls = view.findViewById(R.id.layout_selection_controls);
        view.findViewById(R.id.btn_select_all).setOnClickListener(v -> adapter.selectAll());
        view.findViewById(R.id.btn_cancel_selection).setOnClickListener(v -> exitSelectionMode());

        view.findViewById(R.id.btn_sort).setOnClickListener(this::showSortMenu);

        EditText searchBar = view.findViewById(R.id.search_bar);
        ImageButton btnSearch = view.findViewById(R.id.btn_search_home);

        btnSearch.setOnClickListener(v -> {
            if (searchBar.getVisibility() == View.GONE) {
                searchBar.setVisibility(View.VISIBLE);
                searchBar.requestFocus();
            } else {
                searchBar.setVisibility(View.GONE);
                searchBar.setText("");
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                filterData();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        ChipGroup chipGroup = view.findViewById(R.id.filter_chip_group);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = "All";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chip_all) currentFilter = "All";
                else if (id == R.id.chip_movie) currentFilter = "Movie";
                else if (id == R.id.chip_series) currentFilter = "Series";
                else if (id == R.id.chip_anime) currentFilter = "Anime";
                else if (id == R.id.chip_other) currentFilter = "Other";
            }
            filterData();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof com.example.taranood.MainActivity) {
            ((com.example.taranood.MainActivity) getActivity()).setBottomNavigationVisibility(View.VISIBLE);
        }
        loadData();
    }

    private void loadData() {
        currentSort = StorageHelper.getSortType(getContext(), "home", getString(R.string.recently_added));
        allItems = StorageHelper.loadWatchItems(getContext());
        filterData();
    }

    private void filterData() {
        if (allItems == null) return;

        List<WatchItem> filteredList = allItems.stream()
                .filter(item -> !"Completed".equals(item.getStatus()))
                .filter(item -> "All".equals(currentFilter) || item.getType().equals(currentFilter))
                .filter(item -> item.getTitle().toLowerCase().contains(searchQuery))
                .sorted((o1, o2) -> {
                    if (currentSort.equals(getString(R.string.recently_added))) {
                        return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                    } else if (currentSort.equals(getString(R.string.recently_watched))) {
                        return Long.compare(o2.getLastUpdated(), o1.getLastUpdated());
                    } else if (currentSort.equals(getString(R.string.recently_updated))) {
                        return Long.compare(o2.getLastUpdated(), o1.getLastUpdated());
                    } else if (currentSort.equals(getString(R.string.alphabetic))) {
                        return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                    } else if (currentSort.equals(getString(R.string.progress))) {
                        return Double.compare(calculateProgress(o2), calculateProgress(o1));
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        if (adapter == null) {
            adapter = new WatchAdapter(filteredList, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(filteredList);
        }

        if (filteredList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private double calculateProgress(WatchItem item) {
        if ("Movie".equals(item.getType())) {
            return item.getTotalRuntime() > 0 ? (double) item.getMinutesWatched() / item.getTotalRuntime() : 0;
        } else if ("Series".equals(item.getType()) || "Anime".equals(item.getType())) {
            return item.getTotalEpisodes() > 0 ? (double) item.getEpisodesWatched() / item.getTotalEpisodes() : 0;
        }
        return 0;
    }

    private void showSortMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add(R.string.recently_added);
        popup.getMenu().add(R.string.recently_watched);
        popup.getMenu().add(R.string.recently_updated);
        popup.getMenu().add(R.string.alphabetic);
        popup.getMenu().add(R.string.progress);

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle() != null) {
                currentSort = menuItem.getTitle().toString();
                StorageHelper.saveSortType(getContext(), "home", currentSort);
                filterData();
            }
            return true;
        });
        popup.show();
    }

    private void showAddOptions(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add(0, 1, 0, R.string.manual_add);
        popup.getMenu().add(0, 2, 1, R.string.quick_add);

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == 1) {
                AddEditDialogFragment.newInstance(null).show(getChildFragmentManager(), "add_item");
            } else if (menuItem.getItemId() == 2) {
                QuickAddDialogFragment.newInstance().show(getChildFragmentManager(), "quick_add");
            }
            return true;
        });
        popup.show();
    }

    private void showDeleteConfirmationDialog() {
        int selectedCount = adapter.getSelectedIds().size();
        if (selectedCount == 0) return;

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.delete_items)
                .setMessage(getString(R.string.delete_confirmation, selectedCount))
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteSelectedItems())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void enterSelectionMode() {
        isSelectionMode = true;
        selectionControls.setVisibility(View.VISIBLE);
        fab.setImageResource(android.R.drawable.ic_menu_delete);
        fab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF4444));
        adapter.setSelectionMode(true);
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        selectionControls.setVisibility(View.GONE);
        fab.setImageResource(android.R.drawable.ic_input_add);
        fab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1E8EE6));
        adapter.setSelectionMode(false);
    }

    private void deleteSelectedItems() {
        Set<String> selectedIds = adapter.getSelectedIds();
        int count = selectedIds.size();

        // Check if any selected items are favorites
        boolean hasFavorites = false;
        for (WatchItem item : allItems) {
            if (selectedIds.contains(item.getId()) && item.isFavorite()) {
                hasFavorites = true;
                break;
            }
        }

        if (hasFavorites) {
            Toast.makeText(getContext(), R.string.favorite_delete_error, Toast.LENGTH_LONG).show();
            return;
        }

        allItems.removeIf(item -> selectedIds.contains(item.getId()));
        StorageHelper.saveWatchItems(getContext(), allItems);
        exitSelectionMode();
        loadData();
        Toast.makeText(getContext(), getString(R.string.delete_success, count), Toast.LENGTH_SHORT).show();
    }

    public void refreshData() {
        loadData();
    }

    @Override
    public void onItemClick(WatchItem item) {
        AddEditDialogFragment.newInstance(item.getId()).show(getChildFragmentManager(), "edit_item");
    }

    @Override
    public void onFavoriteClick(WatchItem item, int position) {
        item.setFavorite(!item.isFavorite());
        StorageHelper.saveWatchItems(getContext(), allItems);
        adapter.notifyItemChanged(position);

        String action = item.isFavorite() ?
                "Added \"" + item.getTitle() + "\" to favorites" :
                "Removed \"" + item.getTitle() + "\" from favorites";
        StorageHelper.saveLogEntry(getContext(), new com.example.taranood.models.LogEntry(
                action,
                item.getTitle(),
                ""
        ));
    }

    @Override
    public void onLongClick(WatchItem item) {
        if (!isSelectionMode) {
            enterSelectionMode();
            adapter.toggleSelection(item.getId());
        }
    }

    @Override
    public void onRewatchClick(WatchItem item) {
        // HomeFragment usually doesn't show completed items, but if it does:
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.rewatch)
                .setMessage(R.string.rewatch_confirmation)
                .setPositiveButton(R.string.rewatch, (dialog, which) -> {
                    String oldStatus = item.getStatus();
                    item.setStatus("Watching");
                    if ("Movie".equals(item.getType())) {
                        item.setMinutesWatched(1);
                    } else {
                        item.setEpisodesWatched(1);
                    }
                    item.setCurrentEpisodeMinutes(0);
                    item.setLastUpdated(System.currentTimeMillis());
                    StorageHelper.saveWatchItems(getContext(), allItems);

                    StorageHelper.saveLogEntry(getContext(), new com.example.taranood.models.LogEntry(
                            "Status changed for \"" + item.getTitle() + "\" (Rewatch)",
                            item.getTitle(),
                            oldStatus + " → Watching"
                    ));

                    loadData();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDetailClick(WatchItem item) {
        com.example.taranood.utils.DialogUtils.showWatchItemDetails(getContext(), item);
    }

    @Override
    public void onWatchLinkClick(WatchItem item) {
        item.setLastUpdated(System.currentTimeMillis());
        StorageHelper.saveWatchItems(getContext(), allItems);
        
        filterData();

        String link = item.getWatchLink();
        if (link != null && !link.trim().isEmpty()) {
            try {
                if (!link.startsWith("http://") && !link.startsWith("https://")) {
                    link = "https://" + link;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(intent);
            } catch (Exception e) {
                android.widget.Toast.makeText(getContext(), "Could not open link", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
}