package com.anyNews.anynews.Activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.google.gson.Gson;

/**
 * Battery / notification unrestricted help (Way2 structure, AnyNews branding).
 */
public class NotificationHelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.settings_page_bg));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.settings_page_bg));
        setContentView(R.layout.activity_notification_help);

        TextView version = findViewById(R.id.notif_help_version);
        version.setText(getString(R.string.settings_app_version, resolveVersionName()));

        try {
            Allsharedpreference pref = new Allsharedpreference(this);
            LanguageModelResponse lm = new Gson().fromJson(
                    pref.getShared_String(Allsharedpreference.Translator), LanguageModelResponse.class);
            if (lm != null && lm.getData() != null && lm.getData().getVersion() != null
                    && !lm.getData().getVersion().trim().isEmpty()) {
                version.setText(lm.getData().getVersion().trim() + " " + resolveVersionName());
            }
        } catch (Exception ignored) { }

        findViewById(R.id.notif_help_back).setOnClickListener(v -> finish());

        findViewById(R.id.notif_help_contact).setOnClickListener(v -> {
            String email = ProfileHubConfigHelper.getContactEmail(this);
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                intent.putExtra(Intent.EXTRA_SUBJECT, "AnyNews notifications help");
                startActivity(Intent.createChooser(intent, getString(R.string.profile_contact_us)));
            } catch (Exception e) {
                Toast.makeText(this, email, Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.notif_help_go_settings).setOnClickListener(v -> openSystemAppSettings());
    }

    private String resolveVersionName() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName != null ? info.versionName : "";
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    private void openSystemAppSettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
                return;
            }
        } catch (Exception ignored) { }
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.notif_help_go_settings, Toast.LENGTH_SHORT).show();
        }
    }
}
