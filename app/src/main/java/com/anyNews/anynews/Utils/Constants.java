package com.anyNews.anynews.Utils;

public class Constants {

    public static String SURVEY_STATUS = "SURVEY_STATUS" ;
    public static String IS_LANGUAGE_SELECTED = "isLanguageSelected";
    public  static  String Text_Size="Text_Size";

    public static String PREF_NAME = "my_pref";

    public static String ApiKEY = "ApiKey";

    public static String DraftID = "DraftID";

    public static String Test_Size = "Test_Size";


    public static String Version_ID = "Version_ID";

    public static String POST_ID = "Post_ID";

    public static String DeviceID = "DeviceID";

    /**
     * Stable per-install UUID when ANDROID_ID is missing/empty.
     * Survives process death; cleared on uninstall / clear-data (expected new draft).
     */
    public static String INSTALL_UUID = "INSTALL_UUID";

    public static String FCM_TOKEN = "FCM_TOKEN";
    public static String USER_SESSION_ID = "user_session_id";
    public static String HOME_SCREEN = "HOME_SCREEN";
    public static final String Channel_ID=  "AnyNewsApp";

    public static  String IS_USER_ALREADY_LOGIN = "IS_USER_ALREADY_LOGIN";

    public static  String IS_ALREADY_LOGIN = "IS_ALREADY_LOGIN";

    public static  String NOT_FIRST_TIME_LOGIN = "FIRST_TIME_LOGIN";

    public static String CategoryID = "CategoryID";
    /** Display name for the last Profile/Search category feed (matches {@link #CategoryID}). */
    public static String CategoryName = "CategoryName";

    public static String Search_icon = "SearchIcon";

    public static String PhoneNumber = "PhoneNumber";

    public static String UserID ="USERID";

    public static String SessionID ="SessionID";

    public static String Buyer_Name ="Buyer_Name";

    public static String CategoryIDDefault ="CategoryIDDefault";

    public static String Add_Remove = "Add_Remove";

    public static String ArticleNewsID = "Article_NewsID" ;

    public static String Title ="Title";

    public static  String Description="Description";

    public static  String Long="Longitude";

    public static  String AddressId="AddressID";

    public static String Response = "Response";

    public static String Delivery_type = "Delivery_Type";

    public  static  String Address_Default = "Address_Default";

    public static String OrderId = "OrderID";

    public static String switchLocation = "switchLocation";

    public static String switchNotification = "switchNotification";

    /**
     * Legacy in-app Settings pref. Not consulted for playback: it defaulted off
     * and the Settings column no longer writes it, which blocked admin ON.
     */
    public static String VIDEO_AUTOPLAY = "settings_video_autoplay";
    /**
     * Legacy admin global ({@code video_autoplay_enabled}). Splash + feed poll.
     * Old APKs read only this. New APK uses it when the split flags are absent.
     */
    public static String VIDEO_AUTOPLAY_ENABLED = "video_autoplay_enabled";
    /** Admin full-video card autoplay ({@code video_autoplay_full_enabled}). */
    public static String VIDEO_AUTOPLAY_FULL_ENABLED = "video_autoplay_full_enabled";
    /** Admin half-article / half-video card autoplay ({@code video_autoplay_half_enabled}). */
    public static String VIDEO_AUTOPLAY_HALF_ENABLED = "video_autoplay_half_enabled";
    /**
     * Article card TTS / read-aloud master ({@code tts_enabled}). Splash.
     * Default OFF — hide speaker until admin enables.
     */
    public static String TTS_ENABLED = "tts_enabled";
    /** Top-right on article image ({@code tts_speaker_on_image}). Default OFF. */
    public static String TTS_SPEAKER_ON_IMAGE = "tts_speaker_on_image";
    /** Engagement row ({@code tts_speaker_on_engagement}). Default OFF. */
    public static String TTS_SPEAKER_ON_ENGAGEMENT = "tts_speaker_on_engagement";
    /** BCP-47 locale from admin ({@code tts_locale}), e.g. te-IN. */
    public static String TTS_LOCALE = "tts_locale";
    /** Preferred voice name/id substring ({@code tts_voice}). */
    public static String TTS_VOICE = "tts_voice";
    /** system | google ({@code tts_engine}). */
    public static String TTS_ENGINE = "tts_engine";
    /**
     * Admin feed Exo chrome A/B ({@code feed_video_player}): {@code current} | {@code playstore}.
     * Splash + session + feed poll. Null omit must not wipe cache. Default {@code current}.
     */
    public static String FEED_VIDEO_PLAYER = "feed_video_player";
    public static String NIGHT_MODE = "settings_night_mode";
    public static String LOCAL_STORIES = "settings_local_stories";
    public static String PARENTAL_CONTROL = "settings_parental_control";
    public static String NOTIFICATION_ENABLED = "settings_notification_enabled";

    /**
     * Last app versionCode that showed the install/update POST_NOTIFICATIONS dialog.
     * Missing key → never asked this install; ask on first Home after install/update.
     */
    public static String NOTIFICATION_RUNTIME_ASK_VERSION = "notification_runtime_ask_version_code";

    /** True after any POST_NOTIFICATIONS runtime dialog was shown (install or re-prompt). */
    public static String NOTIFICATION_RUNTIME_EVER_ASKED = "notification_runtime_ever_asked";

    /** Admin wave: show OS permission once on next open (cleared after show/ack). */
    public static String NOTIFICATION_REPROMPT_DUE = "notification_reprompt_due";
    public static String NOTIFICATION_REPROMPT_ENABLED = "notification_reprompt_enabled";
    public static String NOTIFICATION_REPROMPT_WAVE = "notification_reprompt_wave";
    /**
     * Last campaign wave this device already showed/acked locally.
     * Blocks re-ask even if a stuck server due=true comes back for the same wave.
     */
    public static String NOTIFICATION_REPROMPT_SHOWN_WAVE =
            "notification_reprompt_shown_wave";
    /** Admin: ask OS notification permission on every app open while ON (default false). */
    public static String PROMPT_NOTIFICATIONS_ON_OPEN = "prompt_notifications_on_open";

    public static String WalletAmount = "Wallet_Amount" ;

    public static String Email = "Email";
    public static String USER_NAME = "USER_NAME";
    /** Last known own profile photo URL for instant Profile paint. */
    public static String PROFILE_PIC_URL = "PROFILE_PIC_URL";

    //
    public static String Lang = "LanName";

    //


    public static String LanguageID = "LanguageID";
    /**
     * Language id of the last successful main page-0 feed paint. Used so Home can
     * detect a language switch that left a previous-language OpenPath/offline pack.
     */
    public static String LAST_FEED_LANGUAGE_ID = "LastFeedLanguageID";
    public static String RegionID = "RegionID";


