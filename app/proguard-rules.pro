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