package com.anyNews.anynews.Utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;


public class ModelClass implements Serializable {


    public static class Datum implements Serializable {

        @SerializedName("is_source")
        @Expose
        private Boolean is_source;

        public Boolean getDownloadable() {
            return downloadable;
        }

        public void setDownloadable(Boolean downloadable) {
            this.downloadable = downloadable;
        }

        @SerializedName("downloadable")
        @Expose
        private Boolean downloadable;


        @SerializedName("source_link")
        @Expose
        private String source_link;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("category_id")
        @Expose
        private Integer categoryId;
        /** Post geo — used with location_label for the card pin (not user district). */
        @SerializedName("state_id")
        @Expose
        private Integer state_id;
        @SerializedName("dist_id")
        @Expose
        private Integer dist_id;
        /** Server-resolved district/state name for the location pin. */
        @SerializedName("location_label")
        @Expose
        private String location_label;
        @SerializedName("post_type")
        @Expose
        private String postType;
        @SerializedName("media_type")
        @Expose
        private String mediaType;
        @SerializedName("images")
        @Expose
        private List<String> images = null;
        @SerializedName("video_url")
        @Expose
        private String videoUrl;
        @SerializedName("thumbnail")
        @Expose
        private String thumbnail;
        @SerializedName("videos")
        @Expose
        private List<Object> videos = null;
        @SerializedName("thumbnails")
        @Expose
        private List<Object> thumbnails = null;
        @SerializedName("published_time")
        @Expose
        private Long publishedTime;
        @SerializedName("shares")
        @Expose
        private Integer shares;
        @SerializedName("likes")
        @Expose
        private Integer likes;
        @SerializedName("comments")
        @Expose
        private Integer comments;
        @SerializedName("downloads")
        @Expose
        private Integer downloads;
        @SerializedName("sticky_position")
        @Expose
        private Integer stickyPosition;
        @SerializedName("comments_flag")
        @Expose
        private Boolean commentsFlag;
        @SerializedName("writer_type")
        @Expose
        private String writerType;
        @SerializedName("share_link")
        @Expose
        private String shareLink;
        @SerializedName("background_image")
        @Expose
        private String backgroundImage;
        @SerializedName("post_id")
        @Expose
        private Long postId;
        @SerializedName("writer_id")
        @Expose
        private Long writerId;
        @SerializedName("like_status")
        @Expose
        private Integer likeStatus;
        @SerializedName("bookmark_status")
        @Expose
        private Integer bookmarkStatus;
        @SerializedName("writer_name")
        @Expose
        private String writerName;
        @SerializedName("writer_profile")
        @Expose
        private String writerProfile;

        @SerializedName("sub_post_id")
        @Expose
        private Long subPostId;

        @SerializedName("yes_percent")
        @Expose
        private int yes_percent;

        @SerializedName("no_percent")
        @Expose
        private int no_percent;
        @SerializedName("selected_option")
        @Expose
        private int selected_option;


        @SerializedName("is_follow")
        @Expose
        private Integer isFollow;

        @SerializedName("strip_content")
        @Expose
        private String strip_content;


        @SerializedName("survey_status")
        @Expose
        private Integer surveyStatus;

        @SerializedName("options")
        @Expose
        private List<SurveyOptions> surveyOptions = null;

        @SerializedName("yes_option")
        @Expose
        private String yes_option;

        @SerializedName("no_option")
        @Expose
        private String no_option;

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        @SerializedName("downloadUrl")
        @Expose
        private String downloadUrl;

        public String getPost_date_time() {
            return post_date_time;
        }

        public void setPost_date_time(String post_date_time) {
            this.post_date_time = post_date_time;
        }

        @SerializedName("post_date_time")
        @Expose
        private String post_date_time;

        public String getImage_type() {
            return image_type;
        }

        public void setImage_type(String image_type) {
            this.image_type = image_type;
        }

        @SerializedName("image_type")
        @Expose
        private String image_type;
        @SerializedName("load_ads")
        @Expose
        private boolean load_ads;

        public String getAd_screen_type() {
            return ad_screen_type;
        }

        public void setAd_screen_type(String ad_screen_type) {
            this.ad_screen_type = ad_screen_type;
        }

        public String getAd_view_type() {
            return ad_view_type;
        }

        public void setAd_view_type(String ad_view_type) {
            this.ad_view_type = ad_view_type;
        }

        @SerializedName("ad_screen_type")
        @Expose
        private String ad_screen_type;
        @SerializedName("ad_view_type")
        @Expose
        private String ad_view_type;

        public boolean isAd_check() {
            return ad_check;
        }

        public void setAd_check(boolean ad_check) {
            this.ad_check = ad_check;
        }

