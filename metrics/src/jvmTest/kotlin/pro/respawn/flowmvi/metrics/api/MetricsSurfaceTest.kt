package pro.respawn.flowmvi.metrics.api

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import pro.respawn.flowmvi.metrics.sampleSnapshot

class MetricsSurfaceTest : FreeSpec({

    "fromVersion returns V1 for known and unknown versions" {
        MetricSurface.fromVersion(MetricsSchemaVersion.V1_0) shouldBe MetricSurface.V1
        MetricSurface.fromVersion(MetricsSchemaVersion(9, 9)) shouldBe MetricSurface.V1
    }

    "downgradeTo returns same instance when version matches" {
        val snapshot = sampleSnapshot()

        val downgraded = snapshot.downgradeTo(snapshot.meta.schemaVersion)

        downgraded shouldBe snapshot
        downgraded shouldBeSameInstanceAs snapshot
    }

    "downgradeTo changes schema version and keeps data" {
        val snapshot = sampleSnapshot()
        val targetVersion = MetricsSchemaVersion(2, 0)

        val downgraded = snapshot.downgradeTo(targetVersion)

        downgraded.meta.schemaVersion shouldBe targetVersion
        downgraded shouldNotBeSameInstanceAs snapshot
        downgraded.copy(meta = snapshot.meta) shouldBe snapshot
    }

    "downgrade is idempotent for the same target" {
        val snapshot = sampleSnapshot()
        val target = MetricsSchemaVersion(1, 0)

        val first = snapshot.downgradeTo(target)
        val second = first.downgradeTo(target)

        first shouldBe second
        first.meta.schemaVersion shouldBe target
    }

    "downgrade then restore current preserves payload" {
        val snapshot = sampleSnapshot()
        val older = MetricsSchemaVersion(0, 9)

        val downgraded = snapshot.downgradeTo(older)
        val restored = downgraded.downgradeTo(MetricsSchemaVersion.CURRENT)

        restored.meta.schemaVersion shouldBe MetricsSchemaVersion.CURRENT
        restored.copy(meta = snapshot.meta) shouldBe snapshot
    }
})
