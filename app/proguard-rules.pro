-keep class com.jeison.agrocontrol.network.** { *; }
-keep class com.jeison.agrocontrol.data.** { *; }
-keep class com.jeison.agrocontrol.auth.** { *; }

# Keep rules for EncryptedSharedPreferences y AndroidX Security
-keep class androidx.security.crypto.** { *; }

# Atributos de Gson y Retrofit
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn okhttp3.**
-dontwarn retrofit2.**
