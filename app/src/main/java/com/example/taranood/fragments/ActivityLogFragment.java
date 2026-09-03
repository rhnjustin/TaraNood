package com.example.taranood.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taranood.MainActivity;
import com.example.taranood.R;
import com.example.taranood.adapters.LogAdapter;
import com.example.taranood.models.LogEntry;
import com.example.taranood.utils.StorageHelper;
import com.google.gson.Gson;

import java.util.List;
import java.util.Set;

public class ActivityLogFragment extends Fragment {

    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private TextView tvEmpty, tvTitle;
    private ImageButton btnBack, btnCancel, btnSelectAll, btnClear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_log, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(View.GONE);
        }

        recyclerView = view.findViewById(R.id.recycler_activity_log);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvEmpty = view.findViewById(R.id.tv_empty_logs);
        tvTitle = view.findViewById(R.id.tv_title_logs);

        btnBack = view.findViewById(R.id.btn_back_logs);
        btnCancel = view.findViewById(R.id.btn_cancel_selection);
        btnSelectAll = view.findViewById(R.id.btn_select_all);
        btnClear = view.findViewById(R.id.btn_clear_logs);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnCancel.setOnClickListener(v -> exitMultiSelectMode());
        btnSelectAll.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.selectAll(StorageHelper.getLogs(getContext()));
            }
        });

        btnClear.setOnClickListener(v -> {
            if (adapter != null && adapter.isMultiSelectMode()) {
                if (adapter.getSelectedIds().isEmpty()) {
                    exitMultiSelectMode();
                } else {
                    showDeleteSelectedLogsDialog();
                }
            } else {
                showClearLogsDialog();
            }
        });

        loadLogs();

        return view;
    }

    private void enterMultiSelectMode(LogEntry log) {
        if (adapter == null) return;
        adapter.setMultiSelectMode(true);
        adapter.toggleSelection(log.getId());

        btnBack.setVisibility(View.GONE);
        btnCancel.setVisibility(View.VISIBLE);
        btnSelectAll.setVisibility(View.VISIBLE);
        btnClear.setImageTintList(ColorStateList.valueOf(Color.RED));

        updateSelectionTitle(1);
    }

    private void exitMultiSelectMode() {
        if (adapter == null) return;
        adapter.setMultiSelectMode(false);

        btnBack.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.GONE);
        btnSelectAll.setVisibility(View.GONE);

        TypedValue typedValue = new TypedValue();
        if (getContext() != null) {
            getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
            btnClear.setImageTintList(ColorStateList.valueOf(typedValue.data));
        }

        tvTitle.setText(R.string.activity_log_title);
    }

    private void updateSelectionTitle(int count) {
        tvTitle.setText(getString(R.string.selected_count, count));
    }

    private void showDeleteSelectedLogsDialog() {
        int count = adapter.getSelectedIds().size();
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Logs")
                .setMessage("Are you sure you want to delete " + count + " selected log(s)?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    List<LogEntry> logs = StorageHelper.getLogs(getContext());
                    Set<String> selectedIds = adapter.getSelectedIds();
                    logs.removeIf(l -> selectedIds.contains(l.getId()));

                    saveLogs(logs);
                    exitMultiSelectMode();
                    loadLogs();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveLogs(List<LogEntry> logs) {
        if (getContext() == null) return;
        String json = new Gson().toJson(logs);
        getContext().getSharedPreferences("taranood_prefs", android.content.Context.MODE_PRIVATE)
                .edit().putString("activity_logs", json).apply();
    }

    private void loadLogs() {
        if (getContext() == null) return;
        List<LogEntry> logs = StorageHelper.getLogs(getContext());
        if (logs.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (adapter == null) {
                adapter = new LogAdapter(logs);
                adapter.setOnLogLongClickListener(this::enterMultiSelectMode);
                adapter.setOnSelectionChangeListener(this::updateSelectionTitle);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateList(logs);
            }
        }
    }

    private void showClearLogsDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Clear Logs")
                .setMessage("Are you sure you want to clear all activity logs?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    getContext().getSharedPreferences("taranood_prefs", android.content.Context.MODE_PRIVATE)
                            .edit().remove("activity_logs").apply();
                    loadLogs();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
