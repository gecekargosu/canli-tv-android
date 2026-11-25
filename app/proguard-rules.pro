# UMAY Multi TV — R8 / shrinkResources keep kuralları (9.88 / K-15)
#
# Kural: yalnız yansıma (reflection) veya manifest dışı yükleme ile erişilen
# sınıflar korunur. Kendi sınıflarımız manifestte tanımlı olduğu için otomatik korunur.

# Hata günlüğü okunabilir kalsın (sınıf adı + satır numarası).
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod

# Google Play Billing: ürün/abonelik nesneleri Parcelable + yansıma ile okunuyor.
-keep class com.android.billingclient.** { *; }
-keep interface com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# AdMob + UMP (rıza formu): SDK içinde yansıma ve webview köprüsü var.
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.gms.**

# Media3: çözücü/uzantı seçimi ve HLS varyant çözümü yansıma kullanıyor.
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# org.json platformda; yine de yansıma uyarılarını kapat.
-keep class org.json.** { *; }
-dontwarn org.json.**
