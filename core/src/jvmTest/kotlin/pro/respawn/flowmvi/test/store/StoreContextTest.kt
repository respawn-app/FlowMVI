package pro.respawn.flowmvi.test.store

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.intent
import pro.respawn.flowmvi.dsl.pipelineContext
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravelPlugin

@OptIn(DelicateStoreApi::class)
class StoreContextTest : FreeSpec({
    asUnconfined()
    val plugin = testTimeTravelPlugin()

    beforeEach {
        plugin.reset()
    }

    "Given a store" - {
        val store = testStore(plugin)

        "then child jobs context contains the pipeline" {
            store.subscribeAndTest {
                intent i@{
                    this@i.launch a@{
                        this@a.launch {
                            pipelineContext<TestState, TestIntent, TestAction>().shouldNotBeNull()
                        }
                    }
                }
            }
        }
        "then subscriber's context does not have the pipeline" {
            store.subscribeAndTest {
                coroutineContext[PipelineContext].shouldBeNull()
            }
        }
    }
})
