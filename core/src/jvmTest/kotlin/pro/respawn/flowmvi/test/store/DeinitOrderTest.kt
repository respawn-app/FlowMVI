package pro.respawn.flowmvi.test.store

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.plugins.cache
import pro.respawn.flowmvi.plugins.deinit
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore

class DeinitOrderTest : FreeSpec({
    configure()
    "given store with cache plugin" - {
        var deinits = 0
        val store = testStore {
            val firstValue by cache { 0 }
            deinit {
                deinits shouldBe 1
                ++deinits
                firstValue shouldBe 0
            }
            val secondValue by cache { 1 }
            deinit {
                deinits shouldBe 0
                secondValue shouldBe 1
                ++deinits
            }
        }
        "then onStop can access plugins declared above" {
            store.subscribeAndTest {
                // nothing to do in the body
            }
            idle()
            deinits shouldBe 2
        }
    }
})
