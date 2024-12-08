package pro.respawn.flowmvi.test.store

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.StateStrategy.Atomic
import pro.respawn.flowmvi.dsl.LambdaIntent
import pro.respawn.flowmvi.dsl.intent
import pro.respawn.flowmvi.dsl.state
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.exceptions.RecursiveStateTransactionException
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravel

class StoreStatesTest : FreeSpec({

    asUnconfined()

    val timeTravel = testTimeTravel()
    beforeEach { timeTravel.reset() }

    "given lambdaIntent store" - {
        val store = testStore(timeTravel) {
            configure {
                parallelIntents = false
            }
        }
        "and intent that blocks state" - {
            val blockingIntent = LambdaIntent<TestState, TestAction> {
                launch {
                    updateState {
                        awaitCancellation()
                    }
                }
            }
            "then state is never updated by another intent" {
                store.subscribeAndTest {
                    emit(blockingIntent)
                    idle()
                    intent {
                        updateState {
                            TestState.SomeData(1)
                        }
                    }
                    idle()
                    states.test {
                        awaitItem() shouldBe TestState.Some
                        expectNoEvents()
                    }
                }
            }
            "then withState is never executed" {
                store.subscribeAndTest {
                    emit(blockingIntent)
                    idle()
                    intent {
                        withState {
                            throw AssertionError("WithState was executed")
                        }
                    }
                    idle()
                    states.test {
                        awaitItem() shouldBe TestState.Some
                        expectNoEvents()
                    }
                }
            }
            "then updateStateImmediate overrides the state locks" {
                val newState = TestState.SomeData(1)
                store.subscribeAndTest {
                    states.test {
                        awaitItem() shouldBe TestState.Some
                        emit(blockingIntent)
                        idle()
                        intent { updateStateImmediate { newState } }
                        awaitItem() shouldBe newState
                        state shouldBe newState
                    }
                }
            }
        }
    }
    "given non-reentrant atomic store" - {
        val store = testStore {
            configure {
                stateStrategy = Atomic(reentrant = false)
                parallelIntents = false
            }
        }
        "and recursive intent that blocks state" - {
            val blockingIntent = LambdaIntent<TestState, TestAction> {
                updateState {
                    updateState { awaitCancellation() }
                    this
                }
            }
            // TODO: Throws correctly, but crashes the test suite
            "then store throws".config(enabled = false) {
                shouldThrowExactly<RecursiveStateTransactionException> {
                    store.subscribeAndTest {
                        emit(blockingIntent)
                    }
                }
            }
        }
    }
})
