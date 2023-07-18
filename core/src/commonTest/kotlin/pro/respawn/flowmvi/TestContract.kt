package pro.respawn.flowmvi

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

sealed class TestState : MVIState {
    object Some : TestState()
    data class SomeData<T>(val data: T) : TestState()
}

sealed class TestAction : MVIAction {
    object Some : TestAction()
    data class SomeData<T>(val data: T) : TestAction()
}

sealed class TestIntent : MVIIntent {
    object Some : TestIntent()
    data class SomeData<T>(val data: T) : TestIntent()
}
