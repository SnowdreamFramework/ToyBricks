# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/snowdream/Library/Android/sdk/tools/proguard/proguard-android.txt
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


#保留InterfaceLoader及其子类不被混淆
-keep class * implements com.github.snowdream.toybricks.annotation.InterfaceLoader{*;}

# @Interface and @Implementation specifies not to shrink, optimize, or obfuscate the annotated class
# or class member as an entry point.
-dontwarn org.jetbrains.annotations.**
-dontwarn com.github.snowdream.toybricks.annotation.**
-keep class com.github.snowdream.toybricks.annotation.**
-keep @com.github.snowdream.toybricks.annotation.Interface class *
-keep @com.github.snowdream.toybricks.annotation.Implementation class *
