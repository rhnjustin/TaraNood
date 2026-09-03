package com.example.taranood.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;

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

import java.util.List;
import java.util.stream.Collectors;

public class FavoritesFragment extends Fragment implements WatchAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private View emptyState;
    private WatchAdapter adapter;
    private List<WatchItem> allItems;
    private String searchQuery = "";
    private String currentSort;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.recycler_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyState = view.findViewById(R.id.empty_state_favorites);

        view.findViewById(R.id.btn_sort_favorites).setOnClickListener(this::showSortMenu);

        EditText searchBar = view.findViewById(R.id.search_bar_favorites);
        ImageButton btnSearch = view.findViewById(R.id.btn_search_favorites);

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
        currentSort = StorageHelper.getSortType(getContext(), "favorites", getString(R.string.recently_added));
        allItems = StorageHelper.loadWatchItems(getContext());
        filterData();
    }

    private void filterData() {
        if (allItems == null) return;

        List<WatchItem> favoriteItems = allItems.stream()
                .filter(WatchItem::isFavorite)
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
            adapter = new WatchAdapter(favoriteItems, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(favoriteItems);
        }

        if (favoriteItems.isEmpty()) {
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
                StorageHelper.saveSortType(getContext(), "favorites", currentSort);
                filterData();
            }
            return true;
        });
        popup.show();
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

        String action = item.isFavorite() ?
                "Added \"" + item.getTitle() + "\" to favorites" :
                "Removed \"" + item.getTitle() + "\" from favorites";
        StorageHelper.saveLogEntry(getContext(), new com.example.taranood.models.LogEntry(
                action,
                item.getTitle(),
                ""
        ));

        loadData();
    }

    @Override
    public void onLongClick(WatchItem item) {}

    @Override
    public void onRewatchClick(WatchItem item) {
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
