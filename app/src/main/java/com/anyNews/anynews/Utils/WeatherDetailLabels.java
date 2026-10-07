package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Weather detail chrome keyed by {@link Constants#LanguageID}. */
public final class WeatherDetailLabels {

    private WeatherDetailLabels() {}

    private static int lang(@Nullable Context c) {
        if (c == null) return 0;
        return DailyUpdatesShareLanguage.effective(c);
    }

    @NonNull
    public static String defaultRegion(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ആന്ധ്രപ്രദേശ്, ഇന്ത്യ";
            case 2: return "आंध्र प्रदेश, भारत";
            case 3: return "ఆంధ్రప్రదేశ్, భారత్";
            case 4: return "ஆந்திரப் பிரதேசம், இந்தியா";
            case 5: return "ಆಂಧ್ರಪ್ರದೇಶ, ಭಾರತ";
            case 6: return "ଆନ୍ଧ୍ର ପ୍ରଦେଶ, ଭାରତ";
            default: return "Andhra Pradesh, India";
        }
    }

    @NonNull
    public static String feelsHighLow(
            @Nullable Context c, int feels, int high, int low) {
        switch (lang(c)) {
            case 1:
                return "അനുഭവം: " + feels + "°C  പരമാവധി: " + high + "°C  കുറഞ്ഞത്: " + low + "°C";
            case 2:
                return "महसूस: " + feels + "°C  अधिकतम: " + high + "°C  न्यूनतम: " + low + "°C";
            case 3:
                return "అనుభూతి: " + feels + "°C  గరిష్టం: " + high + "°C  కనిష్టం: " + low + "°C";
            case 4:
                return "உணர்வு: " + feels + "°C  அதிகபட்சம்: " + high + "°C  குறைந்தபட்சம்: " + low + "°C";
            case 5:
                return "ಅನುಭವ: " + feels + "°C  ಗರಿಷ್ಠ: " + high + "°C  ಕನಿಷ್ಠ: " + low + "°C";
            case 6:
                return "ଅନୁଭବ: " + feels + "°C  ସର୍ବାଧିକ: " + high + "°C  ସର୍ବନିମ୍ନ: " + low + "°C";
            default:
                return "Feels: " + feels + "°C  High: " + high + "°C  Low: " + low + "°C";
        }
    }

    @NonNull
    public static String rainHumidity(@Nullable Context c, int precip, int humidity) {
        switch (lang(c)) {
            case 1: return "മഴ " + precip + "% · ഈർപ്പം " + humidity + "%";
            case 2: return "बारिश " + precip + "% · नमी " + humidity + "%";
            case 3: return "వర్షం " + precip + "% · తేమ " + humidity + "%";
            case 4: return "மழை " + precip + "% · ஈரப்பதம் " + humidity + "%";
            case 5: return "ಮಳೆ " + precip + "% · ತೇವಾಂಶ " + humidity + "%";
            case 6: return "ବର୍ଷା " + precip + "% · ଆର୍ଦ୍ରତା " + humidity + "%";
            default: return "Rain " + precip + "% · Humidity " + humidity + "%";
        }
    }

    @NonNull
    public static String windSpeed(@Nullable Context c, int kmh) {
        switch (lang(c)) {
            case 1: return "കാറ്റ് വേഗം " + kmh + " km/h";
            case 2: return "हवा की गति " + kmh + " km/h";
            case 3: return "గాలి వేగం " + kmh + " km/h";
            case 4: return "காற்று வேகம் " + kmh + " km/h";
            case 5: return "ಗಾಳಿ ವೇಗ " + kmh + " km/h";
            case 6: return "ପବନ ବେଗ " + kmh + " km/h";
            default: return "Wind " + kmh + " km/h";
        }
    }

    @NonNull
    public static String windDir(@Nullable Context c, @NonNull String dir) {
        switch (lang(c)) {
            case 1: return "കാറ്റ് ദിശ " + dir;
            case 2: return "हवा की दिशा " + dir;
            case 3: return "గాలి దిశ " + dir;
            case 4: return "காற்று திசை " + dir;
            case 5: return "ಗಾಳಿ ದಿಕ್ಕು " + dir;
            case 6: return "ପବନ ଦିଗ " + dir;
            default: return "Wind dir " + dir;
        }
    }

    @NonNull
    public static String visibility(@Nullable Context c, double km) {
        switch (lang(c)) {
            case 1: return "ദൃശ്യത " + km + " km";
            case 2: return "दृश्यता " + km + " km";
            case 3: return "దృష్టి దూరం " + km + " km";
            case 4: return "பார்வை தூரம் " + km + " km";
            case 5: return "ಗೋಚರತೆ " + km + " km";
            case 6: return "ଦୃଶ୍ୟତା " + km + " km";
            default: return "Visibility " + km + " km";
        }
    }
}
