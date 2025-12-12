package pro.respawn.flowmvi.metrics.api

/**
 * Returns a snapshot adjusted for the target schema version.
 *
 * Currently no-op aside from updating [Meta.schemaVersion]; future versions can drop/rename fields here.
 */
public fun MetricsSnapshot.downgradeTo(target: MetricsSchemaVersion): MetricsSnapshot =
    if (target == meta.schemaVersion) this else copy(meta = meta.copy(schemaVersion = target))
