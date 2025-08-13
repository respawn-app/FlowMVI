package pro.respawn.flowmvi.savedstate.dsl

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class CallbackSaverTest : FreeSpec({

    "Given a CallbackSaver" - {

        "when the delegate saver works normally" - {
            "then save and restore should work and callbacks should be called" {
                val testState = "test-state"
                var savedState: String? = null
                var saveCallbackCalled = false
                var restoreCallbackCalled = false
                var saveCallbackValue: String? = null
                var restoreCallbackValue: String? = null

                val delegateSaver = Saver(
                    save = { savedState = it },
                    restore = { savedState }
                )

                val callbackSaver = CallbackSaver(
                    delegate = delegateSaver,
                    onSave = {
                        saveCallbackCalled = true
                        saveCallbackValue = it
                    },
                    onRestore = {
                        restoreCallbackCalled = true
                        restoreCallbackValue = it
                    }
                )

                callbackSaver.save(testState)
                saveCallbackCalled shouldBe true
                saveCallbackValue shouldBe testState
                savedState shouldBe testState

                val result = callbackSaver.restore()
                restoreCallbackCalled shouldBe true
                restoreCallbackValue shouldBe testState
                result shouldBe testState
            }
        }

        "when saving null state" - {
            "then save callback should be called with null" {
                var savedState: String? = null
                var saveCallbackCalled = false
                var saveCallbackValue: String? = "not-null"

                val delegateSaver = Saver(
                    save = { savedState = it },
                    restore = { savedState }
                )

                val callbackSaver = CallbackSaver(
                    delegate = delegateSaver,
                    onRestore = {},
                    onSave = {
                        saveCallbackCalled = true
                        saveCallbackValue = it
                    }
                )

                callbackSaver.save(null)
                saveCallbackCalled shouldBe true
                saveCallbackValue shouldBe null
                savedState shouldBe null
            }
        }

        "when restoring null state" - {
            "then restore callback should be called with null" {
                var restoreCallbackCalled = false
                var restoreCallbackValue: String? = "not-null"

                val delegateSaver = Saver(
                    save = { },
                    restore = { null }
                )

                val callbackSaver = CallbackSaver(
                    delegate = delegateSaver,
                    onSave = {},
                    onRestore = {
                        restoreCallbackCalled = true
                        restoreCallbackValue = it
                    }
                )

                val result = callbackSaver.restore()
                restoreCallbackCalled shouldBe true
                restoreCallbackValue shouldBe null
                result shouldBe null
            }
        }

        "when no callbacks are provided" - {
            "then it should work like a normal delegate" {
                val testState = "test-state"
                var savedState: String? = null

                val delegateSaver = Saver(
                    save = { savedState = it },
                    restore = { savedState }
                )

                val callbackSaver = CallbackSaver(
                    delegate = delegateSaver, onSave = {},
                    onRestore = {}
                )

                callbackSaver.save(testState)
                savedState shouldBe testState

                val result = callbackSaver.restore()
                result shouldBe testState
            }
        }

        "when delegate throws exception during save" - {
            "then callback should still be called before exception propagates" {
                val testException = RuntimeException("Save failed")
                var saveCallbackCalled = false
                var saveCallbackValue: String? = null
                var exceptionThrown = false

                val delegateSaver = Saver<String>(
                    save = { throw testException },
                    restore = { "restored" }
                )

                val callbackSaver = CallbackSaver(
                    delegate = delegateSaver,
                    onRestore = {},
                    onSave = {
                        saveCallbackCalled = true
                        saveCallbackValue = it
                    }
                )

                try {
                    callbackSaver.save("test")
                } catch (e: RuntimeException) {
                    exceptionThrown = true
                    e shouldBe testException
                }

                saveCallbackCalled shouldBe true
                saveCallbackValue shouldBe "test"
                exceptionThrown shouldBe true
            }
        }

        "when delegate throws exception during restore" - {
            "then restore callback should not be called" {
                val testException = RuntimeException("Restore failed")
                var restoreCallbackCalled = false
                var exceptionThrown = false

                val delegateSaver = Saver<String>(
                    save = { },
                    restore = { throw testException }
                )

                val callbackSaver = CallbackSaver(
                    delegate = delegateSaver,
                    onSave = {},
                    onRestore = {
                        restoreCallbackCalled = true
                    }
                )

                try {
                    callbackSaver.restore()
                } catch (e: RuntimeException) {
                    exceptionThrown = true
                    e shouldBe testException
                }

                restoreCallbackCalled shouldBe false
                exceptionThrown shouldBe true
            }
        }
    }
})