    public static String Show_Animation="Show_Animation";


    /* from now */
    public static String State_Name="State_Name";

    public static String Distric_Name="Distric_Name";

    public static String StateID="StateID";

    public static String StaticDataApi="staticDataApi";

    public static String is_login="is_login";
    /**
     * After user taps Logout, splash must not re-apply server is_login (mobile still on draft)
     * until the next successful OTP login clears this flag.
     */
    public static String FORCE_LOGGED_OUT = "force_logged_out";
    public static String is_reporter="is_reporter";
    /**
     * Soft profile-photo reminder for approved reporters: last dismiss timestamp (ms).
     * Re-show after {@link #PROFILE_PHOTO_REMINDER_COOLDOWN_MS} if still missing a photo.
     */
    public static String PROFILE_PHOTO_REMINDER_DISMISSED_AT = "profile_photo_reminder_dismissed_at";
    /** ~3 days between soft reminders when photo still missing. */
    public static final long PROFILE_PHOTO_REMINDER_COOLDOWN_MS = 3L * 24 * 60 * 60 * 1000;
    /**
     * Cached Profile "Reporter" desk card UI for logged-in non-approved applicants.
     * 0 = hidden, 1 = awaiting approval, 2 = rejected. Avoids GONE→VISIBLE flicker
     * while {@code reporter_applicant_get} soft-refreshes in the background.
     */
    public static String REPORTER_DESK_UI_STATE = "reporter_desk_ui_state";
    public static final int REPORTER_DESK_HIDDEN = 0;
    public static final int REPORTER_DESK_PENDING = 1;
    public static final int REPORTER_DESK_REJECTED = 2;
    /** Last known "% Achieved" (profile completion) for instant Profile paint. */
    public static String PROFILE_ACHIEVED_PCT = "profile_achieved_pct";
    public static String reporter_language_id="reporter_language_id";
    public static String REPORTER_STATUS_CHANGED = "REPORTER_STATUS_CHANGED";
    public static String dist_name="dist_name";


    public static String Selected_Distric_Name="Selected_Distric_Name";
    public static String Selected_Distric_Name1="Selected_Distric_Name1";
    /**
     * Server district id for the selected geography. Numeric ids collide across languages —
     * always pair with {@link #LanguageID} / {@link #Selected_Distric_Name_LangId}.
     * Per-language snapshots use {@code Selected_Distric_Id_lang_{languageId}}.
     */
    public static String Selected_Distric_Id = "Selected_Distric_Id";
    /**
     * Language id for which {@link #Selected_Distric_Name1} / {@link #Selected_Distric_Name}
     * was last stored. Used so resume/offline paint never re-apply a Telugu label when UI is Hindi.
     */
    public static String Selected_Distric_Name_LangId = "Selected_Distric_Name_LangId";
    public static String MOVE_NEXT_SCREEN="MOVE_NEXT_SCREEN";
    public static String REFERRER_POST="referrer_post";

    public static String Selected_Phone_Number="Selected_Phone_Number";

    public static String CUSST_ID="CUSST_ID";
    public static String AVAILABLEPOINTS="AVAILABLEPOINTS";
    public static String REDEEMPOINTS="REDEEMPOINTS";
    public static String REFERAL_LINK="REFERAL_LINK";
    public static String REFERAL_LINK2="REFERAL_LINK2";
    public static String PAN_CARD_STATUS="PAN_CARD_STATUS";
    public static String ARTICLE_ID="ARTICLE_ID";
    public static String  PRD_ID="PRD_ID";

    /**
     * Logged-in user id (Long). Must never share a prefs key with string fields
     * like {@link #Selected_Distric_Name} — that caused ClassCastException on Home launch.
     * Legacy builds used key "Selected_Distric_Name"; {@link SharedPrefsData#getLong}
     * / {@link SharedPrefsData#putLong} migrate and clear the collision.
     */
    public static String USER_ID = "user_id";

    public static String Back_Button_Move="Back_Button_Move";

    public static String POST_UPDATED_ID = "post_id";

