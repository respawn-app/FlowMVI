package pro.respawn.flowmvi.metrics.api

/**
 * Metric surface encapsulates the concrete naming/label layout for rendered metrics.
 *
 * Keep new versions additive or provide downgrade paths for older consumers.
 */
public sealed interface MetricSurface {
    public val version: MetricsSchemaVersion

    public companion object {
        public val V1: MetricSurface = V1Surface

        public fun fromVersion(version: MetricsSchemaVersion): MetricSurface = when (version) {
            MetricsSchemaVersion.V1_0 -> V1
            else -> V1 // fallback until newer surfaces are introduced
        }
    }
}

private data object V1Surface : MetricSurface {
    override val version: MetricsSchemaVersion = MetricsSchemaVersion.V1_0
}
