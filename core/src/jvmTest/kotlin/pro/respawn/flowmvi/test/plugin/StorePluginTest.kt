package pro.respawn.flowmvi.test.plugin

import io.kotest.core.spec.style.FreeSpec
import pro.respawn.flowmvi.util.asUnconfined

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
})
