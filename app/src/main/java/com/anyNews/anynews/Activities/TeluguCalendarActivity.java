package com.anyNews.anynews.Activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.R;

/**
 * Standalone entry for Telugu calendar (deep links / legacy). Footer Calendar on
 * HomePage embeds {@link TeluguCalendarController} in the content host instead.
 */
public class TeluguCalendarActivity extends AppCompatActivity {

    @Nullable private TeluguCalendarController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telugu_calendar);
        controller = new TeluguCalendarController(
                this, findViewById(R.id.telugu_calendar_root), true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (controller != null) {
            controller.onHostResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (controller != null) {
            controller.destroy();
            controller = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (controller != null && controller.handleBackPress()) {
            return;
        }
        super.onBackPressed();
    }
}
