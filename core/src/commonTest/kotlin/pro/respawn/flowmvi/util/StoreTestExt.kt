@file:Suppress("Filename")
@file:OptIn(ExperimentalStdlibApi::class)

package pro.respawn.flowmvi.util

import io.kotest.core.spec.style.scopes.FreeSpecContainerScope
import io.kotest.core.spec.style.scopes.FreeSpecTerminalScope
import io.kotest.core.test.testCoroutineScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Store<S, I, A>.test(
    crossinline block: suspend Store<S, I, A>.() -> Unit
) = coroutineScope {
    val job = start(this)
    block()
    job.cancelAndJoin()
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun FreeSpecContainerScope.idle() = testCoroutineScheduler.advanceUntilIdle()

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun FreeSpecTerminalScope.idle() = testCoroutineScheduler.advanceUntilIdle()

@OptIn(ExperimentalCoroutinesApi::class)
fun kotlinx.coroutines.test.TestScope.idle() = advanceUntilIdle()
