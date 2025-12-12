package pro.respawn.flowmvi.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import pro.respawn.flowmvi.metrics.api.MetricsSchemaVersion
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class MetaMetricsCollectorTest : FreeSpec({

    configure()

    "Meta.schemaVersion fresh snapshot matches CURRENT" {
        testCollectorWithTime { collector, _, _ ->
            collector.snapshot().meta.schemaVersion shouldBe MetricsSchemaVersion.CURRENT
        }
    }

    "Meta.schemaVersion after lifecycle start is still CURRENT" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().meta.schemaVersion shouldBe MetricsSchemaVersion.CURRENT
        }
    }

    "Meta.schemaVersion after reset is still CURRENT" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.reset()
            collector.snapshot().meta.schemaVersion shouldBe MetricsSchemaVersion.CURRENT
        }
    }

    "Meta.generatedAt equals fixed test clock instant at snapshot time" {
        testCollectorWithTime { collector, clock, _ ->
            clock.now() shouldBe Instant.fromEpochMilliseconds(0)
            collector.snapshot().meta.generatedAt shouldBe Instant.fromEpochMilliseconds(0)
        }
    }

    "Meta.generatedAt unaffected by monotonic time source advancement" {
        testCollectorWithTime { collector, _, ts ->
            ts.advanceBy(5.seconds)
            collector.snapshot().meta.generatedAt shouldBe Instant.fromEpochMilliseconds(0)
        }
    }

    "Meta.generatedAt is not null even with no events" {
        testCollectorWithTime { collector, _, _ ->
            collector.snapshot().meta.generatedAt.shouldNotBeNull()
        }
    }

    "Meta.startTime set on first onStart" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().meta.startTime shouldBe Instant.fromEpochMilliseconds(0)
        }
    }

    "Meta.startTime keeps first value across multiple starts" {
        testCollectorWithTime { collector, clock, _ ->
            onStart()
            onStop(null)
            clock.advanceBy(10.seconds)
            onStart()
            collector.snapshot().meta.startTime shouldBe Instant.fromEpochMilliseconds(0)
        }
    }

    "Meta.startTime absent before any start" {
        testCollectorWithTime { collector, _, _ ->
            collector.snapshot().meta.startTime.shouldBeNull()
        }
    }

    "Meta.runId is null before first start" {
        testCollectorWithTime { collector, _, _ ->
            collector.snapshot().meta.runId.shouldBeNull()
        }
    }

    "Meta.runId set on start and non-empty" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val runId = collector.snapshot().meta.runId
            runId.shouldNotBeNull()
            runId.shouldNotBeBlank()
        }
    }

    "Meta.runId stable within a single run" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val first = collector.snapshot().meta.runId
            val second = collector.snapshot().meta.runId
            second shouldBe first
        }
    }

    "Meta.runId persists after stop until next start" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val first = collector.snapshot().meta.runId
            onStop(null)
            collector.snapshot().meta.runId shouldBe first
        }
    }

    "Meta.runId changes on next run after restart" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val first = collector.snapshot().meta.runId
            onStop(null)
            onStart()
            val second = collector.snapshot().meta.runId
            second.shouldNotBeNull()
            second shouldNotBe first
        }
    }

    "Meta.runId not affected by reset" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val first = collector.snapshot().meta.runId
            collector.reset()
            collector.snapshot().meta.runId shouldBe first
        }
    }

    "Meta.storeName comes from StoreConfiguration.name after first start" {
        testCollectorWithTime(
            configuration = { name = "TestStore" }
        ) { collector, _, _ ->
            onStart()
            collector.snapshot().meta.storeName shouldBe "TestStore"
        }
    }

    "Meta.storeName null when config has no name" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().meta.storeName.shouldBeNull()
        }
    }

    "Meta.storeName persists across restart without reconfiguring" {
        testCollectorWithTime(
            configuration = { name = "Persisted" }
        ) { collector, clock, _ ->
            onStart()
            onStop(null)
            clock.advanceBy(3.seconds)
            onStart()
            collector.snapshot().meta.storeName shouldBe "Persisted"
        }
    }

    "Meta.storeId is non-empty UUID after first start" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val id = collector.snapshot().meta.storeId
            id.shouldNotBeNull()
            id.shouldNotBeBlank()
            id.length shouldBe 36
        }
    }

    "Meta.storeId stable across lifecycle until reset" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val first = collector.snapshot().meta.storeId
            onStop(null)
            onStart()
            collector.snapshot().meta.storeId shouldBe first
        }
    }

    "Meta.storeId does not change after reset" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val first = collector.snapshot().meta.storeId
            collector.reset()
            collector.snapshot().meta.storeId shouldBe first
        }
    }

    "Meta.windowSeconds equals constructor value" {
        testCollectorWithTime(windowSeconds = 60) { collector, _, _ ->
            collector.snapshot().meta.windowSeconds shouldBe 60
        }
    }

    "Meta.windowSeconds equals custom window" {
        testCollectorWithTime(windowSeconds = 5) { collector, _, _ ->
            collector.snapshot().meta.windowSeconds shouldBe 5
        }
    }

    "Meta.windowSeconds exposed even with zero events" {
        testCollectorWithTime(windowSeconds = 7) { collector, _, _ ->
            collector.snapshot().meta.windowSeconds shouldBe 7
        }
    }

    "Meta.emaAlpha equals constructor value" {
        testCollectorWithTime(emaAlpha = 0.5) { collector, _, _ ->
            collector.snapshot().meta.emaAlpha shouldBe 0.5f
        }
    }

    "Meta.emaAlpha equals custom alpha" {
        testCollectorWithTime(emaAlpha = 0.1) { collector, _, _ ->
            collector.snapshot().meta.emaAlpha shouldBe 0.1f
        }
    }

    "Meta.emaAlpha exposed when no samples" {
        testCollectorWithTime(emaAlpha = 0.33) { collector, _, _ ->
            collector.snapshot().meta.emaAlpha shouldBe 0.33f
        }
    }
})
