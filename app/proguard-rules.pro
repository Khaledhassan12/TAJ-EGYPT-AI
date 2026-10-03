# ProGuard & R8 Configuration for TAJ EGY (tag.egypt.com)

# Preserve Line Numbers for meaningful stack traces
-keepattributes SourceFile,LineNumberTable

# Preserve Annotations for Room, Moshi, and Compose
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Project Domain Models and Entities
-keep class tag.egypt.com.model.** { *; }
-keep class tag.egypt.com.storage.** { *; }

# Room Database Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-dontwarn androidx.room.**

# OkHttp & Okio Rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Coroutines Rules
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Android KeyStore Security Rules
-keep class tag.egypt.com.security.** { *; }