        @SerializedName("ad_check")
        @Expose
        private boolean ad_check;

        public boolean isIs_gallery() {
            return is_gallery;
        }

        public void setIs_gallery(boolean is_gallery) {
            this.is_gallery = is_gallery;
        }

        @SerializedName("is_gallery")
        @Expose
        private boolean is_gallery;
        @SerializedName("ad_type")
        @Expose
        private String ad_type;
        @SerializedName("ads_from")
        @Expose
        private String ads_from;

        public String getAds_from() {
            return ads_from;
        }

        public void setAds_from(String ads_from) {
            this.ads_from = ads_from;
        }

        public String getWeb_url() {
            return web_url;
        }

        public void setWeb_url(String web_url) {
            this.web_url = web_url;
        }

        @SerializedName("web_url")
        @Expose
        private String web_url;

        public int getAd_req_position() {
            return ad_req_position;
        }

        public void setAd_req_position(int ad_req_position) {
            this.ad_req_position = ad_req_position;
        }

        @SerializedName("ad_req_position")
        @Expose
        private int ad_req_position;

        public int getNext_ad_req_position() {
            return next_ad_req_position;
        }

        public void setNext_ad_req_position(int next_ad_req_position) {
            this.next_ad_req_position = next_ad_req_position;
        }

        @SerializedName("next_ad_req_position")
        @Expose
        private int next_ad_req_position;

        public boolean isLoad_ads() {
            return load_ads;
        }

        public Boolean getAd_audio_mute() {
            return ad_audio_mute;
        }

        public void setAd_audio_mute(Boolean ad_audio_mute) {
            this.ad_audio_mute = ad_audio_mute;
        }

        @SerializedName("ad_audio_mute")
        @Expose
        private Boolean ad_audio_mute;


        public void setLoad_ads(boolean load_ads) {
            this.load_ads = load_ads;
        }

        public String getAd_type() {
            return ad_type;
        }

        public void setAd_type(String ad_type) {
            this.ad_type = ad_type;
        }

        public String getAd_code() {
            return ad_code;
        }

        public void setAd_code(String ad_code) {
            this.ad_code = ad_code;
        }

        @SerializedName("ad_code")
        @Expose
        private String ad_code;

        public String getNext_req_ad_code() {
            return next_req_ad_code;
        }

        public void setNext_req_ad_code(String next_req_ad_code) {
            this.next_req_ad_code = next_req_ad_code;
        }

        @SerializedName("next_req_ad_code")
        @Expose
        private String next_req_ad_code;

        public String getVasttag_ur() {
            return vasttag_ur;
        }

        public void setVasttag_ur(String vasttag_ur) {
            this.vasttag_ur = vasttag_ur;
        }

        @SerializedName("vasttag_url")
        @Expose
        private String vasttag_ur;


        private boolean isRatedUp = false;


        public Boolean getIs_source() {
            return is_source;
        }

        public void setIs_source(Boolean is_source) {
            this.is_source = is_source;
        }

        public String getSource_link() {
            return source_link;
        }

        public void setSource_link(String source_link) {
            this.source_link = source_link;
        }

        public Integer getIsFollow() {
            return isFollow;
        }

        public void setIsFollow(Integer isFollow) {
            this.isFollow = isFollow;
        }

        public Long getSubPostId() {
            return subPostId;
        }

        public void setSubPostId(Long subPostId) {
            this.subPostId = subPostId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
        }

        public Integer getState_id() {
            return state_id;
        }

        public void setState_id(Integer state_id) {
            this.state_id = state_id;
        }

        public Integer getDist_id() {
            return dist_id;
        }

        public void setDist_id(Integer dist_id) {
            this.dist_id = dist_id;
        }

        public String getLocation_label() {
            return location_label;
        }

        public void setLocation_label(String location_label) {
            this.location_label = location_label;
        }

        public String getPostType() {
            return postType;
        }

        public void setPostType(String postType) {
            this.postType = postType;
        }

