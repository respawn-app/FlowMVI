package pro.respawn.flowmvi.test.store

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravel

class StoreLifecycleTest : FreeSpec({
    configure()
    val timeTravel = testTimeTravel()
    afterEach { timeTravel.reset() }

    "Given store" - {
        val store = testStore(timeTravel)

        "when store is started" - {
            "then lifecycle and store share the same state" {
                val lc = store.start(this)

                // Check active state
                lc.isActive shouldBe true
                store.isActive shouldBe true

                // Check started state after idle
                idle()
                lc.isStarted shouldBe true
                store.isStarted shouldBe true

                // Check that awaitStartup completes
                lc.awaitStartup()

                // Check state after closing
                store.close()
                lc.isActive shouldBe false
                store.isActive shouldBe false
                lc.isStarted shouldBe false
                store.isStarted shouldBe false
            }

            "then waiting methods work correctly" {
                val lc = store.start(this)

                // Test awaitUntilClosed
                var awaitCompleted = false
                launch {
                    lc.awaitUntilClosed()
                    awaitCompleted = true
                }
                idle()
                awaitCompleted shouldBe false

                // Test closeAndWait
                var closeCompleted = false
                launch {
                    lc.closeAndWait()
                    closeCompleted = true
                }
                idle()
                closeCompleted shouldBe true

                // Verify final state
                lc.isActive shouldBe false
                store.isActive shouldBe false
            }
        }

        "when store is restarted" - {
            "then lifecycles behave correctly" {
                // Create a new store for this test
                val newStore = testStore()

                // First lifecycle
                val lc1 = newStore.start(this)
                lc1.isActive shouldBe true
                lc1.close()
                lc1.isActive shouldBe false

                // Wait for the store to fully close
                idle()

                // Second lifecycle
                val lc2 = newStore.start(this)
                lc2.isActive shouldBe true
                lc1.isActive shouldBe false // Previous lifecycle remains closed
                lc2.close()
                lc2.isActive shouldBe false
            }
        }
    }
})
