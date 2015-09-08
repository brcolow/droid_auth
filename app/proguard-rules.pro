# ACRA Stuff
-keepattributes SourceFile,LineNumberTable

# Keep all the ACRA classes
-keep class org.acra.** { *; }
-keepclassmembers **.MenuBuilder { void setOptionalIconsVisible(boolean); }