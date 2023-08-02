@file:Suppress("TestFunctionName")

package pro.respawn.flowmvi.util

import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.dsl.BuildStore
import pro.respawn.flowmvi.dsl.LambdaIntent
import pro.respawn.flowmvi.dsl.reduceLambdas
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.TimeTravelPlugin
import pro.respawn.flowmvi.plugins.consoleLoggingPlugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin

internal fun testTimeTravelPlugin() = timeTravelPlugin<TestState, LambdaIntent<TestState, TestAction>, TestAction>()

internal fun testStore(
    timeTravel: TimeTravelPlugin<TestState, LambdaIntent<TestState, TestAction>, TestAction> = timeTravelPlugin(),
    initial: TestState = TestState.Some,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    configure: BuildStore<TestState, LambdaIntent<TestState, TestAction>, TestAction> = {},
) = store<_, _, _>(initial) {
    debuggable = false
    name = "TestStore"
    actionShareBehavior = behavior
    install(timeTravel)
    reduceLambdas()
    install(consoleLoggingPlugin(name))
    configure()
}
