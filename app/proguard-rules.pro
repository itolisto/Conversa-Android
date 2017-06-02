# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/edgargomez/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include url and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interfaces
# class:
#-keepclassmembers class fqcn.of.javascript.interfaces.for.webview {
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
#Fabric
#
#-keepattributes SourceFile,LineNumberTable
#
# Keep source file names, line numbers, and Parse class/method names for easier debugging
#
-keepattributes SourceFile,LineNumberTable
-keepnames class com.parse.** { *; }
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
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepclasseswithmembers class * {
    public (android.content.Context, android.util.AttributeSet, int);
}
# Google Play Services library
-keep class * extends java.util.ListResourceBundle {
    protected Object[ ][ ] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
#
# Required for PubNub
#
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod
-keep class com.google.android.gms.ads.identifier.** { *; }
# joda time
-keep class org.joda.time.** { *; }
-dontwarn org.joda.time.**
# jackson
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.**
# gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
# Retrofit 2.X
## https://square.github.io/retrofit/ ##
-dontwarn retrofit2.**
#-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
# Pubnub
-dontwarn com.pubnub.**
#-keep class com.pubnub.** { *; }
-dontwarn org.slf4j.**