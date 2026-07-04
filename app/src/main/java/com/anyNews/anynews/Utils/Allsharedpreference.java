package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by barediarjun on 12/12/16.
 */

public class Allsharedpreference {


    public static final String DEVICE_ID = "DEVICE_ID";

    public static final String IS_USER_ALREADY_LOGIN = "IS_USER_ALREADY_LOGIN";
    public static final String IS_ALREADY_LOGIN = "IS_ALREADY_LOGIN";
    public static final String NOT_FIRST_TIME_LOGIN = "FIRST_TIME_LOGIN";
    public static final String Otp = "Otp";
    public static final String Registration = "Registration";
    public static final String Language = "Language";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    public static final String Translator = "Translator";
    public static final String ADVERTISER_ID = "advertiser_ID";

    public static final String PREFS_NAME = "dogemo";
    public static final String Serial_Number = "Serial_Number";
    public static final String EMAIL_ID = "email_id";
    public static final String FIRSTFLIPTIMEANIMATION = "firstFlipTimeAnimation";

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;

    public Allsharedpreference(Context context) {

        this._context = context;
        pref = _context.getSharedPreferences(PREFS_NAME, PRIVATE_MODE);
        editor = pref.edit();

    }


    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }




    public void setShared_String(String key, String sKEY_IS_EDITTEXT) {
        editor.putString(key, sKEY_IS_EDITTEXT);
        editor.commit();
    }

    public String getShared_String(String key) {
        return pref.getString(key, null);
    }
  public void setAdvertiserId(String key, String sKEY_IS_EDITTEXT) {
        editor.putString(key, sKEY_IS_EDITTEXT);
        editor.commit();
    }

    public String getAdvertiserId(String key) {
        return pref.getString(key, null);
    }

    public void setShared_Int(String key, int sKEY_IS_EDITTEXT) {
        editor.putInt(key, sKEY_IS_EDITTEXT);
        editor.commit();
    }

    public int getShared_Int(String key) {
        return pref.getInt(key, 0);
    }

    public void setShared_Booleen(String key, Boolean sKEY_IS_EDITTEXT) {
        editor.putBoolean(key, sKEY_IS_EDITTEXT);
        editor.commit();
    }
   public String getEmailId() {
        return pref.getString(EMAIL_ID, "");
    }

    public void setEmailID(String value) {
        editor.putString(EMAIL_ID, value);
        editor.commit();
    }

    public Boolean getShared_Booleen(String key) {
        return pref.getBoolean(key, false);
    }

    public void movetoactivtywithfinish(Class movetoanotheractivty) {

        Intent moveintent = new Intent(_context.getApplicationContext(), movetoanotheractivty);
        moveintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        _context.startActivity(moveintent);

    }

    public void movetoactivtywithoutfinish(Class movetoanotheractivty) {

        Intent moveintent = new Intent(_context.getApplicationContext(), movetoanotheractivty);
        _context.startActivity(moveintent);


    }

    public void showToast(String sdf) {

        Toast.makeText(_context, sdf, Toast.LENGTH_LONG).show();


    }

    public void clearAllSharedPreferences() {
        editor.clear();
        editor.apply();
    }


    public boolean getShowFirstTimeAnimation() {
        return pref.getBoolean(FIRSTFLIPTIMEANIMATION,true);
    }
    public void setShowFirstTimeAnimation(boolean key) {
        editor.putBoolean( FIRSTFLIPTIMEANIMATION,key);
        editor.commit();
    }
}
