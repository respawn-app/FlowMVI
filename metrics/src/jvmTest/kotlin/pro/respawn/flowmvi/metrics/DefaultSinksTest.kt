package pro.respawn.flowmvi.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import pro.respawn.flowmvi.metrics.api.Sink

class DefaultSinksTest : FreeSpec({

    "MappingSink maps values before delegating" {
        val received = mutableListOf<String>()
        val sink = MappingSink<Int, String>(delegate = { received += it }) { "value-$it" }

        sink.emit(3)
        sink.emit(4)

        received shouldContainExactly listOf("value-3", "value-4")
    }

    "JsonSink serializes with provided serializer" {
        val emitted = mutableListOf<String>()
        val sink = JsonSink(
            delegate = Sink { emitted += it },
            json = Json { encodeDefaults = false },
            serializer = MetricsSnapshot.serializer()
        )

        sink.emit(sampleSnapshot())

        val encoded = emitted.single()
        val decoded = Json.decodeFromString(MetricsSnapshot.serializer(), encoded)

        decoded shouldBe sampleSnapshot()
    }

    "LoggingJsonMetricsSink emits JSON through logger" {
        val logger = RecordingLogger()
        val sink = LoggingJsonMetricsSink(
            json = Json { encodeDefaults = false },
            logger = logger,
            level = StoreLogLevel.Info,
            tag = "MyMetrics"
        )

        sink.emit(sampleSnapshot())

        val entry = logger.entries.single()
        entry.level shouldBe StoreLogLevel.Info
        entry.tag shouldBe "MyMetrics"
        Json.parseToJsonElement(entry.message).jsonObject["meta"]!!
            .jsonObject["runId"]!!.jsonPrimitive.content shouldBe "demo-run-id"
    }
})