    public static String Event_Status = "event_status";
    public static String ENGAGEMENT_BADGE_COLOR = "engagement_badge_color";
    public static String THEME_COLOR = "theme_color";
    public static String ALLOW_SCREEN_CAPTURE = "allow_screen_capture";
    public static String LANGUAGE_FONT_SETTINGS = "language_font_settings";
    /** JSON map of custom font key → download URL (admin-uploaded TTF/OTF). */
    public static String CUSTOM_FONT_URLS = "custom_font_urls";
    public static String ACTION_ICON_ROW_OFFSET = "action_icon_row_offset";
    public static String ACTION_ICON_SIZE_DP = "action_icon_size_dp";
    /** Horizontal gap (dp) between article action icons. Missing pref uses 2. */
    public static String ACTION_ICON_GAP_DP = "action_icon_gap_dp";
    /**
     * Vertical gap (dp) between right-side actions on full image / full video.
     * Missing pref uses 6 (tighter than the old 20dp full-image spread).
     */
    public static String FULL_SIDE_ACTION_GAP_DP = "full_side_action_gap_dp";
    public static String LIKE_CIRCLE_SIZE_DP = "like_circle_size_dp";
    /** In-app Search: show between share and ⋮ on half engagement row. */
    public static String SEARCH_ICON_ON_ENGAGEMENT = "search_icon_on_engagement";
    /** In-app Search: show beside News|Read toggle chrome. */
    public static String SEARCH_ICON_ON_TOGGLE = "search_icon_on_toggle";
    /** Search glyph size (dp) when on the News|Read toggle bar. */
    public static String SEARCH_ICON_SIZE_DP = "search_icon_size_dp";
    public static String ENABLE_VIDEO_POST = "enable_video_post";
    public static String ENABLE_ARTICLE_POST = "enable_article_post";
    public static String ENABLE_FULL_POST = "enable_full_post";
    /** Admin: require 100% profile (name/email/mobile/photo) before compose / Create_User_Post. */
    public static String REQUIRE_FULL_PROFILE_TO_POST = "require_full_profile_to_post";
    /**
     * LanguageID that {@link Allsharedpreference#Translator} was last saved for.
     * Used to detect Hindi pack leftovers after switching to Telugu (etc.).
     */
    public static String TRANSLATOR_LANGUAGE_ID = "translator_language_id";
    /** Admin: show writer avatar on feed cards (default true). */
    public static String SHOW_WRITER_PHOTO_ON_CARDS = "show_writer_photo_on_cards";
    public static String TITLE_BODY_GAP_DP = "title_body_gap_dp";
    public static String CONTENT_BLOCK_OFFSET_DP = "content_block_offset_dp";
    public static String FOOTER_STYLE = "footer_style";
    public static String WRITER_STRIP_STYLE = "writer_strip_style";
    /** JSON of full-video share primary card config from splash. */
    public static String FULL_VIDEO_SHARE_CARD = "full_video_share_card";
    /** JSON of {@link com.anyNews.anynews.Pojo.PollSurveyColorsConfig} from splash. */
    public static String POLL_SURVEY_COLORS = "poll_survey_colors";
    /** Flip feed position across DayNight / battery-saver recreates (light↔dark). */
    public static String FLIP_RESTORE_INDEX = "flip_restore_index";
    public static String FLIP_RESTORE_POST_ID = "flip_restore_post_id";
    public static String FLIP_RESTORE_PENDING = "flip_restore_pending";
    /** epoch ms when flip checkpoint was written (theme recreate TTL). */
    public static String FLIP_RESTORE_AT = "flip_restore_at";
    /** Language id that owned {@link #FLIP_RESTORE_INDEX}. Mismatch → stay on article 0. */
    public static String FLIP_RESTORE_LANGUAGE_ID = "flip_restore_language_id";
    /**
     * Daily Updates hub was the Home destination across a DayNight recreate.
     * Cleared on splash cold start. Does not change language or night-mode prefs.
     */
    public static String DAILY_UPDATES_RESTORE = "daily_updates_restore";
    /** epoch ms when {@link #DAILY_UPDATES_RESTORE} was written (theme recreate TTL). */
    public static String DAILY_UPDATES_RESTORE_AT = "daily_updates_restore_at";
    /**
     * Set when the user picks a different language so the next Home open
     * starts at that language's latest article instead of the previous seat.
     */
    public static String LANGUAGE_SWITCH_RESET_FLIP = "language_switch_reset_flip";
    /** JSON of {@link com.anyNews.anynews.Pojo.CricketMiniPlayerPosition} from splash / poll. */
    public static String CRICKET_MINI_PLAYER_POSITION = "cricket_mini_player_position";
    /** "true"/"false" admin toggle for the floating cricket mini player (default false). */
    public static String CRICKET_MINI_PLAYER_ENABLED = "cricket_mini_player_enabled";
    public static String CRICKET_MINI_PLAYER_POLL_INTERVAL_SECONDS = "cricket_mini_player_poll_interval_seconds";
    /** Idle Home cricket poll when mini ON but no live match (admin; splash/session). */
    public static String CRICKET_MINI_PLAYER_IDLE_POLL_INTERVAL_SECONDS =
            "cricket_mini_player_idle_poll_interval_seconds";
    public static String SESSION_HEARTBEAT_INTERVAL_SECONDS = "session_heartbeat_interval_seconds";
    /** "true"/"false" admin true-live presence monitoring (default false). */
    public static String SESSION_LIVE_ENABLED = "session_live_enabled";
    /** Presence window seconds (interval × 2) from splash — informational for client. */
    public static String SESSION_LIVE_WINDOW_SECONDS = "session_live_window_seconds";
    /** Opening Banner (admin). Default OFF — OpenPath unchanged until true. */
    public static String FESTIVAL_SPLASH_ENABLED = "festival_splash_enabled";
    /** Show banner when opening from push. Default OFF (fast push). */
    public static String FESTIVAL_SPLASH_SHOW_ON_PUSH = "festival_splash_show_on_push";
    /** Show banner when opening from share / deep link. Default OFF. */
    public static String FESTIVAL_SPLASH_SHOW_ON_SHARE = "festival_splash_show_on_share";
    public static String FESTIVAL_SPLASH_DURATION_SECONDS = "festival_splash_duration_seconds";
    public static String FESTIVAL_SPLASH_IMAGE_URL = "festival_splash_image_url";
    /** URL that matches the local filesDir cache file. */
    public static String FESTIVAL_SPLASH_CACHED_URL = "festival_splash_cached_url";
    /** Server updated_at (ms) from splash/session — cache bust when image replaced. */
    public static String FESTIVAL_SPLASH_UPDATED_AT = "festival_splash_updated_at";
    /** updated_at that matches the local filesDir cache file. */
    public static String FESTIVAL_SPLASH_CACHED_UPDATED_AT = "festival_splash_cached_updated_at";
    /** ?v= (or t=/ver=) token that matches the local cache file. */
    public static String FESTIVAL_SPLASH_CACHED_URL_VERSION = "festival_splash_cached_url_version";
    /** CRC32:byteLength of the local cache file (debug / future content compare). */
    public static String FESTIVAL_SPLASH_CACHED_CONTENT_HASH = "festival_splash_cached_content_hash";
    /**
     * ARGB edge/dominant color sampled from last cached Opening Banner bitmap.
     * Used as window/splash gap fill on next cold start (0 = unset → brand orange).
     */
    public static String FESTIVAL_SPLASH_GAP_COLOR = "festival_splash_gap_color";
    /**
     * Max articles for Offline News pack (splash/runtime; default 24, hard 8–50).
     * Used as MAIN_CAP + thrifty prefetch target.
     */
    public static String OFFLINE_NEWS_PACK_MAX = "offline_news_pack_max";
    /**
     * Seconds before auto-hide of top News|Read chrome after each show (splash/runtime).
     * Default 3; 0 = disable; clamped 0–30.
     */
    public static String HOME_MODE_CHROME_AUTO_HIDE_SECONDS =
            "home_mode_chrome_auto_hide_seconds";
    /**
     * Minutes to keep the news card after the app goes to the background
     * (splash / session). Default 15. Not the splash-skip timer.
     */
    public static String FEED_POSITION_KEEP_MINUTES = "feed_position_keep_minutes";
    /** Wall-clock millis when the process last left the foreground. */
    public static String FEED_POSITION_LEFT_AT = "feed_position_left_at";
    /** Flip index saved with {@link #FEED_POSITION_LEFT_AT}. */
    public static String FEED_POSITION_INDEX = "feed_position_index";
    /** Post id saved with {@link #FEED_POSITION_LEFT_AT}. */
    public static String FEED_POSITION_POST_ID = "feed_position_post_id";
    /**
     * Language that owned {@link #FEED_POSITION_INDEX}. A different
     * {@link #LanguageID} must not resume that card. 0 = seat written before
     * this field existed (share-return still valid until the next save).
     */
    public static String FEED_POSITION_LANGUAGE_ID = "feed_position_language_id";
    /**
     * True when {@link #FEED_POSITION_INDEX} was saved from a share sheet.
     * Home and recents save the same seat with this false. A cold splash
     * clears the seat either way.
     */
    public static String FEED_POSITION_FROM_SHARE = "feed_position_from_share";
    /**
     * Share sheet was opened. Consumed when the process actually backgrounds.
     * A dismissed sheet clears this so the next Home press is a normal open.
     */
    public static String FEED_POSITION_SHARE_DEPARTURE = "feed_position_share_departure";
    /**
     * Main News FlipView content-tap mode: classic | reader | hybrid (splash/session).
     * Default hybrid.
     */
    public static String NEWS_TAP_MODE = "news_tap_mode";
    /** Hybrid: top % of half-article page → News|Read. Default 50. */
    public static String NEWS_TAP_HALF_UPPER_PCT = "news_tap_half_upper_pct";
    /** Hybrid: top % of full post → News|Read. Default 50. */
    public static String NEWS_TAP_FULL_UPPER_PCT = "news_tap_full_upper_pct";
    /** Hybrid: right-edge width % → Classic side-rail hide. Default 28. */
    public static String NEWS_TAP_FULL_RIGHT_RAIL_PCT = "news_tap_full_right_rail_pct";
    /**
     * Half articles: also hide likes/comments/share/flag when Classic hide
     * hides the writers row. Default false. Splash/session.
     */
    public static String NEWS_TAP_HALF_HIDE_ENGAGEMENT = "news_tap_half_hide_engagement";
    /**
     * Hide district pin/label on main News feed for non-district posts.
     * District/local posts and Local feed still show district. Default true.
     * Splash/session: {@code hide_district_on_main_feed}.
     */
    public static String HIDE_DISTRICT_ON_MAIN_FEED = "hide_district_on_main_feed";
    /**
     * District / location pill click opens Local (Main→Local, Local/category→Main).
     * Play Store / July default ON. OFF = display-only pill (no feed navigation).
     * Splash/session: {@code location_pill_opens_local_feed}.
     */
    public static String LOCATION_PILL_OPENS_LOCAL_FEED = "location_pill_opens_local_feed";
    /**
     * Rewards feature master (Profile rewards row + optional main-feed slot).
     * Does NOT control Profile wallet — see {@link #SHOW_WALLET_IN_PROFILE}.
     * Default false. Splash/session: {@code rewards_master}.
     */
    public static String REWARDS_MASTER = "rewards_master";
    /**
     * Profile wallet row (coins + convert). Independent of {@link #REWARDS_MASTER}.
     * Default true when missing. Splash/session/feed: {@code show_wallet_in_profile}.
     */
    public static String SHOW_WALLET_IN_PROFILE = "show_wallet_in_profile";
    /**
     * When hide-district is ON, show rewards promo in the main-feed district slot
     * for non-district posts. Never on Local / district posts. Default false.
     * Splash/session: {@code rewards_in_main_feed_slot}.
     */
    public static String REWARDS_IN_MAIN_FEED_SLOT = "rewards_in_main_feed_slot";
    /**
     * When Rewards chip is ON, keep showing it after the user joins Rewards
     * (instead of Flash or empty). Default false.
     * Splash/session: {@code keep_rewards_after_join}.
     */
    public static String KEEP_REWARDS_AFTER_JOIN = "keep_rewards_after_join";
    /**
     * When hide-district is ON, show Flash News chip in the main-feed right slot
     * when Rewards is not taking it (prefer Flash after join unless keep-rewards).
     * Default true. Splash/session: {@code flash_news_in_main_feed_slot}.
     */
    public static String FLASH_NEWS_IN_MAIN_FEED_SLOT = "flash_news_in_main_feed_slot";
    /**
     * Show Flash News and Rewards chips together on the writers row.
     * Default false. Ignored unless {@link #REWARDS_MASTER} is ON.
     * Splash / session / feed poll: {@code show_flash_and_rewards_together}.
     */
    public static String SHOW_FLASH_AND_REWARDS_TOGETHER = "show_flash_and_rewards_together";
    /**
     * Dual-chip pair text size in dp. Default 13. Splash / session / feed poll:
     * {@code dual_chip_size_dp}.
     */
    public static String DUAL_CHIP_SIZE_DP = "dual_chip_size_dp";
    /**
     * Extra end margin (dp) that shifts the Flash + Rewards pair left only.
     * Default 0. Splash / session / feed poll: {@code dual_chip_shift_left_dp}.
     */
    public static String DUAL_CHIP_SHIFT_LEFT_DP = "dual_chip_shift_left_dp";
    /**
     * Extra lift (dp) that moves the Flash + Rewards pair up. Default 0.
     * Splash / session / feed poll: {@code dual_chip_shift_up_dp}.
     */
    public static String DUAL_CHIP_SHIFT_UP_DP = "dual_chip_shift_up_dp";
    /**
     * When explicitly true, reporters cannot see Rewards / rewards wallet UI.
     * Missing key does not hide (Allow reporters ON, or splash never delivered the gate).
     * Splash: rewards_exclude_reporters. Settings Allow reporters writes this too.
     */
    public static String REWARDS_EXCLUDE_REPORTERS = "rewards_exclude_reporters";
    /**
     * In-app Settings Rewards switch. Missing or true = user wants Rewards
     * (admin master still required). False hides Profile row + feed chip.
     */
    public static String USER_REWARDS_ENABLED = "user_rewards_enabled";
    /**
     * Hours of last-read articles shown under Read / చదివినవి (splash/runtime).
     * Default 24; clamped 1–168. Older reads stay out of Read but News still strips them.
     */
    public static String READ_HISTORY_HOURS = "read_history_hours";
    /**
     * Days to keep in-app recent searches locally (splash/runtime; default 7, 1–90).
     * Pruned on search open — no network.
     */
    public static String SEARCH_RECENT_TTL_DAYS = "search_recent_ttl_days";
    /**
     * In-feed native ad after every N real articles (splash/runtime; default 4, 2–10).
     */
    public static String FEED_AD_EVERY_N = "feed_ad_every_n";
    /** Between-news format: native | interstitial (splash/session). */
    public static String FEED_AD_FORMAT = "feed_ad_format";
    /** Between-news AdMob unit ID (splash/session; Root_Settings.ad_code). */
    public static String FEED_AD_UNIT_ID = "feed_ad_unit_id";
    /** Flip positions ahead to prefetch next native ad (default 10, 3–12). */
    public static String AD_PREFETCH_LOOKAHEAD = "ad_prefetch_lookahead";
    /** Max simultaneous native ad loads (default 4, 1–5). */
    public static String MAX_NATIVE_AD_LOADS_IN_FLIGHT = "max_native_ad_loads_in_flight";
    /**
     * Flush batched real-ad impressions after every N painted ads (default 3, 1–50).
     * Admin: Configuration → Performance Tuning → Ad impression flush every N.
     */
    public static String AD_IMPR_FLUSH_EVERY_N = "ad_impr_flush_every_n";
    /**
     * Admin destroy-after-impression (default OFF for flip smoothness).
     * ON: destroy claimed/impressed natives after flip-away settle.
     * OFF: seat-sticky keep on same reqPos; never unpaid cross-seat reuse.
     * Admin: Configuration → Performance Tuning → Destroy native ads after impression.
     */
    public static String DESTROY_NATIVE_ADS_AFTER_IMPRESSION =
            "destroy_native_ads_after_impression";
    /**
     * Try secondary native unit after primary no-fill (legacy bool; default ON).
     * Prefer {@link #FEED_NATIVE_AD_UNIT_MODE}.
     */
    public static String USE_SECOND_NATIVE_AD_UNIT = "use_second_native_ad_unit";
    /**
     * Feed native unit mode: primary_only | primary_then_secondary | secondary_only.
     * Admin: Configuration → Advertisements → Feed native ad unit mode.
     */
    public static String FEED_NATIVE_AD_UNIT_MODE = "feed_native_ad_unit_mode";
    /** Secondary AdMob native unit (splash/session; Root_Settings.ad_code_secondary). */
    public static String FEED_AD_SECOND_UNIT_ID = "feed_ad_second_unit_id";
    /**
     * ViewsBatchTracker flush interval seconds (splash/session; default 60, 15–300).
     */
    public static String VIEWS_BATCH_FLUSH_INTERVAL_SECONDS =
            "views_batch_flush_interval_seconds";
    /** ViewsBatchTracker early flush at N events (default 12, 3–100). */
    public static String VIEWS_BATCH_FLUSH_AT_COUNT = "views_batch_flush_at_count";
    /**
     * MediaAccessReporter flush interval seconds (splash/session; default 60, 15–300).
     */
    public static String MEDIA_ACCESS_FLUSH_INTERVAL_SECONDS =
            "media_access_flush_interval_seconds";
    /**
     * RewardsEngagementBatcher flush interval seconds (splash/session; default 45).
     * Only used when rewards master ON + joined.
     */
    public static String REWARDS_ENGAGEMENT_FLUSH_INTERVAL_SECONDS =
            "rewards_engagement_flush_interval_seconds";
    /** Epoch ms of last successful get_user_profile soft-refresh (profile TTL). */
    public static String PROFILE_CACHE_FETCHED_AT = "profile_cache_fetched_at_ms";
    /** Epoch ms of last rewards_hub soft-refresh from Profile (hub TTL). */
    public static String REWARDS_HUB_FLAGS_FETCHED_AT = "rewards_hub_flags_fetched_at_ms";
    /** Disk cache of List_All_Categories decrypted JSON (keyed by language). */
    public static String CATEGORIES_DISK_CACHE_JSON = "categories_disk_cache_json";
    public static String CATEGORIES_DISK_CACHE_LANG = "categories_disk_cache_lang";
    /** "true"/"false" admin toggle for the bottom-footer cricket tab (default false). */
    public static String CRICKET_FOOTER_TAB_ENABLED = "cricket_footer_tab_enabled";

