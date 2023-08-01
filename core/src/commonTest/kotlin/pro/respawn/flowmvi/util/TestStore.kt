@file:Suppress("TestFunctionName")

package pro.respawn.flowmvi.util

import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.dsl.BuildStore
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.TimeTravelPlugin
import pro.respawn.flowmvi.plugins.consoleLoggingPlugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin

internal fun testStore(
    timeTravel: TimeTravelPlugin<TestState, TestIntent, TestAction> = timeTravelPlugin(),
    initial: TestState = TestState.Some,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    configure: BuildStore<TestState, TestIntent, TestAction> = {},
) = store<_, _, _>(initial) {
    debuggable = false
    name = "TestStore"
    actionShareBehavior = behavior
    install(timeTravel)
    install(consoleLoggingPlugin(name))
    configure()
}
