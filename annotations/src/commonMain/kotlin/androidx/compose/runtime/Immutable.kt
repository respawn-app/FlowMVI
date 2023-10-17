package androidx.compose.runtime

/**
 * This is a backport annotation from [Immutable](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime/src/commonMain/kotlin/androidx/compose/runtime/Immutable.kt).
 *
 * [Immutable] can be used to mark class as producing immutable instances. The immutability of the
 * class is not validated and is a promise by the type that all publicly accessible properties
 * and fields will not change after the instance is constructed. This is a stronger promise than
 * `val` as it promises that the value will never change not only that values cannot be changed
 * through a setter.
 *
 * [Immutable] is used by composition which enables composition optimizations that can be
 * performed based on the assumption that values read from the type will not change.  See
 * [StableMarker] for additional details.
 *
 * `data` classes that only contain `val` properties that do not have custom getters can safely
 * be marked as [Immutable] if the types of properties are either primitive types or also
 * [Immutable]:
 *
 * @see StableMarker
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@StableMarker
public annotation class Immutable
