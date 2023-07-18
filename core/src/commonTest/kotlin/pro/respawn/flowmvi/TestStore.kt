@file:Suppress("TestFunctionName")

package pro.respawn.flowmvi

import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.Reduce
import pro.respawn.flowmvi.plugins.consoleLoggingPlugin
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.util.TestInfo
import pro.respawn.flowmvi.util.testPlugin

internal fun testStore(
    holder: TestInfo<TestState, TestIntent, TestAction> = TestInfo(),
    initial: TestState = TestState.Some,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    reduce: Reduce<TestState, TestIntent, TestAction>,
) = store<_, _, _>("TestStore", initial) {
    actionShareBehavior = behavior
    reduce(reduce = reduce)
    install(testPlugin(holder))
    install(consoleLoggingPlugin("Logging"))
}
