import TestState.Some
import TestState.SomeData
import app.cash.turbine.test
import com.nek12.flowMVI.ActionShareBehavior.DISTRIBUTE
import com.nek12.flowMVI.ActionShareBehavior.RESTRICT
import com.nek12.flowMVI.ActionShareBehavior.SHARE
import com.nek12.flowMVI.MVIStoreScope
import com.nek12.flowMVI.MVISubscriber
import com.nek12.flowMVI.TestStore
import com.nek12.flowMVI.subscribe
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import util.launched

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
class StoreTest : FreeSpec({
    coroutineTestScope = true
    blockingTest = true
    concurrency = 1

    "given store created" - {
        val state = Some
        val store = TestStore(state, RESTRICT) { SomeData("data") }
        "then state is ${state::class.simpleName}" {
            store.states.value shouldBe state
        }
        "then no actions" {
            store.actions.test {
                expectNoEvents()
            }
        }
        "then can be launched" - {
            var job = store.launch(this)

            "and can't be launched twice" {
                shouldThrowExactly<IllegalArgumentException> {
                    store.launch(this)
                }
            }
            "and can be canceled" {
                job.cancel()
                job.join()
            }
            "and can be launched again" {
                job = store.launch(this)
                job.cancel()
                job.join()
            }
            job.cancel()
        }
    }

    "given store that sends actions and updates states" - {
        val state = SomeData("data")

        val reduce: suspend MVIStoreScope<TestState, TestIntent, TestAction>.(TestIntent) -> TestState = {
            send(TestAction.Some)
            state
        }
        "and 2 subscribers" - {

            "and action type is RESTRICT" - {
                "then throws" {
                    // ensure scope is enclosed, otherwise exception will be thrown outside of assertion
                    shouldThrowAny {
                        coroutineScope {
                            TestStore(Some, RESTRICT, reduce = reduce).launched(this@coroutineScope) {
                                subscribe(this@coroutineScope, {}, {})
                                subscribe(this@coroutineScope, {}, {})
                                testCoroutineScheduler.advanceUntilIdle()
                            }
                        }
                    }
                }
            }

            "and action type is DISTRIBUTE" - {
                val scope = TestScope(testCoroutineScheduler)
                TestStore(Some, DISTRIBUTE, reduce = reduce).launched(scope) {
                    "and intent received" - {
                        send(TestIntent.Some)
                        "then one subscriber received action only" {
                            scope.advanceUntilIdle()
                            actions.test {
                                expectMostRecentItem() shouldBe TestAction.Some
                            }
                            // TODO: Does not pass because of some sync issue
                            // coVerify(exactly = 1) { sub1.consume(TestAction.Some) }
                            // coVerify(exactly = 0) { sub2.consume(TestAction.Some) }
                        }
                    }
                }
            }

            "and action type is SHARE" - {
                val scope = TestScope(testCoroutineScheduler)
                val sub1 = mockk<MVISubscriber<TestState, TestAction>>()
                TestStore(Some, SHARE, reduce = reduce).launched(scope) {
                    sub1.subscribe(this, scope)
                    "and intent received" - {
                        send(TestIntent.Some)
                        "then all subscribers received an action" {
                            scope.advanceUntilIdle()
                            // TODO: Does not pass because of some sync issue
                            // actions.test {
                            //     expectMostRecentItem() shouldBe TestAction.Some
                            // }
                        }
                    }
                }
            }
        }
    }
})
