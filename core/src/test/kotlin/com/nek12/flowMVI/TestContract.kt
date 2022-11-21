import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIState

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
