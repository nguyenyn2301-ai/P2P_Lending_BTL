package model;

import java.sql.Timestamp;

public class Notification {
    private long notificationId;
    private long userId;
    private String title;
    private String message;
    private boolean read;
    private Timestamp createdAt;

    public Notification() {}

    public long getNotificationId() { return notificationId; }
    public void setNotificationId(long notificationId) { this.notificationId = notificationId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