    // Profile hub / More (splash-delivered; defaults applied in ProfileHubConfigHelper).
    /** When true, Categories stay in footer. Default false = profile hub only. */
    public static String CATEGORIES_IN_FOOTER = "categories_in_footer";
    public static String SHOW_HEADER_PROFILE_ICON = "show_header_profile_icon";
    public static String ENABLE_STATUS_VIDEOS = "enable_status_videos";
    /** Admin Status Videos lookback days (splash / session; default 5). */
    public static String STATUS_VIDEOS_DAYS = "status_videos_days";
    /** Profile hub Become a Reporter row (splash hiring toggle). Default true. */
    public static String ENABLE_BECOME_A_REPORTER = "enable_become_a_reporter";
    /** Profile hub Birthday Special row (splash master). Default true; OFF hides row. */
    public static String ENABLE_BIRTHDAY_SPECIAL = "enable_birthday_special";
    /** JSON map of language_id → not-hiring message (splash). */
    public static String BECOME_A_REPORTER_NOT_HIRING_MESSAGES =
            "become_a_reporter_not_hiring_messages";
    /** JSON map of language_id → hiring-ON banner (splash). Empty = no banner. */
    public static String BECOME_A_REPORTER_HIRING_MESSAGES =
            "become_a_reporter_hiring_messages";
    /** CSV of language ids for Become a Reporter audience (empty = all). */
    public static String BECOME_A_REPORTER_LANGUAGE_IDS =
            "become_a_reporter_language_ids";
    /** CSV of state ids for Become a Reporter audience (empty = all). */
    public static String BECOME_A_REPORTER_STATE_IDS =
            "become_a_reporter_state_ids";
    /** CSV of district ids for Become a Reporter audience (empty = all). */
    public static String BECOME_A_REPORTER_DIST_IDS =
            "become_a_reporter_dist_ids";
    /** Login source flag: reopen applicant flow after OTP. */
    public static String LOGIN_SOURCE_BECOME_REPORTER = "login_source_become_a_reporter";
    /** Profile hub: hide Status Videos "New" badge after user opens Status Videos once. */
    public static String STATUS_VIDEOS_BADGE_SEEN = "status_videos_badge_seen";
    /** Profile hub: Become a Reporter orange NEW badge (hide after first open). */
    public static String BECOME_REPORTER_BADGE_SEEN = "become_reporter_badge_seen";
    /** Profile hub: Birthday Special orange NEW badge (hide after first open). */
    public static String BIRTHDAY_SPECIAL_BADGE_SEEN = "birthday_special_badge_seen";
    public static String PROFILE_SHOW_BOOKMARKS = "profile_show_bookmarks";
    public static String PROFILE_SHOW_REFERRAL = "profile_show_referral";
    public static String REFERRAL_PROGRAM_ENABLED = "referral_program_enabled";
    public static String PROFILE_SHOW_SETTINGS_ROW = "profile_show_settings_row";
    public static String PROFILE_HEADER_ICON_SIZE_DP = "profile_header_icon_size_dp";
    public static String PROFILE_CATEGORY_ICON_SIZE_DP = "profile_category_icon_size_dp";
    public static String PROFILE_MENU_TEXT_SIZE_SP = "profile_menu_text_size_sp";
    /** Home footer center "+" FAB diameter (dp). Splash-delivered; default 42. */
    public static String FOOTER_PLUS_ICON_SIZE_DP = "footer_plus_icon_size_dp";
    /**
     * Center Share icon on magazine / special cards (dp). Splash + feed poll.
     * Not the article action-row share icon. Missing pref uses 32.
     */
    public static String MAGAZINE_SHARE_ICON_SIZE_DP = "magazine_share_icon_size_dp";
    /**
     * Footer WhatsApp glyph between Special and Daily Updates (dp).
     * Splash + feed poll. Missing pref uses 48 (28 July 2026 center icon).
     * Not the likes-row WhatsApp.
     */
    public static String FOOTER_WHATSAPP_ICON_SIZE_DP = "footer_whatsapp_icon_size_dp";
    /**
     * Legacy likes-row / side WhatsApp switch. Old installs read only this.
     * New installs use the half and full flags and fall back here when a
     * split flag has never been stored. Default ON. Not the footer icon.
     */
    public static String ACTION_ROW_WHATSAPP_ENABLED = "action_row_whatsapp_enabled";
    /** Half videos and half articles: likes-row WhatsApp icon and count. */
    public static String ACTION_ROW_WHATSAPP_HALF_ENABLED = "action_row_whatsapp_half_enabled";
    /** Full posts that use the side WhatsApp icon and count. */
    public static String ACTION_ROW_WHATSAPP_FULL_ENABLED = "action_row_whatsapp_full_enabled";
    // Home bottom-nav slot visibility (admin Root_Settings; defaults true except categories).
    public static String FOOTER_SHOW_RELOAD = "footer_show_reload";
    public static String FOOTER_SHOW_SPECIAL = "footer_show_special";
    public static String FOOTER_SHOW_PLUS = "footer_show_plus";
    public static String FOOTER_SHOW_PROFILE = "footer_show_profile";
    public static String FOOTER_SHOW_BOOKMARKS = "footer_show_bookmarks";
    /** Home footer Video News tab. Default true. */
    public static String FOOTER_SHOW_VIDEOS = "footer_show_videos";
    // Settings panel toggle visibility (admin Root_Settings; defaults true / false as noted).
    public static String SETTINGS_SHOW_NOTIFICATIONS = "settings_show_notifications";
    public static String SETTINGS_SHOW_VIDEO_AUTOPLAY = "settings_show_video_autoplay";
    public static String SETTINGS_SHOW_NIGHT_MODE = "settings_show_night_mode";
    public static String SETTINGS_SHOW_LOCAL_STORIES = "settings_show_local_stories";
    /** Contact Us mailto target from admin Root_Settings; fallback support@anynews.co.in. */
    public static String CONTACT_EMAIL = "contact_email";
    /**
     * Home update-banner subtitle from admin App update prompt (splash + feed).
     * Empty → Android uses R.string.app_update_available_subtitle.
     */
    public static String UPDATE_BANNER_SUBTITLE = "update_banner_subtitle";
    /** Full jatakam WhatsApp digits (country code) from admin; empty = CTA disabled. */
    public static String FULL_JATAKAM_WHATSAPP = "full_jatakam_whatsapp";
    /**
     * Default raasi for calendar flip page (0-based 0–11 after parse).
     * Splash {@code default_rashi_id}: 1–12, 0–11, or mesha…meena.
     */
    public static String DEFAULT_RASHI_ID = "default_rashi_id";
    public static String DEFAULT_RASHI_ID_SET = "default_rashi_id_set";
    /**
     * Splash {@code calendar_panchang_source}:
     * {@code prokerala_hyderabad} (default, Way2-like) or {@code astronomical_hyderabad}.
     */
    public static String CALENDAR_PANCHANG_SOURCE = "calendar_panchang_source";
    public static final String CALENDAR_SOURCE_PROKERALA = "prokerala_hyderabad";
    public static final String CALENDAR_SOURCE_ASTRONOMICAL = "astronomical_hyderabad";
    /**
     * Telugu calendar detail-card UI knobs (Root_Settings → splash).
     * Defaults applied in {@link CalendarUiConfigHelper} when unset.
     */
    public static String CALENDAR_DETAIL_CARD_MIN_HEIGHT_DP = "calendar_detail_card_min_height_dp";
    public static String CALENDAR_DETAIL_TITLE_SIZE_SP = "calendar_detail_title_size_sp";
    public static String CALENDAR_DETAIL_ITEM_SIZE_SP = "calendar_detail_item_size_sp";
    public static String CALENDAR_DETAIL_ITEM_PAD_DP = "calendar_detail_item_pad_dp";
    /** Month grid height share (stored as percent 35–70 for SharedPreferences). */
    public static String CALENDAR_MONTH_HEIGHT_FRACTION = "calendar_month_height_fraction";
    /** Selected-day circle diameter as % of base 26dp (40–100). */
    public static String CALENDAR_SELECTED_CIRCLE_RADIUS_PERCENT =
            "calendar_selected_circle_radius_percent";

