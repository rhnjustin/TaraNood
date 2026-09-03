package com.example.taranood.models;

import java.io.Serializable;
import java.util.UUID;

public class LogEntry implements Serializable {
    private String id;
    private String action; // e.g., "Added", "Updated", "Status Changed", "Favorited", "Completed"
    private String title;
    private String details; // e.g., "Type: Anime", "Episodes: 12 → 15", "Planned → Watching"
    private long timestamp;

    public LogEntry(String action, String title, String details) {
        this.id = UUID.randomUUID().toString();
        this.action = action;
        this.title = title;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getAction() { return action; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public long getTimestamp() { return timestamp; }
}
