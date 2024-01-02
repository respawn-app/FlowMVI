package pro.respawn.flowmvi.savedstate.api

public inline val NullRecover: suspend (Exception) -> Nothing? get() = { null }
public inline val ThrowRecover: suspend (e: Exception) -> Nothing? get() = { throw it }

public interface Saver<T> {

    public suspend fun save(state: T?)
    public suspend fun restore(): T?
    public suspend fun recover(e: Exception): T? = ThrowRecover(e)
}