    /** TTD feature (splash {@code ttd} object). */
    public static String TTD_CONFIG_SET = "ttd_config_set";
    public static String TTD_ENABLED = "ttd_enabled";
    public static String TTD_BOOKING_ENABLED = "ttd_booking_enabled";
    public static String TTD_BOOKING_DAYS = "ttd_booking_days";
    /** ISO datetime (IST preferred) when booking CTA becomes visible. Empty = no open gate. */
    public static String TTD_BOOKING_OPENS_AT = "ttd_booking_opens_at";
    /** ISO datetime (IST preferred) when booking CTA hides. Empty = no close gate. */
    public static String TTD_BOOKING_CLOSES_AT = "ttd_booking_closes_at";
    public static String TTD_OFFICIAL_URL = "ttd_official_url";
    public static String TTD_BOOKING_URL = "ttd_booking_url";
    public static String TTD_EDARSHAN_URL = "ttd_edarshan_url";
    public static String TTD_APP_URL = "ttd_app_url";
    public static String TTD_SHOW_QUICK = "ttd_show_quick";
    public static String TTD_SHOW_DARSHAN = "ttd_show_darshan";
    public static String TTD_SHOW_SEVAS = "ttd_show_sevas";
    public static String TTD_SHOW_NEWS = "ttd_show_news";
    public static String TTD_SHOW_LINKS = "ttd_show_links";
    public static String TTD_HERO_TITLE = "ttd_hero_title";
    public static String TTD_HERO_SUBTITLE = "ttd_hero_subtitle";
    public static String TTD_BLESSING = "ttd_blessing";

