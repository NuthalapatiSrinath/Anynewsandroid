package com.anyNews.anynews.Utils;



import android.content.Context;

import android.text.TextUtils;



import com.anyNews.anynews.Pojo.NewUserProfileResponse;



/**

 * "% profile completed" next to Edit/Complete Profile = field completion, not earnings.

 * Uses max(server profile_completion, local field tally) so a stale 0% never sticks

 * when name/email/mobile/photo are already filled (prefs and/or API).

 */

public final class ProfileAchievementHelper {



    private ProfileAchievementHelper() {

    }



    public static int resolveAchievedPct(NewUserProfileResponse.Data d) {

        if (d == null) return 0;

        int local = computeLocalProfileCompletionPct(d);

        Integer fromServer = parsePct(d.getProfileCompletion());

        if (fromServer != null) {

            // Prefer the higher of server vs local — server 0 must not hide filled fields.

            return Math.max(fromServer, local);

        }

        if (local > 0) {

            return local;

        }

        Integer approval = parsePct(d.getApproval_rates());

        return approval != null ? approval : 0;

    }



    /**

     * Soft-refresh / cache payloads often omit email or still say "Guest" while login

     * prefs already hold the real identity. Fill blanks from prefs before tallying.

     */

    public static void mergeIdentityFromPrefs(Context context, NewUserProfileResponse.Data d) {

        if (context == null || d == null) return;

        Context app = context.getApplicationContext();

        try {

            String prefName = SharedPrefsData.getString(app, Constants.USER_NAME, Constants.PREF_NAME);

            String apiName = d.getUserName() != null ? d.getUserName().trim() : "";

            if (isBlankOrGuest(apiName) && !isBlankOrGuest(prefName)) {

                d.setUserName(prefName.trim());

            }

        } catch (Exception ignored) {

        }

        try {

            String apiEmail = d.getEmail() != null ? d.getEmail().trim() : "";

            if (!apiEmail.contains("@")) {

                String email = "";

                try {

                    Allsharedpreference pref = new Allsharedpreference(app);

                    if (pref.getEmailId() != null) email = pref.getEmailId().trim();

                } catch (Exception ignored) {

                }

                if (!email.contains("@")) {

                    String e = SharedPrefsData.getString(app, Constants.Email, Constants.PREF_NAME);

                    if (e != null) email = e.trim();

                }

                if (email.contains("@")) {

                    d.setEmail(email);

                }

            }

        } catch (Exception ignored) {

        }

        try {

            String apiMobile = d.getMobileNumber() != null ? d.getMobileNumber() : "";

            String apiDigits = apiMobile.replaceAll("\\D", "");

            if (apiDigits.length() < 10) {

                String mobile = SharedPrefsData.getString(

                        app, Constants.Selected_Phone_Number, Constants.PREF_NAME);

                if (TextUtils.isEmpty(mobile) || "null".equalsIgnoreCase(mobile)) {

                    mobile = SharedPrefsData.getString(app, Constants.PhoneNumber, Constants.PREF_NAME);

                }

                if (!TextUtils.isEmpty(mobile) && !"null".equalsIgnoreCase(mobile)

                        && mobile.replaceAll("\\D", "").length() >= 10) {

                    d.setMobileNumber(mobile.trim());

                }

            }

        } catch (Exception ignored) {

        }

        try {

            String apiPic = d.getProfilePic() != null ? d.getProfilePic().trim() : "";

            boolean apiDefault = apiPic.isEmpty()

                    || apiPic.toLowerCase().contains("user_profile.webp");

            if (apiDefault) {

                String pic = SharedPrefsData.getString(

                        app, Constants.PROFILE_PIC_URL, Constants.PREF_NAME);

                if (!TextUtils.isEmpty(pic) && !"null".equalsIgnoreCase(pic)

                        && !pic.toLowerCase().contains("user_profile.webp")) {

                    d.setProfilePic(pic.trim());

                }

            }

        } catch (Exception ignored) {

        }

    }



    /** Name/email/mobile/photo from prefs alone (post-login, before profile API). */

    public static int computePctFromPrefs(Context context) {

        if (context == null) return 0;

        try {

            NewUserProfileResponse.Data stub = new NewUserProfileResponse().new Data();

            mergeIdentityFromPrefs(context, stub);

            return computeLocalProfileCompletionPct(stub);

        } catch (Exception e) {

            return 0;

        }

    }



    /** max(server/local on payload, prefs tally) — never let soft-refresh downgrade to 0. */

    public static int resolveAchievedPctWithPrefs(Context context, NewUserProfileResponse.Data d) {

        if (d != null) {

            mergeIdentityFromPrefs(context, d);

        }

        int fromPayload = resolveAchievedPct(d);

        int fromPrefs = computePctFromPrefs(context);

        return Math.max(fromPayload, fromPrefs);

    }



    /** Name, email, mobile, custom photo — 25% each. */

    public static int computeLocalProfileCompletionPct(NewUserProfileResponse.Data d) {

        if (d == null) return 0;

        int filled = 0;

        final int total = 4;



        String name = d.getUserName() != null ? d.getUserName().trim() : "";

        if (!isBlankOrGuest(name)) {

            filled++;

        }



        String email = d.getEmail() != null ? d.getEmail().trim() : "";

        if (email.contains("@")) {

            filled++;

        }



        String mobile = d.getMobileNumber() != null ? d.getMobileNumber().replaceAll("\\D", "") : "";

        if (mobile.length() >= 10) {

            filled++;

        }



        String pic = d.getProfilePic() != null ? d.getProfilePic() : "";

        if (!pic.isEmpty() && !pic.toLowerCase().contains("user_profile.webp")) {

            filled++;

        }



        return Math.round(100f * filled / total);

    }



    public static Integer parsePct(String raw) {

        if (raw == null) return null;

        try {

            String t = raw.replace("%", "").trim();

            if (t.isEmpty() || "null".equalsIgnoreCase(t)) return null;

            return Math.max(0, Math.min(100, (int) Math.round(Double.parseDouble(t))));

        } catch (Exception e) {

            return null;

        }

    }



    private static boolean isBlankOrGuest(String name) {

        if (name == null) return true;

        String t = name.trim();

        return t.isEmpty()

                || "guest".equalsIgnoreCase(t)

                || "null".equalsIgnoreCase(t);

    }

}


