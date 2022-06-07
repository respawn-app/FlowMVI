import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIState

sealed class TestState: MVIState {
    object Some: TestState()
    data class SomeData(val data: String): TestState()
}

sealed class TestAction: MVIAction {
    object Some: TestAction()
    data class SomeData(val data: String): TestAction()
}

sealed class TestIntent: MVIIntent {
    object Some: TestIntent()
    data class SomeData(val data: String): TestIntent()
}
