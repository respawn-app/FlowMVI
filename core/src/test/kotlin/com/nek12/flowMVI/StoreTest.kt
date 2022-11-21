package com.nek12.flowMVI

import TestAction
import TestIntent
import TestState
import TestState.Some
import TestState.SomeData
import app.cash.turbine.test
import com.nek12.flowMVI.ActionShareBehavior.DISTRIBUTE
import com.nek12.flowMVI.ActionShareBehavior.RESTRICT
import com.nek12.flowMVI.ActionShareBehavior.SHARE
import io.kotest.assertions.one
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import util.TestSubscriber
import util.idle
import util.launched
import kotlin.random.Random

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class, ExperimentalKotest::class)
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
        val reduce: Reducer<TestState, TestIntent, TestAction> = { send(TestAction.Some) }
        "and 2 subscribers" - {
            val sub1 = TestSubscriber<TestState, TestAction>()
            val sub2 = TestSubscriber<TestState, TestAction>()
            "and action type is RESTRICT" - {
                "then throws" {
                    // ensure scope is enclosed, otherwise exception will be thrown outside of assertion
                    shouldThrowAny {
                        coroutineScope {
                            TestStore(Some, RESTRICT, reduce = reduce).launched(this@coroutineScope) {
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
                TestStore(Some, DISTRIBUTE, reduce = reduce).launched(this) {
                    "and intent received" - {
                        send(TestIntent.Some)
                        "then one subscriber received action only" {
                            val job1 = sub1.subscribe(this@launched, this)
                            val job2 = sub2.subscribe(this@launched, this)
                            idle()
                            job1.cancel()
                            job2.cancel()
                            sub1.states.shouldContainOnly(Some)
                            sub2.states.shouldContainOnly(Some)
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
                TestStore(Some, SHARE, reduce = reduce).launched(scope) {
                    val job1 = sub1.subscribe(this@launched, scope)
                    val job2 = sub2.subscribe(this@launched, scope)
                    idle()
                    "and intent received" - {
                        send(TestIntent.Some)
                        "then all subscribers received an action" {
                            idle()
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
        var intents = 0
        val initial = SomeData(0)
        val store = TestStore(initial, RESTRICT) { ++intents }

        "given subscriber" - {
            val sub = TestSubscriber<TestState, TestAction>()

            "and multiple parallel state updates" - {
                val scope = this
                store.launched(scope) {
                    val job = sub.subscribe(this@launched, scope)
                    idle()
                    val jobs = 100

                    (1..jobs).map {
                        async {
                            store.updateState<SomeData<Int>, _> {
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
                        sub.states.last() shouldBe SomeData(jobs)
                    }
                    job.cancel()
                }
            }
        }
    }
})
