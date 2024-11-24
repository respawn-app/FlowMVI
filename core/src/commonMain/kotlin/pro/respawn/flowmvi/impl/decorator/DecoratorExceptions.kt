package pro.respawn.flowmvi.impl.decorator

import pro.respawn.flowmvi.api.UnrecoverableException

public class AlreadyProceededException internal constructor(name: String) : UnrecoverableException(
    message = """
            You have tried to call proceed() in ${name}, which is likely an error.
            Ensure you call proceed() exactly once, or disable this check with allowNonProceedingBehavior = true
    """.trimIndent()
)

public class NeverProceededException internal constructor(name: String) : UnrecoverableException(
    message = """
            You haven't called proceed() in ${name}, which is likely an error.
            Ensure you call proceed() exactly once, or disable this check with allowNonProceedingBehavior = true
    """.trimIndent()
)
