package util

import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIState
import com.nek12.flowMVI.MVIStore
import io.kotest.core.test.testCoroutineScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

suspend inline fun <S: MVIState, I: MVIIntent, A: MVIAction>
        MVIStore<S, I, A>.launched(scope: CoroutineScope, block: MVIStore<S, I, A>.() -> Unit) = launch(scope).apply {
    block()
    cancel()
    join()
}
