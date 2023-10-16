package androidx.compose.runtime

/**
 * This is a backport annotation from [Stable](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime/src/commonMain/kotlin/androidx/compose/runtime/Stable.kt).
 *
 * Stable is used to communicate some guarantees to the compose compiler about how a certain type
 * or function will behave.
 *
 * When applied to a class or an interface, [Stable] indicates that the following must be true:
 *
 *   1) The result of [equals] will always return the same result for the same two instances.
 *   2) When a public property of the type changes, composition will be notified.
 *   3) All public property types are stable.
 *
 * When applied to a function or a property, the [Stable] annotation indicates that the function
 * will return the same result if the same parameters are passed in. This is only meaningful if
 * the parameters and results are themselves [Stable], [Immutable], or primitive.
 *
 * The invariants that this annotation implies are used for optimizations by the compose compiler,
 * and have undefined behavior if the above assumptions are not met. As a result, one should not
 * use this annotation unless they are certain that these conditions are satisfied.
 *
 * @see Immutable
 * @see StableMarker
 */
@MustBeDocumented
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY,
)
@Retention(AnnotationRetention.BINARY)
@StableMarker
public annotation class Stable
