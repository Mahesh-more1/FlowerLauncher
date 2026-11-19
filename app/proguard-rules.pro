# FlowerLauncher ProGuard Rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer {
    static **$$serializer INSTANCE;
}

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }

# Application & Service Entry Points
-keep class com.flowerlauncher.FlowerApp { *; }
-keep class com.flowerlauncher.ui.HomeScreenActivity { *; }
-keep class com.flowerlauncher.service.** { *; }
