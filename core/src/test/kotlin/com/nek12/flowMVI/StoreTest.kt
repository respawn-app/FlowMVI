package com.nek12.flowMVI

import app.cash.turbine.test
import com.nek12.flowMVI.ActionShareBehavior.DISTRIBUTE
import com.nek12.flowMVI.ActionShareBehavior.RESTRICT
import com.nek12.flowMVI.ActionShareBehavior.SHARE
import io.kotest.assertions.one
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import util.TestSubscriber
import util.idle
import util.launched
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(
    ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class, ExperimentalKotest::class,
    ExperimentalTime::class
)
class StoreTest : FreeSpec({
    coroutineTestScope = true
    blockingTest = true
    concurrency = 1

    "given store created" - {
        val state = TestState.Some
        val store = TestStore(state, RESTRICT) { updateState { TestState.SomeData("data") } }
        "then state is ${state::class.simpleName}" {
            store.states.value shouldBe state
        }
        "then no actions" {
            store.actions.test {
                expectNoEvents()
            }
        }
        "then can be launched" - {
            var job = store.start(this)

            "and can't be launched twice" {
                shouldThrowExactly<IllegalArgumentException> {
                    store.start(this)
                }
            }
            "and can be canceled" {
                job.cancel()
                job.join()
            }
            "and can be launched again" {
                job = store.start(this)
                job.cancel()
                job.join()
            }
            job.cancel()
        }
    }

    "given store that sends actions and updates states" - {
        val reduce: Reducer<TestState, TestIntent, TestAction> = { send(TestAction.Some) }
        "and 2 subscribers" - {
            val sub1 = TestSubscriber<TestState, TestAction>()
            val sub2 = TestSubscriber<TestState, TestAction>()
            "and action type is RESTRICT" - {
                "then throws" {
                    // ensure scope is enclosed, otherwise exception will be thrown outside of assertion
                    shouldThrowAny {
                        coroutineScope {
                            TestStore(TestState.Some, RESTRICT, reduce = reduce).launched(this@coroutineScope) {
                                subscribe(this@coroutineScope, {}, {})
                                subscribe(this@coroutineScope, {}, {})
                            }
                        }
                    }
                }
            }
            sub1.reset()
            sub2.reset()

            "and action type is DISTRIBUTE" - {
                TestStore(TestState.Some, DISTRIBUTE, reduce = reduce).launched(this) {
                    "and intent received" - {
                        send(TestIntent.Some)
                        "then one subscriber received action only" {
                            val job1 = sub1.subscribe(this@launched, this)
                            val job2 = sub2.subscribe(this@launched, this)
                            idle()
                            job1.cancel()
                            job2.cancel()
                            sub1.states.shouldContainOnly(TestState.Some)
                            sub2.states.shouldContainOnly(TestState.Some)
                            one {
                                sub1.actions.shouldContainExactly(TestAction.Some)
                                sub2.actions.shouldContainExactly(TestAction.Some)
                            }
                        }
                    }
                }
            }

            sub1.reset()
            sub2.reset()

            "and action type is SHARE" - {
                val scope = this
                TestStore(TestState.Some, SHARE, reduce = reduce).launched(scope) {
                    val job1 = sub1.subscribe(this@launched, scope)
                    val job2 = sub2.subscribe(this@launched, scope)
                    idle()
                    "and intent received" - {
                        send(TestIntent.Some)
                        idle()
                        "then all subscribers received an action" {
                            sub1.actions.shouldContainExactly(TestAction.Some)
                            sub2.actions.shouldContainExactly(TestAction.Some)
                        }
                    }
                    job1.cancel()
                    job2.cancel()
                }
            }
            sub1.reset()
            sub2.reset()
        }
    }

    "given store" - {
        val initial = TestState.SomeData(0)

        "given subscriber" - {
            "and multiple parallel state updates" - {
                val scope = this
                var intents = 0
                val sub = TestSubscriber<TestState, TestAction>()
                TestStore(initial, RESTRICT) { ++intents }.launched(scope) {
                    val job = sub.subscribe(this@launched, scope)
                    idle()
                    val jobs = 100

                    (1..jobs).map {
                        async {
                            updateState<TestState.SomeData<Int>, _> {
                                println("updating state $data")
                                delay(Random.nextLong(10, 100))
                                copy(data = data + 1)
                            }
                        }
                    }.awaitAll()

                    scope.idle()

                    "then has exact amount of state updates" {
                        sub.states shouldHaveSize jobs + 1 // with initial state
                    }
                    "then last action contains latest state" {
                        sub.states.last() shouldBe TestState.SomeData(jobs)
                    }
                    job.cancel()
                }
            }

            "and withState is not reentrant" {
                TestStore(initial, RESTRICT) {}.launched(this) {
                    shouldThrowExactly<TimeoutCancellationException> {
                        withTimeout(3000) { // this will actually skip everything because of the scheduler
                            withState {
                                withState { } // should deadlock here
                            }
                        }
                        idle()
                    }
                }
            }
        }
    }
})
