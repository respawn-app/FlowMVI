@file:Suppress("TestFunctionName")

package com.nek12.flowMVI

import TestAction
import TestIntent
import TestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal fun TestStore(
    initialState: TestState,
    behavior: ActionShareBehavior,
    recover: MVIStore<TestState, TestIntent, TestAction>.(e: Exception) -> TestState = { throw it },
    reduce: suspend MVIStore<TestState, TestIntent, TestAction>.(TestIntent) -> TestState,
) = MVIStore(initialState, behavior, recover, reduce)
