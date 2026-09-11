package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EncreptionRequest {

    public Boolean getShow_push_perm_alert() {
        return show_push_perm_alert;
    }

    public void setShow_push_perm_alert(Boolean show_push_perm_alert) {
        this.show_push_perm_alert = show_push_perm_alert;
    }

    @SerializedName("show_push_perm_alert")
    @Expose
    private Boolean show_push_perm_alert;
    @SerializedName("data")
    @Expose
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public CardsDisplay getCards_display() {
        return cards_display;
    }

    public void setCards_display(CardsDisplay cards_display) {
        this.cards_display = cards_display;
    }

    @SerializedName("cards_display")
    @Expose
    private CardsDisplay cards_display;

    @SerializedName("update_menu_status")
    @Expose
    private Integer updateMenuStatus;

    /**
     * Admin Home update-banner subtitle. Empty/null → hardcoded string fallback.
     */
    @SerializedName("update_banner_subtitle")
    @Expose
    private String update_banner_subtitle;

    @SerializedName("callAppEvents")
    @Expose
    private boolean callAppEvents;

    public boolean isShow_ads() {
        return show_ads;
    }

    public void setShow_ads(boolean show_ads) {
        this.show_ads = show_ads;
    }

    @SerializedName("show_ads")
    @Expose
    private boolean show_ads;

    public boolean isUpdate_user_language() {
        return update_user_language;
    }

    public void setUpdate_user_language(boolean update_user_language) {
        this.update_user_language = update_user_language;
    }

    @SerializedName("update_user_language")
    @Expose
    private boolean update_user_language;

    public boolean isShow_reporter_login() {
        return show_reporter_login;
    }

    public void setShow_reporter_login(boolean show_reporter_login) {
        this.show_reporter_login = show_reporter_login;
    }

    @SerializedName("show_reporter_login")
    @Expose
    private boolean show_reporter_login;

    public boolean isIs_reporter() {
        return is_reporter;
    }

    public void setIs_reporter(boolean is_reporter) {
        this.is_reporter = is_reporter;
    }

    @SerializedName("is_reporter")
    @Expose
    private boolean is_reporter;

    public Integer getReporter_language_id() {
        return reporter_language_id;
    }

    public void setReporter_language_id(Integer reporter_language_id) {
        this.reporter_language_id = reporter_language_id;
    }

    @SerializedName("reporter_language_id")
    @Expose
    private Integer reporter_language_id;

    public List<AdsPojo> getAd_positions() {
        return ad_positions;
    }

    public void setAd_positions(List<AdsPojo> ad_positions) {
        this.ad_positions = ad_positions;
    }

    @SerializedName("ads_data")
    @Expose
    private List<AdsPojo> ad_positions;

    @SerializedName("admob_app_id")
    @Expose
    private String admob_app_id;

    public String getAdmob_app_id() {
        return admob_app_id;
    }

    public void setAdmob_app_id(String admob_app_id) {
        this.admob_app_id = admob_app_id;
    }

    @SerializedName("engagement_badge_color")
    @Expose
    private String engagement_badge_color;

    @SerializedName("theme_color")
    @Expose
    private String theme_color;

    @SerializedName("poll_survey_colors")
    @Expose
    private PollSurveyColorsConfig poll_survey_colors;

    @SerializedName("allow_screen_capture")
    @Expose
    private Boolean allow_screen_capture;

    @SerializedName("video_autoplay_enabled")
    @Expose
    private Boolean video_autoplay_enabled;

    @SerializedName("enable_birthday_special")
    @Expose
    private Boolean enable_birthday_special;

    @SerializedName("weather_enabled")
    @Expose
    private Boolean weather_enabled;

    @SerializedName("fuel_enabled")
    @Expose
    private Boolean fuel_enabled;

    @SerializedName("gold_enabled")
    @Expose
    private Boolean gold_enabled;

    @SerializedName("ttd_enabled")
    @Expose
    private Boolean ttd_enabled;

    @SerializedName("jobs_enabled")
    @Expose
    private Boolean jobs_enabled;

    /** Writers-row chips from feed poll. Null when omitted — do not wipe splash. */
    @SerializedName("hide_district_on_main_feed")
    @Expose
    private Boolean hide_district_on_main_feed;

    @SerializedName("rewards_master")
    @Expose
    private Boolean rewards_master;

    @SerializedName("rewards_in_main_feed_slot")
    @Expose
    private Boolean rewards_in_main_feed_slot;

    @SerializedName("keep_rewards_after_join")
    @Expose
    private Boolean keep_rewards_after_join;

    @SerializedName("flash_news_in_main_feed_slot")
    @Expose
    private Boolean flash_news_in_main_feed_slot;

    @SerializedName("show_flash_and_rewards_together")
    @Expose
    private Boolean show_flash_and_rewards_together;

    @SerializedName("flash_news_chip_label")
    @Expose
    private String flash_news_chip_label;

    /** Daily Updates hub kill switches + language matrices (same shape as splash). */
    @SerializedName("daily_updates")
    @Expose
    private Object daily_updates;

    @SerializedName("action_icon_size_dp")
    @Expose
    private Integer action_icon_size_dp;

    @SerializedName("like_circle_size_dp")
    @Expose
    private Integer like_circle_size_dp;

    @SerializedName("magazine_share_icon_size_dp")
    @Expose
    private Integer magazine_share_icon_size_dp;

    @SerializedName("magazine_share_margin_end_dp")
    @Expose
    private Integer magazine_share_margin_end_dp;

    @SerializedName("magazine_share_margin_bottom_dp")
    @Expose
    private Integer magazine_share_margin_bottom_dp;

    @SerializedName("magazine_share_margin_start_dp")
    @Expose
    private Integer magazine_share_margin_start_dp;

    @SerializedName("magazine_share_margin_top_dp")
    @Expose
    private Integer magazine_share_margin_top_dp;

    @SerializedName("enable_video_post")
    @Expose
    private Boolean enable_video_post;

    @SerializedName("enable_article_post")
    @Expose
    private Boolean enable_article_post;

    @SerializedName("enable_full_post")
    @Expose
    private Boolean enable_full_post;

    @SerializedName("require_full_profile_to_post")
    @Expose
    private Boolean require_full_profile_to_post;

    @SerializedName("show_writer_photo_on_cards")
    @Expose
    private Boolean show_writer_photo_on_cards;

    @SerializedName("title_body_gap_dp")
    @Expose
    private Integer title_body_gap_dp;

    @SerializedName("content_block_offset_dp")
    @Expose
    private Integer content_block_offset_dp;

    @SerializedName("pn_show_article_image")
    @Expose
    private Boolean pn_show_article_image;

    @SerializedName("pn_style_mode")
    @Expose
    private String pn_style_mode;

    @SerializedName("pn_large_icon_url")
    @Expose
    private String pn_large_icon_url;

    @SerializedName("push_include_logo_default")
    @Expose
    private Boolean push_include_logo_default;

    @SerializedName("pn_left_icon_size_dp")
    @Expose
    private Integer pn_left_icon_size_dp;

    @SerializedName("pn_thumb_size_dp")
    @Expose
    private Integer pn_thumb_size_dp;

    @SerializedName("pn_title_size_sp")
    @Expose
    private Integer pn_title_size_sp;

    /** In-feed ad after every N real articles (Performance; default 4, range 2–10). */
    @SerializedName("feed_ad_every_n")
    @Expose
    private Integer feed_ad_every_n;

    @SerializedName("ad_prefetch_lookahead")
    @Expose
    private Integer ad_prefetch_lookahead;

    @SerializedName("max_native_ad_loads_in_flight")
    @Expose
    private Integer max_native_ad_loads_in_flight;

    @SerializedName("ad_impr_flush_every_n")
    @Expose
    private Integer ad_impr_flush_every_n;

    @SerializedName("feed_ad_format")
    @Expose
    private String feed_ad_format;

    @SerializedName("ad_code")
    @Expose
    private String ad_code;

    @SerializedName("views_batch_flush_interval_seconds")
    @Expose
    private Integer views_batch_flush_interval_seconds;

    @SerializedName("views_batch_flush_at_count")
    @Expose
    private Integer views_batch_flush_at_count;

    @SerializedName("media_access_flush_interval_seconds")
    @Expose
    private Integer media_access_flush_interval_seconds;

    @SerializedName("rewards_engagement_flush_interval_seconds")
    @Expose
    private Integer rewards_engagement_flush_interval_seconds;

    public Integer getFeed_ad_every_n() {
        return feed_ad_every_n;
    }

    public Integer getAd_prefetch_lookahead() {
        return ad_prefetch_lookahead;
    }

    public Integer getMax_native_ad_loads_in_flight() {
        return max_native_ad_loads_in_flight;
    }

    public Integer getAd_impr_flush_every_n() {
        return ad_impr_flush_every_n;
    }

    public String getFeed_ad_format() {
        return feed_ad_format;
    }

    public String getAd_code() {
        return ad_code;
    }

    public Integer getViews_batch_flush_interval_seconds() {
        return views_batch_flush_interval_seconds;
    }

    public Integer getViews_batch_flush_at_count() {
        return views_batch_flush_at_count;
    }

    public Integer getMedia_access_flush_interval_seconds() {
        return media_access_flush_interval_seconds;
    }

    public Integer getRewards_engagement_flush_interval_seconds() {
        return rewards_engagement_flush_interval_seconds;
    }

    public Boolean getAllow_screen_capture() {
        return allow_screen_capture;
    }

    /** True only when the server explicitly sent allow_screen_capture=true. */
    public boolean isAllow_screen_capture() {
        return Boolean.TRUE.equals(allow_screen_capture);
    }

    /** Null when omitted — do not overwrite splash/admin prefs with a false default. */
    public boolean hasAllow_screen_capture() {
        return allow_screen_capture != null;
    }

    public void setAllow_screen_capture(Boolean allow_screen_capture) {
        this.allow_screen_capture = allow_screen_capture;
    }

    public Boolean getVideo_autoplay_enabled() {
        return video_autoplay_enabled;
    }

    /** Null when omitted — do not overwrite splash/admin prefs with a false default. */
    public boolean hasVideo_autoplay_enabled() {
        return video_autoplay_enabled != null;
    }

    public void setVideo_autoplay_enabled(Boolean video_autoplay_enabled) {
        this.video_autoplay_enabled = video_autoplay_enabled;
    }

    public Boolean getHide_district_on_main_feed() {
        return hide_district_on_main_feed;
    }

    public Boolean getRewards_master() {
        return rewards_master;
    }

    public Boolean getRewards_in_main_feed_slot() {
        return rewards_in_main_feed_slot;
    }

    public Boolean getKeep_rewards_after_join() {
        return keep_rewards_after_join;
    }

    public Boolean getFlash_news_in_main_feed_slot() {
        return flash_news_in_main_feed_slot;
    }

    public Boolean getShow_flash_and_rewards_together() {
        return show_flash_and_rewards_together;
    }

    public String getFlash_news_chip_label() {
        return flash_news_chip_label;
    }

    public Boolean getEnable_birthday_special() {
        return enable_birthday_special;
    }

    public boolean hasEnable_birthday_special() {
        return enable_birthday_special != null;
    }

    public Boolean getWeather_enabled() {
        return weather_enabled;
    }

    public Boolean getFuel_enabled() {
        return fuel_enabled;
    }

    public Boolean getGold_enabled() {
        return gold_enabled;
    }

    public Boolean getTtd_enabled() {
        return ttd_enabled;
    }

    public Boolean getJobs_enabled() {
        return jobs_enabled;
    }

    public Object getDaily_updates() {
        return daily_updates;
    }

    public Integer getAction_icon_size_dp() {
        return action_icon_size_dp;
    }

    public void setAction_icon_size_dp(Integer action_icon_size_dp) {
        this.action_icon_size_dp = action_icon_size_dp;
    }

    public Integer getLike_circle_size_dp() {
        return like_circle_size_dp;
    }

    public void setLike_circle_size_dp(Integer like_circle_size_dp) {
        this.like_circle_size_dp = like_circle_size_dp;
    }

    public Integer getMagazine_share_icon_size_dp() {
        return magazine_share_icon_size_dp;
    }

    public void setMagazine_share_icon_size_dp(Integer magazine_share_icon_size_dp) {
        this.magazine_share_icon_size_dp = magazine_share_icon_size_dp;
    }

    public Integer getMagazine_share_margin_end_dp() {
        return magazine_share_margin_end_dp;
    }

    public void setMagazine_share_margin_end_dp(Integer magazine_share_margin_end_dp) {
        this.magazine_share_margin_end_dp = magazine_share_margin_end_dp;
    }

    public Integer getMagazine_share_margin_bottom_dp() {
        return magazine_share_margin_bottom_dp;
    }

    public void setMagazine_share_margin_bottom_dp(Integer magazine_share_margin_bottom_dp) {
        this.magazine_share_margin_bottom_dp = magazine_share_margin_bottom_dp;
    }

    public Integer getMagazine_share_margin_start_dp() {
        return magazine_share_margin_start_dp;
    }

    public void setMagazine_share_margin_start_dp(Integer magazine_share_margin_start_dp) {
        this.magazine_share_margin_start_dp = magazine_share_margin_start_dp;
    }

    public Integer getMagazine_share_margin_top_dp() {
        return magazine_share_margin_top_dp;
    }

    public void setMagazine_share_margin_top_dp(Integer magazine_share_margin_top_dp) {
        this.magazine_share_margin_top_dp = magazine_share_margin_top_dp;
    }

    public Boolean getEnable_video_post() {
        return enable_video_post;
    }

    public void setEnable_video_post(Boolean enable_video_post) {
        this.enable_video_post = enable_video_post;
    }

    public Boolean getEnable_article_post() {
        return enable_article_post;
    }

    public void setEnable_article_post(Boolean enable_article_post) {
        this.enable_article_post = enable_article_post;
    }

    public Boolean getEnable_full_post() {
        return enable_full_post;
    }

    public void setEnable_full_post(Boolean enable_full_post) {
        this.enable_full_post = enable_full_post;
    }

    public Boolean getRequire_full_profile_to_post() {
        return require_full_profile_to_post;
    }

    /** True only when the poll/splash envelope explicitly included the field. */
    public boolean hasRequire_full_profile_to_post() {
        return require_full_profile_to_post != null;
    }

    public void setRequire_full_profile_to_post(Boolean require_full_profile_to_post) {
        this.require_full_profile_to_post = require_full_profile_to_post;
    }

    public Boolean getShow_writer_photo_on_cards() {
        return show_writer_photo_on_cards;
    }

    public void setShow_writer_photo_on_cards(Boolean show_writer_photo_on_cards) {
        this.show_writer_photo_on_cards = show_writer_photo_on_cards;
    }

    public Integer getTitle_body_gap_dp() {
        return title_body_gap_dp;
    }

    public void setTitle_body_gap_dp(Integer title_body_gap_dp) {
        this.title_body_gap_dp = title_body_gap_dp;
    }

    public Integer getContent_block_offset_dp() {
        return content_block_offset_dp;
    }

    public void setContent_block_offset_dp(Integer content_block_offset_dp) {
        this.content_block_offset_dp = content_block_offset_dp;
    }

    public Boolean getPn_show_article_image() {
        return pn_show_article_image;
    }

    public void setPn_show_article_image(Boolean pn_show_article_image) {
        this.pn_show_article_image = pn_show_article_image;
    }

    public String getPn_style_mode() {
        return pn_style_mode;
    }

    public void setPn_style_mode(String pn_style_mode) {
        this.pn_style_mode = pn_style_mode;
    }

    public String getPn_large_icon_url() {
        return pn_large_icon_url;
    }

    public void setPn_large_icon_url(String pn_large_icon_url) {
        this.pn_large_icon_url = pn_large_icon_url;
    }

    public Boolean getPush_include_logo_default() {
        return push_include_logo_default;
    }

    public void setPush_include_logo_default(Boolean push_include_logo_default) {
        this.push_include_logo_default = push_include_logo_default;
    }

    public Integer getPn_left_icon_size_dp() {
        return pn_left_icon_size_dp;
    }

    public void setPn_left_icon_size_dp(Integer pn_left_icon_size_dp) {
        this.pn_left_icon_size_dp = pn_left_icon_size_dp;
    }

    public Integer getPn_thumb_size_dp() {
        return pn_thumb_size_dp;
    }

    public void setPn_thumb_size_dp(Integer pn_thumb_size_dp) {
        this.pn_thumb_size_dp = pn_thumb_size_dp;
    }

    public Integer getPn_title_size_sp() {
        return pn_title_size_sp;
    }

    public void setPn_title_size_sp(Integer pn_title_size_sp) {
        this.pn_title_size_sp = pn_title_size_sp;
    }

    public String getEngagement_badge_color() {
        return engagement_badge_color;
    }

    public void setEngagement_badge_color(String engagement_badge_color) {
        this.engagement_badge_color = engagement_badge_color;
    }

    public String getTheme_color() {
        return theme_color;
    }

    public void setTheme_color(String theme_color) {
        this.theme_color = theme_color;
    }

    public PollSurveyColorsConfig getPoll_survey_colors() {
        return poll_survey_colors;
    }

    public void setPoll_survey_colors(PollSurveyColorsConfig poll_survey_colors) {
        this.poll_survey_colors = poll_survey_colors;
    }

    public Integer getUpdateMenuStatus() {
        return updateMenuStatus;
    }

    public void setUpdateMenuStatus(Integer updateMenuStatus) {
        this.updateMenuStatus = updateMenuStatus;
    }

    public String getUpdate_banner_subtitle() {
        return update_banner_subtitle;
    }

    public void setUpdate_banner_subtitle(String update_banner_subtitle) {
        this.update_banner_subtitle = update_banner_subtitle;
    }

    public boolean isCallAppEvents() {
        return callAppEvents;
    }

    public void setCallAppEvents(boolean callAppEvents) {
        this.callAppEvents = callAppEvents;
    }

    // Cricket Live Mini Player: the single active match (or null). Read on every
    // feed load so admin toggles reflect quickly in the app.
    @SerializedName("active_match")
    @Expose
    private ActiveMatch active_match;

    public ActiveMatch getActive_match() {
        return active_match;
    }

    public void setActive_match(ActiveMatch active_match) {
        this.active_match = active_match;
    }
}
