package com.nek12.flowMVI.android

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Catches exceptions only, rethrowing any throwables
 */
inline fun <T> Flow<T>.catchExceptions(crossinline block: (Exception) -> Unit) =
    catch { (it as? Exception)?.let(block) ?: throw it }
