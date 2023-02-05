@file:Suppress("TestFunctionName")

package com.nek12.flowMVI

internal fun TestStore(
    initialState: TestState,
    behavior: ActionShareBehavior,
    recover: Recover<TestState> = { throw it },
    reduce: Reducer<TestState, TestIntent, TestAction>,
) = MVIStore(initialState, behavior, recover = recover, reduce = reduce)