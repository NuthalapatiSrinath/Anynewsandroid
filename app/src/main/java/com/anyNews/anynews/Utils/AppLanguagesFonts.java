package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.anyNews.anynews.R;

public class AppLanguagesFonts {
    static Context context;
    private static AppLanguagesFonts appLanguagesFonts;

    private AppLanguagesFonts(Context context) {
        this.context = context;
    }

    public static AppLanguagesFonts getInstance(Context context) {
        if (appLanguagesFonts == null) {
            appLanguagesFonts = new AppLanguagesFonts(context);
        }
        return appLanguagesFonts;
    }

    public static Typeface getTeluguFont() {
        return ResourcesCompat.getFont(context, R.font.robotolighttelugu);
    }

    public static Typeface getTamilLanguage() {
        return ResourcesCompat.getFont(context, R.font.mallannaregular);
    }

    public static Typeface getEnglisLanguage() {
        return ResourcesCompat.getFont(context, R.font.mallannaregular);
    }

    public static void setTypeFace(TextView view) {
        int languageId = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
        if (languageId == 4) {
            view.setTypeface(AppLanguagesFonts.getTamilLanguage());
        } else {
            if (languageId != 1) {
                view.setTypeface(AppLanguagesFonts.getTeluguFont());
            }
        }

    }

}
