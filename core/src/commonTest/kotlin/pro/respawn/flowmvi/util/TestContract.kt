package pro.respawn.flowmvi.util

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

sealed class TestState : MVIState {
    data object Some : TestState()
    data class SomeData<T>(val data: T) : TestState()
}

sealed class TestAction : MVIAction {
    data object Some : TestAction()
    data class SomeData<T>(val data: T) : TestAction()
}

sealed class TestIntent : MVIIntent {
    data object Some : TestIntent()
    data class SomeData<T>(val data: T) : TestIntent()
}
