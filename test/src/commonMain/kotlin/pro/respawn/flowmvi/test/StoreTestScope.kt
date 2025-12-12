package pro.respawn.flowmvi.test

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeout
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Store
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * A class which implements a dsl for testing [Store].
 */
@OptIn(InternalFlowMVIAPI::class)
public class StoreTestScope<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor(
    public val provider: Provider<S, I, A>,
    public val store: Store<S, I, A>,
    public val timeoutMs: Long = 3000L,
) : Store<S, I, A> by store, Provider<S, I, A> by provider {

    override val states: StateFlow<S> by provider::states
    override fun hashCode(): Int = store.hashCode()
    override fun equals(other: Any?): Boolean = store == other
    override suspend fun emit(intent: I): Unit = store.emit(intent)
    override fun intent(intent: I): Unit = store.intent(intent)

    /**
     * Assert that [Provider.state] is equal to [state]
     */
    public suspend inline infix fun I.resultsIn(state: S) {
        emit(this)
        assertEquals(states.value, state, "Expected state to be $state but got ${states.value}")
    }

    /**
     * Assert that [Provider.state]'s state is of type [S]
     */
    public suspend inline fun <reified S> I.resultsIn() {
        emit(this)
        val _ = assertIs<S>(states.value)
    }

    /**
     * Assert that intent [this] passes checks defined in [assertion]
     */
    public suspend inline infix fun I.resultsIn(assertion: () -> Unit) {
        emit(this)
        assertion()
    }

    /**
     * Assert that intent [this] results in [action]
     */
    public suspend inline infix fun I.resultsIn(action: A) {
        emit(this)
        withTimeout(timeoutMs) {
            assertEquals(action, actions.firstOrNull())
        }
    }
}
