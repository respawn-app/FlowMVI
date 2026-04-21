@file:OptIn(InternalFlowMVIAPI::class)

package pro.respawn.flowmvi.test.plugin

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.exceptions.InvalidStateException
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.transitions
import pro.respawn.flowmvi.plugins.transitionsPlugin
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle

// region Contract types for FSM testing

private sealed interface FSMState : MVIState {
    data object Loading : FSMState
    data class Content(val items: List<String> = emptyList()) : FSMState
    data class Error(val message: String) : FSMState
}

private sealed interface FSMIntent : MVIIntent {
    data object Load : FSMIntent
    data class DataLoaded(val items: List<String>) : FSMIntent
    data class LoadFailed(val message: String) : FSMIntent
    data object Refresh : FSMIntent
    data object Retry : FSMIntent
    data class UpdateFilter(val filter: String) : FSMIntent
    data object Unhandled : FSMIntent
}

private sealed interface FSMAction : MVIAction {
    data class ShowError(val message: String) : FSMAction
    data class NavigateTo(val screen: String) : FSMAction
}

// endregion

// region Plugin factories

private fun testFsmPlugin() = transitionsPlugin<FSMState, FSMIntent, FSMAction> {
    state<FSMState.Loading> {
        on<FSMIntent.Load> {
            // no-op handler, just consumes the intent
        }
        on<FSMIntent.DataLoaded> {
            transitionTo(FSMState.Content(it.items))
        }
        on<FSMIntent.LoadFailed> {
            transitionTo(FSMState.Error(it.message), FSMAction.ShowError(it.message))
        }
    }
    state<FSMState.Content> {
        on<FSMIntent.Refresh> {
            transitionTo(FSMState.Loading)
        }
        on<FSMIntent.UpdateFilter> {
            transitionTo(state.copy(items = state.items + it.filter))
        }
    }
    state<FSMState.Error> {
        on<FSMIntent.Retry> {
            transitionTo(FSMState.Loading)
        }
    }
}

// endregion

