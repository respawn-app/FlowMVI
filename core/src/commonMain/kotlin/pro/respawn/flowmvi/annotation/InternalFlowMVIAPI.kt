package pro.respawn.flowmvi.annotation

private const val Message = """
This API is internal to the library and is exposed for performance reasons.
Do NOT use this API directly as public API already exists for the same thing.
If you have the use-case for this api you can't avoid, please submit an issue BEFORE you use it. 
This API may be removed, changed or change behavior without prior notice at any moment.
"""

/**
 * Marker annotation for store apis that are not thread-safe
 */
@RequiresOptIn(message = Message)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
public annotation class InternalFlowMVIAPI
