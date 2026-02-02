package pro.respawn.flowmvi.metrics.api

/**
 * Returns a snapshot adjusted for the target schema version.
 *
 * Currently no-op aside from updating [Meta.schemaVersion]; future versions can drop/rename fields here.
 */
public fun MetricsSnapshot.downgradeTo(target: MetricsSchemaVersion): MetricsSnapshot {
    if (target == meta.schemaVersion) return this
    val downgradedState = if (target < MetricsSchemaVersion.V1_1) {
        state.copy(
            startedInInitialState = false,
            timeToFirstState = null
        )
    } else {
        state
    }
    return copy(
        meta = meta.copy(schemaVersion = target),
        state = downgradedState
    )
}
