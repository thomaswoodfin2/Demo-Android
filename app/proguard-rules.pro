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
 -keep class com.google.** { *; }
  -keep class com.** { *; }
  -keep class javax.** { *; }
  -keep class libcore.** { *; }
  -keep class io.grpc.** { *; }
     -dontwarn com.google.**
 -keep class android.app.** { *; }
 -keep class com.pinpoint.appointment.** { *; }
 -keep class org.** { *; }
  -keep class okio.** { *; }
  -dontwarn org.**
  -dontwarn okio.**
   -keep class com.crashlytics.** { *; }
   -keep class com.facebook.** { *; }
    -dontwarn me.leolin.**

   -keep class me.leolin.** { *; }


 -dontwarn com.squareup.picasso.**
 -keep class com.squareup.picasso.** { *; }
 #### -- OkHttp --

 -dontwarn com.squareup.okhttp.internal.**
  -keep class com.squareup.okhttp.internal.** { *; }

 #### -- Apache Commons --

 -dontwarn org.apache.commons.logging.**
  -keep class org.apache.commons.logging.** { *; }
