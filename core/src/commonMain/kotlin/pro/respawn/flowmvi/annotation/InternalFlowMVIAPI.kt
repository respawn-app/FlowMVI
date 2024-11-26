package pro.respawn.flowmvi.annotation

private const val Message = """
This API is internal to the library.
Do not use this API directly as public API already exists for the same thing.
If you have the use-case for this api you can't avoid, please submit an issue.
"""

/**
 * Marker annotation for store apis that are not thread-safe
 */
@RequiresOptIn(message = Message)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class InternalFlowMVIAPI
