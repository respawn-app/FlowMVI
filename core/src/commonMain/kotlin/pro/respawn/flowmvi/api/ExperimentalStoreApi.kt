package pro.respawn.flowmvi.api

private const val Message = """
This API is experimental - it has no automated testing and breaking changes are very likely. Use at your own risk.
This is not a "regular" experimental annotation you can just opt-in to without consideration. You have been warned.
"""

/**
 * Marker annotation for store apis that are experimental.
 */
@RequiresOptIn(message = Message)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class ExperimentalStoreApi
