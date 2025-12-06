package pro.respawn.flowmvi.metrics.api

public fun interface Metrics {

    public suspend fun snapshot(): MetricsSnapshot
    public companion object
}
