package pro.respawn.flowmvi.test.plugin

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.plugins.AbstractStorePlugin
import pro.respawn.flowmvi.test.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore
import java.util.Collections

class SubUnsubPlugin<S : MVIState, I : MVIIntent, A : MVIAction> : AbstractStorePlugin<S, I, A>() {

    val subs = Collections.synchronizedList(mutableListOf<Int>())
    val unsubs = Collections.synchronizedList(mutableListOf<Int>())

    override suspend fun PipelineContext<S, I, A>.onSubscribe(subscriberCount: Int) {
        subs.add(subscriberCount)
    }

    override suspend fun PipelineContext<S, I, A>.onUnsubscribe(subscriberCount: Int) {
        unsubs.add(subscriberCount)
    }
}

class StorePluginTest : FreeSpec({
    asUnconfined()
    // TODO:
    //   action: emit, action()
    //   intent: emit, action()
    //   recover plugin rethrows
    //   recover plugin doesn't go into an infinite loop
    //   duplicate plugin throws
    //   all store plugin events are invoked
    //   subscriber count is correct
    //   subscriber count decrements correctly
    //   saved state plugin
    //   while subscribed plugin: job cancelled, multiple subs, single sub

    "given plugin that remembers subscription events" - {
        val plugin = SubUnsubPlugin<TestState, TestIntent, TestAction>()
        "and a store" - {
            val store = testStore { install(plugin) }
            "then concurrent subscription count always matches unsubscription count" {
                store.test {
                    idle()
                    val jobs = List(100) {
                        subscribe {
                            awaitCancellation()
                        }
                    }
                    jobs.map { async { it.cancelAndJoin() } }.awaitAll()
                    idle()
                }
                idle()
                plugin.subs.size shouldBe plugin.unsubs.size
            }
        }
    }
})