class TransitionsPluginTest : FreeSpec({
    configure()

    "Given an FSM transitions plugin" - {
        val plugin = testFsmPlugin()

        "and the store is in Loading state" - {
            plugin.test(FSMState.Loading) {

                "When DataLoaded intent is sent" - {
                    val items = listOf("a", "b", "c")
                    val result = onIntent(FSMIntent.DataLoaded(items))

                    "Then state transitions to Content" {
                        states.value shouldBe FSMState.Content(items)
                    }
                    "Then intent is consumed" {
                        result.shouldBeNull()
                    }
                }
            }
        }

        "and transitionTo with sideEffect emits action" - {
            plugin.test(FSMState.Loading) {
                onStart()

                "When LoadFailed intent is sent" - {
                    val result = onIntent(FSMIntent.LoadFailed("network error"))

                    "Then state transitions to Error" {
                        states.value shouldBe FSMState.Error("network error")
                    }
                    "Then action is emitted via timeTravel" {
                        timeTravel.actions.last() shouldBe FSMAction.ShowError("network error")
                    }
                    "Then intent is consumed" {
                        result.shouldBeNull()
                    }
                }
            }
        }

        "and handler calls action() directly" - {
            val actionPlugin = transitionsPlugin<FSMState, FSMIntent, FSMAction> {
                state<FSMState.Loading> {
                    on<FSMIntent.Load> {
                        action(FSMAction.NavigateTo("home"))
                    }
                }
            }
            actionPlugin.test(FSMState.Loading) {
                onStart()

                "When Load intent is sent" - {
                    val result = onIntent(FSMIntent.Load)

                    "Then action is emitted" {
                        timeTravel.actions.last() shouldBe FSMAction.NavigateTo("home")
                    }
                    "Then state remains unchanged" {
                        states.value shouldBe FSMState.Loading
                    }
                    "Then intent is consumed" {
                        result.shouldBeNull()
                    }
                }
            }
        }

        "and handler uses updateState directly" - {
            val updatePlugin = transitionsPlugin<FSMState, FSMIntent, FSMAction> {
                state<FSMState.Loading> {
                    on<FSMIntent.DataLoaded> {
                        updateState { FSMState.Content(it.items) }
                    }
                }
            }
            updatePlugin.test(FSMState.Loading) {

                "When DataLoaded intent is sent" - {
                    onIntent(FSMIntent.DataLoaded(listOf("x")))

                    "Then state is updated via updateState" {
                        states.value shouldBe FSMState.Content(listOf("x"))
                    }
                }
            }
        }

        "and handler receives correctly typed state" - {
            var capturedState: FSMState? = null
            val typedPlugin = transitionsPlugin<FSMState, FSMIntent, FSMAction> {
                state<FSMState.Content> {
                    on<FSMIntent.UpdateFilter> {
                        capturedState = state
                        transitionTo(state.copy(items = state.items + it.filter))
                    }
                }
            }
            val initial = FSMState.Content(listOf("existing"))
            typedPlugin.test(initial) {

                "When UpdateFilter intent is sent" - {
                    onIntent(FSMIntent.UpdateFilter("new"))

                    "Then handler receives typed Content state" {
                        capturedState.shouldNotBeNull()
                        capturedState.shouldBeInstanceOf<FSMState.Content>()
                        (capturedState as FSMState.Content).items shouldBe listOf("existing")
                    }
                    "Then state is updated with filter applied" {
                        states.value shouldBe FSMState.Content(listOf("existing", "new"))
                    }
                }
            }
        }

        "and Load intent is sent in Loading state" - {
            plugin.test(FSMState.Loading) {

                "When Load intent is sent" - {
                    val result = onIntent(FSMIntent.Load)

                    "Then intent is consumed" {
                        result.shouldBeNull()
                    }
                }
            }
        }

        "and an unhandled intent is sent in Loading state" - {
            plugin.test(FSMState.Loading) {

                "When Unhandled intent is sent" - {
                    val result = onIntent(FSMIntent.Unhandled)

                    "Then intent passes through" {
                        result.shouldNotBeNull()
                        result shouldBe FSMIntent.Unhandled
                    }
                }
            }
        }

        "and an intent is sent for a state with no handlers" - {
            val minimalPlugin = transitionsPlugin<FSMState, FSMIntent, FSMAction> {
                state<FSMState.Loading> {
                    on<FSMIntent.Load> { }
                }
                // no state<Content> block
            }
            minimalPlugin.test(FSMState.Content(listOf("data"))) {

                "When Refresh intent is sent" - {
                    val result = onIntent(FSMIntent.Refresh)

                    "Then intent passes through because no state block for Content" {
                        result.shouldNotBeNull()
                        result shouldBe FSMIntent.Refresh
                    }
                }
            }
        }
    }

    "Given an FSM plugin with onState enforcement" - {
        val plugin = testFsmPlugin()

        "and same-type state update" - {
            plugin.test(FSMState.Content()) {

                "When onState is called with same Content type" - {
                    val old = FSMState.Content(listOf("old"))
                    val new = FSMState.Content(listOf("new"))
                    val result = onState(old, new)

                    "Then new state is allowed" {
                        result shouldBe new
                    }
                }
            }
        }

        "and external cross-type transition in non-debug mode" - {
            plugin.test(FSMState.Loading, configuration = { debuggable = false }) {

                "When onState is called with cross-type transition" - {
                    val old = FSMState.Loading
                    val new = FSMState.Content(listOf("data"))
                    val result = onState(old, new)

                    "Then transition is silently vetoed and old state is returned" {
                        result shouldBe old
                    }
                }
            }
        }

        "and external cross-type transition in debug mode" - {
            plugin.test(FSMState.Loading, configuration = { debuggable = true }) {

                "When onState is called with cross-type transition" - {
                    "Then InvalidStateException is thrown" {
                        shouldThrow<InvalidStateException> {
                            onState(FSMState.Loading, FSMState.Content(listOf("data")))
                        }
                    }
                }
            }
        }
    }

    "Given a TransitionsBuilder" - {

        "When duplicate state is defined" - {
            "Then IllegalArgumentException is thrown" {
                shouldThrow<IllegalArgumentException> {
                    transitionsPlugin<FSMState, FSMIntent, FSMAction> {
                        state<FSMState.Loading> {
                            on<FSMIntent.Load> { }
                        }
                        state<FSMState.Loading> {
                            on<FSMIntent.DataLoaded> { }
                        }
                    }
                }
            }
        }

        "When duplicate intent handler is defined for same state" - {
            "Then IllegalArgumentException is thrown" {
                shouldThrow<IllegalArgumentException> {
                    transitionsPlugin<FSMState, FSMIntent, FSMAction> {
                        state<FSMState.Loading> {
                            on<FSMIntent.Load> { }
                            on<FSMIntent.Load> { }
                        }
                    }
                }
            }
        }
    }

    "Given a store with transitions and reduce plugins" - {

        "When a handled intent is sent" - {
            "Then transitions plugin consumes it and reduce is not invoked" {
                var reduceInvoked = false
                val coexistStore = store<FSMState, FSMIntent, FSMAction>(FSMState.Loading) {
                    configure {
                        debuggable = true
                        parallelIntents = false
                        name = "CoexistenceTestStore"
                    }
                    transitions {
                        state<FSMState.Loading> {
                            on<FSMIntent.DataLoaded> {
                                transitionTo(FSMState.Content(it.items))
                            }
                        }
                    }
                    reduce {
                        reduceInvoked = true
                    }
                }
                val lc = coexistStore.start(this)
                lc.awaitStartup()
                coexistStore.emit(FSMIntent.DataLoaded(listOf("item")))
                idle()
                coexistStore.states.value shouldBe FSMState.Content(listOf("item"))
                reduceInvoked shouldBe false
                lc.closeAndWait()
            }
        }

        "When an unhandled intent is sent" - {
            "Then it falls through to reduce" {
                var reduceInvoked = false
                val coexistStore = store<FSMState, FSMIntent, FSMAction>(FSMState.Loading) {
                    configure {
                        debuggable = true
                        parallelIntents = false
                        name = "CoexistenceTestStore2"
                    }
                    transitions {
                        state<FSMState.Loading> {
                            on<FSMIntent.DataLoaded> {
                                transitionTo(FSMState.Content(it.items))
                            }
                        }
                    }
                    reduce {
                        reduceInvoked = true
                    }
                }
                val lc = coexistStore.start(this)
                lc.awaitStartup()
                coexistStore.emit(FSMIntent.Unhandled)
                idle()
                reduceInvoked shouldBe true
                lc.closeAndWait()
            }
        }
    }
})
