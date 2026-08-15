package com.anyNews.anynews.Activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.TextUtils;
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
import android.widget.ProgressBar;
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
import com.anyNews.anynews.Utils.CalendarRaasiLabels;
import com.anyNews.anynews.Utils.Constants;
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
    private LinearLayout festivalStrip;
    private ImageView festivalIcon;
    private TextView festivalText;
    private ProgressBar loading;
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
            // Same pattern as HomePage article FlipView (not ViewPager2 3D transformer).
            CalendarFlipAdapter adapter = new CalendarFlipAdapter();
            flipPager.setAdapter(adapter);
            flipPager.setOnFlipListener(this);
            flipPager.setOverFlipMode(OverFlipMode.GLOW);
            // Do NOT call peakNext() — infinite FlipView peak bobs the whole calendar
            // page (Shubha card / content). Swipe-arrow bob alone is the affordance.
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

    /** Re-apply insets when standalone activity resumes. */
    public void onHostResume() {
        if (applyWindowInsets) {
            applySystemBarInsets();
        }
    }

    @Override
    public void onFlippedToPage(FlipView v, int position, long id) {
        updateFlipIndicators(position);
        refreshBlinkShareContentDescription(position);
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
                displayMonth.add(Calendar.MONTH, -1);
                selectedDay = clampSelectedDay();
                ensureYearAndBind();
            });
        }
        if (next != null) {
            next.setOnClickListener(v -> {
                displayMonth.add(Calendar.MONTH, 1);
                selectedDay = clampSelectedDay();
                ensureYearAndBind();
            });
        }

        if (festivalStrip != null) {
            festivalStrip.setOnClickListener(v -> showFestivalsSheet());
        }
        // Arrow / "రాశి ఫలాలు" is visual hint only — navigate by FlipView swipe.
        if (swipeCue != null) {
            swipeCue.setOnClickListener(null);
            swipeCue.setClickable(false);
            swipeCue.setFocusable(false);
        }
        if (swipeLabel != null) {
            swipeLabel.setText(CalendarRaasiLabels.swipeHint(host));
            swipeLabel.setTextColor(themeAccent);
            swipeLabel.setClickable(false);
        }
        if (swipeArrow != null) {
            swipeArrow.setColorFilter(themeAccent, PorterDuff.Mode.SRC_IN);
            swipeArrow.setClickable(false);
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
        startSwipeBob();
        updateFlipIndicators(flipPager != null ? flipPager.getCurrentPage() : 0);
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
     * Tap runs day or raasi share for the visible flip page.
     */
    private void wireBlinkShareOverlay() {
        blinkShare = root.findViewById(R.id.telugu_calendar_blink_share);
        if (blinkShare == null) return;
        blinkShare.setVisibility(View.VISIBLE);
        blinkShare.setClickable(true);
        blinkShare.setFocusable(true);
        // Match full_image_share_bg: blue circular PNG, no white tint / dark chip.
        blinkShare.setImageResource(R.drawable.ic_share_full);
        blinkShare.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        blinkShare.clearColorFilter();
        blinkShare.setBackground(null);
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
        startBlinkShareAnimation();
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
        if (visible) {
            blinkShare.setVisibility(View.VISIBLE);
            startBlinkShareAnimation();
        } else {
            stopBlinkShareAnimation();
            blinkShare.setVisibility(View.GONE);
        }
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
            knowRaasi.setOnClickListener(v -> showRaasiForm());
        }
        // Full inline body — no more→modal flow.
        if (raasiMore != null) {
            raasiMore.setVisibility(View.GONE);
            raasiMore.setOnClickListener(null);
        }
        if (raasiBody != null) {
            raasiBody.setOnClickListener(null);
            raasiBody.setClickable(false);
            raasiBody.setMaxLines(Integer.MAX_VALUE);
            raasiBody.setEllipsize(null);
        }
        if (raasiDetail != null) {
            raasiDetail.setOnClickListener(null);
            raasiDetail.setClickable(false);
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
        updateFlipIndicators(flipPager != null ? flipPager.getCurrentPage() : 0);
    }

    /** Apply LanguageID chrome (pick / know / lucky). Call on every rebind. */
    private void applyRaasiChromeLabels() {
        TextView knowRaasi = root.findViewById(R.id.telugu_calendar_know_raasi);
        if (knowRaasi != null) {
            knowRaasi.setText(CalendarRaasiLabels.knowRaasi(host));
        }
        TextView pickTitle = root.findViewById(R.id.telugu_calendar_pick_raasi_title);
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
        if (swipeArrow == null) return;
        if (swipeBob != null) swipeBob.cancel();
        swipeBob = ObjectAnimator.ofFloat(swipeArrow, View.TRANSLATION_Y, 0f, dp(8));
        swipeBob.setDuration(700);
        swipeBob.setRepeatMode(ValueAnimator.REVERSE);
        swipeBob.setRepeatCount(ValueAnimator.INFINITE);
        swipeBob.setInterpolator(new AccelerateDecelerateInterpolator());
        swipeBob.start();
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
        LinearLayout row = root.findViewById(R.id.telugu_calendar_weekday_row);
        if (row == null) return;
        row.removeAllViews();
        for (int i = 0; i < 7; i++) {
            TextView tv = new TextView(host);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tv.setLayoutParams(lp);
            tv.setGravity(Gravity.CENTER);
            tv.setText(WEEKDAYS_TE[i]);
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
            return;
        }
        if (loading != null) loading.setVisibility(View.VISIBLE);
        final int reqYear = year;
        PanchangRepository.loadYear(host, reqYear, (pack, fromCache) -> {
            if (host.isFinishing() || host.isDestroyed()) return;
            if (pack == null || pack.year != reqYear) return;
            yearPack = pack;
            currentYearLoaded = pack.year;
            if (loading != null) loading.setVisibility(View.GONE);
            bindAll();
        });
    }

    private void bindAll() {
        bindMonthHeader();
        bindGrid();
        bindDetailCards();
        bindFestivalStrip();
    }

    private void bindMonthHeader() {
        int m0 = displayMonth.get(Calendar.MONTH);
        int year = displayMonth.get(Calendar.YEAR);
        if (monthTitle != null) {
            monthTitle.setText(EN_MONTHS[m0] + " " + year);
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

        int screenH = host.getResources().getDisplayMetrics().heightPixels;
        int gridBudget = (int) (screenH * 0.42f);
        int cellH = Math.max(dp(48), Math.min(dp(62), gridBudget / Math.max(rows, 5)));
        fillGridCells(leadingEmpty, daysInMonth, totalSlots, cellH, rows);
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
            int numColor = COLOR_WHITE;
            if (selected) {
                numColor = COLOR_BLUE;
            } else if (festival || iconKey != null) {
                numColor = COLOR_GREEN;
            } else if (sunday) {
                numColor = COLOR_RED;
            }
            num.setTextColor(numColor);
            FrameLayout.LayoutParams nlp = new FrameLayout.LayoutParams(dp(26), dp(26));
            nlp.gravity = Gravity.TOP | Gravity.START;
            nlp.leftMargin = dp(2);
            nlp.topMargin = dp(2);
            num.setLayoutParams(nlp);
            if (selected) {
                num.setBackgroundResource(R.drawable.bg_calendar_day_selected);
            }
            cell.addView(num);

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
        if (fests.isEmpty()) {
            festivalStrip.setVisibility(View.GONE);
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
        String enPart = pick.dateDisplayEn != null
                ? pick.dateDisplayEn
                : EN_MONTHS[displayMonth.get(Calendar.MONTH)].toUpperCase(Locale.US) + " " + pick.day;
        festivalText.setText(enPart + " - " + pick.name);
        festivalText.setTextColor(COLOR_BLUE);
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
        int gap = dp(5);
        for (int i = 0; i < RaasiContent.COUNT; i++) {
            final int id = i;
            boolean sel = id == selectedRaasi;
            LinearLayout tile = new LinearLayout(host);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER_HORIZONTAL);
            tile.setBackgroundResource(sel
                    ? R.drawable.bg_raasi_tile_selected
                    : R.drawable.bg_raasi_tile);
            tile.setPadding(dp(4), dp(8), dp(4), dp(6));
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(i % 4, 1f);
            lp.rowSpec = GridLayout.spec(i / 4);
            lp.setMargins(gap, gap, gap, gap);
            tile.setLayoutParams(lp);

            FrameLayout top = new FrameLayout(host);
            LinearLayout.LayoutParams topLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
            top.setLayoutParams(topLp);

            ImageView icon = new ImageView(host);
            FrameLayout.LayoutParams ilp = new FrameLayout.LayoutParams(dp(44), dp(44));
            ilp.gravity = Gravity.CENTER;
            icon.setLayoutParams(ilp);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int resId = RaasiContent.iconResId(host, i);
            if (resId != 0) {
                icon.setImageResource(resId);
            } else {
                // Fallback glyph if drawable missing
                icon.setVisibility(View.GONE);
                TextView glyph = new TextView(host);
                glyph.setText(RaasiContent.GLYPHS[i]);
                glyph.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                glyph.setTextColor(COLOR_WHITE);
                glyph.setGravity(Gravity.CENTER);
                glyph.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                top.addView(glyph);
            }
            if (resId != 0) top.addView(icon);

            if (sel) {
                ImageView check = new ImageView(host);
                FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(dp(16), dp(16));
                clp.gravity = Gravity.BOTTOM | Gravity.END;
                clp.rightMargin = dp(0);
                clp.bottomMargin = dp(0);
                check.setLayoutParams(clp);
                check.setImageResource(R.drawable.ic_raasi_check);
                top.addView(check);
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
            name.setPadding(dp(1), dp(2), dp(1), dp(2));
            name.setBackground(null);
            name.setTextColor(COLOR_WHITE);
            if (sel) name.setTypeface(Typeface.DEFAULT_BOLD);
            tile.addView(name);

            tile.setOnClickListener(v -> {
                selectedRaasi = id;
                persistSelectedRaasi(id);
                bindRaasiPicker();
                // Inline preview only — full modal opens via "మరిన్ని" / more.
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
        if (raasiSignLine != null) {
            raasiSignLine.setVisibility(View.GONE);
        }
        if (raasiTitle != null) {
            Calendar today = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
            int y = today.get(Calendar.YEAR);
            int m0 = today.get(Calendar.MONTH);
            int d = today.get(Calendar.DAY_OF_MONTH);
            String dateDisp = String.format(Locale.US, "%02d %s %d", d, EN_MONTHS[m0], y);
            raasiTitle.setText(CalendarRaasiLabels.phalaluTitle(host, selectedRaasi, dateDisp));
        }
        if (raasiBody != null) {
            raasiBody.setMaxLines(Integer.MAX_VALUE);
            raasiBody.setEllipsize(null);
            raasiBody.setSingleLine(false);
            raasiBody.setText(daily.body != null ? daily.body : "");
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
            goToRaasiPage(true);
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

        appendShareSection(sb, "శుభ సమయాలు", day.safeShubha());
        appendShareSection(sb, "అశుభ సమయాలు", day.safeAshubha());
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
            flipPager.smoothFlipTo(0);
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

    private int dp(int v) {
        return Math.round(v * host.getResources().getDisplayMetrics().density);
    }

    private void bindPageCalendarViews(@NonNull View page) {
        calendarPageRoot = page.findViewById(R.id.telugu_calendar_scroll);
        if (calendarPageRoot == null && page.getId() == R.id.telugu_calendar_scroll) {
            calendarPageRoot = page;
        }
        monthTitle = page.findViewById(R.id.telugu_calendar_month);
        teluguLine = page.findViewById(R.id.telugu_calendar_telugu_line);
        grid = page.findViewById(R.id.telugu_calendar_grid);
        pager = page.findViewById(R.id.telugu_calendar_pager);
        detailPageIndicator = page.findViewById(R.id.telugu_calendar_detail_page_indicator);
        dayShare = page.findViewById(R.id.telugu_calendar_day_share);
        festivalStrip = page.findViewById(R.id.telugu_calendar_festival_strip);
        festivalIcon = page.findViewById(R.id.telugu_calendar_festival_icon);
        festivalText = page.findViewById(R.id.telugu_calendar_festival_text);
        loading = page.findViewById(R.id.telugu_calendar_loading);
        swipeArrow = page.findViewById(R.id.telugu_calendar_swipe_arrow);
        swipeLabel = page.findViewById(R.id.telugu_calendar_swipe_label);
        swipeCue = page.findViewById(R.id.telugu_calendar_swipe_cue);
        flipIndicator = page.findViewById(R.id.telugu_calendar_flip_indicator);
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
                pages.add(new CardPage("…", "", new ArrayList<>()));
                notifyDataSetChanged();
                return;
            }
            String dateStr = d.dateDisplayEn != null ? d.dateDisplayEn : "";
            List<PanchangModels.TimeItem> shubha = d.safeShubha();
            boolean onlyVehicle = shubha.size() == 1
                    && shubha.get(0) != null
                    && shubha.get(0).label != null
                    && shubha.get(0).label.contains("వాహన");
            String shubhaTitle = onlyVehicle ? "శుభ ముహూర్తాలు" : "శుభ సమయాలు";
            pages.add(new CardPage(shubhaTitle, dateStr, shubha));
            pages.add(new CardPage("అశుభ సమయాలు", dateStr, d.safeAshubha()));
            pages.add(new CardPage("తిథి", dateStr, d.safeTithiCard()));
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
                pages.add(new CardPage("నక్షత్రం", dateStr, nak));
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
            TextView title = v.findViewById(R.id.card_title);
            TextView date = v.findViewById(R.id.card_date);
            LinearLayout items = v.findViewById(R.id.card_items);
            if (title != null) title.setText(page.title);
            if (date != null) date.setText(page.date);
            if (items != null) {
                items.removeAllViews();
                for (PanchangModels.TimeItem it : page.items) {
                    if (it == null) continue;
                    TextView row = new TextView(container.getContext());
                    row.setText("•  " + safe(it.label) + ": " + safe(it.range));
                    row.setTextColor(COLOR_WHITE);
                    row.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    row.setPadding(0, dp(3), 0, dp(3));
                    items.addView(row);
                }
            }
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
        final List<PanchangModels.TimeItem> items;

        CardPage(String title, String date, List<PanchangModels.TimeItem> items) {
            this.title = title;
            this.date = date;
            this.items = items != null ? items : new ArrayList<>();
        }
    }
}
