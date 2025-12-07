package pro.respawn.flowmvi.metrics.openmetrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import pro.respawn.flowmvi.metrics.AppendableStringSink
import pro.respawn.flowmvi.metrics.sampleSnapshot

class OpenMetricsSinkJvmTest : FreeSpec({

    val snapshot = sampleSnapshot()

    "applies custom namespace" {
        val buffer = StringBuilder()
        val sink = OpenMetricsSink(
            delegate = AppendableStringSink(buffer),
            namespace = "custom",
            includeHelp = false,
            includeUnit = false
        )

        sink.emit(snapshot)

        val rendered = buffer.toString()
        rendered.shouldContain("custom_intents_total")
        rendered.shouldNotContain("flowmvi_intents_total")
    }

    "help and unit sections can be disabled while timestamps are included" {
        val buffer = StringBuilder()
        val sink = OpenMetricsSink(
            delegate = AppendableStringSink(buffer),
            includeHelp = false,
            includeUnit = false,
            includeTimestamp = true
        )

        sink.emit(snapshot)

        val rendered = buffer.toString()
        rendered.shouldNotContain("# HELP")
        rendered.shouldNotContain("# UNIT")
        val sampleLine = rendered.lineSequence()
            .first { it.startsWith("flowmvi_config_window_seconds") }
        sampleLine.shouldContain("1700000000000")
    }

    "quantiles are rendered as gauge samples with labels" {
        val buffer = StringBuilder()
        val sink = OpenMetricsSink(
            delegate = AppendableStringSink(buffer),
            includeHelp = false,
            includeUnit = false
        )

        sink.emit(snapshot)

        val rendered = buffer.toString()
        rendered.shouldContain("# TYPE flowmvi_intents_duration_seconds gauge")
        rendered.shouldContain("""intents_duration_seconds{quantile="0.9",run_id="demo-run-id",""")
        rendered.shouldContain("""intents_duration_seconds{quantile="0.99",""")
    }

    "adds EOF marker when trailingEof enabled" {
        val buffer = StringBuilder()
        OpenMetricsSink(AppendableStringSink(buffer)).emit(snapshot)

        buffer.toString().trimEnd().endsWith("# EOF").shouldBeTrue()
    }

    "schema version label is present on samples" {
        val buffer = StringBuilder()
        val sink = OpenMetricsSink(
            delegate = AppendableStringSink(buffer),
            includeHelp = false,
            includeUnit = false
        )

        sink.emit(snapshot)

        buffer.toString().shouldContain("""schema_version="1.0"""")
    }

    "escapes quotes backslashes and newlines in labels" {
        val buffer = StringBuilder()
        val weirdMeta = snapshot.meta.copy(
            storeName = "strange\"store\nx\\y",
            storeId = "id\"with\\slash",
        )
        val sink = OpenMetricsSink(
            delegate = AppendableStringSink(buffer),
            includeHelp = false,
            includeUnit = false
        )

        sink.emit(snapshot.copy(meta = weirdMeta))

        val rendered = buffer.toString()
        rendered.shouldContain("""store="strange\"store\nx\\y"""")
        rendered.shouldContain("""store_id="id\"with\\slash"""")
    }

    "formats NaN and infinities in gauge samples" {
        val buffer = StringBuilder()
        val modified = snapshot.copy(
            intents = snapshot.intents.copy(opsPerSecond = Double.NaN),
            actions = snapshot.actions.copy(opsPerSecond = Double.POSITIVE_INFINITY),
            state = snapshot.state.copy(opsPerSecond = Double.NEGATIVE_INFINITY),
        )
        val sink = OpenMetricsSink(
            delegate = AppendableStringSink(buffer),
            includeHelp = false,
            includeUnit = false
        )

        sink.emit(modified)

        val rendered = buffer.toString()
        rendered.shouldContain("""flowmvi_intents_ops_per_second{run_id="demo-run-id""")
        rendered.shouldContain(""" NaN""")
        rendered.shouldContain("""flowmvi_actions_ops_per_second{run_id="demo-run-id""")
        rendered.shouldContain(""" +Inf""")
        rendered.shouldContain("""flowmvi_state_ops_per_second{run_id="demo-run-id""")
        rendered.shouldContain(""" -Inf""")
    }

    "prometheus sink omits EOF and units by default" {
        val buffer = StringBuilder()
        PrometheusSink(AppendableStringSink(buffer), includeHelp = true, includeTimestamp = false).emit(snapshot)

        val rendered = buffer.toString()
        rendered.shouldContain("# TYPE flowmvi_intents_total counter")
        rendered.shouldNotContain("# EOF")
        rendered.shouldNotContain("# UNIT")
    }
})
