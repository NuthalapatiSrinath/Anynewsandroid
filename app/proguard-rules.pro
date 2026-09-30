# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.google.firebase.example.fireeats.model.** { *; }
-keep class com.anyNews.anynews.Pojo.** { *; }

# ==== Rules ported from production (AnyNews_2026) proguard-rules.pro ====

# Keep app classes
-keep class com.anyNews.anynews.** { *; }

# Missing optional classes (suppress R8 missing-class errors)
-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe
-dontwarn com.google.android.exoplayer2.analytics.AnalyticsCollector$Factory
-dontwarn com.google.protobuf.java_com_google_android_gmscore_sdk_target_granule__proguard_group_gtm_N1281923064GeneratedExtensionRegistryLite$Loader
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Retrofit
-keep interface retrofit2.** { *; }
-keep class retrofit2.** { *; }

# Keep the method parameters for Retrofit interface methods
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

# Media3 / ExoPlayer
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }

# YouTube player (pierfrancescosoffritti)
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }

# CanHub / vanniktech image cropper (CropImageActivity is used via Activity Result + Parcelables)
-keep class com.canhub.cropper.** { *; }
-dontwarn com.canhub.cropper.**

# Google Mobile Ads / AdMob (MobileAdsInitProvider is declared in the merged
# manifest; without these keeps R8 strips the whole SDK -> ClassNotFound crash on launch)
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.internal.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Ad mediation (Facebook Audience Network) + UMP consent
-keep class com.google.ads.mediation.** { *; }
-keep class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# IMA (interactive media ads)
-keep class com.google.ads.interactivemedia.** { *; }
-dontwarn com.google.ads.interactivemedia.**

# Footer: hide the Daily Updates center slot without MenuItem.setVisible rebuilding
# the bar. These members are reached by reflection so the item views stay put.
-keepclassmembers class com.google.android.material.bottomnavigation.BottomNavigationView {
    private com.google.android.material.bottomnavigation.BottomNavigationPresenter presenter;
}
-keepclassmembers class com.google.android.material.bottomnavigation.BottomNavigationPresenter {
    public void setUpdateSuspended(boolean);
}
-keepclassmembers class com.google.android.material.bottomnavigation.BottomNavigationMenuView {
    private androidx.transition.TransitionSet set;
    private int selectedItemId;
    private int selectedItemPosition;
}
