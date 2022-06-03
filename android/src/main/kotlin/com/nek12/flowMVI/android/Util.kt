package com.nek12.flowMVI.android

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Catches exceptions only, rethrowing any throwables
 */
internal inline fun <T> Flow<T>.catchExceptions(crossinline block: suspend (Exception) -> Unit) =
    catch { throwable -> (throwable as? Exception)?.let { block(it) } ?: throw throwable }
