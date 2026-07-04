package com.anyNews.anynews.testing;


import com.anyNews.anynews.models.PaymentHistoryModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.List;

public class PostItem {

  public static class Datum {

    @SerializedName("points")
    @Expose
    private Long points;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("reason")
    @Expose
    private String reason;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("post_status")
    @Expose
    private Long postStatus;
    @SerializedName("createdAt")
    @Expose
    private String createdAt;

    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("transaction_status")
    @Expose
    private Integer transactionStatus;


    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("post_type")
    @Expose
    private String postType;
    @SerializedName("media_type")
    @Expose
    private String mediaType;
    @SerializedName("user_post_id")
    @Expose
    private Integer userPostId;

    public String getTitle() {
      return title;
    }

    public String getImageUrl() {
      return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
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

    public Integer getUserPostId() {
      return userPostId;
    }

    public void setUserPostId(Integer userPostId) {
      this.userPostId = userPostId;
    }


    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public Integer getTransactionStatus() {
      return transactionStatus;
    }

    public void setTransactionStatus(Integer transactionStatus) {
      this.transactionStatus = transactionStatus;
    }

    public Long getPoints() {
      return points;
    }

    public void setPoints(Long points) {
      this.points = points;
    }


    public void setTitle(String title) {
      this.title = title;
    }

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }

    public String getImage() {
      return image;
    }

    public void setImage(String image) {
      this.image = image;
    }

    public Long getPostStatus() {
      return postStatus;
    }

    public void setPostStatus(Long postStatus) {
      this.postStatus = postStatus;
    }

    public String getCreatedAt() {
      return createdAt;
    }

    public void setCreatedAt(String createdAt) {
      this.createdAt = createdAt;
    }

  }


  @SerializedName("success")
  @Expose
  private Boolean success;
  @SerializedName("data")
  @Expose
  private List<Datum> data = null;
  @SerializedName("skip")
  @Expose
  private Long skip;
  @SerializedName("first_transaction_id")
  @Expose
  private Long firstTransactionId;
  @SerializedName("total_points")
  @Expose
  private Long totalPoints;


  @SerializedName("first_post_id")
  @Expose
  private Long firstPostId;

  public Long getFirstPostId() {
    return firstPostId;
  }

  public void setFirstPostId(Long firstPostId) {
    this.firstPostId = firstPostId;
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

  public Long getSkip() {
    return skip;
  }

  public Boolean getSuccess() {
    return success;
  }


  public void setSkip(Long skip) {
    this.skip = skip;
  }

  public Long getFirstTransactionId() {
    return firstTransactionId;
  }

  public void setFirstTransactionId(Long firstTransactionId) {
    this.firstTransactionId = firstTransactionId;
  }

  public Long getTotalPoints() {
    return totalPoints;
  }

  public void setTotalPoints(Long totalPoints) {
    this.totalPoints = totalPoints;
  }

  private String description;

  private String time;

  private String title;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
