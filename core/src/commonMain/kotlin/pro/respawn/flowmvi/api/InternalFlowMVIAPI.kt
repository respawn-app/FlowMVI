package pro.respawn.flowmvi.api

private const val Message = """
Internal FlowMVI API. Do not use this API directly as public API already contains everything you should need.
"""

/**
 * Marker annotation for store apis that are not thread-safe
 */
@RequiresOptIn(message = Message)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class InternalFlowMVIAPI
