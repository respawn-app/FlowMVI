package pro.respawn.flowmvi

/**
 * Marker annotation for store apis that are not thread-safe
 */
@RequiresOptIn(
    message = "This API is low-level. Make sure you are not abusing it, because thread sync issues are likely."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class DelicateStoreApi