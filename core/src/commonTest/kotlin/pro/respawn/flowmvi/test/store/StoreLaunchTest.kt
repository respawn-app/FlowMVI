package pro.respawn.flowmvi.test.store

import io.kotest.core.spec.style.FreeSpec
import pro.respawn.flowmvi.util.testStore

class StoreLaunchTest : FreeSpec({
    "Given store" - {
        val store = testStore()
    }
})
