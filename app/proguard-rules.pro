# ZYON VIRTUAL PC 16
-keep class com.zyon.virtualpc.nativebridge.ZyonNative { *; }
-keepclasseswithmembernames class * { native <methods>; }
-keep class com.zyon.virtualpc.ZyonApp { *; }
-repackageclasses 'z'
-allowaccessmodification
-renamesourcefileattribute ''
-keepattributes !SourceFile,!LineNumberTable
