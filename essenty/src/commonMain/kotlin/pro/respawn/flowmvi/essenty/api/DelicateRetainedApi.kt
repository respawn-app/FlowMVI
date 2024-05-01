package pro.respawn.flowmvi.essenty.api

private const val Message = """
Be careful with this API, as creating stores inside components allows you to use the component and its internals
inside the builder. If you capture the component in the builder's closure in any way (referencing 
functions, properties, or the context), the component will be forcefully retained, leading to a memory leak.
If you use retained components, you do not need to retain anything, just use a regular store builder.
If you do not, create a separate Container class that hosts the store, inject it as a factory, and retain that reference
instead, or at least avoid referencing the component inside the builder.
"""

/**
 * Annotation marker that warns usages of retained stores, which can lead to memory leaks. See the error message
 * for details.
 */
@RequiresOptIn(message = Message, level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class DelicateRetainedApi
