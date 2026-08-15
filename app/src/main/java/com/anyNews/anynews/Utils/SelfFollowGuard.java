package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.models.FollowersProfileModel;
import com.anyNews.anynews.models.FollowingMenderModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Client safety net: never show or follow the signed-in account in follow lists.
 * Server also excludes self; this covers stale cache / older APIs.
 */
public final class SelfFollowGuard {

    private SelfFollowGuard() {
    }

    public static boolean isSelf(@Nullable Context context, @Nullable Long writerId) {
        if (context == null || writerId == null || writerId <= 0) return false;
        // Writers.writer_id for reporters === Users.id (not draft_id).
        long selfUserId = WalletUserId.resolve(context);
        return selfUserId > 0 && writerId == selfUserId;
    }

    public static boolean isSelf(@Nullable Context context, @Nullable Integer writerId) {
        if (writerId == null) return false;
        return isSelf(context, writerId.longValue());
    }

    public static ArrayList<FollowingMenderModel.Datum> withoutSelfMembers(
            @Nullable Context context,
            @Nullable List<FollowingMenderModel.Datum> source) {
        ArrayList<FollowingMenderModel.Datum> out = new ArrayList<>();
        if (source == null) return out;
        for (FollowingMenderModel.Datum item : source) {
            if (item == null) continue;
            if (isSelf(context, item.getWriterId())) continue;
            out.add(item);
        }
        return out;
    }

    public static ArrayList<FollowersProfileModel.Datum> withoutSelfSuggestions(
            @Nullable Context context,
            @Nullable List<FollowersProfileModel.Datum> source) {
        ArrayList<FollowersProfileModel.Datum> out = new ArrayList<>();
        if (source == null) return out;
        for (FollowersProfileModel.Datum item : source) {
            if (item == null) continue;
            if (isSelf(context, item.getWriterId())) continue;
            out.add(item);
        }
        return out;
    }
}
