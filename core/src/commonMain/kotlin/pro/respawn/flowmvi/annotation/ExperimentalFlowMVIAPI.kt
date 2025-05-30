package pro.respawn.flowmvi.annotation

private const val Message = """
This API is experimental - it has no automated testing and breaking changes are very likely in the next releases. 
Use at your own risk. This is not a "regular" experimental annotation you can just opt-in to without consideration.
You have been warned.
"""

/**
 * Marker annotation for store apis that are experimental.
 */
@RequiresOptIn(message = Message)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class ExperimentalFlowMVIAPI
