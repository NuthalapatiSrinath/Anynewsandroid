package com.anyNews.anynews.Activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.DailyUpdatesShareLanguage;
import com.anyNews.anynews.Utils.DailyUpdatesShareLinks;

/**
 * Standalone entry for Telugu calendar (deep links / legacy). Footer Calendar on
 * HomePage embeds {@link TeluguCalendarController} in the content host instead.
 */
public class TeluguCalendarActivity extends AppCompatActivity {

    @Nullable private TeluguCalendarController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DailyUpdatesShareLinks.beginLanguage(this);
        setContentView(R.layout.activity_telugu_calendar);
        controller = new TeluguCalendarController(
                this, findViewById(R.id.telugu_calendar_root), true);
        String page = null;
        try {
            if (getIntent() != null) {
                page = getIntent().getStringExtra(DailyUpdatesShareLinks.EXTRA_PAGE);
            }
        } catch (Exception ignored) {
        }
        page = DailyUpdatesShareLinks.canonicalKey(page);
        if (controller != null && page != null) {
            final String openPage = page;
            final TeluguCalendarController openController = controller;
            openController.setOpenedFromDailyUpdates(true);
            View calendarRoot = findViewById(R.id.telugu_calendar_root);
            Runnable showPage = () -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if ("rashi".equals(openPage)) {
                    openController.selectRaasiPage(false);
                } else if ("panchangam".equals(openPage)) {
                    openController.selectPanchangamPage(false);
                }
            };
            if (calendarRoot != null) {
                calendarRoot.post(showPage);
            } else {
                showPage.run();
            }
        }
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
        DailyUpdatesShareLanguage.end();
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