    /** Weather / Fuel / Gold — splash root_settings blobs for Daily Updates. */
    public static String WEATHER_CONFIG_SET = "weather_config_set";
    public static String WEATHER_ENABLED = "weather_enabled";
    public static String WEATHER_CONFIG_JSON = "weather_config_json";
    public static String FUEL_CONFIG_SET = "fuel_config_set";
    public static String FUEL_ENABLED = "fuel_enabled";
    public static String FUEL_CONFIG_JSON = "fuel_config_json";
    public static String GOLD_CONFIG_SET = "gold_config_set";
    public static String GOLD_ENABLED = "gold_enabled";
    public static String GOLD_CONFIG_JSON = "gold_config_json";

    /** Jobs hub from splash {@code jobs} (Mongo collection + stats). */
    public static String JOBS_CONFIG_SET = "jobs_config_set";
    public static String JOBS_ENABLED = "jobs_enabled";
    public static String JOBS_CONFIG_JSON = "jobs_config_json";

    /**
     * Daily Updates hub (splash {@code daily_updates}): Panchangam / Mana Zilla /
     * Flash News kill switches + card background art offset + typography / insets.
     */
    public static String DAILY_UPDATES_CONFIG_SET = "daily_updates_config_set";
    public static String DAILY_UPDATES_CONFIG_JSON = "daily_updates_config_json";
    public static String DU_PANCHANGAM_ENABLED = "du_panchangam_enabled";
    /** Rashi Phalalu Daily Updates card (splash daily_updates.rashi_enabled). */
    public static String DU_RASHI_ENABLED = "du_rashi_enabled";
    /** Cricket / Crex Daily Updates card (splash daily_updates.cricket_enabled). Default ON. */
    public static String DU_CRICKET_ENABLED = "du_cricket_enabled";
    /**
     * Old Cricket Live Hub match switch (splash daily_updates.cricket_match_on
     * and session cricket_match_on). Default false until the server says a match is on.
     */
    public static String DU_CRICKET_MATCH_ON = "du_cricket_match_on";
    public static String DU_MANA_ZILLA_ENABLED = "du_mana_zilla_enabled";
    public static String DU_FLASH_NEWS_ENABLED = "du_flash_news_enabled";
    /** Stock Market row kill switch (splash daily_updates.stock_enabled). */
    public static String DU_STOCK_ENABLED = "du_stock_enabled";
    public static String DU_STOCK_TITLE = "du_stock_title";
    public static String DU_STOCK_SUBTITLE = "du_stock_subtitle";
    public static String DU_STOCK_OFFSET_Y_DP = "du_stock_offset_y_dp";
    /** Cached splash stock summary JSON (nifty/sensex) — real prices only. */
    public static String DU_STOCK_SUMMARY_JSON = "du_stock_summary_json";
    /** CSV of user-pinned stock symbols (local SharedPreferences). */
    public static String STOCK_PINNED_SYMBOLS = "stock_pinned_symbols";
    /** dp; negative shifts card bg art left. */
    public static String DU_CARD_IMAGE_OFFSET_DP = "du_card_image_offset_dp";
    /** CSV of language ids allowed for Mana Zilla (default "3" = Telugu). */
    public static String DU_MANA_ZILLA_LANGUAGE_IDS = "du_mana_zilla_language_ids";
    /** Per-feature language allow-lists (CSV). Empty = unset (all ON). "-" = every language OFF. */
    public static String DU_WEATHER_LANGUAGE_IDS = "du_weather_language_ids";
    public static String DU_FUEL_LANGUAGE_IDS = "du_fuel_language_ids";
    public static String DU_GOLD_LANGUAGE_IDS = "du_gold_language_ids";
    public static String DU_TTD_LANGUAGE_IDS = "du_ttd_language_ids";
    public static String DU_JOBS_LANGUAGE_IDS = "du_jobs_language_ids";
    public static String DU_PANCHANGAM_LANGUAGE_IDS = "du_panchangam_language_ids";
    public static String DU_CRICKET_LANGUAGE_IDS = "du_cricket_language_ids";
    public static String DU_RASHI_LANGUAGE_IDS = "du_rashi_language_ids";
    public static String DU_FLASH_NEWS_LANGUAGE_IDS = "du_flash_news_language_ids";
    public static String DU_STOCK_LANGUAGE_IDS = "du_stock_language_ids";
    public static String DU_MANA_HEADLINE_OFFSET_X_DP = "du_mana_headline_offset_x_dp";
    public static String DU_MANA_HEADLINE_OFFSET_Y_DP = "du_mana_headline_offset_y_dp";
    public static String DU_MANA_HEADLINE_TEXT_SIZE_SP = "du_mana_headline_text_size_sp";
    /** Splash Redis/memory Flash ticker headlines JSON array. */
    public static String DU_FLASH_HEADLINES_JSON = "du_flash_headlines_json";
    /** Language stamp for {@link #DU_FLASH_HEADLINES_JSON} — reject cross-lang Telugu ticker. */
    public static String DU_FLASH_HEADLINES_LANG = "du_flash_headlines_lang";
    public static String DU_LOCAL_HEADLINES_LANG = "du_local_headlines_lang";
    /** Splash language stamp for weather/fuel/gold/jobs blobs. */
    public static String LOCAL_UPDATES_SPLASH_LANG = "local_updates_splash_lang";
    /** Splash Redis/memory Mana Zilla local titles JSON array. */
    public static String DU_LOCAL_HEADLINES_JSON = "du_local_headlines_json";
    public static String DU_LOCAL_HEADLINES_DIST = "du_local_headlines_dist";

