package com.example.taranood.models;

import java.io.Serializable;
import java.util.UUID;

public class WatchItem implements Serializable {
    private String id;
    private String title;
    private String type; // Movie, Series, Anime, Other
    private String status; // Planned, Watching, Completed
    private String notes;
    private String imageUri;
    private boolean isFavorite;

    // Optional watch link
    private String watchLink;

    // Progress for Movies
    private int minutesWatched;
    private int totalRuntime;

    // Progress for Series/Anime
    private int episodesWatched;
    private int totalEpisodes;
    private int currentEpisodeMinutes;

    // Progress for Other
    private String customProgress;

    // Fields to store previous progress for restoration
    private int lastMinutesWatched;
    private int lastEpisodesWatched;

    private long timestamp;
    private long lastUpdated;

    public WatchItem() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.lastUpdated = this.timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public String getWatchLink() { return watchLink; }
    public void setWatchLink(String watchLink) { this.watchLink = watchLink; }
    public int getMinutesWatched() { return minutesWatched; }
    public void setMinutesWatched(int minutesWatched) { this.minutesWatched = minutesWatched; }
    public int getTotalRuntime() { return totalRuntime; }
    public void setTotalRuntime(int totalRuntime) { this.totalRuntime = totalRuntime; }
    public int getEpisodesWatched() { return episodesWatched; }
    public void setEpisodesWatched(int episodesWatched) { this.episodesWatched = episodesWatched; }
    public int getTotalEpisodes() { return totalEpisodes; }
    public void setTotalEpisodes(int totalEpisodes) { this.totalEpisodes = totalEpisodes; }
    public int getCurrentEpisodeMinutes() { return currentEpisodeMinutes; }
    public void setCurrentEpisodeMinutes(int currentEpisodeMinutes) { this.currentEpisodeMinutes = currentEpisodeMinutes; }
    public String getCustomProgress() { return customProgress; }
    public void setCustomProgress(String customProgress) { this.customProgress = customProgress; }
    public int getLastMinutesWatched() { return lastMinutesWatched; }
    public void setLastMinutesWatched(int lastMinutesWatched) { this.lastMinutesWatched = lastMinutesWatched; }
    public int getLastEpisodesWatched() { return lastEpisodesWatched; }
    public void setLastEpisodesWatched(int lastEpisodesWatched) { this.lastEpisodesWatched = lastEpisodesWatched; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}