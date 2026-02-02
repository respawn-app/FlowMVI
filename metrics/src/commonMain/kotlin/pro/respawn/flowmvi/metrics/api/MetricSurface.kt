package pro.respawn.flowmvi.metrics.api

/**
 * Metric surface encapsulates the concrete naming/label layout for rendered metrics
 * (the shape of serialized [MetricsSnapshot]).
 *
 * Keep new versions additive or provide downgrade paths for older consumers.
 */
public sealed interface MetricSurface {

    /** Schema version this surface renders. */
    public val version: MetricsSchemaVersion

    /** Predefined metric surfaces and a resolver by schema version. */
    @Suppress("UndocumentedPublicProperty")
    public companion object {

        public val V1: MetricSurface = V1Surface
        public val V1_1: MetricSurface = V11Surface

        /**
         * Resolve a surface instance for the requested [version], falling back to the latest supported one.
         */
        public fun fromVersion(version: MetricsSchemaVersion): MetricSurface = when (version) {
            MetricsSchemaVersion.V1_0 -> V1
            MetricsSchemaVersion.V1_1 -> V1_1
            else -> V1_1 // fallback until newer surfaces are introduced
        }
    }
}

private data object V1Surface : MetricSurface {

    override val version: MetricsSchemaVersion = MetricsSchemaVersion.V1_0
}

private data object V11Surface : MetricSurface {

    override val version: MetricsSchemaVersion = MetricsSchemaVersion.V1_1
}
