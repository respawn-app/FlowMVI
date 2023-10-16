package androidx.compose.runtime

/**
 * This is a backport annotation from [StableMarker](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime/src/commonMain/kotlin/androidx/compose/runtime/StableMarker.kt).
 *
 * [StableMarker] marks an annotation as indicating a type is stable. A stable type obeys the
 * following assumptions,
 *
 *   1) The result of [equals] will always return the same result for the same two instances.
 *   2) When a public property of the type changes, composition will be notified.
 *   3) All public property types are stable.
 *
 * A type that is immutable obeys the above assumptions because the public values will never
 * change. The [Immutable] annotation is provided to mark immutable types as stable.
 *
 * An object whose public properties do not change but is not immutable (for example, it has
 * private mutable state or uses property delegation to a [MutableState] object, but is otherwise
 * immutable), should use the [Stable] annotation.
 *
 * Mutable object that do not notify composition when they changed should not be marked as stable.
 *
 * When all types passed as parameters to a [Composable] function are marked as stable then the
 * parameter values are compared for equality based on positional memoization and the call is
 * skipped if all the values are the equal to the previous call.
 *
 * Primitive value types (such as Int, Float, etc), String and enum types are considered, a
 * priori, stable.
 *
 * @see Immutable
 * @see Stable
 */
@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class StableMarker
