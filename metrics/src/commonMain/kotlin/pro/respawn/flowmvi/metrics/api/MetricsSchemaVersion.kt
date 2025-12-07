package pro.respawn.flowmvi.metrics.api

import kotlinx.serialization.Serializable

/**
 * Schema version for metrics payloads and rendered metric surfaces.
 *
 * Major increments may change names/labels/units; minor increments must be additive.
 *
 * @property major incompatible/breaking dimension of the schema.
 * @property minor additive dimension of the schema.
 */
@Serializable
public data class MetricsSchemaVersion(
    val major: Int,
    val minor: Int,
) : Comparable<MetricsSchemaVersion> {

    /** Canonical wire representation, e.g. "1.0". */
    public val value: String get() = "$major.$minor"

    /** Numeric form convenient for gauges. */
    internal fun toDouble(): Double = major + minor / 1000.0

    override fun compareTo(other: MetricsSchemaVersion): Int = compareValuesBy(
        this,
        other,
        MetricsSchemaVersion::major,
        MetricsSchemaVersion::minor
    )

    override fun toString(): String = value

    /** Canonical constants for schema evolution. */
    public companion object {
        /** Initial released schema. */
        public val V1_0: MetricsSchemaVersion = MetricsSchemaVersion(1, 0)

        /** The version used by newly produced snapshots. */
        public val CURRENT: MetricsSchemaVersion = V1_0
    }
}
