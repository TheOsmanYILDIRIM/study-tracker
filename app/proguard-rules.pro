# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    companion object *;
}
