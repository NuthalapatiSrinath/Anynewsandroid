package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Localized profile-photo empty-state labels.
 * Uses in-app {@link Constants#LanguageID} (same pattern as {@link HomeOnlineModeLabels}).
 */
public final class ProfilePhotoLabels {

    private ProfilePhotoLabels() {
    }

    /** Short affordance under / on empty avatar: tap = upload. */
    public static String tapToAdd(@Nullable Context context) {
        switch (currentLanguageId(context)) {
            case 1: // Malayalam
                return "ഫോട്ടോ ചേർക്കുക";
            case 2: // Hindi
                return "फ़ोटो जोड़ें";
            case 3: // Telugu
                return "ఫోటో జత చేయండి";
            case 4: // Tamil
                return "புகைப்படம் சேர்";
            case 5: // Kannada
                return "ಫೋಟೋ ಸೇರಿಸಿ";
            case 6: // Odia
                return "ଫଟୋ ଯୋଡନ୍ତୁ";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.profile_tap_add_photo);
                    } catch (Exception ignored) {
                    }
                }
                return "Tap to add photo";
        }
    }

    /** Bottom-right / edit-profile action: replace existing avatar. */
    public static String changePhoto(@Nullable Context context) {
        switch (currentLanguageId(context)) {
            case 1: // Malayalam
                return "ഫോട്ടോ മാറ്റുക";
            case 2: // Hindi
                return "फ़ोटो बदलें";
            case 3: // Telugu
                return "ఫోటో మార్చండి";
            case 4: // Tamil
                return "புகைப்படம் மாற்று";
            case 5: // Kannada
                return "ಫೋಟೋ ಬದಲಾಯಿಸಿ";
            case 6: // Odia
                return "ଫଟୋ ବଦଳାନ୍ତୁ";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.profile_change_photo);
                    } catch (Exception ignored) {
                    }
                }
                return "Change photo";
        }
    }

    public static String contentDescription(@Nullable Context context) {
        return tapToAdd(context);
    }

    private static int currentLanguageId(@Nullable Context context) {
        if (context == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
            return 0;
        }
    }
}
