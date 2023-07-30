package pro.respawn.flowmvi.api

private const val Message = """
This API is low-level and ignores ALL plugins and validations. Use it for performance-critical operations only.
If you use it, make sure to not introduce races to your state management.
    """

/**
 * Marker annotation for store apis that are not thread-safe
 */
@RequiresOptIn(message = Message)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class DelicateStoreApi
