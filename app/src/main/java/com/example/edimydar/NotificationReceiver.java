package com.example.edimydar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import android.os.Build;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskId = intent.getStringExtra("task_id");
        String taskTitle = intent.getStringExtra("task_title");
        String taskText = intent.getStringExtra("task_text");

        // Build the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Notifications";
            String description = "Notifications for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("task_channel", name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_channel")
                .setSmallIcon(R.drawable.ic_notification_icon) // Replace with your icon
                .setContentTitle(taskTitle)
                .setContentText(taskText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Show the notification
        notificationManager.notify(taskId.hashCode(), builder.build());
    }

}