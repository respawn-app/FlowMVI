package pro.respawn.flowmvi.savedstate.api

public val NullRecover: suspend (Exception) -> Nothing? = { null }
public val ThrowRecover: suspend (e: Exception) -> Nothing? = { throw it }

public interface Saver<T> {

    public suspend fun save(state: T?)
    public suspend fun restore(): T?
    public suspend fun recover(e: Exception): T? = throw e
}
