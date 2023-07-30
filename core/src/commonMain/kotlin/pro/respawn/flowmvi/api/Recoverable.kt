package pro.respawn.flowmvi.api

/**
 * An entity that can [recover] from exceptions happening during its lifecycle. Most often, a [Store]
 */
@Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION") // https://youtrack.jetbrains.com/issue/KTIJ-7642
public fun interface Recoverable {

    public suspend fun recover(e: Exception)
}
