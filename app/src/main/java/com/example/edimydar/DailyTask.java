package com.example.edimydar;

public class DailyTask {
    String title;
    boolean checked;

    public DailyTask(String title, boolean checked) {
        this.title = title;
        this.checked = checked;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
