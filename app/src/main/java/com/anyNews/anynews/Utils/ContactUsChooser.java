package com.anyNews.anynews.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Contact Us chooser: WhatsApp (Configuration full_jatakam_whatsapp) or Mail.
 * Does not open mail immediately — user picks.
 */
public final class ContactUsChooser {

    private ContactUsChooser() {}

    public static void show(
            @NonNull Context context,
            @Nullable String mailSubject) {
        final String subject =
                TextUtils.isEmpty(mailSubject) ? "Contact — AnyNews" : mailSubject;
        CharSequence[] items =
                new CharSequence[] {
                    context.getString(R.string.contact_chooser_whatsapp),
                    context.getString(R.string.contact_chooser_mail)
                };
        new AlertDialog.Builder(context)
                .setTitle(R.string.contact_chooser_title)
                .setItems(
                        items,
                        (d, which) -> {
                            if (which == 0) {
                                openWhatsApp(context);
                            } else {
                                openMail(context, subject);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static void openWhatsApp(@NonNull Context context) {
        String number = ProfileHubConfigHelper.getFullJatakamWhatsapp(context);
        if (TextUtils.isEmpty(number)) {
            number =
                    SharedPrefsData.getString(
                            context, Constants.FULL_JATAKAM_WHATSAPP, Constants.PREF_NAME);
        }
        if (TextUtils.isEmpty(number)) {
            Toast.makeText(context, R.string.ad_center_whatsapp_missing, Toast.LENGTH_LONG).show();
            return;
        }
        String digits = number.replaceAll("[^0-9]", "");
        if (digits.length() < 10) {
            Toast.makeText(context, R.string.ad_center_whatsapp_missing, Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Uri uri =
                    Uri.parse(
                            "https://wa.me/"
                                    + digits
                                    + "?text="
                                    + Uri.encode(
                                            context.getString(R.string.contact_whatsapp_prefill)));
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception e) {
            Toast.makeText(context, R.string.ad_center_whatsapp_missing, Toast.LENGTH_SHORT).show();
        }
    }

    public static void openMail(@NonNull Context context, @Nullable String subject) {
        String to = ProfileHubConfigHelper.getContactEmail(context);
        try {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:" + to));
            email.putExtra(
                    Intent.EXTRA_SUBJECT,
                    TextUtils.isEmpty(subject) ? "Contact — AnyNews" : subject);
            context.startActivity(
                    Intent.createChooser(email, context.getString(R.string.profile_contact_us)));
        } catch (Exception e) {
            Toast.makeText(context, R.string.contact_mail_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
