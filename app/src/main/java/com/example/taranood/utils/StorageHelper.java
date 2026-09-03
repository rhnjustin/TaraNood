package com.example.taranood.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.taranood.models.WatchItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StorageHelper {
    private static final String PREF_NAME = "taranood_prefs";
    private static final String KEY_WATCH_ITEMS = "watch_items";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_AGE = "user_age";
    private static final String KEY_USER_IMAGE = "user_image";
    private static final String KEY_LOGS = "activity_logs";
    private static final String KEY_DEFAULT_ADD_TYPE = "default_add_type";
    private static final String KEY_LOG_PREVIEW_COUNT = "log_preview_count";
    private static final String KEY_SORT_TYPE_PREFIX = "sort_type_";

    public static void saveWatchItems(Context context, List<WatchItem> items) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(items);
        prefs.edit().putString(KEY_WATCH_ITEMS, json).apply();
    }

    public static List<WatchItem> loadWatchItems(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_WATCH_ITEMS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<WatchItem>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void setDarkMode(Context context, boolean isEnabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_DARK_MODE, isEnabled).apply();
    }

    public static boolean isDarkMode(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK_MODE, true); // Default to dark mode as requested
    }

    public static void saveUserProfile(Context context, String name, int age, String imageUri) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_USER_NAME, name)
                .putInt(KEY_USER_AGE, age)
                .putString(KEY_USER_IMAGE, imageUri)
                .apply();
    }

    public static String getUserName(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_USER_NAME, "User");
    }

    public static int getUserAge(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_USER_AGE, 0);
    }

    public static String getUserImage(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_USER_IMAGE, null);
    }

    public static void saveLogEntry(Context context, com.example.taranood.models.LogEntry entry) {
        List<com.example.taranood.models.LogEntry> logs = getLogs(context);
        logs.add(0, entry); // Newest first
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(logs);
        prefs.edit().putString(KEY_LOGS, json).apply();
    }

    public static List<com.example.taranood.models.LogEntry> getLogs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LOGS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<com.example.taranood.models.LogEntry>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void setDefaultAddType(Context context, String type) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_DEFAULT_ADD_TYPE, type).apply();
    }

    public static String getDefaultAddType(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_DEFAULT_ADD_TYPE, "Movie"); // Default to Movie
    }

    public static void setLogPreviewCount(Context context, int count) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_LOG_PREVIEW_COUNT, count).apply();
    }

    public static int getLogPreviewCount(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_LOG_PREVIEW_COUNT, 5); // Default to 5
    }

    public static void saveSortType(Context context, String pageKey, String sortType) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_SORT_TYPE_PREFIX + pageKey, sortType).apply();
    }

    public static String getSortType(Context context, String pageKey, String defaultSort) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_SORT_TYPE_PREFIX + pageKey, defaultSort);
    }
    
    public static void clearAllData(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static String exportAllData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new Gson().toJson(prefs.getAll());
    }

    public static boolean importAllData(Context context, String json) {
        try {
            Type type = new TypeToken<java.util.Map<String, Object>>() {}.getType();
            java.util.Map<String, Object> data = new Gson().fromJson(json, type);
            SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
            editor.clear();
            for (java.util.Map.Entry<String, Object> entry : data.entrySet()) {
                Object value = entry.getValue();
                String key = entry.getKey();
                if (value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (value instanceof String) {
                    editor.putString(key, (String) value);
                } else if (value instanceof Double) {
                    Double d = (Double) value;
                    if (d == Math.floor(d) && !Double.isInfinite(d)) {
                        // Check if it fits in an Integer
                        if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                            editor.putInt(key, d.intValue());
                        } else {
                            editor.putLong(key, d.longValue());
                        }
                    } else {
                        editor.putFloat(key, d.floatValue());
                    }
                }
            }
            return editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addSampleData(Context context) {
        List<WatchItem> items = loadWatchItems(context);
        if (items.isEmpty()) {
            WatchItem item1 = new WatchItem();
            item1.setTitle("Inception");
            item1.setType("Movie");
            item1.setStatus("Watching");
            item1.setMinutesWatched(45);
            item1.setTotalRuntime(148);
            items.add(item1);

            WatchItem item2 = new WatchItem();
            item2.setTitle("One Piece");
            item2.setType("Anime");
            item2.setStatus("Watching");
            item2.setEpisodesWatched(1000);
            item2.setTotalEpisodes(1100);
            items.add(item2);

            WatchItem item3 = new WatchItem();
            item3.setTitle("Breaking Bad");
            item3.setType("Series");
            item3.setStatus("Completed");
            item3.setEpisodesWatched(62);
            item3.setTotalEpisodes(62);
            item3.setFavorite(true);
            items.add(item3);

            saveWatchItems(context, items);
        }
    }
}
