@file:Suppress("TestFunctionName")

package pro.respawn.flowmvi.util

import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.StateStrategy
import pro.respawn.flowmvi.decorator.DecoratorBuilder
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.dsl.BuildStore
import pro.respawn.flowmvi.dsl.LambdaIntent
import pro.respawn.flowmvi.dsl.reduceLambdas
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.plugins.TimeTravel
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.plugins.timeTravel

internal typealias TestTimeTravel = TimeTravel<TestState, LambdaIntent<TestState, TestAction>, TestAction>

@OptIn(ExperimentalFlowMVIAPI::class)
internal fun testDecorator(
    configure: DecoratorBuilder<TestState, TestIntent, TestAction>.() -> Unit
) = decorator(configure)

internal fun testTimeTravel() = TestTimeTravel()

internal fun testStore(
    timeTravel: TestTimeTravel = testTimeTravel(),
    initial: TestState = TestState.Some,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    configure: BuildStore<TestState, LambdaIntent<TestState, TestAction>, TestAction> = {},
) = store(initial) {
    configure {
        debuggable = true
        allowTransientSubscriptions = true
        name = "TestStore"
        actionShareBehavior = behavior
        stateStrategy = StateStrategy.Atomic()
        logger = PlatformStoreLogger
    }
    enableLogging()
    timeTravel(timeTravel)
    configure()
    reduceLambdas()
}
