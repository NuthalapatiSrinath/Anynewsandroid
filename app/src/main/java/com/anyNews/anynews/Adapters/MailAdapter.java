package com.anyNews.anynews.Adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Pojo.ListOfCommentsResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AvatarImageHelper;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.SharedPrefsData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private static final int TYPE_PARENT = 0;
    private static final int TYPE_REPLY = 1;

    private static final int[] AVATAR_COLORS = {
            0xFF2E7D32, // green
            0xFF1565C0, // blue
            0xFFF9A825, // yellow
            0xFFC2185B, // pink
            0xFF6A1B9A, // purple
            0xFF00838F, // teal
            0xFFE65100, // deep orange
            0xFF455A64  // blue grey
    };

    private Context mContext;
    private List<ListOfCommentsResponse.Datum> allComments;
    private final List<DisplayItem> displayItems = new ArrayList<>();
    private final Map<Long, List<ListOfCommentsResponse.Datum>> repliesByParent = new HashMap<>();
    private final Map<Long, ListOfCommentsResponse.Datum> commentById = new HashMap<>();
    private final Set<Long> expandedParents = new HashSet<>();
    ShowReportLayout showReportLayout;
    OnReplyListener onReplyListener;
    OnCopyListener onCopyListener;
    OnLikeListener onLikeListener;
    Allsharedpreference allsharedpreference;

    public MailAdapter(Context mContext, List<ListOfCommentsResponse.Datum> mEmailData,
                       ShowReportLayout showReportLayout, OnReplyListener onReplyListener,
                       OnCopyListener onCopyListener, OnLikeListener onLikeListener) {
        this.mContext = mContext;
        this.allComments = mEmailData != null ? mEmailData : new ArrayList<>();
        this.showReportLayout = showReportLayout;
        this.onReplyListener = onReplyListener;
        this.onCopyListener = onCopyListener;
        this.onLikeListener = onLikeListener;
        allsharedpreference = new Allsharedpreference(mContext);
        rebuildThreadData();
    }

    public void setComments(List<ListOfCommentsResponse.Datum> comments) {
        this.allComments = comments != null ? comments : new ArrayList<>();
        rebuildThreadData();
    }

    private void rebuildThreadData() {
        repliesByParent.clear();
        commentById.clear();
        displayItems.clear();

        List<ListOfCommentsResponse.Datum> parents = new ArrayList<>();
        for (ListOfCommentsResponse.Datum datum : allComments) {
            if (datum.getId() != null) {
                commentById.put(datum.getId(), datum);
            }
            Long parentId = datum.getParentCommentId();
            if (parentId != null && parentId > 0) {
                List<ListOfCommentsResponse.Datum> replies = repliesByParent.get(parentId);
                if (replies == null) {
                    replies = new ArrayList<>();
                    repliesByParent.put(parentId, replies);
                }
                replies.add(datum);
            } else {
                parents.add(datum);
            }
        }

        Collections.sort(parents, new Comparator<ListOfCommentsResponse.Datum>() {
            @Override
            public int compare(ListOfCommentsResponse.Datum a, ListOfCommentsResponse.Datum b) {
                long timeA = a.getCommentTime() != null ? a.getCommentTime() : 0L;
                long timeB = b.getCommentTime() != null ? b.getCommentTime() : 0L;
                return Long.compare(timeB, timeA);
            }
        });

        for (List<ListOfCommentsResponse.Datum> replies : repliesByParent.values()) {
            Collections.sort(replies, new Comparator<ListOfCommentsResponse.Datum>() {
                @Override
                public int compare(ListOfCommentsResponse.Datum a, ListOfCommentsResponse.Datum b) {
                    long timeA = a.getCommentTime() != null ? a.getCommentTime() : 0L;
                    long timeB = b.getCommentTime() != null ? b.getCommentTime() : 0L;
                    return Long.compare(timeA, timeB);
                }
            });
        }

        Set<Long> rootsToExpand = new HashSet<>();
        for (Long expandedId : expandedParents) {
            Long rootId = resolveRootCommentId(expandedId);
            if (rootId != null) {
                rootsToExpand.add(rootId);
            }
        }
        expandedParents.clear();
        expandedParents.addAll(rootsToExpand);

        for (ListOfCommentsResponse.Datum parent : parents) {
            int replyCount = getDescendantReplyCount(parent.getId());
            if (parent.getReplyCount() != null && parent.getReplyCount() > replyCount) {
                replyCount = parent.getReplyCount();
            }
            displayItems.add(new DisplayItem(TYPE_PARENT, parent, replyCount));
            if (parent.getId() != null && expandedParents.contains(parent.getId())) {
                appendFlattenedReplies(parent.getId());
            }
        }
    }

    private Long resolveRootCommentId(Long commentId) {
        if (commentId == null || commentId <= 0) {
            return null;
        }
        Long current = commentId;
        Set<Long> seen = new HashSet<>();
        while (current != null && current > 0 && !seen.contains(current)) {
            seen.add(current);
            ListOfCommentsResponse.Datum node = commentById.get(current);
            if (node == null) {
                return current;
            }
            Long parentId = node.getParentCommentId();
            if (parentId == null || parentId <= 0) {
                return current;
            }
            current = parentId;
        }
        return commentId;
    }

    private int getDescendantReplyCount(Long rootId) {
        if (rootId == null) {
            return 0;
        }
        int total = 0;
        List<Long> stack = new ArrayList<>();
        stack.add(rootId);
        while (!stack.isEmpty()) {
            Long id = stack.remove(stack.size() - 1);
            List<ListOfCommentsResponse.Datum> kids = repliesByParent.get(id);
            if (kids == null) {
                continue;
            }
            for (ListOfCommentsResponse.Datum kid : kids) {
                total += 1;
                if (kid.getId() != null) {
                    stack.add(kid.getId());
                }
            }
        }
        return total;
    }

    private void appendFlattenedReplies(Long rootId) {
        List<ListOfCommentsResponse.Datum> direct = repliesByParent.get(rootId);
        if (direct == null) {
            return;
        }
        for (ListOfCommentsResponse.Datum reply : direct) {
            displayItems.add(new DisplayItem(TYPE_REPLY, reply, 0));
            if (reply.getId() != null) {
                appendFlattenedReplies(reply.getId());
            }
        }
    }

    public void expandParent(Long parentId) {
        if (parentId != null && parentId > 0) {
            expandedParents.add(parentId);
            rebuildThreadData();
            notifyDataSetChanged();
        }
    }

    private void toggleReplies(int position) {
        DisplayItem item = displayItems.get(position);
        if (item.type != TYPE_PARENT || item.replyCount <= 0 || item.datum.getId() == null) {
            return;
        }
        Long parentId = item.datum.getId();
        if (expandedParents.contains(parentId)) {
            expandedParents.remove(parentId);
        } else {
            expandedParents.add(parentId);
        }
        rebuildThreadData();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position).type;
    }

    @NonNull
    @Override
    public MailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_items_list, parent, false);
        return new MailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MailViewHolder holder, int position) {
        DisplayItem item = displayItems.get(position);
        ListOfCommentsResponse.Datum datum = item.datum;
        boolean isReply = item.type == TYPE_REPLY;

        holder.comment_time.setText(formatShortAgo(datum.getCommentTime()));
        holder.comment_details.setText(datum.getComment());

        // Empty emails used to match (Allsharedpreference defaults to "") so every
        // comment looked like "Me". Require a real address or matching draft_id.
        boolean isMe = isOwnComment(datum);
        String displayName = resolveDisplayName(datum, isMe);
        holder.person_name.setText(displayName);
        holder.imageViewReport.setVisibility(isMe ? View.GONE : View.VISIBLE);

        String district = resolveDistrictLabel(datum, isMe);
        if (TextUtils.isEmpty(district)) {
            holder.location_row.setVisibility(View.GONE);
        } else {
            holder.location_row.setVisibility(View.VISIBLE);
            holder.comment_location.setText(district);
        }

        bindAvatar(holder, displayName, datum, isMe);

        // user_comment_count badge — hide by default for a clean UI.
        if (holder.avatar_badge != null) {
            holder.avatar_badge.setVisibility(View.GONE);
        }

        boolean liked = datum.getLikedByMe() != null && datum.getLikedByMe();
        int likes = datum.getLikes() != null ? datum.getLikes() : 0;
        holder.like_count.setText(String.valueOf(likes));
        // Reset heart size after screenshot/resume density glitches — layout XML is 18dp.
        float density = mContext.getResources().getDisplayMetrics().density;
        int heartPx = Math.round(18f * density);
        ViewGroup.LayoutParams heartLp = holder.imageViewLike.getLayoutParams();
        if (heartLp != null && (heartLp.width != heartPx || heartLp.height != heartPx)) {
            heartLp.width = heartPx;
            heartLp.height = heartPx;
            holder.imageViewLike.setLayoutParams(heartLp);
        }
        holder.imageViewLike.setImageResource(
                liked ? R.drawable.ic_comment_heart_filled : R.drawable.ic_comment_heart_outline);

        int indent = isReply
                ? (int) (20 * density)
                : 0;
        holder.itemRoot.setPadding(indent, holder.itemRoot.getPaddingTop(),
                holder.itemRoot.getPaddingRight(), holder.itemRoot.getPaddingBottom());

        if (!isReply && item.replyCount > 0) {
            holder.view_replies_link.setVisibility(View.VISIBLE);
            boolean expanded = expandedParents.contains(datum.getId());
            if (expanded) {
                holder.view_replies_link.setText("Hide replies");
            } else if (item.replyCount == 1) {
                holder.view_replies_link.setText("View 1 reply");
            } else {
                holder.view_replies_link.setText("View " + item.replyCount + " replies");
            }
            holder.view_replies_link.setOnClickListener(view -> {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    toggleReplies(adapterPosition);
                }
            });
        } else {
            holder.view_replies_link.setVisibility(View.GONE);
            holder.view_replies_link.setOnClickListener(null);
        }

        holder.imageViewReport.setOnClickListener(view -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && showReportLayout != null) {
                showReportLayout.showReportLayout(findSourceIndex(adapterPosition));
            }
        });
        holder.comment_reply_btn.setOnClickListener(view -> {
            if (onReplyListener != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onReplyListener.onReply(findSourceIndex(adapterPosition));
                }
            }
        });
        holder.imageViewCopy.setOnClickListener(view -> {
            if (onCopyListener != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onCopyListener.onCopy(findSourceIndex(adapterPosition));
                }
            }
        });
        holder.like_row.setOnClickListener(view -> {
            if (onLikeListener != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onLikeListener.onLike(findSourceIndex(adapterPosition));
                }
            }
        });
    }

    private void bindAvatar(MailViewHolder holder, String displayName,
                            ListOfCommentsResponse.Datum datum, boolean isMe) {
        String letterSource = !TextUtils.isEmpty(datum.getUserName()) ? datum.getUserName() : displayName;
        char letter = '?';
        if (!TextUtils.isEmpty(letterSource)) {
            letter = Character.toUpperCase(letterSource.trim().charAt(0));
        }
        holder.avatar_letter.setText(String.valueOf(letter));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(avatarColorFor(letterSource));
        holder.avatar_letter.setBackground(bg);

        String picUrl = datum.getUser_profile_pic();
        if (isMe) {
            String localPic = SharedPrefsData.getString(
                    mContext, Constants.PROFILE_PIC_URL, Constants.PREF_NAME);
            if (!AvatarImageHelper.isUnusablePhoto(localPic)) {
                picUrl = localPic;
            }
        }
        // Treat shared silhouette placeholders as missing so we don't paint the blue default.
        boolean hasPic = !AvatarImageHelper.isUnusablePhoto(picUrl);
        if (hasPic) {
            holder.circular_image.setVisibility(View.VISIBLE);
            holder.avatar_letter.setVisibility(View.INVISIBLE);
            AvatarImageHelper.loadPicasso(holder.circular_image, picUrl);
        } else {
            holder.circular_image.setVisibility(View.GONE);
            holder.avatar_letter.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Own comment = matching non-empty email, or matching positive draft_id.
     * Empty email must never match (prefs default to "").
     */
    private boolean isOwnComment(ListOfCommentsResponse.Datum datum) {
        if (datum == null) return false;
        String myEmail = allsharedpreference != null ? allsharedpreference.getEmailId() : null;
        String theirEmail = datum.getEmail();
        if (!TextUtils.isEmpty(myEmail) && myEmail.contains("@")
                && !TextUtils.isEmpty(theirEmail) && theirEmail.contains("@")
                && myEmail.trim().equalsIgnoreCase(theirEmail.trim())) {
            return true;
        }
        if (datum.getDraftId() != null) {
            int myDraft = SharedPrefsData.getInt(mContext, Constants.DraftID, Constants.PREF_NAME);
            long theirDraft = datum.getDraftId();
            if (myDraft > 0 && theirDraft > 0 && theirDraft == myDraft) {
                return true;
            }
        }
        return false;
    }

    /** Prefer profile name over the old hardcoded "Me" label. */
    private String resolveDisplayName(ListOfCommentsResponse.Datum datum, boolean isMe) {
        if (isMe) {
            String local = SharedPrefsData.getString(
                    mContext, Constants.USER_NAME, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(local) && !"guest".equalsIgnoreCase(local.trim())) {
                return local.trim();
            }
        }
        if (datum != null && !TextUtils.isEmpty(datum.getUserName())
                && !"guest".equalsIgnoreCase(datum.getUserName().trim())) {
            return datum.getUserName().trim();
        }
        if (isMe) {
            String email = allsharedpreference != null ? allsharedpreference.getEmailId() : null;
            if (!TextUtils.isEmpty(email) && email.contains("@")) {
                return email.substring(0, email.indexOf('@')).trim();
            }
        }
        return "User";
    }

    /**
     * Own comments: app-selected district in the current language.
     * Hide labels whose script conflicts with LanguageID (e.g. Hindi while Telugu).
     */
    private String resolveDistrictLabel(ListOfCommentsResponse.Datum datum, boolean isMe) {
        int lang = SharedPrefsData.getInt(mContext, Constants.LanguageID, Constants.PREF_NAME);
        String district = null;
        if (isMe) {
            String localDistrict = DistrictLabelHelper.resolveDisplayLabel(mContext);
            if (DistrictLabelHelper.isUsable(localDistrict)
                    && DistrictLabelHelper.labelCompatibleWithLanguage(localDistrict, lang)) {
                district = localDistrict.trim() + " (D)";
            }
        }
        if (TextUtils.isEmpty(district) && datum != null) {
            String apiDistrict = datum.getDistrict();
            if (DistrictLabelHelper.isUsable(apiDistrict)) {
                String bare = apiDistrict.trim();
                if (bare.endsWith("(D)")) {
                    bare = bare.substring(0, bare.length() - 3).trim();
                }
                if (DistrictLabelHelper.labelCompatibleWithLanguage(bare, lang)) {
                    district = apiDistrict.trim();
                }
            }
        }
        return district;
    }

    private static int avatarColorFor(String key) {
        if (TextUtils.isEmpty(key)) {
            return AVATAR_COLORS[0];
        }
        int hash = Math.abs(key.toLowerCase(Locale.US).hashCode());
        return AVATAR_COLORS[hash % AVATAR_COLORS.length];
    }

    static String formatShortAgo(Long commentTimeMs) {
        if (commentTimeMs == null || commentTimeMs <= 0) {
            return "";
        }
        long diff = System.currentTimeMillis() - commentTimeMs;
        if (diff < 0) {
            diff = 0;
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if (seconds < 60) {
            return seconds <= 5 ? "just now" : seconds + "s ago";
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 60) {
            return minutes + "m ago";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours < 24) {
            return hours + "h ago";
        }
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days < 7) {
            return days + "d ago";
        }
        return (days / 7) + "w ago";
    }

    private int findSourceIndex(int displayPosition) {
        if (displayPosition < 0 || displayPosition >= displayItems.size()) {
            return displayPosition;
        }
        ListOfCommentsResponse.Datum target = displayItems.get(displayPosition).datum;
        for (int i = 0; i < allComments.size(); i++) {
            ListOfCommentsResponse.Datum candidate = allComments.get(i);
            if (candidate.getId() != null && candidate.getId().equals(target.getId())) {
                return i;
            }
        }
        return displayPosition;
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    public class MailViewHolder extends RecyclerView.ViewHolder {
        TextView comment_details;
        TextView person_name;
        TextView comment_time;
        View comment_reply_btn;
        TextView view_replies_link;
        TextView avatar_letter;
        TextView avatar_badge;
        TextView comment_location;
        TextView like_count;
        CircleImageView circular_image;
        ImageView imageViewReport;
        ImageView imageViewCopy;
        ImageView imageViewLike;
        LinearLayout layoutComment;
        FrameLayout layoutImage;
        LinearLayout location_row;
        LinearLayout like_row;
        View itemRoot;

        public MailViewHolder(@NonNull View itemView) {
            super(itemView);
            itemRoot = itemView.findViewById(R.id.comment_item_root);
            comment_details = itemView.findViewById(R.id.comment_details);
            person_name = itemView.findViewById(R.id.person_name);
            comment_time = itemView.findViewById(R.id.comment_time);
            comment_reply_btn = itemView.findViewById(R.id.comment_reply_btn);
            view_replies_link = itemView.findViewById(R.id.view_replies_link);
            avatar_letter = itemView.findViewById(R.id.avatar_letter);
            avatar_badge = itemView.findViewById(R.id.avatar_badge);
            comment_location = itemView.findViewById(R.id.comment_location);
            like_count = itemView.findViewById(R.id.like_count);
            circular_image = itemView.findViewById(R.id.circular_image);
            imageViewReport = itemView.findViewById(R.id.imageViewReport);
            imageViewCopy = itemView.findViewById(R.id.imageViewCopy);
            imageViewLike = itemView.findViewById(R.id.imageViewLike);
            layoutComment = itemView.findViewById(R.id.layoutComment);
            layoutImage = itemView.findViewById(R.id.layoutImage);
            location_row = itemView.findViewById(R.id.location_row);
            like_row = itemView.findViewById(R.id.like_row);
        }
    }

    private static class DisplayItem {
        final int type;
        final ListOfCommentsResponse.Datum datum;
        final int replyCount;

        DisplayItem(int type, ListOfCommentsResponse.Datum datum, int replyCount) {
            this.type = type;
            this.datum = datum;
            this.replyCount = replyCount;
        }
    }

    public interface ShowReportLayout {
        void showReportLayout(int position);
    }

    public interface OnReplyListener {
        void onReply(int position);
    }

    public interface OnCopyListener {
        void onCopy(int position);
    }

    public interface OnLikeListener {
        void onLike(int position);
    }
}
