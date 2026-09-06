package com.anyNews.anynews.Activities;

import android.os.Handler;
import android.os.Looper;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Ui.HomeFeedRevealView;
import com.anyNews.anynews.Utils.CalendarRaasiLabels;
import com.anyNews.anynews.Utils.CalendarUiConfigHelper;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.HomeModeChromeConfig;
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.ThemeColorHelper;
import com.anyNews.anynews.flipview.FlipView;
import com.anyNews.anynews.flipview.OverFlipMode;
import com.anyNews.anynews.panchang.PanchangModels;
import com.anyNews.anynews.panchang.PanchangRepository;
import com.anyNews.anynews.panchang.RaasiContent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Telugu Panchang calendar + daily raasi (2 FlipView pages), bindable into
 * {@link TeluguCalendarActivity} or HomePage content host above the footer.
 */
public final class TeluguCalendarController implements FlipView.OnFlipListener {

    private static final String PREF_SELECTED_RAASI = "telugu_calendar_selected_raasi";
    private static final String PREF_SELECTED_RAASI_SET = "telugu_calendar_selected_raasi_set";
    private static final String PREF_RAASI_NAME = "telugu_calendar_raasi_user_name";
    private static final String PREF_RAASI_POB = "telugu_calendar_raasi_pob";
    private static final String PREF_RAASI_DOB = "telugu_calendar_raasi_dob";
    /** Fallback when admin default unset: Scorpio (0-based 7), competitor-like. */
    private static final int FALLBACK_DEFAULT_RAASI = 7;

    private static final String[] WEEKDAYS_TE = {
            "ఆది", "సోమ", "మంగళ", "బుధ", "గురు", "శుక్ర", "శని"
    };
    private static final String[] EN_MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private static final int COLOR_RED = 0xFFE53935;
    private static final int COLOR_GREEN = 0xFF22C55E;
    private static final int COLOR_BLUE = 0xFF3B82F6;
    private static final int COLOR_ORANGE = 0xFFEF4923;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFB0B0B0;
    private static final int COLOR_CELL_LINE = 0xFFC8C8C8;
    private static final int COLOR_GOLD = 0xFFF5C542;
    private static final int COLOR_GOLD_DARK = 0xFF3D2E0A;

    @NonNull private final AppCompatActivity host;
    @NonNull private final View root;
    private final boolean applyWindowInsets;

    private FlipView flipPager;
    /** Page 0 root — non-scrolling LinearLayout (id kept for bind). */
    private View calendarPageRoot;
    private NestedScrollView raasiScroll;
    private TextView monthTitle;
    private TextView teluguLine;
    private GridLayout grid;
    private ViewPager pager;
    private TextView detailPageIndicator;
    private TextView flipIndicator;
    private TextView flipIndicatorRaasi;
    private ImageView dayShare;
    private ImageView raasiShare;
    /** Magazine/gallery-style blinking Share (bottom-right overlay on calendar root). */
    @Nullable private ImageView blinkShare;
    /** Panchangam | Raasi Phalalu top chrome (News|Read pattern — show ~3s then auto-hide). */
    @Nullable private View ttdToggleRoot;
    @Nullable private View ttdThumb;
    @Nullable private TextView ttdSegCalendar;
    @Nullable private TextView ttdSegTtd;
    private final Handler ttdChromeHandler = new Handler(Looper.getMainLooper());
    private boolean ttdChromeUserHidden;
    private boolean ttdChromeAutoHidePending;
    private boolean raasiSegmentSelected;
    private final Runnable ttdChromeAutoHideRunnable = this::runTtdChromeAutoHide;
    private LinearLayout festivalStrip;
    private ImageView festivalIcon;
    private TextView festivalText;
    @Nullable private View calendarBody;
    @Nullable private HomeFeedRevealView loadReveal;
    /** True after first successful pack bind + body reveal. */
    private boolean calendarContentRevealed;
    private View sheetScrim;
    private LinearLayout festivalList;
    private ImageView swipeArrow;
    private TextView swipeLabel;
    private View swipeCue;
    private GridLayout raasiGrid;
    private View raasiDetail;
    private TextView raasiSignLine;
    private TextView raasiTitle;
    private TextView raasiBody;
    private TextView raasiMore;
    private TextView luckyHeading;
    private TextView luckyColorLabel;
    private TextView luckyNumbersLabel;
    private TextView luckyDirectionLabel;
    private TextView luckyColor;
    private TextView luckyNumbers;
    private TextView luckyDirection;
    private TextView fullJatakamCta;
    @Nullable private ImageView raasiDetailIcon;
    @Nullable private LinearLayout raasiSections;
    @Nullable private TextView rashiDateLabel;
    @Nullable private TextView rashiDateValue;
    @Nullable private TextView rashiNakLabel;
    @Nullable private TextView rashiNakValue;
    @Nullable private TextView rashiYogaLabel;
    @Nullable private TextView rashiYogaValue;
    @Nullable private TextView rashiLordLabel;
    @Nullable private TextView rashiLordValue;
    @Nullable private TextView rashiElementLabel;
    @Nullable private TextView rashiElementValue;
    @Nullable private TextView rashiGemLabel;
    @Nullable private TextView rashiGemValue;
    @Nullable private TextView rashiColorLabel;
    @Nullable private TextView rashiColorValue;
    @Nullable private TextView rashiShubham;
    @Nullable private ImageView raasiBack;
    @Nullable private ImageView raasiHeaderShare;
    private View raasiFormScrim;
    private View raasiFormSheetColumn;
    private NestedScrollView raasiFormScroll;
    private EditText raasiFormName;
    private EditText raasiFormDob;
    private EditText raasiFormPlace;
    private boolean raasiFormImeInstalled;
    private boolean raasiFormSoftInputSaved;
    private int raasiFormPrevSoftInputMode;
    private View raasiDetailScrim;
    private TextView raasiDetailSignTitle;
    private TextView raasiDetailPhalaluTitle;
    private TextView raasiDetailBody;
    private TextView raasiDetailLuckyHeading;
    private TextView raasiDetailLuckyLine1;
    private TextView raasiDetailLuckyLine2;
    private TextView raasiDetailFullJatakam;
    private boolean systemBarInsetsInstalled;

    private Calendar displayMonth;
    private int selectedDay;
    private int currentYearLoaded = -1;
    @Nullable private PanchangModels.YearPack yearPack;
    private DetailPagerAdapter detailAdapter;
    private int selectedRaasi = 0;
    private ObjectAnimator swipeBob;
    /** One-shot: rebind grid after page first gets a real height. */
    private boolean pendingMonthGridRelayout;
    private int themeAccent;
    private boolean calendarPageWired;
    private boolean raasiPageWired;
    /** Identity of the page view last fully bound (FlipView may recreate). */
    @Nullable private View wiredCalendarPageInstance;
    @Nullable private View wiredRaasiPageInstance;

    public TeluguCalendarController(@NonNull AppCompatActivity host,
                                      @NonNull View root,
                                      boolean applyWindowInsets) {
        this.host = host;
        this.root = root;
        this.applyWindowInsets = applyWindowInsets;
        if (applyWindowInsets) {
            applySystemBarInsets();
        }

        themeAccent = ThemeColorHelper.getThemeColor(host);
        displayMonth = Calendar.getInstance();
        selectedDay = displayMonth.get(Calendar.DAY_OF_MONTH);
        displayMonth.set(Calendar.DAY_OF_MONTH, 1);
        selectedRaasi = loadSelectedRaasi();
        detailAdapter = new DetailPagerAdapter();

        flipPager = root.findViewById(R.id.telugu_calendar_flip_pager);
        sheetScrim = root.findViewById(R.id.telugu_calendar_sheet_scrim);
        festivalList = root.findViewById(R.id.telugu_calendar_festival_list);
        raasiFormScrim = root.findViewById(R.id.telugu_calendar_raasi_form_scrim);
        raasiFormSheetColumn = root.findViewById(R.id.raasi_form_sheet_column);
        raasiFormScroll = root.findViewById(R.id.raasi_form_scroll);
        raasiFormName = root.findViewById(R.id.raasi_form_name);
        raasiFormDob = root.findViewById(R.id.raasi_form_dob);
        raasiFormPlace = root.findViewById(R.id.raasi_form_place);
        raasiDetailScrim = root.findViewById(R.id.telugu_calendar_raasi_detail_scrim);
        raasiDetailSignTitle = root.findViewById(R.id.raasi_detail_sign_title);
        raasiDetailPhalaluTitle = root.findViewById(R.id.raasi_detail_phalalu_title);
        raasiDetailBody = root.findViewById(R.id.raasi_detail_body);
        raasiDetailLuckyHeading = root.findViewById(R.id.raasi_detail_lucky_heading);
        raasiDetailLuckyLine1 = root.findViewById(R.id.raasi_detail_lucky_line1);
        raasiDetailLuckyLine2 = root.findViewById(R.id.raasi_detail_lucky_line2);
        raasiDetailFullJatakam = root.findViewById(R.id.raasi_detail_full_jatakam);

        if (flipPager != null) {
            // Same FlipView host as articles, but page switch is toggle-only
            // (Panchangam | Raasi) — user swipe between pages is disabled.
            CalendarFlipAdapter adapter = new CalendarFlipAdapter();
            flipPager.setAdapter(adapter);
            flipPager.setOnFlipListener(this);
            flipPager.setOverFlipMode(OverFlipMode.GLOW);
            flipPager.setmIsFlippingEnabled(false);
            // Do NOT call peakNext() — infinite FlipView peak bobs the whole calendar.
            flipPager.setFlipDistanceperpage(200,
                    host.getResources().getDisplayMetrics().density);
        }

        View close = root.findViewById(R.id.telugu_calendar_sheet_close);
        if (close != null) close.setOnClickListener(v -> hideFestivalsSheet());
        if (sheetScrim != null) {
            sheetScrim.setOnClickListener(v -> hideFestivalsSheet());
            View sheet = root.findViewById(R.id.telugu_calendar_sheet);
            if (sheet != null) sheet.setOnClickListener(v -> { /* consume */ });
        }

        if (raasiFormScrim != null) {
            raasiFormScrim.setOnClickListener(v -> hideRaasiForm());
            View form = root.findViewById(R.id.telugu_calendar_raasi_form);
            if (form != null) form.setOnClickListener(v -> { /* consume */ });
            if (raasiFormSheetColumn != null) {
                raasiFormSheetColumn.setOnClickListener(v -> { /* consume */ });
            }
            installRaasiFormImeHandling();
        }
        View formClose = root.findViewById(R.id.raasi_form_close);
        if (formClose != null) formClose.setOnClickListener(v -> hideRaasiForm());
        TextView formSubmit = root.findViewById(R.id.raasi_form_submit);
        if (formSubmit != null) {
            formSubmit.setOnClickListener(v -> submitRaasiForm());
            formSubmit.setText(CalendarRaasiLabels.submit(host));
        }
        TextView formTitle = root.findViewById(R.id.raasi_form_title);
        if (formTitle != null) formTitle.setText(CalendarRaasiLabels.knowRaasiTitle(host));
        if (raasiFormName != null) {
            raasiFormName.setHint(CalendarRaasiLabels.fullName(host));
            wireRaasiFormFieldFocus(raasiFormName);
        }
        if (raasiFormDob != null) {
            raasiFormDob.setHint(CalendarRaasiLabels.dobTime(host));
            wireRaasiDobPicker();
        }
        if (raasiFormPlace != null) {
            raasiFormPlace.setHint(CalendarRaasiLabels.birthPlace(host));
            wireRaasiFormFieldFocus(raasiFormPlace);
        }

        TextView festTitle = root.findViewById(R.id.telugu_calendar_festivals_title);
        if (festTitle != null) festTitle.setText(CalendarRaasiLabels.festivalsTitle(host));

        if (raasiDetailScrim != null) {
            raasiDetailScrim.setOnClickListener(v -> hideRaasiDetailSheet());
            View detailSheet = root.findViewById(R.id.telugu_calendar_raasi_detail_sheet);
            if (detailSheet != null) detailSheet.setOnClickListener(v -> { /* consume */ });
        }
        View detailClose = root.findViewById(R.id.raasi_detail_close);
        if (detailClose != null) detailClose.setOnClickListener(v -> hideRaasiDetailSheet());
        if (raasiDetailFullJatakam != null) {
            raasiDetailFullJatakam.setVisibility(View.GONE);
            raasiDetailFullJatakam.setOnClickListener(null);
        }
        if (raasiDetailLuckyHeading != null) {
            raasiDetailLuckyHeading.setText(CalendarRaasiLabels.luckyHeading(host));
        }

        wireBlinkShareOverlay();
        // Standalone TeluguCalendarActivity only — HomePage embeds its own sibling
        // Panchangam|Raasi chrome above the calendar host (FlipView cannot cover it).
        if (applyWindowInsets) {
            wireSegmentToggleChrome();
        } else {
            View embeddedToggle = root.findViewById(R.id.calendar_ttd_toggle_root);
            if (embeddedToggle != null) {
                embeddedToggle.setVisibility(View.GONE);
            }
        }
    }

