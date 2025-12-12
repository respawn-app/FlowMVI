package pro.respawn.flowmvi.metrics.api

/** Generic sink for values. */
public fun interface Sink<T> {

    /** Emits a value downstream. Implementations should return quickly. */
    public suspend fun emit(value: T)
}
