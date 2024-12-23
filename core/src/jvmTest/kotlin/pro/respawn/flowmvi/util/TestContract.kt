package pro.respawn.flowmvi.util

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.LambdaIntent

sealed interface TestState : MVIState {
    data object Some : TestState
    data class SomeData<T>(val data: T) : TestState
}

sealed interface TestAction : MVIAction {
    data object Some : TestAction
    data class SomeData<T>(val data: T) : TestAction
}

typealias TestIntent = LambdaIntent<TestState, TestAction>
