@file:Suppress("Filename")

package pro.respawn.flowmvi.util

import io.kotest.core.spec.style.scopes.FreeSpecContainerScope
import io.kotest.core.spec.style.scopes.FreeSpecTerminalScope
import io.kotest.core.test.testCoroutineScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.store.MVIStore

suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction>
MVIStore<S, I, A>.launched(scope: CoroutineScope, block: MVIStore<S, I, A>.() -> Unit) = start(scope).apply {
    block()
    cancel()
    join()
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun FreeSpecContainerScope.idle() = testCoroutineScheduler.advanceUntilIdle()

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun FreeSpecTerminalScope.idle() = testCoroutineScheduler.advanceUntilIdle()

@OptIn(ExperimentalCoroutinesApi::class)
fun kotlinx.coroutines.test.TestScope.idle() = advanceUntilIdle()