    // DU typography (sp)
    public static String DU_PAGE_TITLE_TEXT_SIZE_SP = "du_page_title_text_size_sp";
    public static String DU_PAGE_SUBTITLE_TEXT_SIZE_SP = "du_page_subtitle_text_size_sp";
    public static String DU_CARD_TITLE_TEXT_SIZE_SP = "du_card_title_text_size_sp";
    public static String DU_CARD_BODY_TEXT_SIZE_SP = "du_card_body_text_size_sp";
    public static String DU_CARD_CTA_TEXT_SIZE_SP = "du_card_cta_text_size_sp";
    public static String DU_CARD_HERO_TEXT_SIZE_SP = "du_card_hero_text_size_sp";
    public static String DU_STOCK_TITLE_TEXT_SIZE_SP = "du_stock_title_text_size_sp";
    public static String DU_STOCK_BODY_TEXT_SIZE_SP = "du_stock_body_text_size_sp";
    public static String DU_STOCK_CTA_TEXT_SIZE_SP = "du_stock_cta_text_size_sp";
    public static String DU_FLASH_CHIP_TEXT_SIZE_SP = "du_flash_chip_text_size_sp";
    public static String DU_FLASH_HEADLINE_TEXT_SIZE_SP = "du_flash_headline_text_size_sp";

    // Per-box content start (horizontal paddingStart dp — clears corner art)
    public static String DU_WEATHER_CONTENT_START_DP = "du_weather_content_start_dp";
    public static String DU_FUEL_CONTENT_START_DP = "du_fuel_content_start_dp";
    public static String DU_GOLD_CONTENT_START_DP = "du_gold_content_start_dp";
    public static String DU_TTD_CONTENT_START_DP = "du_ttd_content_start_dp";
    public static String DU_JOBS_CONTENT_START_DP = "du_jobs_content_start_dp";
    public static String DU_PANCHANGAM_CONTENT_START_DP = "du_panchangam_content_start_dp";
    public static String DU_RASHI_CONTENT_START_DP = "du_rashi_content_start_dp";
    public static String DU_CRICKET_CONTENT_START_DP = "du_cricket_content_start_dp";
    public static String DU_STOCK_CONTENT_START_DP = "du_stock_content_start_dp";

