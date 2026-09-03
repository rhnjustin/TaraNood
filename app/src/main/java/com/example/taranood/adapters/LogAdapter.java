package com.example.taranood.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taranood.R;
import com.example.taranood.models.LogEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import android.widget.CheckBox;
import java.util.stream.Collectors;

public class LogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items = new ArrayList<>();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private OnLogLongClickListener longClickListener;
    private OnSelectionChangeListener selectionChangeListener;

    private boolean multiSelectMode = false;
    private Set<String> selectedIds = new HashSet<>();

    public interface OnLogLongClickListener {
        void onLogLongClick(LogEntry log);
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount);
    }

    public LogAdapter(List<LogEntry> logs) {
        processLogs(logs);
    }

    public void setOnLogLongClickListener(OnLogLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public void setMultiSelectMode(boolean enabled) {
        this.multiSelectMode = enabled;
        if (!enabled) {
            selectedIds.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    public void toggleSelection(String id) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }
        notifyDataSetChanged();
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(selectedIds.size());
        }
    }

    public void selectAll(List<LogEntry> logs) {
        selectedIds.clear();
        for (LogEntry log : logs) {
            selectedIds.add(log.getId());
        }
        notifyDataSetChanged();
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(selectedIds.size());
        }
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(0);
        }
    }

    public Set<String> getSelectedIds() {
        return selectedIds;
    }

    private void processLogs(List<LogEntry> logs) {
        items.clear();
        if (logs == null || logs.isEmpty()) return;

        String lastDate = "";
        for (LogEntry log : logs) {
            String dateHeader = formatDateHeader(log.getTimestamp());
            if (!dateHeader.equals(lastDate)) {
                items.add(dateHeader);
                lastDate = dateHeader;
            }
            items.add(log);
        }
    }

    private String formatDateHeader(long timestamp) {
        Calendar cal = Calendar.getInstance();

        // Reset current time to midnight
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DATE, -1);

        Calendar itemDate = Calendar.getInstance();
        itemDate.setTimeInMillis(timestamp);
        itemDate.set(Calendar.HOUR_OF_DAY, 0);
        itemDate.set(Calendar.MINUTE, 0);
        itemDate.set(Calendar.SECOND, 0);
        itemDate.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat headerFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        String dateStr = headerFormat.format(new Date(timestamp));

        if (itemDate.getTimeInMillis() == today.getTimeInMillis()) {
            return "Today - " + dateStr;
        } else if (itemDate.getTimeInMillis() == yesterday.getTimeInMillis()) {
            return "Yesterday - " + dateStr;
        } else {
            return dateStr;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
            return new LogViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvHeader.setText((String) items.get(position));
        } else if (holder instanceof LogViewHolder) {
            LogViewHolder logHolder = (LogViewHolder) holder;
            LogEntry log = (LogEntry) items.get(position);
            
            logHolder.tvDate.setText(timeFormat.format(new Date(log.getTimestamp())));
            logHolder.tvAction.setText(log.getAction());
            
            if (log.getDetails() == null || log.getDetails().isEmpty()) {
                logHolder.tvDetails.setVisibility(View.GONE);
            } else {
                logHolder.tvDetails.setVisibility(View.VISIBLE);
                logHolder.tvDetails.setText(log.getDetails());
            }

            // Setup icon and color
            android.content.Context context = logHolder.itemView.getContext();
            int color = androidx.core.content.ContextCompat.getColor(context, R.color.sky_blue);
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

            logHolder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(color));
            logHolder.ivIcon.setImageResource(iconRes);

            // Selection mode UI
            logHolder.checkBox.setVisibility(multiSelectMode ? View.VISIBLE : View.GONE);
            logHolder.checkBox.setChecked(selectedIds.contains(log.getId()));

            logHolder.itemView.setOnClickListener(v -> {
                if (multiSelectMode) {
                    toggleSelection(log.getId());
                }
            });

            logHolder.itemView.setOnLongClickListener(v -> {
                if (!multiSelectMode && longClickListener != null) {
                    longClickListener.onLogLongClick(log);
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<LogEntry> newList) {
        processLogs(newList);
        notifyDataSetChanged();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tv_header);
        }
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvAction, tvDetails;
        View iconContainer;
        ImageView ivIcon;
        CheckBox checkBox;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_log_date);
            tvAction = itemView.findViewById(R.id.tv_log_action);
            tvDetails = itemView.findViewById(R.id.tv_log_details);
            iconContainer = itemView.findViewById(R.id.log_icon_container);
            ivIcon = itemView.findViewById(R.id.iv_log_icon);
            checkBox = itemView.findViewById(R.id.checkbox_log);
        }
    }
}
