@file:Suppress("TestFunctionName")

package pro.respawn.flowmvi.util

import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.dsl.BuildStore
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.Reduce
import pro.respawn.flowmvi.plugins.TimeTravelPlugin
import pro.respawn.flowmvi.plugins.consoleLoggingPlugin
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.timeTravelPlugin

internal fun testStore(
    timeTravel: TimeTravelPlugin<TestState, TestIntent, TestAction> = timeTravelPlugin(),
    initial: TestState = TestState.Some,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    configure: BuildStore<TestState, TestIntent, TestAction> = {},
) = store<_, _, _> {
    actionShareBehavior = behavior
    reduce(reduce = reduce)
    install(timeTravel)
    install(consoleLoggingPlugin("Logging"))
    initial(initial)
    configure()
}
