@file:Suppress("TestFunctionName")

package pro.respawn.flowmvi

internal fun TestStore(
    initial: TestState,
    behavior: ActionShareBehavior,
    recover: Recover<TestState> = { throw it },
    reduce: Reduce<TestState, TestIntent, TestAction>,
) = MVIStore(initial, behavior, recover = recover, reduce = reduce)
