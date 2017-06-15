# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/edgargomez/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#
# EventBus
#
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
#
#AVLoadingIndicatorView
#
-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }
#
#Branch
#
-keep class com.google.android.gms.ads.identifier.** { *; }
#
# Required for Parse
#
-keepattributes *Annotation*
-keepattributes Signature
-dontwarn android.net.SSLCertificateSocketFactory
-dontwarn android.app.Notification
-dontwarn com.squareup.**
-dontwarn okio.**
#
# Required for Flurry
#
# Required to preserve the Flurry SDK
# -keep class com.flurry.** { *; }
# -dontwarn com.flurry.**
# -keepattributes *Annotation*,EnclosingMethod,Signature
# -keepclasseswithmembers class * {
#   public (android.content.Context, android.util.AttributeSet, int);
# }
#
# Google Play Services library
#  -keep class * extends java.util.ListResourceBundle {
#   protected Object[ ][ ] getContents();
# }
#
# -keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
#  public static final *** NULL;
# }
#
# -keepnames @com.google.android.gms.common.annotation.KeepName class *
# -keepclassmembernames class * {
#    @com.google.android.gms.common.annotation.KeepName *;
#  }
#
# -keepnames class * implements android.os.Parcelable {
#  public static final ** CREATOR;
# }
#
# Required for Fresco
#
# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**
-dontwarn com.facebook.infer.**