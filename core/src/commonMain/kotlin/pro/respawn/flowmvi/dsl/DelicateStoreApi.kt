package pro.respawn.flowmvi.dsl

/**
 * Marker annotation for store apis that are not thread-safe
 */
@RequiresOptIn(
    message = "This API is low-level. If you use it, make sure to not introduce races to your state management",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class DelicateStoreApi
