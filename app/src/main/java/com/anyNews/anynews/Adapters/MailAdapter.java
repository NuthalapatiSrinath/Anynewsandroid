package com.anyNews.anynews.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Pojo.ListOfCommentsResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.AvatarImageHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.MediaUrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private static final int TYPE_PARENT = 0;
    private static final int TYPE_REPLY = 1;

    private Context mContext;
    private List<ListOfCommentsResponse.Datum> allComments;
    private final List<DisplayItem> displayItems = new ArrayList<>();
    private final Map<Long, List<ListOfCommentsResponse.Datum>> repliesByParent = new HashMap<>();
    private final Map<Long, ListOfCommentsResponse.Datum> commentById = new HashMap<>();
    private final Set<Long> expandedParents = new HashSet<>();
    CommonMethods commonMethods;
    ShowReportLayout showReportLayout;
    OnReplyListener onReplyListener;
    Allsharedpreference allsharedpreference;

    public MailAdapter(Context mContext, List<ListOfCommentsResponse.Datum> mEmailData,
                       ShowReportLayout showReportLayout, OnReplyListener onReplyListener) {
        this.mContext = mContext;
        this.allComments = mEmailData != null ? mEmailData : new ArrayList<>();
        this.showReportLayout = showReportLayout;
        this.onReplyListener = onReplyListener;
        commonMethods = new CommonMethods((Activity) mContext);
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

        // Resolve expand targets that point at nested replies back to the
        // top-level root so "View N replies" opens after posting a reply-to-reply.
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
            // Prefer server reply_count when it already includes all descendants.
            if (parent.getReplyCount() != null && parent.getReplyCount() > replyCount) {
                replyCount = parent.getReplyCount();
            }
            displayItems.add(new DisplayItem(TYPE_PARENT, parent, replyCount));
            if (parent.getId() != null && expandedParents.contains(parent.getId())) {
                appendFlattenedReplies(parent.getId());
            }
        }
    }

    /**
     * Walk up parent_comment_id until a top-level comment (no parent) is found.
     */
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

    /** Count all nested replies under a top-level comment (any depth). */
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

    /**
     * Instagram-style flatten: depth-first list all descendant replies under
     * the top-level root so reply-to-reply is visible in "View N replies".
     */
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
            // May be a nested reply id; rebuildThreadData resolves to root.
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

        holder.comment_time.setText(commonMethods.getHrsFormatfromDate(datum.getCommentTime()));
        holder.comment_details.setText(datum.getComment());

        AvatarImageHelper.loadPicasso(holder.circular_image, datum.getUser_profile_pic());

        AppHelper.showLog("EMAIL ID SERVER:" + datum.getEmail());
        AppHelper.showLog("EMAIL ID LOCAL:" + allsharedpreference.getEmailId());
        if (allsharedpreference.getEmailId() != null && datum.getEmail() != null
                && allsharedpreference.getEmailId().equalsIgnoreCase(datum.getEmail())) {
            holder.imageViewReport.setVisibility(View.GONE);
            holder.person_name.setText("Me");
        } else {
            holder.imageViewReport.setVisibility(View.VISIBLE);
            holder.person_name.setText(datum.getUserName());
        }

        int indent = isReply
                ? (int) (32 * mContext.getResources().getDisplayMetrics().density)
                : 0;
        holder.layoutComment.setPadding(indent, holder.layoutComment.getPaddingTop(),
                holder.layoutComment.getPaddingRight(), holder.layoutComment.getPaddingBottom());
        holder.layoutImage.setPadding(indent, holder.layoutImage.getPaddingTop(),
                holder.layoutImage.getPaddingRight(), holder.layoutImage.getPaddingBottom());

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
            if (adapterPosition != RecyclerView.NO_POSITION) {
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
        TextView comment_reply_btn;
        TextView view_replies_link;
        CircleImageView circular_image;
        ImageView imageViewReport;
        LinearLayout layoutComment;
        LinearLayout layoutImage;

        public MailViewHolder(@NonNull View itemView) {
            super(itemView);

            comment_details = itemView.findViewById(R.id.comment_details);
            person_name = itemView.findViewById(R.id.person_name);
            comment_time = itemView.findViewById(R.id.comment_time);
            comment_reply_btn = itemView.findViewById(R.id.comment_reply_btn);
            view_replies_link = itemView.findViewById(R.id.view_replies_link);
            circular_image = itemView.findViewById(R.id.circular_image);
            imageViewReport = itemView.findViewById(R.id.imageViewReport);
            layoutComment = itemView.findViewById(R.id.layoutComment);
            layoutImage = itemView.findViewById(R.id.layoutImage);
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
}
