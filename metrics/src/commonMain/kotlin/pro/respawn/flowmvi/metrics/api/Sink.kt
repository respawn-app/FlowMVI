package pro.respawn.flowmvi.metrics.api

/** Generic sink for values. */
public fun interface Sink<T> {

    /** Emits a value downstream. Implementations should return quickly. */
    public fun emit(value: T)
}
