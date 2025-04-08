package com.example.edimydar;

import com.example.edimydar.DailyTask;

import java.util.Date;

public class NormalTask extends DailyTask {
    private String dueDate;
    private String dueTime;

    public NormalTask(String title, boolean isChecked, String dueDate, String dueTime) {
        super(title, isChecked); // Call the parent constructor
        this.dueDate = dueDate;
        this.dueTime = dueTime;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }
}