    /** Inflate calendar layout into a HomePage host and bind (no window inset overrides). */
    @NonNull
    public static TeluguCalendarController attachEmbedded(@NonNull AppCompatActivity host,
                                                          @NonNull ViewGroup hostContainer) {
        hostContainer.removeAllViews();
        View calendarRoot = LayoutInflater.from(host).inflate(
                R.layout.activity_telugu_calendar, hostContainer, false);
        hostContainer.addView(calendarRoot, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return new TeluguCalendarController(host, calendarRoot, false);
    }

    /**
     * targetSdk 35 edge-to-edge: pad root with status/nav insets so calendar header
     * and raasi content sit fully inside the app under the system bars.
     */
    private void applySystemBarInsets() {
        if (!applyWindowInsets) return;
        Window window = host.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        View insetRoot = this.root.getId() == R.id.telugu_calendar_root
                ? this.root
                : this.root.findViewById(R.id.telugu_calendar_root);
        if (insetRoot == null) insetRoot = this.root;
        if (systemBarInsetsInstalled) {
            ViewCompat.requestApplyInsets(insetRoot);
            return;
        }
        systemBarInsetsInstalled = true;
        final View target = insetRoot;
        final int padL = target.getPaddingLeft();
        final int padT = target.getPaddingTop();
        final int padR = target.getPaddingRight();
        final int padB = target.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(target, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(padL + bars.left, padT + bars.top, padR + bars.right, padB + bars.bottom);
            // Consume only system bars so the find-rashi sheet still receives IME insets.
            return new WindowInsetsCompat.Builder(windowInsets)
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.NONE)
                    .build();
        });
        ViewCompat.requestApplyInsets(target);
    }

    /** Re-apply insets when standalone activity resumes; re-show segment chrome. */
    public void onHostResume() {
        if (applyWindowInsets) {
            applySystemBarInsets();
            setSegmentChromeVisible(true, false);
        }
    }

    @Override
    public void onFlippedToPage(FlipView v, int position, long id) {
        raasiSegmentSelected = position == 1;
        updateFlipIndicators(position);
        refreshBlinkShareContentDescription(position);
        syncSegmentThumb(raasiSegmentSelected);
        // Blue blink FAB only on Panchangam; Raasi keeps gold header share.
        setBlinkShareVisible(position == 0 && !isBlinkShareCoveredBySheet());
    }

    /** True when the Raasi Phalalu flip page is showing. */
    public boolean isRaasiPageVisible() {
        return raasiSegmentSelected
                || (flipPager != null && flipPager.getCurrentPage() == 1);
    }

    /** Left segment: Panchangam calendar page (programmatic flip; swipe disabled). */
    public void selectPanchangamPage(boolean smooth) {
        raasiSegmentSelected = false;
        syncSegmentThumb(false);
        setBlinkShareVisible(!isBlinkShareCoveredBySheet());
        if (flipPager == null) return;
        if (flipPager.getCurrentPage() == 0) return;
        if (smooth) flipPager.smoothFlipTo(0);
        else flipPager.flipTo(0);
    }

    /** Right segment: Raasi Phalalu page (programmatic flip; swipe disabled). */
    public void selectRaasiPage(boolean smooth) {
        raasiSegmentSelected = true;
        syncSegmentThumb(true);
        setBlinkShareVisible(false);
        goToRaasiPage(smooth);
    }

    /**
     * Wire Flip 0 (calendar). Re-attaches listeners whenever FlipView recreates the page.
     */
    private void wireCalendarPage() {
        if (monthTitle == null) return;
        boolean newInstance = wiredCalendarPageInstance != calendarPageRoot;
        wiredCalendarPageInstance = calendarPageRoot;
        calendarPageWired = true;

        View scope = calendarPageRoot != null ? calendarPageRoot : root;
        ImageView prev = scope.findViewById(R.id.telugu_calendar_prev);
        ImageView next = scope.findViewById(R.id.telugu_calendar_next);
        if (prev != null) {
            prev.setOnClickListener(v -> {
                revealSegmentChromeIfHidden();
                displayMonth.add(Calendar.MONTH, -1);
                selectedDay = clampSelectedDay();
                ensureYearAndBind();
            });
        }
        if (next != null) {
            next.setOnClickListener(v -> {
                revealSegmentChromeIfHidden();
                displayMonth.add(Calendar.MONTH, 1);
                selectedDay = clampSelectedDay();
                ensureYearAndBind();
            });
        }

        if (festivalStrip != null) {
            festivalStrip.setOnClickListener(v -> {
                revealSegmentChromeIfHidden();
                showFestivalsSheet();
            });
        }
        // Swipe-to-rashi cue retired — Rashi is the top segment only.
        stopSwipeBob();
        if (swipeCue != null) {
            swipeCue.setVisibility(View.GONE);
            swipeCue.setOnClickListener(null);
            swipeCue.setClickable(false);
            swipeCue.setFocusable(false);
        }
        if (swipeLabel != null) {
            swipeLabel.setVisibility(View.GONE);
            swipeLabel.setOnClickListener(null);
        }
        if (swipeArrow != null) {
            swipeArrow.setVisibility(View.GONE);
            swipeArrow.setOnClickListener(null);
        }
        hideLegacyCardShare(dayShare);
        if (pager != null) {
            pager.setAdapter(detailAdapter);
            pager.setPageMargin(dp(8));
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    updateDetailPageIndicator(position);
                }
            });
        }

        fillWeekdayHeaders();
        updateFlipIndicators(flipPager != null ? flipPager.getCurrentPage() : 0);
        // Tap header / weekday / cards area to re-show segment chrome after ~3s auto-hide.
        installSegmentChromeRevealTargets();
        if (newInstance || yearPack == null) {
            ensureYearAndBind();
        } else if (grid != null && grid.getChildCount() == 0) {
            bindAll();
        }
    }

    /**
     * Wire the Special/full-post blue circular Share onto the calendar root
     * (bottom-right above footer). Same asset + sizing as
     * {@code FlipperAdapter} {@code full_image_share_bg}: {@code R.drawable.ic_share_full},
     * no tint, no chip background. Blink uses {@code R.anim.blink_share}.
     * Tap runs day share on the Panchangam flip page only — Raasi Phalalu keeps
     * the gold header share ({@code telugu_calendar_raasi_header_share}) and never
     * shows this blue FAB overlay.
     */
    private void wireBlinkShareOverlay() {
        blinkShare = root.findViewById(R.id.telugu_calendar_blink_share);
        if (blinkShare == null) return;
        blinkShare.setClickable(true);
        blinkShare.setFocusable(true);
        // Match full_image_share_bg: blue circular PNG, no white tint / dark chip.
        blinkShare.setImageResource(R.drawable.ic_share_full);
        blinkShare.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        blinkShare.clearColorFilter();
        blinkShare.setBackground(null);
        ViewGroup.MarginLayoutParams mlp =
                blinkShare.getLayoutParams() instanceof ViewGroup.MarginLayoutParams
                        ? (ViewGroup.MarginLayoutParams) blinkShare.getLayoutParams()
                        : null;
        if (mlp != null) {
            mlp.bottomMargin = applyWindowInsets ? dp(28) : dp(10);
            blinkShare.setLayoutParams(mlp);
        }
        blinkShare.setOnClickListener(v -> {
            int page = flipPager != null ? flipPager.getCurrentPage() : 0;
            if (page == 1) {
                shareSelectedRaasi();
            } else {
                shareSelectedDay();
            }
        });
        refreshBlinkShareContentDescription(
                flipPager != null ? flipPager.getCurrentPage() : 0);
        // Show on Panchangam only; Raasi uses header gold share.
        setBlinkShareVisible(!isRaasiPageVisible());
    }

    private void refreshBlinkShareContentDescription(int flipPage) {
        if (blinkShare == null) return;
        String label = flipPage == 1
                ? CalendarRaasiLabels.shareRaasi(host)
                : CalendarRaasiLabels.shareDay(host);
        blinkShare.setContentDescription(label);
    }

    private void startBlinkShareAnimation() {
        if (blinkShare == null) return;
        blinkShare.clearAnimation();
        Animation blink = AnimationUtils.loadAnimation(host, R.anim.blink_share);
        blinkShare.startAnimation(blink);
    }

    private void stopBlinkShareAnimation() {
        if (blinkShare == null) return;
        blinkShare.clearAnimation();
        blinkShare.setAlpha(1f);
    }

    private void setBlinkShareVisible(boolean visible) {
        if (blinkShare == null) return;
        // Raasi Phalalu: gold header share only — never overlay the blue blink FAB.
        if (visible && isRaasiPageVisible()) {
            visible = false;
        }
        if (visible) {
            blinkShare.setVisibility(View.VISIBLE);
            startBlinkShareAnimation();
        } else {
            stopBlinkShareAnimation();
            blinkShare.setVisibility(View.GONE);
        }
    }

    /** True when a calendar sheet is covering the content (blink FAB should stay hidden). */
    private boolean isBlinkShareCoveredBySheet() {
        if (sheetScrim != null && sheetScrim.getVisibility() == View.VISIBLE) return true;
        if (raasiDetailScrim != null && raasiDetailScrim.getVisibility() == View.VISIBLE) {
            return true;
        }
        return raasiFormScrim != null && raasiFormScrim.getVisibility() == View.VISIBLE;
    }

    /**
     * Top Panchangam | Raasi Phalalu pill — same auto-hide behaviour as home News|Read
     * ({@link HomeModeChromeConfig#DEFAULT_AUTO_HIDE_SECONDS} = 3).
     * TTD is not part of this toggle (lives under Daily Updates).
     */
    private void wireSegmentToggleChrome() {
        ttdToggleRoot = root.findViewById(R.id.calendar_ttd_toggle_root);
        if (ttdToggleRoot == null) return;

        ttdThumb = ttdToggleRoot.findViewById(R.id.calendar_ttd_thumb);
        ttdSegCalendar = ttdToggleRoot.findViewById(R.id.calendar_seg_calendar);
        ttdSegTtd = ttdToggleRoot.findViewById(R.id.calendar_seg_ttd);
        applySegmentLabels(ttdSegCalendar, ttdSegTtd);

        if (ttdSegCalendar != null) {
            ttdSegCalendar.setOnClickListener(v -> {
                bumpSegmentChromeAutoHide();
                selectPanchangamPage(true);
            });
        }
        if (ttdSegTtd != null) {
            ttdSegTtd.setOnClickListener(v -> {
                bumpSegmentChromeAutoHide();
                selectRaasiPage(true);
            });
        }
        // Consume taps on the scrim so they don't fall through to FlipView.
        ttdToggleRoot.setOnClickListener(v -> bumpSegmentChromeAutoHide());

        installSegmentChromeRevealTargets();

        ttdToggleRoot.post(() -> {
            syncSegmentThumb(isRaasiPageVisible());
            setSegmentChromeVisible(true, false);
        });
    }

    private static void applySegmentLabels(@Nullable TextView left, @Nullable TextView right) {
        if (left != null) {
            left.setText(CalendarRaasiLabels.segPanchangam(left.getContext()));
        }
        if (right != null) {
            right.setText(CalendarRaasiLabels.segRaasiPhalalu(right.getContext()));
        }
    }

    private void installSegmentChromeRevealTargets() {
        // Wired again from wireCalendarPage / wireRaasiPage when FlipView recreates pages.
        // Content taps always re-show Panchangam|Raasi chrome + re-arm ~3s auto-hide
        // (HomePage hosts the pill for embedded calendar; standalone uses local chrome).
        installChromeRevealClick(monthTitle);
        installChromeRevealClick(teluguLine);
        View scope = calendarPageRoot != null ? calendarPageRoot : root;
        if (monthTitle != null && monthTitle.getParent() instanceof View) {
            View titleCol = (View) monthTitle.getParent();
            installChromeRevealClick(titleCol);
            if (titleCol.getParent() instanceof View) {
                installChromeRevealClick((View) titleCol.getParent());
            }
        }
        installChromeRevealClick(scope.findViewById(R.id.telugu_calendar_weekday_row));
        installChromeRevealClick(scope.findViewById(R.id.telugu_calendar_grid));
        installChromeRevealClick(scope.findViewById(R.id.telugu_calendar_pager_host));
        installChromeRevealClick(scope.findViewById(R.id.telugu_calendar_pager));
        installChromeRevealClick(scope.findViewById(R.id.telugu_calendar_body));
        installChromeRevealClick(scope.findViewById(R.id.telugu_calendar_body_stage));
        // Festival strip keeps its own listener (reveal + open sheet) in wireCalendarPage.
        installRaasiChromeRevealTargets();
    }

    /** Raasi flip page: any clear hit target re-shows the same HomePage / local top toggle. */
    private void installRaasiChromeRevealTargets() {
        View raasiScope = raasiScroll != null ? raasiScroll : root;
        installChromeRevealClick(
                raasiScope.findViewById(R.id.telugu_calendar_pick_raasi_title));
        installChromeRevealClick(
                raasiScope.findViewById(R.id.telugu_calendar_raasi_header_divider));
        // Do not click-wire the full raasi_section — NestedScrollView would fight vertical scroll.
        installChromeRevealClick(
                raasiScope.findViewById(R.id.telugu_calendar_raasi_grid));
        installChromeRevealClick(
                raasiScope.findViewById(R.id.telugu_calendar_rashi_info_bar));
        installChromeRevealClick(
                raasiScope.findViewById(R.id.telugu_calendar_raasi_detail));
        installChromeRevealClick(
                raasiScope.findViewById(R.id.telugu_calendar_raasi_title));
        installChromeRevealClick(
                raasiScope.findViewById(R.id.telugu_calendar_rashi_sections));
        // know_raasi keeps reveal+form in wireRaasiPage — do not overwrite here.
    }

    /**
     * Content tap → always reveal Panchangam|Raasi chrome (if hidden) and re-arm
     * auto-hide. Does not hide chrome while the user is browsing calendar content.
     */
    private void installChromeRevealClick(@Nullable View v) {
        if (v == null) return;
        v.setClickable(true);
        v.setOnClickListener(view -> revealSegmentChromeIfHidden());
    }

    /** Re-show segment chrome after auto-hide; if already visible, re-arm the timer. */
    private void revealSegmentChromeIfHidden() {
        if (!applyWindowInsets && host instanceof HomePage) {
            ((HomePage) host).revealHomeCalendarTtdChrome();
            return;
        }
        if (ttdChromeUserHidden) {
            setSegmentChromeVisible(true, true);
        } else {
            bumpSegmentChromeAutoHide();
        }
    }

    private void syncSegmentThumb(boolean raasiSelected) {
        raasiSegmentSelected = raasiSelected;
        if (applyWindowInsets) {
            layoutSegmentThumb(raasiSelected);
        } else if (host instanceof HomePage) {
            ((HomePage) host).syncHomeCalendarSegmentThumb(raasiSelected);
        }
    }

    private void layoutSegmentThumb(boolean raasiSelected) {
        if (ttdThumb == null || ttdSegCalendar == null || ttdSegTtd == null) return;
        final View thumb = ttdThumb;
        final TextView left = ttdSegCalendar;
        final TextView right = ttdSegTtd;
        Runnable place = () -> {
            int leftW = Math.max(left.getWidth(), dp(108));
            int rightW = Math.max(right.getWidth(), dp(108));
            ViewGroup.LayoutParams lp = thumb.getLayoutParams();
            if (lp != null) {
                lp.width = raasiSelected ? rightW : leftW;
                thumb.setLayoutParams(lp);
            }
            thumb.setTranslationX(raasiSelected ? leftW : 0f);
            left.setAlpha(raasiSelected ? 0.75f : 1f);
            right.setAlpha(raasiSelected ? 1f : 0.75f);
        };
        if (left.getWidth() <= 0) {
            left.post(place);
        } else {
            place.run();
        }
    }

    private void setSegmentChromeVisible(boolean visible, boolean animate) {
        if (ttdToggleRoot == null) return;
        ttdChromeUserHidden = !visible;
        if (visible) {
            scheduleSegmentChromeAutoHide(true);
            ttdToggleRoot.setVisibility(View.VISIBLE);
            if (animate) {
                ttdToggleRoot.animate().cancel();
                ttdToggleRoot.setAlpha(0f);
                ttdToggleRoot.setTranslationY(-dp(24));
                ttdToggleRoot.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(280L)
                        .start();
            } else {
                ttdToggleRoot.animate().cancel();
                ttdToggleRoot.setAlpha(1f);
                ttdToggleRoot.setTranslationY(0f);
            }
            layoutSegmentThumb(isRaasiPageVisible());
        } else {
            cancelTtdChromeAutoHide();
            if (animate) {
                ttdToggleRoot.animate().cancel();
                ttdToggleRoot.animate()
                        .alpha(0f)
                        .translationY(-dp(24))
                        .setDuration(280L)
                        .withEndAction(() -> {
                            if (ttdToggleRoot != null && ttdChromeUserHidden) {
                                ttdToggleRoot.setVisibility(View.GONE);
                            }
                        })
                        .start();
            } else {
                ttdToggleRoot.animate().cancel();
                ttdToggleRoot.setVisibility(View.GONE);
            }
        }
    }

    private void scheduleSegmentChromeAutoHide(boolean restartIfPending) {
        if (!restartIfPending && ttdChromeAutoHidePending) return;
        if (ttdChromeUserHidden || ttdToggleRoot == null) return;
        int seconds = HomeModeChromeConfig.getAutoHideSeconds(host);
        if (seconds <= 0) {
            cancelTtdChromeAutoHide();
            return;
        }
        ttdChromeHandler.removeCallbacks(ttdChromeAutoHideRunnable);
        ttdChromeAutoHidePending = true;
        ttdChromeHandler.postDelayed(ttdChromeAutoHideRunnable, seconds * 1000L);
    }

    private void bumpSegmentChromeAutoHide() {
        if (ttdChromeUserHidden) return;
        scheduleSegmentChromeAutoHide(true);
    }

    private void runTtdChromeAutoHide() {
        ttdChromeAutoHidePending = false;
        if (ttdChromeUserHidden) return;
        setSegmentChromeVisible(false, true);
    }

    private void cancelTtdChromeAutoHide() {
        ttdChromeAutoHidePending = false;
        ttdChromeHandler.removeCallbacks(ttdChromeAutoHideRunnable);
    }

    /** Legacy card share icons stay GONE — share is the root blink overlay. */
    private void hideLegacyCardShare(@Nullable ImageView share) {
        if (share == null) return;
        share.clearAnimation();
        share.setOnClickListener(null);
        share.setClickable(false);
        share.setVisibility(View.GONE);
    }

    /**
     * Wire Flip 1 (raasi). Always re-attach listeners — FlipView may recycle the page.
     */
    private void wireRaasiPage() {
        if (raasiGrid == null) return;
        boolean newInstance = wiredRaasiPageInstance != raasiScroll;
        wiredRaasiPageInstance = raasiScroll;
        raasiPageWired = true;

        TextView knowRaasi = null;
        if (raasiScroll != null) {
            knowRaasi = raasiScroll.findViewById(R.id.telugu_calendar_know_raasi);
        }
        if (knowRaasi == null) {
            knowRaasi = root.findViewById(R.id.telugu_calendar_know_raasi);
        }
        if (knowRaasi != null) {
            knowRaasi.setClickable(true);
            knowRaasi.setOnClickListener(v -> {
                revealSegmentChromeIfHidden();
                showRaasiForm();
            });
        }
        if (raasiBack != null) {
            // Calendar flip path: swipe/back to page 0 — hide header back control.
            raasiBack.setVisibility(View.GONE);
            raasiBack.setOnClickListener(null);
        }
        if (raasiHeaderShare != null) {
            raasiHeaderShare.setOnClickListener(v -> {
                revealSegmentChromeIfHidden();
                shareSelectedRaasi();
            });
        }
        // Full inline body — no more→modal flow.
        if (raasiMore != null) {
            raasiMore.setVisibility(View.GONE);
            raasiMore.setOnClickListener(null);
        }
        if (raasiBody != null) {
            raasiBody.setMaxLines(Integer.MAX_VALUE);
            raasiBody.setEllipsize(null);
        }
        if (fullJatakamCta != null) {
            fullJatakamCta.setVisibility(View.GONE);
            fullJatakamCta.setOnClickListener(null);
        }
        hideLegacyCardShare(raasiShare);

        applyRaasiChromeLabels();
        if (newInstance || raasiGrid.getChildCount() == 0) {
            bindRaasiPicker();
            bindRaasiDetail();
        } else {
            // Same instance: still refresh detail text for selected day/rashi.
            bindRaasiDetail();
        }
        // Nakshatra/yoga come from yearPack DayData. Opening Raasi first can wire
        // this page before (or without) calendar page's ensureYearAndBind.
        if (yearPack == null || currentYearLoaded != displayMonth.get(Calendar.YEAR)) {
            ensureYearAndBind();
        }
        updateFlipIndicators(flipPager != null ? flipPager.getCurrentPage() : 0);
        // Raasi page taps must re-show the same top Panchangam|Raasi toggle (~3s).
        installRaasiChromeRevealTargets();
    }

    /** Apply LanguageID chrome (pick / know / lucky / premium labels). Call on every rebind. */
    private void applyRaasiChromeLabels() {
        TextView knowRaasi = root.findViewById(R.id.telugu_calendar_know_raasi);
        if (knowRaasi == null && raasiScroll != null) {
            knowRaasi = raasiScroll.findViewById(R.id.telugu_calendar_know_raasi);
        }
        if (knowRaasi != null) {
            knowRaasi.setText(CalendarRaasiLabels.knowRaasi(host));
        }
        TextView pickTitle = root.findViewById(R.id.telugu_calendar_pick_raasi_title);
        if (pickTitle == null && raasiScroll != null) {
            pickTitle = raasiScroll.findViewById(R.id.telugu_calendar_pick_raasi_title);
        }
        if (pickTitle != null) pickTitle.setText(CalendarRaasiLabels.pickRaasi(host));
        if (luckyHeading != null) {
            luckyHeading.setText(CalendarRaasiLabels.luckyHeading(host));
        }
        if (luckyColorLabel != null) {
            luckyColorLabel.setText(CalendarRaasiLabels.luckyColor(host));
        }
        if (luckyNumbersLabel != null) {
            luckyNumbersLabel.setText(CalendarRaasiLabels.luckyNumbers(host));
        }
        if (luckyDirectionLabel != null) {
            luckyDirectionLabel.setText(CalendarRaasiLabels.luckyDirection(host));
        }
        if (rashiDateLabel != null) {
            rashiDateLabel.setText(CalendarRaasiLabels.dateLabel(host));
        }
        if (rashiNakLabel != null) {
            rashiNakLabel.setText(CalendarRaasiLabels.nakshatraLabel(host));
        }
        if (rashiYogaLabel != null) {
            rashiYogaLabel.setText(CalendarRaasiLabels.yogaLabel(host));
        }
        if (rashiLordLabel != null) {
            rashiLordLabel.setText(CalendarRaasiLabels.lordLabel(host));
        }
        if (rashiElementLabel != null) {
            rashiElementLabel.setText(CalendarRaasiLabels.elementLabel(host));
        }
        if (rashiGemLabel != null) {
            rashiGemLabel.setText(CalendarRaasiLabels.gemLabel(host));
        }
        if (rashiColorLabel != null) {
            rashiColorLabel.setText(CalendarRaasiLabels.colorAttrLabel(host));
        }
        if (rashiShubham != null) {
            rashiShubham.setText(CalendarRaasiLabels.shubhamBhavatu(host));
        }
        if (fullJatakamCta != null) {
            fullJatakamCta.setVisibility(View.GONE);
        }
        if (raasiMore != null) {
            raasiMore.setVisibility(View.GONE);
        }
        if (flipIndicatorRaasi != null) {
            flipIndicatorRaasi.setVisibility(View.GONE);
        }
    }

    private void goToRaasiPage(boolean smooth) {
        if (flipPager == null) return;
        if (smooth) flipPager.smoothFlipTo(1);
        else flipPager.flipTo(1);
    }

    private void updateFlipIndicators(int position) {
        // Page counters ("1 of 2 Pages") intentionally hidden.
        if (flipIndicator != null) flipIndicator.setVisibility(View.GONE);
        if (flipIndicatorRaasi != null) flipIndicatorRaasi.setVisibility(View.GONE);
    }

    public void destroy() {
        cancelTtdChromeAutoHide();
        if (ttdToggleRoot != null) {
            ttdToggleRoot.animate().cancel();
            ttdToggleRoot.setOnClickListener(null);
        }
        if (ttdSegCalendar != null) ttdSegCalendar.setOnClickListener(null);
        if (ttdSegTtd != null) ttdSegTtd.setOnClickListener(null);
        if (swipeBob != null) {
            swipeBob.cancel();
            swipeBob = null;
        }
        stopBlinkShareAnimation();
        if (blinkShare != null) {
            blinkShare.setOnClickListener(null);
        }
        if (raasiFormScrim != null && raasiFormScrim.getVisibility() == View.VISIBLE) {
            hideRaasiForm();
        } else {
            applyRaasiFormSoftInputMode(false);
        }
    }

    private void startSwipeBob() {
        // Retired with swipe-to-rashi cue.
        stopSwipeBob();
    }

    private void stopSwipeBob() {
        if (swipeBob != null) {
            swipeBob.cancel();
            swipeBob = null;
        }
        if (swipeArrow != null) {
            swipeArrow.setTranslationY(0f);
        }
    }

    private int clampSelectedDay() {
        int max = displayMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar today = Calendar.getInstance();
        if (today.get(Calendar.YEAR) == displayMonth.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == displayMonth.get(Calendar.MONTH)) {
            return Math.min(today.get(Calendar.DAY_OF_MONTH), max);
        }
        return Math.min(Math.max(selectedDay, 1), max);
    }

    private void fillWeekdayHeaders() {
        LinearLayout row = null;
        if (calendarPageRoot != null) {
            row = calendarPageRoot.findViewById(R.id.telugu_calendar_weekday_row);
        }
        if (row == null) {
            row = root.findViewById(R.id.telugu_calendar_weekday_row);
        }
        if (row == null) return;
        row.removeAllViews();
        for (int i = 0; i < 7; i++) {
            TextView tv = new TextView(host);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tv.setLayoutParams(lp);
            tv.setGravity(Gravity.CENTER);
            tv.setText(CalendarRaasiLabels.weekdayShorts(host)[i]);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setTextColor(i == 0 ? COLOR_RED : COLOR_MUTED);
            tv.setPadding(0, dp(2), 0, dp(2));
            row.addView(tv);
        }
    }

    private void ensureYearAndBind() {
        int year = displayMonth.get(Calendar.YEAR);
        if (yearPack != null && currentYearLoaded == year) {
            bindAll();
            revealCalendarContent(false);
            return;
        }
        showCalendarLoading();
        final int reqYear = year;
        PanchangRepository.loadYear(host, reqYear, (pack, fromCache) -> {
            if (host.isFinishing() || host.isDestroyed()) return;
            if (pack == null || pack.year != reqYear) return;
            // Ignore stale soft-fetch if user already moved to another year.
            if (displayMonth.get(Calendar.YEAR) != reqYear) return;
            yearPack = pack;
            currentYearLoaded = pack.year;
            bindAll();
            resizeDetailPagerToContent();
            revealCalendarContent(!calendarContentRevealed);
        });
    }

    /** Hide body + run News-style skeleton shimmer until year pack is ready. */
    private void showCalendarLoading() {
        setBlinkShareVisible(false);
        if (calendarBody != null) {
            calendarBody.animate().cancel();
            calendarBody.setVisibility(View.INVISIBLE);
            calendarBody.setAlpha(1f);
            calendarBody.setTranslationX(0f);
        }
        if (swipeCue != null) {
            swipeCue.setVisibility(View.INVISIBLE);
        }
        if (loadReveal != null) {
            loadReveal.start();
        }
    }

    /**
     * Show grid + cards + holiday together. Optional L→R slide on first paint / refresh.
     */
    private void revealCalendarContent(boolean animateLtr) {
        if (loadReveal != null) {
            loadReveal.stop(!animateLtr, null);
        }
        if (calendarBody != null) {
            calendarBody.animate().cancel();
            calendarBody.setVisibility(View.VISIBLE);
            if (animateLtr) {
                calendarBody.setAlpha(0f);
                calendarBody.post(() -> {
                    if (calendarBody == null) return;
                    int w = calendarBody.getWidth();
                    calendarBody.setTranslationX(w > 0 ? -Math.min(w, dp(120)) : -dp(80));
                    calendarBody.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(320L)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                });
            } else {
                calendarBody.setAlpha(1f);
                calendarBody.setTranslationX(0f);
            }
        }
        if (swipeCue != null) {
            swipeCue.setVisibility(View.GONE);
        }
        setBlinkShareVisible(true);
        calendarContentRevealed = true;
    }

    private void bindAll() {
        bindMonthHeader();
        bindGrid();
        bindDetailCards();
        bindFestivalStrip();
        // Year pack may arrive after the first Raasi bind (Daily Updates → Rashi
        // opens page 1 while loadYear is still in flight). Without this, the info
        // bar keeps "—" for nakshatra until the user taps another rashi.
        if (raasiDetail != null) {
            bindRaasiDetail();
        }
    }

    private void bindMonthHeader() {
        int m0 = displayMonth.get(Calendar.MONTH);
        int year = displayMonth.get(Calendar.YEAR);
        if (monthTitle != null) {
            monthTitle.setText(CalendarRaasiLabels.monthName(host, m0) + " " + year);
            monthTitle.setTextColor(themeAccent != 0 ? themeAccent : 0xFFEF4923);
        }
        if (teluguLine != null) {
            String header = yearPack != null ? yearPack.monthHeader(m0 + 1) : null;
            if (header == null && yearPack != null) {
                PanchangModels.DayData d = yearPack.day(m0 + 1, Math.min(15, selectedDay));
                if (d != null) header = d.teluguHeader;
            }
            teluguLine.setText(header != null ? header : "");
        }
    }

    private void bindGrid() {
        if (grid == null) return;
        Calendar cursor = (Calendar) displayMonth.clone();
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        int firstDow = cursor.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cursor.getActualMaximum(Calendar.DAY_OF_MONTH);
        int leadingEmpty = firstDow - Calendar.SUNDAY;
        int totalCells = leadingEmpty + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);
        int totalSlots = Math.max(rows * 7, 35);

        int cellH = computeMonthGridCellHeight(rows);
        fillGridCells(leadingEmpty, daysInMonth, totalSlots, cellH, rows);
        // Pager uses layout_weight; re-measure after grid height settles.
        resizeDetailPagerToContent();
    }

    /**
     * Size day cells so header + weekday + grid ≈ month height fraction
     * of the page content height, leaving room for taller detail cards + holiday strip.
     */
    private int computeMonthGridCellHeight(int rows) {
        int rowCount = Math.max(rows, 5);
        int screenH = host.getResources().getDisplayMetrics().heightPixels;
        int pageH = 0;
        View heightAnchor = calendarBody != null ? calendarBody : calendarPageRoot;
        if (heightAnchor != null) {
            pageH = heightAnchor.getHeight();
            if (pageH <= 0 && calendarPageRoot != null) {
                pageH = calendarPageRoot.getHeight();
            }
            if (pageH <= 0 && !pendingMonthGridRelayout) {
                pendingMonthGridRelayout = true;
                View postTarget = heightAnchor;
                postTarget.post(() -> {
                    pendingMonthGridRelayout = false;
                    if (grid != null && yearPack != null) bindGrid();
                });
            }
        }
        // Body already sits in weighted stage — use its height directly when known.
        int contentH = pageH > dp(180)
                ? Math.max(pageH, dp(320))
                : Math.max((int) (screenH * 0.55f), dp(320));

        int festivalH = dp(56);
        int swipeH = dp(16); // former swipe-to-rashi cue removed
        int minPagerH = dp(CalendarUiConfigHelper.getCardMinHeightDp(host));
        float monthFraction = CalendarUiConfigHelper.getMonthHeightFraction(host);
        int monthTarget = Math.round(contentH * monthFraction);
        int gridBudget = monthTarget;

        // Never starve the card carousel / holiday strip.
        int maxGridForRest = contentH - festivalH - swipeH - minPagerH;
        if (maxGridForRest > 0) {
            gridBudget = Math.min(gridBudget, maxGridForRest);
        }
        int cellH = gridBudget / rowCount;
        return Math.max(dp(36), Math.min(dp(52), cellH));
    }

    /** Month title row + weekday labels height (measured when laid out, else estimate). */
    private int measureMonthHeaderBlock() {
        int h = 0;
        if (monthTitle != null) {
            View titleRow = monthTitle.getParent() instanceof View
                    ? (View) monthTitle.getParent() : null;
            if (titleRow != null && titleRow.getParent() instanceof View) {
                View headerRow = (View) titleRow.getParent();
                if (headerRow.getHeight() > 0) {
                    h += headerRow.getHeight();
                }
            }
        }
        View weekday = root.findViewById(R.id.telugu_calendar_weekday_row);
        if (weekday != null && weekday.getHeight() > 0) {
            h += weekday.getHeight();
        }
        if (h > dp(40)) return h;
        return dp(70);
    }

    private void fillGridCells(int leadingEmpty, int daysInMonth, int totalSlots,
                               int cellH, int rows) {
        if (grid == null) return;
        grid.removeAllViews();
        int month1 = displayMonth.get(Calendar.MONTH) + 1;
        ViewGroup.LayoutParams glp = grid.getLayoutParams();
        if (glp != null) {
            glp.height = cellH * rows;
            grid.setLayoutParams(glp);
        }

        for (int i = 0; i < totalSlots; i++) {
            int dayNum = i - leadingEmpty + 1;
            FrameLayout cell = new FrameLayout(host);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = cellH;
            lp.columnSpec = GridLayout.spec(i % 7, 1f);
            lp.rowSpec = GridLayout.spec(i / 7);
            lp.setMargins(0, 0, 0, 0);
            cell.setLayoutParams(lp);
            cell.setBackgroundResource(R.drawable.bg_calendar_cell);

            if (dayNum < 1 || dayNum > daysInMonth) {
                cell.setOnClickListener(v -> revealSegmentChromeIfHidden());
                grid.addView(cell);
                continue;
            }

            boolean sunday = (i % 7) == 0;
            boolean selected = dayNum == selectedDay;
            PanchangModels.DayData data =
                    yearPack != null ? yearPack.day(month1, dayNum) : null;
            boolean festival = data != null && data.hasFestivalHighlight();
            String iconKey = data != null ? data.primaryIcon() : null;

            TextView num = new TextView(host);
            num.setText(String.valueOf(dayNum));
            num.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            num.setTypeface(Typeface.DEFAULT_BOLD);
            num.setGravity(Gravity.CENTER);
            num.setIncludeFontPadding(false);
            int numColor = COLOR_WHITE;
            if (selected) {
                numColor = COLOR_BLUE;
            } else if (festival || iconKey != null) {
                numColor = COLOR_GREEN;
            } else if (sunday) {
                numColor = COLOR_RED;
            }
            num.setTextColor(numColor);

            // Perfect geometric circle around the date digits only (not cell-sized oval).
            // Square ring view + number centered inside → oval drawable stays circular.
            int circlePx = CalendarUiConfigHelper.getSelectedCircleDiameterPx(host);
            FrameLayout numWrap = new FrameLayout(host);
            FrameLayout.LayoutParams wrapLp = new FrameLayout.LayoutParams(circlePx, circlePx);
            wrapLp.gravity = Gravity.TOP | Gravity.START;
            wrapLp.leftMargin = dp(2);
            wrapLp.topMargin = dp(2);
            numWrap.setLayoutParams(wrapLp);
            if (selected) {
                numWrap.setBackgroundResource(R.drawable.bg_calendar_day_selected);
            }
            FrameLayout.LayoutParams nlp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            nlp.gravity = Gravity.CENTER;
            num.setLayoutParams(nlp);
            numWrap.addView(num);
            cell.addView(numWrap);

            if (iconKey != null) {
                ImageView icon = new ImageView(host);
                FrameLayout.LayoutParams ilp = new FrameLayout.LayoutParams(dp(15), dp(15));
                ilp.gravity = Gravity.TOP | Gravity.END;
                ilp.topMargin = dp(4);
                ilp.rightMargin = dp(3);
                icon.setLayoutParams(ilp);
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                icon.setImageResource(iconRes(iconKey));
                icon.setContentDescription(iconKey);
                cell.addView(icon);
            }

            int lineColor = selected ? COLOR_BLUE
                    : ((festival || iconKey != null) ? COLOR_GREEN
                    : (sunday ? COLOR_RED : COLOR_CELL_LINE));
            List<String> lines = data != null ? data.safeCellLines() : new ArrayList<>();
            LinearLayout linesCol = new LinearLayout(host);
            linesCol.setOrientation(LinearLayout.VERTICAL);
            FrameLayout.LayoutParams linesLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linesLp.gravity = Gravity.BOTTOM;
            linesLp.leftMargin = dp(2);
            linesLp.rightMargin = dp(2);
            linesLp.bottomMargin = dp(2);
            linesCol.setLayoutParams(linesLp);
            int lineCount = 0;
            for (String line : lines) {
                if (line == null || line.isEmpty()) continue;
                TextView tv = new TextView(host);
                tv.setText(line);
                tv.setTextColor(lineColor);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f);
                tv.setMaxLines(1);
                tv.setEllipsize(TextUtils.TruncateAt.END);
                linesCol.addView(tv);
                if (++lineCount >= 2) break;
            }
            cell.addView(linesCol);

            final int dayClick = dayNum;
            cell.setOnClickListener(v -> {
                selectedDay = dayClick;
                bindGrid();
                bindDetailCards();
                bindFestivalStrip();
                revealSegmentChromeIfHidden();
            });

            grid.addView(cell);
        }
    }

    private void bindDetailCards() {
        if (detailAdapter == null) return;
        PanchangModels.DayData day = selectedDayData();
        detailAdapter.setDay(day);
        if (pager != null) {
            pager.setAdapter(detailAdapter);
            pager.setCurrentItem(0, false);
            resizeDetailPagerToContent();
        }
        updateDetailPageIndicator(0);
    }

    @Nullable
    private PanchangModels.DayData selectedDayData() {
        if (yearPack == null) return null;
        return yearPack.day(displayMonth.get(Calendar.MONTH) + 1, selectedDay);
    }

    private void updateDetailPageIndicator(int position) {
        // Card pagination ("1 / 4") intentionally hidden.
        if (detailPageIndicator != null) {
            detailPageIndicator.setVisibility(View.GONE);
        }
    }

    private void bindFestivalStrip() {
        if (festivalStrip == null || festivalText == null) return;
        if (yearPack == null) {
            festivalStrip.setVisibility(View.GONE);
            return;
        }
        List<PanchangModels.Festival> fests =
                yearPack.namedFestivalsForMonth(displayMonth.get(Calendar.MONTH) + 1);
        if (fests.isEmpty()) {
            fests = yearPack.festivalsForMonth(displayMonth.get(Calendar.MONTH) + 1);
        }
        int accent = themeAccent != 0 ? themeAccent : COLOR_ORANGE;
        int month0 = displayMonth.get(Calendar.MONTH);
        if (fests.isEmpty()) {
            // Fallback: Telugu month header with theme colors (never leave empty flag strip).
            String header = yearPack.monthHeader(month0 + 1);
            if (TextUtils.isEmpty(header)) {
                PanchangModels.DayData d = yearPack.day(month0 + 1, Math.min(15, selectedDay));
                if (d != null) header = d.teluguHeader;
            }
            if (TextUtils.isEmpty(header)) {
                festivalStrip.setVisibility(View.GONE);
                return;
            }
            festivalStrip.setVisibility(View.VISIBLE);
            if (festivalIcon != null) {
                festivalIcon.setImageResource(R.drawable.ic_calendar_flag);
            }
            SpannableStringBuilder sb = new SpannableStringBuilder(header.trim());
            sb.setSpan(new ForegroundColorSpan(accent), 0, sb.length(),
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            festivalText.setText(sb);
            return;
        }
        PanchangModels.Festival pick = fests.get(0);
        for (PanchangModels.Festival f : fests) {
            if (f != null && f.day >= selectedDay) {
                pick = f;
                break;
            }
        }
        for (PanchangModels.Festival f : fests) {
            if (f != null && "flag".equals(f.icon) && f.day >= selectedDay) {
                pick = f;
                break;
            }
        }
        festivalStrip.setVisibility(View.VISIBLE);
        if (festivalIcon != null) {
            festivalIcon.setImageResource(iconRes(pick.icon));
        }
        String datePart;
        if (!TextUtils.isEmpty(pick.dateDisplayTe)
                && languageId() == 3) {
            datePart = pick.dateDisplayTe.trim();
        } else {
            datePart = CalendarRaasiLabels.monthName(host, month0) + " " + pick.day;
        }
        String namePart = pick.name != null ? pick.name.trim() : "";
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int dateStart = sb.length();
        sb.append(datePart);
        sb.setSpan(new ForegroundColorSpan(accent), dateStart, sb.length(),
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (!namePart.isEmpty()) {
            sb.append(" - ");
            int nameStart = sb.length();
            sb.append(namePart);
            sb.setSpan(new ForegroundColorSpan(COLOR_GREEN), nameStart, sb.length(),
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        festivalText.setText(sb);
    }

    private int languageId() {
        try {
            return SharedPrefsData.getInt(host, Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
            return 0;
        }
    }

    @NonNull
    private String formatDetailCardDate(@Nullable PanchangModels.DayData d) {
        if (d == null) return "";
        int y = d.year > 0 ? d.year : displayMonth.get(Calendar.YEAR);
        int m0 = d.month >= 1 && d.month <= 12
                ? d.month - 1
                : displayMonth.get(Calendar.MONTH);
        int dayNum = d.day > 0 ? d.day : selectedDay;
        if (languageId() == 3) {
            if (!TextUtils.isEmpty(d.dateDisplayTe)) {
                String te = d.dateDisplayTe.trim();
                if (te.contains(String.valueOf(y))) return te;
                return te + ", " + y;
            }
            return CalendarRaasiLabels.monthName(host, m0) + " " + dayNum + ", " + y;
        }
        if (!TextUtils.isEmpty(d.dateDisplayEn) && languageId() == 0) {
            return d.dateDisplayEn;
        }
        return CalendarRaasiLabels.monthName(host, m0) + " " + dayNum + ", " + y;
    }

    private void showFestivalsSheet() {
        if (sheetScrim == null || festivalList == null || yearPack == null) return;
        festivalList.removeAllViews();
        List<PanchangModels.Festival> fests =
                yearPack.namedFestivalsForMonth(displayMonth.get(Calendar.MONTH) + 1);
        if (fests.isEmpty()) {
            fests = yearPack.festivalsForMonth(displayMonth.get(Calendar.MONTH) + 1);
        }
        LayoutInflater inflater = LayoutInflater.from(host);
        for (PanchangModels.Festival f : fests) {
            if (f == null) continue;
            View row = inflater.inflate(R.layout.item_calendar_festival_row, festivalList, false);
            ImageView icon = row.findViewById(R.id.festival_row_icon);
            TextView date = row.findViewById(R.id.festival_row_date);
            TextView name = row.findViewById(R.id.festival_row_name);
            if (icon != null) icon.setImageResource(iconRes(f.icon));
            if (date != null) {
                date.setText(f.dateDisplayTe != null
                        ? f.dateDisplayTe
                        : (EN_MONTHS[Math.max(0, f.month - 1)] + " " + f.day));
            }
            if (name != null) name.setText(f.name);
            final int day = f.day;
            row.setOnClickListener(v -> {
                selectedDay = day;
                hideFestivalsSheet();
                bindAll();
            });
            festivalList.addView(row);
        }
        if (fests.isEmpty()) {
            TextView empty = new TextView(host);
            empty.setText(CalendarRaasiLabels.noFestivals(host));
            empty.setTextColor(0xFF666666);
            empty.setPadding(dp(8), dp(16), dp(8), dp(16));
            empty.setGravity(Gravity.CENTER);
            festivalList.addView(empty);
        }
        sheetScrim.setVisibility(View.VISIBLE);
        setBlinkShareVisible(false);
    }

    private void hideFestivalsSheet() {
        if (sheetScrim != null) sheetScrim.setVisibility(View.GONE);
        setBlinkShareVisible(true);
    }

    //region Raasi
    private void bindRaasiPicker() {
        if (raasiGrid == null) return;
        raasiGrid.removeAllViews();
        int gap = dp(4);
        for (int i = 0; i < RaasiContent.COUNT; i++) {
            final int id = i;
            boolean sel = id == selectedRaasi;
            LinearLayout tile = new LinearLayout(host);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER_HORIZONTAL);
            tile.setPadding(dp(2), dp(4), dp(2), dp(4));
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(i % 4, 1f);
            lp.rowSpec = GridLayout.spec(i / 4);
            lp.setMargins(gap, gap, gap, gap);
            tile.setLayoutParams(lp);

            FrameLayout top = new FrameLayout(host);
            LinearLayout.LayoutParams topLp = new LinearLayout.LayoutParams(dp(64), dp(64));
            topLp.gravity = Gravity.CENTER_HORIZONTAL;
            top.setLayoutParams(topLp);
            if (sel) {
                top.setBackgroundResource(R.drawable.bg_rashi_tile_selected_glow);
            }

            ImageView icon = new ImageView(host);
            FrameLayout.LayoutParams ilp = new FrameLayout.LayoutParams(dp(52), dp(52));
            ilp.gravity = Gravity.CENTER;
            icon.setLayoutParams(ilp);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int resId = RaasiContent.iconResId(host, i);
            if (resId != 0) {
                icon.setImageResource(resId);
                top.addView(icon);
            } else {
                TextView glyph = new TextView(host);
                glyph.setText(RaasiContent.GLYPHS[i]);
                glyph.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                glyph.setTextColor(COLOR_GOLD);
                glyph.setGravity(Gravity.CENTER);
                glyph.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                top.addView(glyph);
            }
            tile.addView(top);

            TextView name = new TextView(host);
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            nlp.topMargin = dp(2);
            name.setLayoutParams(nlp);
            name.setText(CalendarRaasiLabels.rashiName(host, i));
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            name.setGravity(Gravity.CENTER);
            name.setMaxLines(1);
            name.setEllipsize(TextUtils.TruncateAt.END);
            name.setTextColor(COLOR_GOLD);
            if (sel) name.setTypeface(Typeface.DEFAULT_BOLD);
            tile.addView(name);

            tile.setOnClickListener(v -> {
                revealSegmentChromeIfHidden();
                selectedRaasi = id;
                persistSelectedRaasi(id);
                bindRaasiPicker();
                bindRaasiDetail();
            });
            raasiGrid.addView(tile);
        }
    }

    private void bindRaasiDetail() {
        if (raasiDetail == null) return;
        selectedRaasi = RaasiContent.clampId(selectedRaasi);
        RaasiContent.Daily daily = RaasiContent.forToday(host, selectedRaasi);
        raasiDetail.setVisibility(View.VISIBLE);
        applyRaasiChromeLabels();
        bindRashiInfoBar();
        if (raasiSignLine != null) {
            raasiSignLine.setVisibility(View.GONE);
        }
        if (raasiTitle != null) {
            raasiTitle.setText(CalendarRaasiLabels.phalaluShortTitle(host, selectedRaasi));
        }
        if (raasiDetailIcon != null) {
            int icon = RaasiContent.iconResIdLarge(host, selectedRaasi);
            if (icon != 0) {
                raasiDetailIcon.setImageResource(icon);
                raasiDetailIcon.setVisibility(View.VISIBLE);
            } else {
                raasiDetailIcon.setVisibility(View.GONE);
            }
        }
        if (rashiLordValue != null) {
            rashiLordValue.setText(RaasiContent.lordName(selectedRaasi));
        }
        if (rashiElementValue != null) {
            rashiElementValue.setText(RaasiContent.elementName(selectedRaasi));
        }
        if (rashiGemValue != null) {
            rashiGemValue.setText(RaasiContent.gemName(selectedRaasi));
        }
        if (rashiColorValue != null) {
            rashiColorValue.setText(daily.color != null ? daily.color : "");
        }
        bindRashiSections(daily);
        if (raasiBody != null) {
            raasiBody.setMaxLines(Integer.MAX_VALUE);
            raasiBody.setEllipsize(null);
            raasiBody.setSingleLine(false);
            raasiBody.setText(daily.body != null ? daily.body : "");
            raasiBody.setVisibility(View.GONE);
        }
        if (raasiMore != null) {
            raasiMore.setVisibility(View.GONE);
            raasiMore.setOnClickListener(null);
        }
        if (fullJatakamCta != null) {
            fullJatakamCta.setVisibility(View.GONE);
            fullJatakamCta.setOnClickListener(null);
        }
        if (luckyColor != null) luckyColor.setText(daily.color);
        if (luckyNumbers != null) luckyNumbers.setText(daily.numbers);
        if (luckyDirection != null) luckyDirection.setText(daily.direction);
    }

    private void bindRashiInfoBar() {
        Calendar cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
        // Prefer selected calendar day when pack is available.
        int y = displayMonth.get(Calendar.YEAR);
        int m0 = displayMonth.get(Calendar.MONTH);
        int d = selectedDay > 0 ? selectedDay : cal.get(Calendar.DAY_OF_MONTH);
        if (rashiDateValue != null) {
            String weekday = weekdayTe(calForSelectedDay());
            String dateTe = formatRashiInfoDate(y, m0, d, weekday);
            rashiDateValue.setText(dateTe);
        }
        PanchangModels.DayData day = selectedDayData();
        if (rashiNakValue != null) {
            String nak = "—";
            if (day != null && day.nakshatra != null && day.nakshatra.name != null) {
                nak = day.nakshatra.name;
            }
            rashiNakValue.setText(nak);
        }
        if (rashiYogaValue != null) {
            boolean shubha = day != null && day.safeShubha() != null && !day.safeShubha().isEmpty();
            rashiYogaValue.setText(shubha
                    ? CalendarRaasiLabels.shubhaYogam(host)
                    : CalendarRaasiLabels.mishraYogam(host));
        }
    }

    @NonNull
    private Calendar calForSelectedDay() {
        Calendar c = (Calendar) displayMonth.clone();
        c.set(Calendar.DAY_OF_MONTH, Math.max(1, selectedDay));
        return c;
    }

    @NonNull
    private String formatRashiInfoDate(int y, int m0, int d, @Nullable String weekday) {
        String[] teMonths = {
                "జనవరి", "ఫిబ్రవరి", "మార్చి", "ఏప్రిల్", "మే", "జూన్",
                "జూలై", "ఆగష్టు", "సెప్టెంబర్", "అక్టోబర్", "నవంబర్", "డిసెంబర్"
        };
        String month = (m0 >= 0 && m0 < 12) ? teMonths[m0] : EN_MONTHS[Math.max(0, Math.min(11, m0))];
        String base = d + " " + month + " " + y;
        if (weekday != null && !weekday.isEmpty()) {
            return base + " " + weekday;
        }
        return base;
    }

    @NonNull
    private static String weekdayTe(@NonNull Calendar c) {
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY: return "ఆదివారం";
            case Calendar.MONDAY: return "సోమవారం";
            case Calendar.TUESDAY: return "మంగళవారం";
            case Calendar.WEDNESDAY: return "బుధవారం";
            case Calendar.THURSDAY: return "గురువారం";
            case Calendar.FRIDAY: return "శుక్రవారం";
            case Calendar.SATURDAY: return "శనివారం";
            default: return "";
        }
    }

    /**
     * Builds detail sections from the curated 3-paragraph body + luck fields.
     * Never reuses the same paragraph under two headings; skips empty / duplicate
     * sections; does not invent Career/Love copy by falling back to another paragraph.
     */
    private void bindRashiSections(@NonNull RaasiContent.Daily daily) {
        if (raasiSections == null) return;
        raasiSections.removeAllViews();
        java.util.LinkedHashSet<String> seenBodies = new java.util.LinkedHashSet<>();

        String body = daily.body != null ? daily.body : "";
        String[] paras = body.split("\n\n");
        String p0 = paras.length > 0 ? paras[0].trim() : "";
        String p1 = paras.length > 1 ? paras[1].trim() : "";
        String p2 = paras.length > 2 ? paras[2].trim() : "";
        String numbers = daily.numbers != null ? daily.numbers.trim() : "";
        String color = daily.color != null ? daily.color.trim() : "";
        String dir = daily.direction != null ? daily.direction.trim() : "";

        String generalBody = p0;
        String careerBody = "";
        String loveBody = "";
        if (!TextUtils.isEmpty(p1) && !p1.equals(p0)) {
            if (rashiParagraphLooksLikeLove(p1)) {
                loveBody = p1;
            } else if (rashiParagraphLooksLikeCareer(p1)) {
                careerBody = p1;
            } else {
                // Keep mid text once under General — avoid wrong Career/Love labels.
                generalBody = TextUtils.isEmpty(generalBody) ? p1 : (generalBody + "\n\n" + p1);
            }
        }

        addRashiSectionRowUnique(
                CalendarRaasiLabels.sectionGeneral(host),
                R.drawable.ic_rashi_sec_general,
                generalBody,
                seenBodies);

        // Finance only when lucky numbers exist (distinct field — no paragraph reuse).
        if (!TextUtils.isEmpty(numbers)) {
            addRashiSectionRowUnique(
                    CalendarRaasiLabels.sectionFinance(host),
                    R.drawable.ic_rashi_sec_finance,
                    "ఈరోజు అదృష్ట సంఖ్యలు " + numbers
                            + ". ఖర్చుల్లో జాగ్రత్త పాటించండి; అవసరమైన పనులకే డబ్బు కేటాయించండి.",
                    seenBodies);
        }

        addRashiSectionRowUnique(
                CalendarRaasiLabels.sectionCareer(host),
                R.drawable.ic_rashi_sec_career,
                careerBody,
                seenBodies);

        // Health only when lucky direction exists.
        if (!TextUtils.isEmpty(dir)) {
            addRashiSectionRowUnique(
                    CalendarRaasiLabels.sectionHealth(host),
                    R.drawable.ic_rashi_sec_health,
                    "ఆరోగ్యం కోసం తేలిక వ్యాయామం, నీరు, సమయానికి విశ్రాంతి అవసరం. అదృష్ట దిక్కు: "
                            + dir + ".",
                    seenBodies);
        }

        addRashiSectionRowUnique(
                CalendarRaasiLabels.sectionLove(host),
                R.drawable.ic_rashi_sec_love,
                loveBody,
                seenBodies);

        // Remedy: third paragraph if present; else color tip when color field exists.
        if (!TextUtils.isEmpty(p2) && !p2.equals(p0) && !p2.equals(p1)) {
            addRashiSectionRowUnique(
                    CalendarRaasiLabels.sectionRemedy(host),
                    R.drawable.ic_rashi_sec_remedy,
                    p2,
                    seenBodies);
        } else if (!TextUtils.isEmpty(color)) {
            addRashiSectionRowUnique(
                    CalendarRaasiLabels.sectionRemedy(host),
                    R.drawable.ic_rashi_sec_remedy,
                    "శుభ రంగు " + color
                            + "ను ధరించండి; నిశ్శబ్ద ప్రార్థన లేదా కృతజ్ఞతతో రోజును ముగించండి.",
                    seenBodies);
        }
    }

    /** Love / relationships / social-gathering cues across curated template languages. */
    private static boolean rashiParagraphLooksLikeLove(@NonNull String text) {
        String t = text.toLowerCase(Locale.ROOT);
        return containsAny(t,
                "ప్రేమ", "స్నేహ", "సంబంధ", "కలయిక", "సామాజిక", "గాసిప్",
                "प्रेम", "मित्र", "संबंध",
                "காதல்", "நட்ப்", "உறவு",
                "ಪ್ರೀತಿ", "ಸ್ನೇಹ", "ಸಂಬಂಧ",
                "പ്രണയ", "സൗഹൃദ", "ബന്ധ",
                "ପ୍ରେମ", "ବନ୍ଧୁ",
                "love", "friendship", "relationship", "gathering", "social", "gossip");
    }

    /** Career / work / business cues across curated template languages. */
    private static boolean rashiParagraphLooksLikeCareer(@NonNull String text) {
        String t = text.toLowerCase(Locale.ROOT);
        return containsAny(t,
                "పని", "వ్యాపార", "ప్రాజెక్ట్", "ఒప్పంద", "అప్లికేషన్", "కాల్స్", "సృజనాత్మక పని",
                "काम", "व्यवसाय", "प्रोजेक्ट", "अनुबंध",
                "வேலை", "வியாபார", "திட்டம்", "ஒப்பந்த",
                "ಕೆಲಸ", "ವ್ಯಾಪಾರ", "ಯೋಜನೆ", "ಒಪ್ಪಂದ",
                "ജോലി", "ബിസിനസ്", "പ്രോജക്ട്", "കരാർ",
                "କାମ", "ବ୍ୟବସାୟ", "ପ୍ରକଳ୍ପ", "ଚୁକ୍ତି",
                "work", "career", "business", "project", "contract", "application", "office");
    }

    private static boolean containsAny(@NonNull String haystack, @NonNull String... needles) {
        for (String n : needles) {
            if (n != null && !n.isEmpty() && haystack.contains(n.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void addRashiSectionRowUnique(
            @NonNull String title,
            int iconRes,
            @Nullable String body,
            @NonNull java.util.Set<String> seenBodies) {
        if (TextUtils.isEmpty(body)) return;
        String key = body.trim();
        if (!seenBodies.add(key)) return;
        addRashiSectionRow(title, iconRes, key);
    }

    private void addRashiSectionRow(@NonNull String title, int iconRes, @NonNull String body) {
        if (raasiSections == null) return;
        if (TextUtils.isEmpty(body)) return;
        LinearLayout row = new LinearLayout(host);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(8);
        row.setLayoutParams(rowLp);

        FrameLayout iconWrap = new FrameLayout(host);
        LinearLayout.LayoutParams iwLp = new LinearLayout.LayoutParams(dp(32), dp(32));
        iwLp.topMargin = dp(2);
        iconWrap.setLayoutParams(iwLp);
        iconWrap.setBackgroundResource(R.drawable.bg_rashi_section_icon);
        ImageView iv = new ImageView(host);
        FrameLayout.LayoutParams ivLp = new FrameLayout.LayoutParams(dp(18), dp(18));
        ivLp.gravity = Gravity.CENTER;
        iv.setLayoutParams(ivLp);
        iv.setImageResource(iconRes);
        iconWrap.addView(iv);
        row.addView(iconWrap);

        LinearLayout textCol = new LinearLayout(host);
        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tcLp.leftMargin = dp(10);
        textCol.setLayoutParams(tcLp);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView titleTv = new TextView(host);
        titleTv.setText(title);
        titleTv.setTextColor(COLOR_GOLD);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(titleTv);

        TextView bodyTv = new TextView(host);
        bodyTv.setText(body);
        bodyTv.setTextColor(0xFFE8E0D0);
        bodyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        bodyTv.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bLp.topMargin = dp(2);
        bodyTv.setLayoutParams(bLp);
        textCol.addView(bodyTv);

        row.addView(textCol);
        raasiSections.addView(row);
    }

    private void showRaasiDetailSheet() {
        if (raasiDetailScrim == null) return;
        selectedRaasi = RaasiContent.clampId(selectedRaasi);
        RaasiContent.Daily daily = RaasiContent.forToday(host, selectedRaasi);
        Calendar today = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
        int y = today.get(Calendar.YEAR);
        int m0 = today.get(Calendar.MONTH);
        int d = today.get(Calendar.DAY_OF_MONTH);
        String dateDisp = String.format(Locale.US, "%02d %s %d", d, EN_MONTHS[m0], y);

        if (raasiDetailSignTitle != null) {
            raasiDetailSignTitle.setText(CalendarRaasiLabels.signLineWithEnglish(host, selectedRaasi));
        }
        if (raasiDetailPhalaluTitle != null) {
            raasiDetailPhalaluTitle.setText(
                    CalendarRaasiLabels.phalaluTitle(host, selectedRaasi, dateDisp));
        }
        if (raasiDetailBody != null) {
            raasiDetailBody.setText(daily.body != null ? daily.body : "");
        }
        if (raasiDetailLuckyHeading != null) {
            raasiDetailLuckyHeading.setText(CalendarRaasiLabels.luckyHeading(host));
        }
        String colorLabel = CalendarRaasiLabels.luckyColor(host);
        String numLabel = CalendarRaasiLabels.luckyNumbers(host);
        String dirLabel = CalendarRaasiLabels.luckyDirection(host);
        if (raasiDetailLuckyLine1 != null) {
            raasiDetailLuckyLine1.setText(
                    colorLabel + ": " + nullToEmpty(daily.color)
                            + "; " + numLabel + ": " + nullToEmpty(daily.numbers));
        }
        if (raasiDetailLuckyLine2 != null) {
            raasiDetailLuckyLine2.setText(dirLabel + ": " + nullToEmpty(daily.direction));
        }
        if (raasiDetailFullJatakam != null) {
            raasiDetailFullJatakam.setVisibility(View.GONE);
            raasiDetailFullJatakam.setOnClickListener(null);
        }
        raasiDetailScrim.setVisibility(View.VISIBLE);
        setBlinkShareVisible(false);
    }

    private void hideRaasiDetailSheet() {
        if (raasiDetailScrim != null) raasiDetailScrim.setVisibility(View.GONE);
        if (raasiFormScrim == null || raasiFormScrim.getVisibility() != View.VISIBLE) {
            setBlinkShareVisible(true);
        }
    }

    private static String nullToEmpty(@Nullable String s) {
        return s != null ? s : "";
    }

    private void showRaasiForm() {
        if (raasiFormScrim == null) return;
        hideRaasiDetailSheet();
        String name = SharedPrefsData.getString(host, PREF_RAASI_NAME, Constants.PREF_NAME);
        String place = SharedPrefsData.getString(host, PREF_RAASI_POB, Constants.PREF_NAME);
        String dob = SharedPrefsData.getString(host, PREF_RAASI_DOB, Constants.PREF_NAME);
        if (raasiFormName != null && name != null) raasiFormName.setText(name);
        if (raasiFormPlace != null && place != null) raasiFormPlace.setText(place);
        if (raasiFormDob != null && !TextUtils.isEmpty(dob)) raasiFormDob.setText(dob);
        applyRaasiFormSoftInputMode(true);
        raasiFormScrim.setVisibility(View.VISIBLE);
        setBlinkShareVisible(false);
        ViewCompat.requestApplyInsets(raasiFormScrim);
        raasiFormScrim.post(this::constrainRaasiFormScroll);
    }

    private void hideRaasiForm() {
        hideRaasiFormKeyboard();
        if (raasiFormSheetColumn != null) {
            raasiFormSheetColumn.setTranslationY(0f);
        }
        if (raasiFormScrim != null) {
            raasiFormScrim.setPadding(
                    raasiFormScrim.getPaddingLeft(),
                    raasiFormScrim.getPaddingTop(),
                    raasiFormScrim.getPaddingRight(),
                    0);
            raasiFormScrim.setVisibility(View.GONE);
        }
        resetRaasiFormScrollHeight();
        applyRaasiFormSoftInputMode(false);
        if (sheetScrim == null || sheetScrim.getVisibility() != View.VISIBLE) {
            setBlinkShareVisible(true);
        }
    }

    /** While the form is open, resize the window so the bottom sheet can sit above the IME. */
    private void applyRaasiFormSoftInputMode(boolean formOpen) {
        Window window = host.getWindow();
        if (window == null) return;
        if (formOpen) {
            if (!raasiFormSoftInputSaved) {
                raasiFormPrevSoftInputMode = window.getAttributes().softInputMode;
                raasiFormSoftInputSaved = true;
            }
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else if (raasiFormSoftInputSaved) {
            window.setSoftInputMode(raasiFormPrevSoftInputMode);
            raasiFormSoftInputSaved = false;
        }
    }

    private void installRaasiFormImeHandling() {
        if (raasiFormScrim == null || raasiFormImeInstalled) return;
        raasiFormImeInstalled = true;
        ViewCompat.setOnApplyWindowInsetsListener(raasiFormScrim, (v, windowInsets) -> {
            if (v.getVisibility() != View.VISIBLE) {
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), 0);
                if (raasiFormSheetColumn != null) {
                    raasiFormSheetColumn.setTranslationY(0f);
                }
                return windowInsets;
            }
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int lift = imeLiftForRaasiForm(ime.bottom);
            // Pad so bottom-gravity sheet rises into the visible area above the IME.
            // When adjustResize already shortened the host, lift is ~0.
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), lift);
            if (raasiFormSheetColumn != null) {
                raasiFormSheetColumn.setTranslationY(0f);
            }
            constrainRaasiFormScroll();
            return windowInsets;
        });
    }

    /**
     * How much to lift the form so its bottom clears the IME, accounting for the
     * calendar host already sitting above HomePage's bottom nav.
     */
    private int imeLiftForRaasiForm(int imeBottom) {
        if (imeBottom <= 0 || raasiFormScrim == null) return 0;
        int[] loc = new int[2];
        raasiFormScrim.getLocationInWindow(loc);
        int scrimBottom = loc[1] + raasiFormScrim.getHeight();
        View rootView = raasiFormScrim.getRootView();
        if (rootView == null) return imeBottom;
        int imeTop = rootView.getHeight() - imeBottom;
        // Positive when the IME covers the bottom of the scrim.
        return Math.max(0, scrimBottom - imeTop);
    }

    /** Cap NestedScrollView height so the form can scroll when IME leaves little room. */
    private void constrainRaasiFormScroll() {
        if (raasiFormScroll == null || raasiFormScrim == null) return;
        if (raasiFormScrim.getVisibility() != View.VISIBLE) return;
        raasiFormScroll.post(() -> {
            if (raasiFormScroll == null || raasiFormScrim == null) return;
            if (raasiFormScrim.getVisibility() != View.VISIBLE) return;
            View content = raasiFormScroll.getChildAt(0);
            if (content == null) return;
            int scrimH = raasiFormScrim.getHeight()
                    - raasiFormScrim.getPaddingTop()
                    - raasiFormScrim.getPaddingBottom();
            if (scrimH <= 0) return;
            View close = root.findViewById(R.id.raasi_form_close);
            int closeH = 0;
            if (close != null && close.getVisibility() == View.VISIBLE) {
                closeH = close.getHeight();
                ViewGroup.LayoutParams clp = close.getLayoutParams();
                if (clp instanceof ViewGroup.MarginLayoutParams) {
                    closeH += ((ViewGroup.MarginLayoutParams) clp).bottomMargin
                            + ((ViewGroup.MarginLayoutParams) clp).topMargin;
                }
            }
            int maxH = Math.max(dp(140), scrimH - closeH - dp(4));
            int scrollW = raasiFormScroll.getWidth();
            if (scrollW <= 0) scrollW = raasiFormScrim.getWidth();
            if (scrollW <= 0) return;
            content.measure(
                    View.MeasureSpec.makeMeasureSpec(scrollW, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int contentH = content.getMeasuredHeight();
            ViewGroup.LayoutParams lp = raasiFormScroll.getLayoutParams();
            if (lp == null) return;
            int target = contentH > maxH ? maxH : ViewGroup.LayoutParams.WRAP_CONTENT;
            if (lp.height != target) {
                lp.height = target;
                raasiFormScroll.setLayoutParams(lp);
            }
            if (target != ViewGroup.LayoutParams.WRAP_CONTENT
                    && raasiFormName != null
                    && raasiFormName.hasFocus()) {
                raasiFormScroll.post(() -> raasiFormScroll.smoothScrollTo(0, 0));
            }
        });
    }

    private void resetRaasiFormScrollHeight() {
        if (raasiFormScroll == null) return;
        ViewGroup.LayoutParams lp = raasiFormScroll.getLayoutParams();
        if (lp != null && lp.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            raasiFormScroll.setLayoutParams(lp);
        }
    }

    private void hideRaasiFormKeyboard() {
        View focus = host.getCurrentFocus();
        if (focus == null) {
            focus = raasiFormName != null ? raasiFormName : raasiFormScrim;
        }
        if (focus == null) return;
        InputMethodManager imm =
                (InputMethodManager) host.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
        focus.clearFocus();
    }

    /** Keep the focused field (and fields below) reachable inside the NestedScrollView. */
    private void wireRaasiFormFieldFocus(@NonNull EditText field) {
        field.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus || raasiFormScroll == null) return;
            constrainRaasiFormScroll();
            raasiFormScroll.post(() -> {
                if (raasiFormScroll == null) return;
                int y = Math.max(0, v.getTop() - dp(12));
                raasiFormScroll.smoothScrollTo(0, y);
            });
        });
    }

    private void wireRaasiDobPicker() {
        if (raasiFormDob == null) return;
        raasiFormDob.setFocusable(false);
        raasiFormDob.setClickable(true);
        raasiFormDob.setCursorVisible(false);
        raasiFormDob.setOnClickListener(v -> openRaasiDobPicker());
        raasiFormDob.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) openRaasiDobPicker();
        });
    }

    private void openRaasiDobPicker() {
        if (raasiFormDob == null || host.isFinishing() || host.isDestroyed()) return;
        Calendar cal = Calendar.getInstance();
        Pattern p = Pattern.compile("(\\d{1,2})[/.-](\\d{1,2})[/.-](\\d{2,4})");
        Matcher m = p.matcher(raasiFormDob.getText() != null
                ? raasiFormDob.getText().toString() : "");
        int y = cal.get(Calendar.YEAR) - 25;
        int mo = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        if (m.find()) {
            try {
                d = Integer.parseInt(m.group(1));
                mo = Integer.parseInt(m.group(2)) - 1;
                y = Integer.parseInt(m.group(3));
                if (y < 100) y += 1900;
                if (y < 1900 || y > cal.get(Calendar.YEAR)) {
                    y = cal.get(Calendar.YEAR) - 25;
                }
            } catch (Exception ignored) {
            }
        }
        DatePickerDialog dlg = new DatePickerDialog(host, (view, year, month, dayOfMonth) -> {
            String formatted = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            raasiFormDob.setText(formatted);
        }, y, Math.max(0, Math.min(11, mo)), Math.max(1, Math.min(31, d)));
        dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
        Calendar min = Calendar.getInstance();
        min.set(1900, Calendar.JANUARY, 1);
        dlg.getDatePicker().setMinDate(min.getTimeInMillis());
        dlg.show();
    }

    private void submitRaasiForm() {
        String name = raasiFormName != null ? raasiFormName.getText().toString().trim() : "";
        String dob = raasiFormDob != null ? raasiFormDob.getText().toString().trim() : "";
        String place = raasiFormPlace != null ? raasiFormPlace.getText().toString().trim() : "";
        int fromDob = parseRashiFromDob(dob);
        if (fromDob < 0) {
            Toast.makeText(host, CalendarRaasiLabels.dobRequired(host), Toast.LENGTH_SHORT).show();
            openRaasiDobPicker();
            return;
        }
        if (!TextUtils.isEmpty(name)) {
            SharedPrefsData.putString(host, PREF_RAASI_NAME, name, Constants.PREF_NAME);
        }
        if (!TextUtils.isEmpty(place)) {
            SharedPrefsData.putString(host, PREF_RAASI_POB, place, Constants.PREF_NAME);
        }
        SharedPrefsData.putString(host, PREF_RAASI_DOB, dob, Constants.PREF_NAME);
        selectedRaasi = fromDob;
        persistSelectedRaasi(fromDob);
        hideRaasiForm();
        hideRaasiDetailSheet();
        if (flipPager != null && flipPager.getCurrentPage() != 1) {
            selectRaasiPage(true);
        }
        bindRaasiPicker();
        bindRaasiDetail();
        Toast.makeText(host, CalendarRaasiLabels.raasiSaved(host), Toast.LENGTH_SHORT).show();
        scrollRaasiDetailIntoView();
    }

    /** Scroll so phalalu body is visible; lucky chips sit above on the know-raasi row. */
    private void scrollRaasiDetailIntoView() {
        if (raasiScroll == null || raasiDetail == null) return;
        raasiDetail.post(() -> {
            if (raasiScroll == null || raasiDetail == null) return;
            int target = Math.max(0, raasiDetail.getTop() - dp(48));
            raasiScroll.smoothScrollTo(0, target);
        });
    }

    private void persistSelectedRaasi(int id) {
        int v = RaasiContent.clampId(id);
        SharedPrefsData.putInt(host, PREF_SELECTED_RAASI, v, Constants.PREF_NAME);
        SharedPrefsData.putBool(host, PREF_SELECTED_RAASI_SET, true, Constants.PREF_NAME);
    }

    private int loadSelectedRaasi() {
        if (SharedPrefsData.getBool(host, PREF_SELECTED_RAASI_SET, Constants.PREF_NAME)) {
            return RaasiContent.clampId(
                    SharedPrefsData.getInt(host, PREF_SELECTED_RAASI, Constants.PREF_NAME));
        }
        // Admin default from splash (0-based stored int), else Scorpio fallback.
        int admin = ProfileHubConfigHelper.getDefaultRashiId(host, FALLBACK_DEFAULT_RAASI);
        return RaasiContent.clampId(admin);
    }

    private int parseRashiFromDob(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) return -1;
        Pattern p = Pattern.compile("(\\d{1,2})[/.-](\\d{1,2})[/.-](\\d{2,4})");
        Matcher m = p.matcher(raw);
        if (!m.find()) return -1;
        try {
            int d = Integer.parseInt(m.group(1));
            int mo = Integer.parseInt(m.group(2));
            int y = Integer.parseInt(m.group(3));
            if (y < 100) y += 1900;
            if (mo < 1 || mo > 12 || d < 1 || d > 31) return -1;
            return RaasiContent.roughRashiFromDob(y, mo, d);
        } catch (Exception e) {
            return -1;
        }
    }

    private void shareSelectedDay() {
        PanchangModels.DayData day = selectedDayData();
        if (day == null) {
            Toast.makeText(host, R.string.telugu_calendar_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        String dateStr = day.dateDisplayEn != null ? day.dateDisplayEn : "";
        if (TextUtils.isEmpty(dateStr)) {
            dateStr = String.format(Locale.US, "%02d %s %d",
                    selectedDay,
                    EN_MONTHS[displayMonth.get(Calendar.MONTH)],
                    displayMonth.get(Calendar.YEAR));
        }
        sb.append("ANYNEWS — ").append(dateStr).append('\n');

        appendShareSection(sb, CalendarRaasiLabels.shubhaTimes(host), day.safeShubha());
        appendShareSection(sb, CalendarRaasiLabels.ashubhaTimes(host),
                day.safeAshubhaMerged(CalendarRaasiLabels.againConnector(host)));
        appendShareSection(sb, "తిథి", day.safeTithiCard());
        if (day.nakshatra != null && day.nakshatra.name != null) {
            List<PanchangModels.TimeItem> nak = new ArrayList<>();
            nak.add(new PanchangModels.TimeItem(
                    day.nakshatra.name,
                    day.nakshatra.endTime != null
                            ? ("ముగింపు: " + day.nakshatra.endTime)
                            : ""));
            if (day.sunrise != null) {
                nak.add(new PanchangModels.TimeItem("సూర్యోదయం", day.sunrise));
            }
            if (day.sunset != null) {
                nak.add(new PanchangModels.TimeItem("సూర్యాస్తమయం", day.sunset));
            }
            appendShareSection(sb, "నక్షత్రం", nak);
        }
        sharePlainText(sb.toString().trim());
    }

    private void shareSelectedRaasi() {
        selectedRaasi = RaasiContent.clampId(selectedRaasi);
        RaasiContent.Daily daily = RaasiContent.forToday(host, selectedRaasi);
        Calendar today = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
        String dateDisp = String.format(Locale.US, "%02d %s %d",
                today.get(Calendar.DAY_OF_MONTH),
                EN_MONTHS[today.get(Calendar.MONTH)],
                today.get(Calendar.YEAR));
        String title = CalendarRaasiLabels.phalaluTitle(host, selectedRaasi, dateDisp);
        StringBuilder sb = new StringBuilder();
        sb.append("ANYNEWS — ").append(title).append('\n');
        if (daily != null && !TextUtils.isEmpty(daily.body)) {
            sb.append('\n').append(daily.body.trim()).append('\n');
        }
        sb.append('\n').append(CalendarRaasiLabels.luckyHeading(host)).append('\n');
        if (daily != null) {
            sb.append(CalendarRaasiLabels.luckyColor(host)).append(": ")
                    .append(nullToEmpty(daily.color)).append('\n');
            sb.append(CalendarRaasiLabels.luckyNumbers(host)).append(": ")
                    .append(nullToEmpty(daily.numbers)).append('\n');
            sb.append(CalendarRaasiLabels.luckyDirection(host)).append(": ")
                    .append(nullToEmpty(daily.direction));
        }
        sharePlainText(sb.toString().trim());
    }

    private void appendShareSection(@NonNull StringBuilder sb,
                                    @NonNull String title,
                                    @Nullable List<PanchangModels.TimeItem> items) {
        if (items == null || items.isEmpty()) return;
        sb.append('\n').append(title).append('\n');
        for (PanchangModels.TimeItem it : items) {
            if (it == null) continue;
            sb.append("• ").append(nullToEmpty(it.label));
            if (!TextUtils.isEmpty(it.range)) {
                sb.append(": ").append(it.range);
            }
            sb.append('\n');
        }
    }

    private void sharePlainText(@NonNull String text) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(host, R.string.telugu_calendar_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, text);
            send.putExtra(Intent.EXTRA_SUBJECT, "ANYNEWS");
            host.startActivity(Intent.createChooser(
                    send, CalendarRaasiLabels.shareChooser(host)));
        } catch (Exception e) {
            Toast.makeText(host, CalendarRaasiLabels.shareChooser(host), Toast.LENGTH_SHORT).show();
        }
    }

    private void openFullJatakamWhatsApp() {
        String number = ProfileHubConfigHelper.getFullJatakamWhatsapp(host);
        if (TextUtils.isEmpty(number)) {
            number = SharedPrefsData.getString(
                    host, Constants.FULL_JATAKAM_WHATSAPP, Constants.PREF_NAME);
        }
        if (TextUtils.isEmpty(number)) {
            Toast.makeText(host, CalendarRaasiLabels.whatsappMissing(host), Toast.LENGTH_LONG).show();
            return;
        }
        String digits = number.replaceAll("[^0-9]", "");
        if (digits.length() < 10) {
            Toast.makeText(host, CalendarRaasiLabels.whatsappMissing(host), Toast.LENGTH_LONG).show();
            return;
        }
        String rashiName = CalendarRaasiLabels.rashiName(host, selectedRaasi);
        String user = SharedPrefsData.getString(host, PREF_RAASI_NAME, Constants.PREF_NAME);
        String text = "నమస్కారం, ANYNEWS నుండి పూర్తి జాతకం గురించి తెలుసుకోవాలనుకుంటున్నాను."
                + (TextUtils.isEmpty(rashiName) ? "" : (" నా రాశి: " + rashiName + "."))
                + (TextUtils.isEmpty(user) ? "" : (" పేరు: " + user + "."));
        try {
            Uri uri = Uri.parse("https://wa.me/" + digits + "?text=" + Uri.encode(text));
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            host.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(host, CalendarRaasiLabels.whatsappMissing(host), Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    /** @return true if back was consumed by calendar UI (sheet / raasi flip). */
    public boolean handleBackPress() {
        if (raasiDetailScrim != null && raasiDetailScrim.getVisibility() == View.VISIBLE) {
            hideRaasiDetailSheet();
            return true;
        }
        if (raasiFormScrim != null && raasiFormScrim.getVisibility() == View.VISIBLE) {
            hideRaasiForm();
            return true;
        }
        if (sheetScrim != null && sheetScrim.getVisibility() == View.VISIBLE) {
            hideFestivalsSheet();
            return true;
        }
        if (flipPager != null && flipPager.getCurrentPage() > 0) {
            selectPanchangamPage(true);
            return true;
        }
        return false;
    }

    private int iconRes(@Nullable String key) {
        if (key == null) return R.drawable.ic_calendar_festival;
        switch (key.trim().toLowerCase(Locale.US)) {
            case "flag":
            case "newyear":
                return R.drawable.ic_calendar_flag;
            case "eid":
            case "star":
                return R.drawable.ic_calendar_eid;
            case "rakhi":
                return R.drawable.ic_calendar_rakhi;
            case "amavasya":
                return R.drawable.ic_calendar_amavasya;
            case "pournami":
                return R.drawable.ic_calendar_pournami;
            case "sankranti":
            case "festival":
            default:
                return R.drawable.ic_calendar_festival;
        }
    }

    private void resizeDetailPagerToContent() {
        if (pager == null) return;
        pager.post(() -> {
            if (pager == null) return;
            // Card host uses layout_weight=1 in page_telugu_calendar.xml so it
            // fills space below the month block. Ensure a solid min height
            // when the page has not finished laying out yet.
            View hostFrame = pager.getParent() instanceof View
                    ? (View) pager.getParent() : null;
            int minH = dp(CalendarUiConfigHelper.getCardMinHeightDp(host));
            ViewGroup.LayoutParams plp = pager.getLayoutParams();
            if (plp != null && plp.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                plp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                pager.setLayoutParams(plp);
            }
            if (hostFrame != null) {
                ViewGroup.LayoutParams hlp = hostFrame.getLayoutParams();
                if (hlp instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) hlp;
                    if (llp.weight <= 0f) {
                        llp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        llp.height = 0;
                        llp.weight = 1f;
                        hostFrame.setLayoutParams(llp);
                    }
                    hostFrame.setMinimumHeight(minH);
                } else if (hlp != null && hlp.height < minH) {
                    hlp.height = minH;
                    hostFrame.setLayoutParams(hlp);
                }
            }
            if (calendarPageRoot != null) {
                calendarPageRoot.requestLayout();
            }
        });
    }

    private int dp(int v) {
        return Math.round(v * host.getResources().getDisplayMetrics().density);
    }

    private void bindPageCalendarViews(@NonNull View page) {
        calendarPageRoot = page.findViewById(R.id.telugu_calendar_scroll);
        if (calendarPageRoot == null && page.getId() == R.id.telugu_calendar_scroll) {
            calendarPageRoot = page;
        }
        calendarBody = page.findViewById(R.id.telugu_calendar_body);
        loadReveal = page.findViewById(R.id.telugu_calendar_load_reveal);
        monthTitle = page.findViewById(R.id.telugu_calendar_month);
        teluguLine = page.findViewById(R.id.telugu_calendar_telugu_line);
        grid = page.findViewById(R.id.telugu_calendar_grid);
        pager = page.findViewById(R.id.telugu_calendar_pager);
        detailPageIndicator = page.findViewById(R.id.telugu_calendar_detail_page_indicator);
        dayShare = page.findViewById(R.id.telugu_calendar_day_share);
        festivalStrip = page.findViewById(R.id.telugu_calendar_festival_strip);
        festivalIcon = page.findViewById(R.id.telugu_calendar_festival_icon);
        festivalText = page.findViewById(R.id.telugu_calendar_festival_text);
        swipeArrow = page.findViewById(R.id.telugu_calendar_swipe_arrow);
        swipeLabel = page.findViewById(R.id.telugu_calendar_swipe_label);
        swipeCue = page.findViewById(R.id.telugu_calendar_swipe_cue);
        flipIndicator = page.findViewById(R.id.telugu_calendar_flip_indicator);
        // New FlipView page instance — require a fresh reveal after bind.
        if (wiredCalendarPageInstance != calendarPageRoot) {
            calendarContentRevealed = false;
        }
        // Paint News-style skeleton immediately (HomeFeedRevealView defaults to GONE).
        if (!calendarContentRevealed && loadReveal != null) {
            loadReveal.start();
        }
    }

    private void bindPageRaasiViews(@NonNull View page) {
        raasiScroll = page.findViewById(R.id.telugu_calendar_raasi_scroll);
        if (raasiScroll == null && page.getId() == R.id.telugu_calendar_raasi_scroll) {
            raasiScroll = (NestedScrollView) page;
        }
        raasiGrid = page.findViewById(R.id.telugu_calendar_raasi_grid);
        raasiDetail = page.findViewById(R.id.telugu_calendar_raasi_detail);
        raasiSignLine = page.findViewById(R.id.telugu_calendar_raasi_sign_line);
        raasiTitle = page.findViewById(R.id.telugu_calendar_raasi_title);
        raasiBody = page.findViewById(R.id.telugu_calendar_raasi_body);
        raasiMore = page.findViewById(R.id.telugu_calendar_raasi_more);
        luckyHeading = page.findViewById(R.id.telugu_calendar_lucky_heading);
        luckyColorLabel = page.findViewById(R.id.telugu_calendar_lucky_color_label);
        luckyNumbersLabel = page.findViewById(R.id.telugu_calendar_lucky_numbers_label);
        luckyDirectionLabel = page.findViewById(R.id.telugu_calendar_lucky_direction_label);
        luckyColor = page.findViewById(R.id.telugu_calendar_lucky_color);
        luckyNumbers = page.findViewById(R.id.telugu_calendar_lucky_numbers);
        luckyDirection = page.findViewById(R.id.telugu_calendar_lucky_direction);
        fullJatakamCta = page.findViewById(R.id.telugu_calendar_full_jatakam);
        if (fullJatakamCta != null) {
            fullJatakamCta.setVisibility(View.GONE);
            fullJatakamCta.setOnClickListener(null);
        }
        raasiShare = page.findViewById(R.id.telugu_calendar_raasi_share);
        flipIndicatorRaasi = page.findViewById(R.id.telugu_calendar_flip_indicator_raasi);
        raasiDetailIcon = page.findViewById(R.id.telugu_calendar_raasi_detail_icon);
        raasiSections = page.findViewById(R.id.telugu_calendar_rashi_sections);
        rashiDateLabel = page.findViewById(R.id.telugu_calendar_rashi_date_label);
        rashiDateValue = page.findViewById(R.id.telugu_calendar_rashi_date_value);
        rashiNakLabel = page.findViewById(R.id.telugu_calendar_rashi_nak_label);
        rashiNakValue = page.findViewById(R.id.telugu_calendar_rashi_nak_value);
        rashiYogaLabel = page.findViewById(R.id.telugu_calendar_rashi_yoga_label);
        rashiYogaValue = page.findViewById(R.id.telugu_calendar_rashi_yoga_value);
        rashiLordLabel = page.findViewById(R.id.telugu_calendar_rashi_lord_label);
        rashiLordValue = page.findViewById(R.id.telugu_calendar_rashi_lord_value);
        rashiElementLabel = page.findViewById(R.id.telugu_calendar_rashi_element_label);
        rashiElementValue = page.findViewById(R.id.telugu_calendar_rashi_element_value);
        rashiGemLabel = page.findViewById(R.id.telugu_calendar_rashi_gem_label);
        rashiGemValue = page.findViewById(R.id.telugu_calendar_rashi_gem_value);
        rashiColorLabel = page.findViewById(R.id.telugu_calendar_rashi_color_label);
        rashiColorValue = page.findViewById(R.id.telugu_calendar_rashi_color_value);
        rashiShubham = page.findViewById(R.id.telugu_calendar_rashi_shubham);
        raasiBack = page.findViewById(R.id.telugu_calendar_raasi_back);
        raasiHeaderShare = page.findViewById(R.id.telugu_calendar_raasi_header_share);
    }

    private final class CalendarFlipAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                int layout = position == 0
                        ? R.layout.page_telugu_calendar
                        : R.layout.page_telugu_calendar_raasi;
                v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                v.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }
            if (position == 0) {
                bindPageCalendarViews(v);
                // Always re-wire — FlipView can recreate page views after flip.
                v.post(() -> wireCalendarPage());
            } else {
                bindPageRaasiViews(v);
                v.post(() -> wireRaasiPage());
            }
            return v;
        }
    }

    private final class DetailPagerAdapter extends PagerAdapter {
        @Nullable private PanchangModels.DayData day;
        private final List<CardPage> pages = new ArrayList<>();

        void setDay(@Nullable PanchangModels.DayData d) {
            day = d;
            pages.clear();
            if (d == null) {
                pages.add(new CardPage("…", "", R.drawable.ic_calendar_sun, new ArrayList<>()));
                notifyDataSetChanged();
                return;
            }
            String dateStr = formatDetailCardDate(d);
            List<PanchangModels.TimeItem> shubha = d.safeShubha();
            boolean onlyVehicle = shubha.size() == 1
                    && shubha.get(0) != null
                    && shubha.get(0).label != null
                    && shubha.get(0).label.contains("వాహన");
            String shubhaTitle = onlyVehicle
                    ? CalendarRaasiLabels.shubhaMuhurthams(host)
                    : CalendarRaasiLabels.shubhaTimes(host);
            pages.add(new CardPage(shubhaTitle, dateStr, R.drawable.ic_calendar_sun, shubha));
            pages.add(new CardPage(
                    CalendarRaasiLabels.ashubhaTimes(host),
                    dateStr,
                    R.drawable.ic_calendar_ashubha_star,
                    d.safeAshubhaMerged(CalendarRaasiLabels.againConnector(host))));
            pages.add(new CardPage(
                    CalendarRaasiLabels.tithiLabel(host),
                    dateStr,
                    R.drawable.ic_calendar_tithi,
                    d.safeTithiCard()));
            if (d.nakshatra != null && d.nakshatra.name != null) {
                List<PanchangModels.TimeItem> nak = new ArrayList<>();
                nak.add(new PanchangModels.TimeItem(
                        d.nakshatra.name,
                        d.nakshatra.endTime != null
                                ? ("ముగింపు: " + d.nakshatra.endTime)
                                : ""));
                if (d.sunrise != null) {
                    nak.add(new PanchangModels.TimeItem("సూర్యోదయం", d.sunrise));
                }
                if (d.sunset != null) {
                    nak.add(new PanchangModels.TimeItem("సూర్యాస్తమయం", d.sunset));
                }
                pages.add(new CardPage(
                        CalendarRaasiLabels.nakshatraLabel(host),
                        dateStr,
                        R.drawable.ic_calendar_nakshatra,
                        nak));
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View v = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.item_calendar_detail_card, container, false);
            CardPage page = pages.get(position);
            ImageView icon = v.findViewById(R.id.card_icon);
            TextView title = v.findViewById(R.id.card_title);
            TextView date = v.findViewById(R.id.card_date);
            LinearLayout items = v.findViewById(R.id.card_items);
            int accent = themeAccent != 0 ? themeAccent : COLOR_ORANGE;
            float titleSp = CalendarUiConfigHelper.getTitleSizeSp(host);
            float itemSp = CalendarUiConfigHelper.getItemSizeSp(host);
            int itemPad = dp(CalendarUiConfigHelper.getItemPadDp(host));
            if (icon != null) {
                icon.setImageResource(page.iconRes);
                icon.setColorFilter(accent, PorterDuff.Mode.SRC_IN);
            }
            if (title != null) {
                title.setText(page.title);
                title.setTextColor(accent);
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSp);
            }
            if (date != null) {
                date.setText(page.date);
                date.setTextSize(TypedValue.COMPLEX_UNIT_SP, Math.max(11f, titleSp - 4f));
            }
            if (items != null) {
                items.removeAllViews();
                for (PanchangModels.TimeItem it : page.items) {
                    if (it == null) continue;
                    String label = safe(it.label);
                    String range = safe(it.range);
                    String body = range.isEmpty() ? label : (label + ": " + range);
                    SpannableStringBuilder rowText = new SpannableStringBuilder();
                    int bulletStart = rowText.length();
                    rowText.append("●  ");
                    rowText.setSpan(new ForegroundColorSpan(accent), bulletStart, bulletStart + 1,
                            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
                    int bodyStart = rowText.length();
                    rowText.append(body);
                    rowText.setSpan(new ForegroundColorSpan(COLOR_WHITE), bodyStart, rowText.length(),
                            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
                    TextView row = new TextView(container.getContext());
                    row.setText(rowText);
                    row.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemSp);
                    row.setPadding(0, itemPad, 0, itemPad);
                    items.addView(row);
                }
            }
            // Detail cards sit in a ViewPager — parent OnClickListeners never fire; wire the card.
            v.setClickable(true);
            v.setOnClickListener(view -> revealSegmentChromeIfHidden());
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        private String safe(String s) {
            return s != null ? s : "";
        }
    }

    private static final class CardPage {
        final String title;
        final String date;
        final int iconRes;
        final List<PanchangModels.TimeItem> items;

        CardPage(String title, String date, int iconRes, List<PanchangModels.TimeItem> items) {
            this.title = title;
            this.date = date;
            this.iconRes = iconRes;
            this.items = items != null ? items : new ArrayList<>();
        }
    }
}
