package pro.respawn.flowmvi.savedstate.dsl

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogger

class LoggingSaverTest : FreeSpec({

    "Given a LoggingSaver" - {

        "when the delegate saver works normally" - {
            "then save and restore should work and logging should occur" {
                val testState = "test-state"
                var savedState: String? = null
                val logMessages = mutableListOf<String>()
                var logLevel: StoreLogLevel? = null
                var logTag: String? = null

                val mockLogger = StoreLogger { level, tag, message ->
                    logLevel = level
                    logTag = tag
                    logMessages.add(message())
                }

                val delegateSaver = Saver(
                    save = { savedState = it },
                    restore = { savedState }
                )

                val loggingSaver = LoggingSaver(
                    delegate = delegateSaver,
                    logger = mockLogger,
                    level = StoreLogLevel.Debug,
                    tag = "TestSaver"
                )

                loggingSaver.save(testState)
                savedState shouldBe testState
                logMessages.size shouldBe 1
                logMessages[0] shouldBe "Saving state: $testState"
                logLevel shouldBe StoreLogLevel.Debug
                logTag shouldBe "TestSaver"

                logMessages.clear()
                val result = loggingSaver.restore()
                result shouldBe testState
                logMessages.size shouldBe 1
                logMessages[0] shouldBe "Restored state: $testState"
                logLevel shouldBe StoreLogLevel.Debug
                logTag shouldBe "TestSaver"
            }
        }

        "when saving null state" - {
            "then it should log null state" {
                var savedState: String? = null
                val logMessages = mutableListOf<String>()

                val mockLogger = StoreLogger { _, _, message ->
                    logMessages.add(message())
                }

                val delegateSaver = Saver(
                    save = { savedState = it },
                    restore = { savedState }
                )

                val loggingSaver = LoggingSaver(
                    delegate = delegateSaver,
                    logger = mockLogger
                )

                loggingSaver.save(null)
                savedState shouldBe null
                logMessages.size shouldBe 1
                logMessages[0] shouldBe "Saving state: null"
            }
        }

        "when restoring null state" - {
            "then it should log null state" {
                val logMessages = mutableListOf<String>()

                val mockLogger = StoreLogger { _, _, message ->
                    logMessages.add(message())
                }

                val delegateSaver = Saver<String>(
                    save = { },
                    restore = { null }
                )

                val loggingSaver = LoggingSaver(
                    delegate = delegateSaver,
                    logger = mockLogger
                )

                val result = loggingSaver.restore()
                result shouldBe null
                logMessages.size shouldBe 1
                logMessages[0] shouldBe "Restored state: null"
            }
        }

        "when using default parameters" - {
            "then it should use default log level and tag" {
                val testState = "test-state"
                var savedState: String? = null
                var logLevel: StoreLogLevel? = null
                var logTag: String? = null

                val mockLogger = StoreLogger { level, tag, _ ->
                    logLevel = level
                    logTag = tag
                }

                val delegateSaver = Saver(
                    save = { savedState = it },
                    restore = { savedState }
                )

                val loggingSaver = LoggingSaver(
                    delegate = delegateSaver,
                    logger = mockLogger
                )

                loggingSaver.save(testState)
                logLevel shouldBe StoreLogLevel.Trace
                logTag shouldBe "Saver"
            }
        }

        "when using custom log level" - {
            "then it should use the specified level for normal operations" {
                val testState = "test-state"
                var savedState: String? = null
                var saveLogLevel: StoreLogLevel? = null
                var restoreLogLevel: StoreLogLevel? = null

                val mockLogger = StoreLogger { level, _, _ ->
                    if (saveLogLevel == null) {
                        saveLogLevel = level
                    } else {
                        restoreLogLevel = level
                    }
                }

                val delegateSaver = Saver(
                    save = { savedState = it },
                    restore = { savedState }
                )

                val loggingSaver = LoggingSaver(
                    delegate = delegateSaver,
                    logger = mockLogger,
                    level = StoreLogLevel.Warn
                )

                loggingSaver.save(testState)
                saveLogLevel shouldBe StoreLogLevel.Warn

                loggingSaver.restore()
                restoreLogLevel shouldBe StoreLogLevel.Warn
            }
        }
    }
})
