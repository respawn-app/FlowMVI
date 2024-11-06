package pro.respawn.flowmvi.test.store

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.plugins.cache
import pro.respawn.flowmvi.plugins.deinit
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.util.testStore

class DeinitOrderTest : FreeSpec({
    "given store with cache plugin" - {
        val store = testStore {
            var deinits = 0
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
        }
    }
})