        public String getMediaType() {
            return mediaType;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public List<String> getImages() {
            return images;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public List<Object> getVideos() {
            return videos;
        }

        public void setVideos(List<Object> videos) {
            this.videos = videos;
        }

        public List<Object> getThumbnails() {
            return thumbnails;
        }

        public void setThumbnails(List<Object> thumbnails) {
            this.thumbnails = thumbnails;
        }

        public Long getPublishedTime() {
            return publishedTime;
        }

        public void setPublishedTime(Long publishedTime) {
            this.publishedTime = publishedTime;
        }

        public Integer getShares() {
            return shares;
        }

        public void setShares(Integer shares) {
            this.shares = shares;
        }

        public Integer getLikes() {
            return likes;
        }

        public void setLikes(Integer likes) {
            this.likes = likes;
        }

        public Integer getComments() {
            return comments;
        }

        public void setComments(Integer comments) {
            this.comments = comments;
        }

        public Integer getDownloads() {
            return downloads;
        }

        public void setDownloads(Integer downloads) {
            this.downloads = downloads;
        }

        public Integer getStickyPosition() {
            return stickyPosition;
        }

        public void setStickyPosition(Integer stickyPosition) {
            this.stickyPosition = stickyPosition;
        }

        public Boolean getCommentsFlag() {
            return commentsFlag;
        }

        public void setCommentsFlag(Boolean commentsFlag) {
            this.commentsFlag = commentsFlag;
        }

        public String getWriterType() {
            return writerType;
        }

        public void setWriterType(String writerType) {
            this.writerType = writerType;
        }

        public String getStrip_content() {
            return strip_content;
        }

        public void setStrip_content(String strip_content) {
            this.strip_content = strip_content;
        }

        public String getShareLink() {
            return shareLink;
        }

        public void setShareLink(String shareLink) {
            this.shareLink = shareLink;
        }

        public String getBackgroundImage() {
            return backgroundImage;
        }

        public void setBackgroundImage(String backgroundImage) {
            this.backgroundImage = backgroundImage;
        }

        public Long getPostId() {
            return postId;
        }

        public void setPostId(Long postId) {
            this.postId = postId;
        }

        public Long getWriterId() {
            return writerId;
        }

        public void setWriterId(Long writerId) {
            this.writerId = writerId;
        }

        public Integer getLikeStatus() {
            return likeStatus;
        }

        public void setLikeStatus(Integer likeStatus) {
            this.likeStatus = likeStatus;
        }

        public Integer getBookmarkStatus() {
            if (bookmarkStatus == null) return 0;
            return bookmarkStatus;
        }

        public void setBookmarkStatus(Integer bookmarkStatus) {
            this.bookmarkStatus = bookmarkStatus;
        }

        public String getWriterName() {
            return writerName;
        }

        public void setWriterName(String writerName) {
            this.writerName = writerName;
        }

        public String getWriterProfile() {
            return writerProfile;
        }

        public void setWriterProfile(String writerProfile) {
            this.writerProfile = writerProfile;
        }

        public boolean isRatedUp() {
            return this.isRatedUp;
        }

        public void setRatedUp(boolean israted) {
            this.isRatedUp = israted;
        }

        @SerializedName("role")
        @Expose
        private String role;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Integer getSurveyStatus() {
            return surveyStatus;
        }

        public void setSurveyStatus(Integer surveyStatus) {
            this.surveyStatus = surveyStatus;
        }

        public List<SurveyOptions> getSurveyOptions() {
            return surveyOptions;
        }

        public void setSurveyOptions(List<SurveyOptions> surveyOptions) {
            this.surveyOptions = surveyOptions;
        }

        public int getYes_percent() {
            return yes_percent;
        }

        public void setYes_percent(int yes_percent) {
            this.yes_percent = yes_percent;
        }

        public int getNo_percent() {
            return no_percent;
        }

        public void setNo_percent(int no_percent) {
            this.no_percent = no_percent;
        }

        public int getSelected_option() {
            return selected_option;
        }

        public void setSelected_option(int selected_option) {
            this.selected_option = selected_option;
        }

        public String getYes_option() {
            return yes_option;
        }

        public void setYes_option(String yes_option) {
            this.yes_option = yes_option;
        }

        public String getNo_option() {
            return no_option;
        }

        public void setNo_option(String no_option) {
            this.no_option = no_option;
        }
    }


    public static class SurveyOptions implements Serializable {
        @SerializedName("id")
        @Expose
        private int id;
        @SerializedName("name")
        @Expose
        private String surveyOptionsName;

        @SerializedName("percent")
        @Expose
        private int percent;

        @SerializedName("count")
        @Expose
        private int count;

        public String getSurveyOptionsName() {
            return surveyOptionsName;
        }

        public void setSurveyOptionsName(String surveyOptionsName) {
            this.surveyOptionsName = surveyOptionsName;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }

        public int getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

    @SerializedName("success")
    @Expose
    private Boolean success;




    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("last_post_id")
    @Expose
    private Long lastPostId;
    @SerializedName("skip")
    @Expose
    private Integer skip;


    @SerializedName("actual_skip")
    @Expose
    private Long actualSkip;
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }


    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public Long getLastPostId() {
        return lastPostId;
    }

    public void setLastPostId(Long lastPostId) {
        this.lastPostId = lastPostId;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Long getActualSkip() {
        return actualSkip;
    }

    public void setActualSkip(Long skip) {
        this.actualSkip = skip;
    }


}