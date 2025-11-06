package pro.respawn.flowmvi.savedstate.dsl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import pro.respawn.flowmvi.savedstate.api.Saver

class RecoveringSaverTest : FreeSpec({

    "Given a RecoveringSaver" - {

        "when the delegate saver works normally" - {
            "then save and restore should work without recovery" {
                val testState = "test-state"
                var savedState: String? = null

                val delegateSaver = Saver(
                    save = { savedState = it },
                    restore = { savedState }
                )

                val recoveringSaver = RecoveringSaver(delegateSaver) { null }

                recoveringSaver.save(testState)
                savedState shouldBe testState

                val result = recoveringSaver.restore()
                result shouldBe testState
            }
        }

        "when the delegate saver throws exceptions during save" - {
            "and recovery function returns a value" - {
                "then save should not throw and recovery should be called" {
                    val testException = RuntimeException("Save failed")
                    var recoveryCallCount = 0
                    var lastRecoveryException: Exception? = null
                    val recoveryValue = "recovered-state"

                    val delegateSaver = Saver(
                        save = { throw testException },
                        restore = { "restored" }
                    )

                    val recoveringSaver = RecoveringSaver(delegateSaver) { e ->
                        recoveryCallCount++
                        lastRecoveryException = e
                        recoveryValue
                    }

                    recoveringSaver.save("test")
                    recoveryCallCount shouldBe 1
                    lastRecoveryException shouldBe testException
                }
            }

            "and recovery function returns null" - {
                "then save should not throw and recovery should be called" {
                    val testException = RuntimeException("Save failed")
                    var recoveryCallCount = 0
                    var lastRecoveryException: Exception? = null

                    val delegateSaver = Saver(
                        save = { throw testException },
                        restore = { "restored" }
                    )

                    val recoveringSaver = RecoveringSaver(delegateSaver) { e ->
                        recoveryCallCount++
                        lastRecoveryException = e
                        null
                    }

                    recoveringSaver.save("test")
                    recoveryCallCount shouldBe 1
                    lastRecoveryException shouldBe testException
                }
            }
        }

        "when the delegate saver throws exceptions during restore" - {
            "and recovery function returns a value" - {
                "then restore should return recovery value" {
                    val testException = RuntimeException("Restore failed")
                    var recoveryCallCount = 0
                    var lastRecoveryException: Exception? = null
                    val recoveryValue = "recovered-state"

                    val delegateSaver = Saver<String>(
                        save = { },
                        restore = { throw testException }
                    )

                    val recoveringSaver = RecoveringSaver(delegateSaver) { e ->
                        recoveryCallCount++
                        lastRecoveryException = e
                        recoveryValue
                    }

                    val result = recoveringSaver.restore()
                    result shouldBe recoveryValue
                    recoveryCallCount shouldBe 1
                    lastRecoveryException shouldBe testException
                }
            }

            "and recovery function returns null" - {
                "then restore should return null" {
                    val testException = RuntimeException("Restore failed")
                    var recoveryCallCount = 0
                    var lastRecoveryException: Exception? = null

                    val delegateSaver = Saver<String>(
                        save = { },
                        restore = { throw testException }
                    )

                    val recoveringSaver = RecoveringSaver(delegateSaver) { e ->
                        recoveryCallCount++
                        lastRecoveryException = e
                        null
                    }

                    val result = recoveringSaver.restore()
                    result shouldBe null
                    recoveryCallCount shouldBe 1
                    lastRecoveryException shouldBe testException
                }
            }
        }

        "when CancellationException is thrown" - {
            "during save" - {
                "then CancellationException should be re-thrown" {
                    val cancellationException = CancellationException("Cancelled")

                    val delegateSaver = Saver<String>(
                        save = { throw cancellationException },
                        restore = { "restored" }
                    )

                    val recoveringSaver = RecoveringSaver(delegateSaver) { "recovered" }

                    shouldThrow<CancellationException> {
                        recoveringSaver.save("test")
                    }
                }
            }

            "during restore" - {
                "then CancellationException should be re-thrown" {
                    val cancellationException = CancellationException("Cancelled")

                    val delegateSaver = Saver<String>(
                        save = { },
                        restore = { throw cancellationException }
                    )

                    val recoveringSaver = RecoveringSaver(delegateSaver) { "recovered" }

                    shouldThrow<CancellationException> {
                        recoveringSaver.restore()
                    }
                }
            }
        }

        "when testing backwards compatibility with deprecated recover" - {
            "and new recover returns null but deprecated recover returns a value" - {
                "then deprecated recover should be called for save" {
                    val testException = RuntimeException("Save failed")
                    val deprecatedRecoveryValue = "deprecated-recovery"
                    var deprecatedRecoverCallCount = 0

                    val delegateSaver = object : Saver<String> {
                        override suspend fun save(state: String?) {
                            throw testException
                        }
                        override suspend fun restore(): String = "restored"

                        @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
                        override suspend fun recover(e: Exception): String {
                            deprecatedRecoverCallCount++
                            return deprecatedRecoveryValue
                        }
                    }

                    val recoveringSaver = RecoveringSaver(delegateSaver) { null }

                    recoveringSaver.save("test")
                    deprecatedRecoverCallCount shouldBe 1
                }

                "then deprecated recover should be called for restore" {
                    val testException = RuntimeException("Restore failed")
                    val deprecatedRecoveryValue = "deprecated-recovery"
                    var deprecatedRecoverCallCount = 0

                    val delegateSaver = object : Saver<String> {
                        override suspend fun save(state: String?) {}
                        override suspend fun restore(): String? {
                            throw testException
                        }

                        @Suppress("DEPRECATION")
                        override suspend fun recover(e: Exception): String? {
                            deprecatedRecoverCallCount++
                            return deprecatedRecoveryValue
                        }
                    }

                    val recoveringSaver = RecoveringSaver(delegateSaver) { null }

                    val result = recoveringSaver.restore()
                    result shouldBe deprecatedRecoveryValue
                    deprecatedRecoverCallCount shouldBe 1
                }
            }

            "and new recover returns null and deprecated recover throws custom exception" - {
                "then deprecated recover exception should propagate for save" {
                    val testException = RuntimeException("Save failed")
                    val deprecatedException = RuntimeException("Deprecated recover failed")
                    var deprecatedRecoverCallCount = 0

                    val delegateSaver = object : Saver<String> {
                        override suspend fun save(state: String?) {
                            throw testException
                        }
                        override suspend fun restore(): String? = "restored"

                        @Suppress("DEPRECATION")
                        override suspend fun recover(e: Exception): String? {
                            deprecatedRecoverCallCount++
                            throw deprecatedException
                        }
                    }

                    val recoveringSaver = RecoveringSaver(delegateSaver) { null }

                    // Should throw the custom exception
                    shouldThrow<RuntimeException> {
                        recoveringSaver.save("test")
                    }.message shouldBe "Deprecated recover failed"
                    deprecatedRecoverCallCount shouldBe 1
                }

                "then deprecated recover exception should propagate for restore" {
                    val testException = RuntimeException("Restore failed")
                    val deprecatedException = RuntimeException("Deprecated recover failed")
                    var deprecatedRecoverCallCount = 0

                    val delegateSaver = object : Saver<String> {
                        override suspend fun save(state: String?) {}
                        override suspend fun restore(): String? {
                            throw testException
                        }

                        @Suppress("DEPRECATION")
                        override suspend fun recover(e: Exception): String? {
                            deprecatedRecoverCallCount++
                            throw deprecatedException
                        }
                    }

                    val recoveringSaver = RecoveringSaver(delegateSaver) { null }

                    shouldThrow<RuntimeException> {
                        recoveringSaver.restore()
                    }.message shouldBe "Deprecated recover failed"
                    deprecatedRecoverCallCount shouldBe 1
                }
            }

            "and new recover returns a value" - {
                "then deprecated recover should not be called" {
                    val testException = RuntimeException("Save failed")
                    val newRecoveryValue = "new-recovery"
                    var deprecatedRecoverCallCount = 0

                    val delegateSaver = object : Saver<String> {
                        override suspend fun save(state: String?) {
                            throw testException
                        }
                        override suspend fun restore(): String? {
                            throw testException
                        }

                        @Suppress("DEPRECATION")
                        override suspend fun recover(e: Exception): String? {
                            deprecatedRecoverCallCount++
                            return "should-not-be-called"
                        }
                    }

                    val recoveringSaver = RecoveringSaver(delegateSaver) { newRecoveryValue }

                    recoveringSaver.save("test")
                    val result = recoveringSaver.restore()

                    result shouldBe newRecoveryValue
                    deprecatedRecoverCallCount shouldBe 0
                }
            }

            "and new recover returns null and deprecated recover uses default implementation" - {
                "then default deprecated recover exception should be ignored for save" {
                    val testException = RuntimeException("Save failed")

                    val delegateSaver = Saver<String>(
                        save = { throw testException },
                        restore = { "restored" }
                    )

                    val recoveringSaver = RecoveringSaver(delegateSaver) { null }

                    // Should not throw - default implementation exception should be caught
                    recoveringSaver.save("test")
                }

                "then default deprecated recover exception should be ignored for restore" {
                    val testException = RuntimeException("Restore failed")

                    val delegateSaver = Saver<String>(
                        save = { },
                        restore = { throw testException }
                    )

                    val recoveringSaver = RecoveringSaver(delegateSaver) { null }

                    val result = recoveringSaver.restore()
                    result shouldBe null
                }
            }
        }
    }
})
