@file:Suppress("TestFunctionName")

package pro.respawn.flowmvi.util

import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.dsl.BuildStore
import pro.respawn.flowmvi.dsl.LambdaIntent
import pro.respawn.flowmvi.dsl.reduceLambdas
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.plugins.TimeTravel
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.plugins.timeTravel

internal typealias TestTimeTravel = TimeTravel<TestState, LambdaIntent<TestState, TestAction>, TestAction>

internal fun testTimeTravel() = TestTimeTravel()

internal fun testStore(
    timeTravel: TestTimeTravel = testTimeTravel(),
    initial: TestState = TestState.Some,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    configure: BuildStore<TestState, LambdaIntent<TestState, TestAction>, TestAction> = {},
) = store(initial) {
    debuggable = false
    name = "TestStore"
    actionShareBehavior = behavior
    atomicStateUpdates = true
    logger = PlatformStoreLogger
    timeTravel(timeTravel)
    enableLogging()
    configure()
    reduceLambdas()
}
