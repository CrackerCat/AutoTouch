package top.bogey.auto_touch.room.bean;

import androidx.annotation.NonNull;

public class SimpleTaskInfo {
    private String id;
    private String title;

    public SimpleTaskInfo(String id, String title) {
        this.id = id;
        this.title = title;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
