package pro.respawn.flowmvi.savedstate.dsl

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogger

class LoggingSaverTest : FreeSpec({

    "Given a LoggingSaver" - {
        "when logging around delegate operations" - {
            "then it should pass data to delegate and emit logs with configured level and tag" {
                val savedStates = mutableListOf<String?>()
                val observedLevels = mutableListOf<StoreLogLevel>()
                val observedTags = mutableListOf<String?>()

                val logger = StoreLogger { level, tag, _ ->
                    observedLevels += level
                    observedTags += tag
                }

                val delegate = Saver(
                    save = { savedStates += it },
                    restore = { savedStates.lastOrNull() }
                )

                val saver = LoggingSaver(
                    delegate = delegate,
                    logger = logger,
                    level = StoreLogLevel.Info,
                    tag = "TestSaver"
                )

                saver.save("value")
                saver.restore()

                savedStates shouldBe listOf("value")
                observedLevels shouldBe listOf(StoreLogLevel.Info, StoreLogLevel.Info)
                observedTags shouldBe listOf("TestSaver", "TestSaver")
            }
        }
    }
})
