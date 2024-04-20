
# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Preserve annotated Javascript interface methods.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep annotation
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

# Keep annotations used via reflection
-keepattributes RuntimeVisibleAnnotations,
                RuntimeVisibleParameterAnnotations,
                RuntimeVisibleTypeAnnotations

# These classes are duplicated between android.jar and core-lambda-stubs.jar.
-dontnote java.lang.invoke.**

# Uncomment this to preserve the line number information for
# debugging stack traces. (Firebase)
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file title.
-renamesourcefileattribute SourceFile

#Parcelable and serializable classes will be affected when serialized
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable
-keepnames class * implements com.google.android.gms.common.internal.ReflectedParcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final *** CREATOR;
}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class ** {
    volatile <fields>;
}

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# These classes are only required by kotlinx.coroutines.debug.AgentPremain, which is only loaded when
# kotlinx-coroutines-core is used as a Java agent, so these are not needed in contexts where ProGuard is used.
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.Signal

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# https://github.com/square/okhttp/issues/6258
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn java.lang.invoke.StringConcatFactory

# Kotlin nullability checks leak variable names
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void checkNotNull(...);
	public static void checkExpressionValueIsNotNull(...);
	public static void checkNotNullExpressionValue(...);
	public static void checkParameterIsNotNull(...);
	public static void checkNotNullParameter(...);
	public static void checkReturnedValueIsNotNull(...);
	public static void checkFieldIsNotNull(...);
	public static void throwUninitializedPropertyAccessException(...);
	public static void throwNpe(...);
	public static void throwJavaNpe(...);
	public static void throwAssert(...);
	public static void throwIllegalArgument(...);
	public static void throwIllegalState(...);
}

# https://issuetracker.google.com/issues/271805005
-keeppackagenames '!**'
-repackageclasses ''
-allowaccessmodification
-optimizationpasses 5
# Dalvik VM can't handle static overridden fields, do not enable
# -overloadaggressively
