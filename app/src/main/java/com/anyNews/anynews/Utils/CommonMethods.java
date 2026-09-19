package com.anyNews.anynews.Utils;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.anyNews.anynews.BuildConfig;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * Created by evontexone on 17/08/16.
 */
public class CommonMethods {

    Activity mActivity;


    public CommonMethods(Activity activity) {
        mActivity = activity;
    }

    public void showSettingsAlertforInternetSettings() {

        String provider = "INTERNET";
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                mActivity);

        alertDialog.setTitle(provider + " SETTINGS");

        alertDialog
                .setMessage(provider + " is not enabled! Want to go to settings menu?");

        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_WIFI_SETTINGS);
                        if (mActivity != null && mActivity.equals(BuildConfig.APPLICATION_ID)){
                            mActivity.startActivity(intent);
                        }
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

//    public void showSettingsAlert() {
//        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(mActivity);
//
//        //Setting Dialog Title
//        alertDialog.setTitle(R.string.GPSAlertDialogTitle);
//
//        //Setting Dialog Message
//        alertDialog.setMessage(R.string.GPSAlertDialogMessage);
//
//        //On Pressing Setting button
//        alertDialog.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                mActivity.startActivity(intent);
//            }
//        });
//
//        //On pressing cancel button
//        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        alertDialog.show();
//    }


    public String getHrsFormatfromDateForMonth(long date) {

        Date date1 = new Date(date);
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd ");  //"dd/MM/yyyy HH:mm:ss"

        return formatter.format(date1);
    }

    /** Reporter desk list: always a calendar date (never "Few hours ago"). */
    public String getReporterListDate(long dateMs) {
        if (dateMs <= 0) return "";
        try {
            return new SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
                    .format(new Date(dateMs));
        } catch (Exception e) {
            return getHrsFormatfromDateForMonth(dateMs);
        }
    }

    public String getHrsFormatfromDate(long date) {
        // Never return null — callers historically did "" + result → literal "null"
        // next to Writer strip "Reporter".
        if (date <= 0L) {
            return "";
        }
        String formattedDate = "";
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

     /*   CharSequence ago = null;

        long time = date;
        long now = System.currentTimeMillis();
        ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
*/


/*
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
          Date date1 = new Date();
          showLog(dateFormat.format(date1));*/


        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        Date date1 = new Date(date);


        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");  //"dd/MM/yyyy HH:mm:ss"
        Date date2 = new Date();


        /* showLog(" "+formatter.format(date2));*/


        long diff = date2.getTime() - date1.getTime();
        // Future / clock-skew timestamps left formattedDate null and painted "null".
        if (diff < 0) {
            try {
                return new SimpleDateFormat("MMM-dd", java.util.Locale.ENGLISH)
                        .format(date1);
            } catch (Exception e) {
                return "";
            }
        }

        long difference_In_Days
                = TimeUnit
                .MILLISECONDS
                .toDays(diff)
                % 365;


        if (difference_In_Days >= 1) {

            DateFormat dateFormat12 = new SimpleDateFormat("MMM-dd");  //dd-MMM

            Calendar cal = Calendar.getInstance();

            //Add one day to current date.
            cal.add(Calendar.DATE, 1);
            showLog(dateFormat12.format(cal.getTime()));

            //Substract one day to current date.
            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -Integer.parseInt(String.valueOf(difference_In_Days)));


            formattedDate = dateFormat12.format(cal.getTime());
            AppHelper.showLog("difference_In_Days #1 :"+formattedDate);
            formattedDate = "Few hours ago";


        } else {

            //  difference in days is equal to 1
            if (difference_In_Days == 0) {

                //    Toast.makeText(mActivity, "  DIFFERENCE IN DATES  "+difference_In_Days, Toast.LENGTH_SHORT).show();


                long difference_In_Hours
                        = (diff
                        / (1000 * 60 * 60))
                        % 24;

                if (difference_In_Hours >= 1) {


                    String present_date = formatter.format(date2).substring(0, 10);
                    String article_post_date = dateFormat.format(date1).substring(0, 10);
                    if (present_date.equals(article_post_date)) {

                        try {
                            formattedDate = dateFormat.format(date1);//"Today" ;
                            formattedDate = formattedDate.substring(11, 16) + " " + formattedDate.substring(19, 22);
                            AppHelper.showLog("difference_In_Days #2 :"+formattedDate);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        // formattedDate=hours.substring(10,15)+""+hours.substring(19,21);
                        //   String hours = formattedDate.substring(10, 21);


                    } else {

                        DateFormat dateFormat12 = new SimpleDateFormat("MMM-dd");  //dd-MMM
                        formattedDate = dateFormat12.format(date1);
                        AppHelper.showLog("difference_In_Days #3 :"+formattedDate);
                        formattedDate = "Few hours ago";
                    }


                    //    dateFormat
                    //  formattedDate=" Today ";
                    //


                    //     Toast.makeText(mActivity, "  DIFFERENCE IN HOURS "+difference_In_Hours, Toast.LENGTH_SHORT).show();



                } else {


                    long difference_In_Minutes
                            = (diff
                            / (1000 * 60))
                            % 60;


                    if (difference_In_Minutes > 0) {

                        formattedDate = "" + difference_In_Minutes + " min ago";
                        AppHelper.showLog("difference_In_Days #4 :"+formattedDate);

                    } else {
                        long difference_In_Seconds
                                = (diff / 1000) % 60;


                        // see this condition  my phone was swith off  connect in skype ok
                        if (difference_In_Seconds > 5) {

                            formattedDate = "" + difference_In_Seconds + " sec ago";
                            AppHelper.showLog("difference_In_Days #5 :"+formattedDate);
                        } else {

                            formattedDate = " just now ";
                            AppHelper.showLog("difference_In_Days #6 :"+formattedDate);
                        }

                        AppHelper.showLog("ELSE :::"+formattedDate);

                        //                      Toast.makeText(mActivity, "  DIFFERENCE IN MINITIES  "+difference_In_Minutes, Toast.LENGTH_SHORT).show();
//
                    }

                }


            } else {

                /*  formattedDate=" Today ";*/
                try {
                    formattedDate = new SimpleDateFormat("MMM-dd", java.util.Locale.ENGLISH)
                            .format(date1);
                } catch (Exception e) {
                    formattedDate = "";
                }

            }


        }



        /*
        if(String.valueOf(ago).equals("0 minutes ago")) {
            formattedDate = "Just Now";
        }else *//* if(){

        } else*//* {
            formattedDate = String.valueOf(ago);
        }*/

        /* long milliSeconds = 656478;
      DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");
      Date date = new Date(milliSeconds);
      showLog(dateFormat.format(date));*/
        if (formattedDate == null) {
            return "";
        }
        String out = formattedDate.trim();
        if (out.isEmpty()
                || "null".equalsIgnoreCase(out)
                || "undefined".equalsIgnoreCase(out)) {
            return "";
        }
        return out;
    }

    public boolean isEmail(String sEmail) {

        String email = sEmail.trim();
        email = email.replace(" ", "");

        //String emailPattern = "([*@])";

        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.length() > 0) {
            return true;
        } else {
            return false;
        }

    }

    public boolean isLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mActivity.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                showLog("Permission is granted");
                return true;
            } else {

                showLog("Permission is revoked");
                ActivityCompat.requestPermissions((Activity) mActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return false;
            }
        } else {
            showLog("Permission is granted");
            return true;
        }
    }

    public boolean isSpaceNotValid(String sEmail) {

        return sEmail.matches("^\\s*$");

    }

    public void showerror(String sAnyText) {
        new MaterialDialog.Builder(mActivity)
                .content(sAnyText)
                .positiveText("OK")
                .titleGravity(GravityEnum.CENTER)
                .cancelable(false)

                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();


                    }
                }).show();
    }

    public void showalert(String sAnyText) {
        new MaterialDialog.Builder(mActivity)
                .content(sAnyText)
                .positiveText("cancel")
                .titleGravity(GravityEnum.CENTER)
                .cancelable(false)

                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();


                    }
                }).show();
    }

    public boolean isPasswordlength(String sPassword) {
        sPassword = sPassword.trim();

        return sPassword.length() >= 8;

    }

    public boolean isStringEmpty(String sString) {
        sString = sString.trim();


        return sString.length() >= 2;

    }

    public void movetoactivtywithfinish(Class movetoanotheractivty) {

        Intent moveintent = new Intent(mActivity.getApplicationContext(), movetoanotheractivty);
        mActivity.startActivity(moveintent);
        mActivity.finish();

    }

    public void movetoactivtywithoutfinish(Class movetoanotheractivty) {

        Intent moveintent = new Intent(mActivity.getApplicationContext(), movetoanotheractivty);
        mActivity.startActivity(moveintent);


    }

    public String getDeviceID() {

        return Settings.Secure.getString(mActivity.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);


    }

    public void hideSoftKeyboard() {

        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = mActivity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(mActivity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    public void showToast(String sdf) {

        Toast.makeText(mActivity.getApplicationContext(), sdf, Toast.LENGTH_LONG).show();


    }

    public void showSnackBar(View view, String sdf) {

        Snackbar.make(view, sdf, Snackbar.LENGTH_LONG).show();

    }

    public void showToastt(String sdf) {

        Toast.makeText(mActivity.getApplicationContext(), sdf, Toast.LENGTH_SHORT).show();


    }


}
