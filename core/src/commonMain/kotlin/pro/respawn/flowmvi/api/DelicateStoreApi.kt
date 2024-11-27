package pro.respawn.flowmvi.api

private const val Message = """
This API is low-level and ignores all plugins, error-handling, and is not thread-safe.
Use this API in a fast, synchronous manner and provide your own thread-safety measures if needed.
Consider using other, safer alternatives provided by the library, or report your use case to maintainers.
"""

/**
 * Marker annotation for store apis that are not thread-safe
 */
@RequiresOptIn(message = Message)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class DelicateStoreApi
