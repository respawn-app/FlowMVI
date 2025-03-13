package pro.respawn.flowmvi.test.store

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FreeSpec
import pro.respawn.flowmvi.logging.log
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore

class NestedRecoverTest : FreeSpec({
    configure()
    "Given a store that throws during state update in init" - {
        val store = testStore {
            init {
                updateState {
                    throw IllegalArgumentException()
                }
            }
            recover {
                log { "Caught exception $it" }
                null
            }
        }

        "then the store must not throw" {
            shouldNotThrowAny {
                store.subscribeAndTest {
                    idle()
                }
            }
        }
    }
})