    // Flash chrome
    public static String DU_FLASH_ICON_SIZE_DP = "du_flash_icon_size_dp";
    public static String DU_FLASH_ICON_MARGIN_START_DP = "du_flash_icon_margin_start_dp";
    public static String DU_FLASH_CONTENT_PAD_START_DP = "du_flash_content_pad_start_dp";

    /**
     * Push tray appearance (admin Configuration → splash / articles config).
     * Layout changes require a Play update; these knobs apply once that APK is installed.
     */
    /** Default true: show article image as left thumb / expanded image. */
    public static String PN_SHOW_ARTICLE_IMAGE = "pn_show_article_image";
    /** "compact_with_image" | "text_only" */
    public static String PN_STYLE_MODE = "pn_style_mode";
    /** Optional remote red brand logo URL; empty + include_logo → bundled red logo. */
    public static String PN_LARGE_ICON_URL = "pn_large_icon_url";
    /** Global default: inject brand logo when FCM omits include_logo. Default false. */
    public static String PN_INCLUDE_LOGO_DEFAULT = "push_include_logo_default";
    /** Left branding ImageView size (dp). */
    public static String PN_LEFT_ICON_SIZE_DP = "pn_left_icon_size_dp";
    /** Article tile size (dp). */
    public static String PN_THUMB_SIZE_DP = "pn_thumb_size_dp";
    /** Headline text size (sp). */
    public static String PN_TITLE_SIZE_SP = "pn_title_size_sp";
    // One-shot post id parsed from a shared deep link (/p/{id}, anynews://post/{id},
    // anws.in/{id}, ...). Persisted by StartingActivity and consumed+cleared by
    // HomePage so the exact post opens even on splash routes that don't forward
    // the SHARE_REDIRECT_ID intent extra (returning-user paths).
    // Cross-language: opening a Hindi share while LanguageID=Telugu must show the
    // Hindi post once, then continue the Telugu feed — never rewrite LanguageID.
    public static String PENDING_SHARE_POST = "pending_share_post_id";
    /**
     * One-shot 0-based gallery/magazine page from share deep links ({@code ?page=N}).
     * Written by StartingActivity with {@link #PENDING_SHARE_POST}; HomePage holds
     * it until FlipperAdapter binds the nested FlipView/ViewPager, then clears.
     * Missing / 0 = first page (legacy short links without page).
     */
    public static String PENDING_SHARE_PAGE = "pending_share_gallery_page";
    /**
     * One-shot FCM push post id. Written only on tray tap (HomePage / splash),
     * never on FCM receive — receive-time sticky opened the latest push on
     * launcher opens and broke stacked-notif correctness. Cleared after land
     * or on non-push splash.
     */
    public static String PENDING_PUSH_POST = "pending_push_post_id";
    /**
     * Recent tray posts (post id + visible title), written when the notification
     * is shown. A notification-history tap sometimes rebuilds the launcher
     * intent with the title and no post id; this log maps that title back.
     */
    public static String RECENT_PUSH_OPEN_LOG = "recent_push_open_log";
    /** One-shot job id from /job/{id} or legacy /job-{base64} share deep links. */
    public static String PENDING_SHARE_JOB = "pending_share_job_id";
    /**
     * Admin share link base URL from splash ({@code share_public_base_url}).
     * e.g. https://share.anynews.co.in
     */
    public static String SHARE_PUBLIC_BASE_URL = "share_public_base_url";
    /**
     * Admin half-video share logo URL from splash / feed poll.
     * Empty or missing → bundled {@code app_share_logo}.
     */
    public static String HALF_VIDEO_SHARE_LOGO_URL = "half_video_share_logo_url";
    /** URL whose image file is already on disk. Empty → use the bundled logo. */
    public static String HALF_VIDEO_SHARE_LOGO_READY = "half_video_share_logo_ready";
    public static String PROFILE_CACHE = "reporter_profile_cache_json";
    /** Last active key for {@link ReporterProfileCache} (draft+phone/email scoped). */
    public static String PROFILE_CACHE_KEY = "reporter_profile_cache_key";

    // Open-path / push-stampede client cache (epoch ms stamps + disk feed file).
    /** Last successful Splash_Screen apply. */
    public static String OPEN_PATH_SPLASH_AT = "open_path_splash_at";
    /** Last successful get_static_data apply. */
    public static String OPEN_PATH_STATIC_AT = "open_path_static_at";
    /** Last successful List_All_Articles page-0 network fetch. */
    public static String OPEN_PATH_FEED_NET_AT = "open_path_feed_net_at";
    public static String Selected_YES_PERCENT="Selected_YES_PERCENT";
    public static String Selected_NO_PERCENT="Selected_NO_PERCENT";
    public static String Selected_YES_COUNT="Selected_YES_COUNT";
    public static String Selected_NO_COUNT="Selected_NO_COUNT";
    // firebase events triggered
    public static String type_firebase = "views";
    public static String type_firebase_button = "button_click";
    public static String button_name_local = "local";
    public static String button_name_myfeed = "my_feed";
    public static String button_name_home = "home";
    public static String button_name_events = "events";
    public static String button_name_category = "category";
    public static String button_name_post = "post";
    public static String button_name_profile = "profile";
    public static String APP_RATING_SUBMITTED = "app_rating_submitted";
    public static String APP_RATING = "app_rating";
    /** Calendar day (yyyy-MM-dd, Asia/Kolkata-aligned local) when in-feed ratings card was shown. */
    public static String APP_RATING_SEEN_DAY = "app_rating_seen_day";
    public static String isUnreadPostsSelected = "isunreadpostsselected";
    /** Video News footer tab: List_All_Articles video_posts=true (half+full video only). */
    public static String isVideoPostsSelected = "isvideopostsselected";
    public final static String AD_REGULAR = "regular";
    public final static String AD_FULL_IMAGE  = "fullImage";
    public final static String AD_MEDIUM  = "medium";
    public final static String VERSION_NAME= "V3" ;
    public final static String VERSION_NAME_DIVIDER= "/" ;
    public final static String VERSION_NAME_ARTICLES= "articles" ;
    public final static String MALAYALAM_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"1"+VERSION_NAME_ARTICLES;
    public final static String HINDI_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"2"+VERSION_NAME_ARTICLES;
    public final static String TELUGU_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"3"+VERSION_NAME_ARTICLES;
    public final static String TAMIL_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"4"+VERSION_NAME_ARTICLES;
    public final static String KANNADA_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"5"+VERSION_NAME_ARTICLES;
    public final static String ODIA_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"5"+VERSION_NAME_ARTICLES;
//StateID


}
