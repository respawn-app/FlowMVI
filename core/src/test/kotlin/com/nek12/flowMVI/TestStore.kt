@file:Suppress("TestFunctionName")

package com.nek12.flowMVI

import TestAction
import TestIntent
import TestState

internal fun TestStore(
    initialState: TestState,
    behavior: ActionShareBehavior,
    recover: MVIStoreScope<TestState, TestIntent, TestAction>.(e: Exception) -> TestState = { throw it },
    reduce: suspend MVIStoreScope<TestState, TestIntent, TestAction>.(TestIntent) -> TestState,
) = MVIStore(initialState, behavior, recover = recover, reduce = reduce)
