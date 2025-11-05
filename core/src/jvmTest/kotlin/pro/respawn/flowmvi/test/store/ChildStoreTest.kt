package pro.respawn.flowmvi.test.store

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.installChild
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravel

class ChildStoreTest : FreeSpec({
    configure()
    val timeTravel = testTimeTravel()
    afterEach { timeTravel.reset() }

    "given a parent store" - {
        "and a child store" - {
            val childStore = testStore()

            "when child store is installed" - {
                val store = testStore {
                    installChild(childStore)
                }

                "then parent store is started and child store is automatically activated" {
                    // Start the parent store
                    val parentLc = store.start(this).apply { awaitStartup() }

                    parentLc.isActive shouldBe true
                    childStore.isActive shouldBe true

                    store.close()
                    idle()

                    parentLc.isActive shouldBe false
                    store.isActive shouldBe false
                    childStore.isActive shouldBe false
                }
            }
        }
        "and a child store that never finishes startup" - {
            val deferred = CompletableDeferred<Unit>()
            val childStore = testStore {
                init { deferred.await() }
            }
            "and child is installed with blocking = true" - {
                val store = testStore {
                    installChild(childStore, blocking = true, force = true)
                }
                "then will not finish startup until child finishes" {
                    val lc = store.start(this)
                    idle()
                    store.isActive shouldBe true
                    store.isStarted shouldBe false
                    childStore.isActive shouldBe true
                    childStore.isStarted shouldBe false

                    deferred.complete(Unit)
                    idle()

                    store.isStarted shouldBe true
                    childStore.isStarted shouldBe true

                    lc.closeAndWait()
                }
            }
        }
    }
})
