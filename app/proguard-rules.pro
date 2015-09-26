# ACRA Stuff
-keepattributes SourceFile,LineNumberTable

# Keep all the ACRA classes
-keep class org.acra.** { *; }
-keep class !android.support.v7.internal.view.menu.**,android.support.** {*;}