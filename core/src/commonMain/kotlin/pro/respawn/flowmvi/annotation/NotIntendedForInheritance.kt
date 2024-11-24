package pro.respawn.flowmvi.annotation

private const val Message = """
This API is not intended to be inherited from. 
Inheriting from this degrades performance and may have unintended consequences, especially if the implicit contracts
of the library are not followed.
Please use a provided builder function or submit a ticket on Github with your use-case.
"""

/**
 * Marks declarations that cannot be safely inherited from.
 */
@Target(AnnotationTarget.CLASS)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = Message)
public annotation class NotIntendedForInheritance
