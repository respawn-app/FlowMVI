@file:OptIn(InternalFlowMVIAPI::class)

package pro.respawn.flowmvi.test.plugin

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.transitions
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle

// region Contract types

private sealed interface ChildState : MVIState {
    data object Initial : ChildState
    data class Loaded(val data: String) : ChildState
}

private sealed interface ChildIntent : MVIIntent {
    data class SetData(val data: String) : ChildIntent
}

private sealed interface ChildAction : MVIAction {
    data class Notify(val message: String) : ChildAction
}

private sealed interface ParentState : MVIState {
    data object Loading : ParentState
    data class Content(
        val childData: ChildState = ChildState.Initial,
        val label: String = "",
    ) : ParentState

    data class Error(val message: String) : ParentState
    data class DualContent(
        val child1Data: ChildState = ChildState.Initial,
        val child2Data: ChildState = ChildState.Initial,
    ) : ParentState
}

private sealed interface ParentIntent : MVIIntent {
    data object GoToContent : ParentIntent
    data object GoToError : ParentIntent
    data object GoToLoading : ParentIntent
    data class UpdateLabel(val label: String) : ParentIntent
    data class SetChildData(val data: String) : ParentIntent
}

private sealed interface ParentAction : MVIAction {
    data class ForwardedNotification(val message: String) : ParentAction
}

// endregion

// region Child store factories

private fun childStore(
    name: String = "ChildStore",
) = store<ChildState, ChildIntent, ChildAction>(ChildState.Initial) {
    configure {
        debuggable = true
        this.name = name
        actionShareBehavior = ActionShareBehavior.Distribute()
    }
    reduce { intent ->
        when (intent) {
            is ChildIntent.SetData -> updateState { ChildState.Loaded(intent.data) }
        }
    }
}

private fun childStoreWithAction(
    name: String = "ChildActionStore",
) = store<ChildState, ChildIntent, ChildAction>(ChildState.Initial) {
    configure {
        debuggable = true
        this.name = name
        actionShareBehavior = ActionShareBehavior.Distribute()
    }
    reduce { intent ->
        when (intent) {
            is ChildIntent.SetData -> {
                updateState { ChildState.Loaded(intent.data) }
                action(ChildAction.Notify("loaded: ${intent.data}"))
            }
        }
    }
}

// endregion

class ComposePluginTest : FreeSpec({
    configure()

    "Given a parent store with top-level compose" - {
        "When child state changes" - {
            "Then parent state reflects the change" {
                val child = childStore()
                val parent = store<ParentState, ParentIntent, ParentAction>(ParentState.Content()) {
                    configure {
                        debuggable = true
                        name = "ParentStore"
                    }
                    transitions {
                        compose(child, merge = { childState ->
                            when (this) {
                                is ParentState.Content -> copy(childData = childState)
                                else -> this
                            }
                        })
                    }
                }

                val lc = parent.start(this)
                lc.awaitStartup()
                idle()

                child.emit(ChildIntent.SetData("hello"))
                idle()

                parent.states.value shouldBe ParentState.Content(childData = ChildState.Loaded("hello"))

                lc.closeAndWait()
            }
        }
    }

    "Given a parent store with top-level compose and consume lambda" - {
        "When child emits an action" - {
            "Then consume lambda receives the child action" {
                val child = childStoreWithAction()
                var forwardedMessage: String? = null
                val parent = store<ParentState, ParentIntent, ParentAction>(ParentState.Content()) {
                    configure {
                        debuggable = true
                        name = "ParentConsumeStore"
                        actionShareBehavior = ActionShareBehavior.Distribute()
                    }
                    transitions {
                        compose(child, merge = { childState ->
                            when (this) {
                                is ParentState.Content -> copy(childData = childState)
                                else -> this
                            }
                        }) { childAction ->
                            when (childAction) {
                                is ChildAction.Notify -> forwardedMessage = childAction.message
                            }
                        }
                    }
                }

                val lc = parent.start(this)
                lc.awaitStartup()
                idle()

                child.emit(ChildIntent.SetData("world"))
                idle()

                forwardedMessage shouldBe "loaded: world"

                lc.closeAndWait()
            }
        }
    }

    "Given a parent store with state-scoped compose" - {
        "When parent enters the scoped state" - {
            "Then child state is merged into parent" {
                val child = childStore()
                val parent = store<ParentState, ParentIntent, ParentAction>(ParentState.Loading) {
                    configure {
                        debuggable = true
                        name = "ScopedComposeStore"
                    }
                    transitions {
                        state<ParentState.Loading> {
                            on<ParentIntent.GoToContent> {
                                transitionTo(ParentState.Content())
                            }
                        }
                        state<ParentState.Content> {
                            compose(child, merge = { childState -> copy(childData = childState) })
                            on<ParentIntent.UpdateLabel> {
                                transitionTo(state.copy(label = it.label))
                            }
                            on<ParentIntent.GoToError> {
                                transitionTo(ParentState.Error("error"))
                            }
                            on<ParentIntent.GoToLoading> {
                                transitionTo(ParentState.Loading)
                            }
                        }
                        state<ParentState.Error> {
                            on<ParentIntent.GoToContent> {
                                transitionTo(ParentState.Content())
                            }
                        }
                    }
                }

                val lc = parent.start(this)
                lc.awaitStartup()
                idle()

                // Parent is in Loading — scoped compose for Content is not active
                parent.states.value.shouldBeInstanceOf<ParentState.Loading>()

                // Change child state before parent enters Content
                child.emit(ChildIntent.SetData("pre-transition"))
                idle()

                // Parent still Loading, child state not merged
                parent.states.value shouldBe ParentState.Loading

                // Transition parent to Content
                parent.emit(ParentIntent.GoToContent)
                idle()

                // Scoped compose activates and merges child's current state
                parent.states.value shouldBe ParentState.Content(
                    childData = ChildState.Loaded("pre-transition"),
                )

                // Further child changes are also merged
                child.emit(ChildIntent.SetData("post-transition"))
                idle()

                parent.states.value shouldBe ParentState.Content(
                    childData = ChildState.Loaded("post-transition"),
                )

                lc.closeAndWait()
            }
        }

        "When parent leaves the scoped state" - {
            "Then child state changes no longer affect parent" {
                val child = childStore()
                val parent = store<ParentState, ParentIntent, ParentAction>(ParentState.Loading) {
                    configure {
                        debuggable = true
                        name = "ScopedDeactivateStore"
                    }
                    transitions {
                        state<ParentState.Loading> {
                            on<ParentIntent.GoToContent> {
                                transitionTo(ParentState.Content())
                            }
                        }
                        state<ParentState.Content> {
                            compose(child, merge = { childState -> copy(childData = childState) })
                            on<ParentIntent.GoToError> {
                                transitionTo(ParentState.Error("error"))
                            }
                        }
                        state<ParentState.Error> {
                            on<ParentIntent.GoToContent> {
                                transitionTo(ParentState.Content())
                            }
                        }
                    }
                }

                val lc = parent.start(this)
                lc.awaitStartup()
                idle()

                // Enter Content so compose is active
                parent.emit(ParentIntent.GoToContent)
                idle()
                parent.states.value.shouldBeInstanceOf<ParentState.Content>()

                // Leave Content → go to Error
                parent.emit(ParentIntent.GoToError)
                idle()
                parent.states.value shouldBe ParentState.Error("error")

                // Change child state while parent is in Error — should not affect parent
                child.emit(ChildIntent.SetData("ignored"))
                idle()

                parent.states.value shouldBe ParentState.Error("error")

                lc.closeAndWait()
            }
        }

        "When parent re-enters the scoped state" - {
            "Then child's current state is merged again" {
                val child = childStore()
                val parent = store<ParentState, ParentIntent, ParentAction>(ParentState.Loading) {
                    configure {
                        debuggable = true
                        name = "ScopedReentryStore"
                    }
                    transitions {
                        state<ParentState.Loading> {
                            on<ParentIntent.GoToContent> {
                                transitionTo(ParentState.Content())
                            }
                        }
                        state<ParentState.Content> {
                            compose(child, merge = { childState -> copy(childData = childState) })
                            on<ParentIntent.GoToError> {
                                transitionTo(ParentState.Error("error"))
                            }
                        }
                        state<ParentState.Error> {
                            on<ParentIntent.GoToContent> {
                                transitionTo(ParentState.Content())
                            }
                        }
                    }
                }

                val lc = parent.start(this)
                lc.awaitStartup()
                idle()

                // Enter Content
                parent.emit(ParentIntent.GoToContent)
                idle()

                // Update child
                child.emit(ChildIntent.SetData("first"))
                idle()
                parent.states.value shouldBe ParentState.Content(childData = ChildState.Loaded("first"))

                // Leave Content → Error
                parent.emit(ParentIntent.GoToError)
                idle()

                // Update child while in Error (child is still running, just not subscribed)
                child.emit(ChildIntent.SetData("second"))
                idle()
                parent.states.value shouldBe ParentState.Error("error")

                // Re-enter Content
                parent.emit(ParentIntent.GoToContent)
                idle()

                // Child's current state (Loaded("second")) should be merged immediately
                parent.states.value shouldBe ParentState.Content(
                    childData = ChildState.Loaded("second"),
                )

                lc.closeAndWait()
            }
        }
    }

    "Given a parent store with compose" - {
        "When parent is stopped" - {
            "Then child store is also stopped" {
                val child = childStore()
                val parent = store<ParentState, ParentIntent, ParentAction>(ParentState.Content()) {
                    configure {
                        debuggable = true
                        name = "LifecycleStore"
                    }
                    transitions {
                        compose(child, merge = { childState ->
                            when (this) {
                                is ParentState.Content -> copy(childData = childState)
                                else -> this
                            }
                        })
                    }
                }

                val lc = parent.start(this)
                lc.awaitStartup()
                idle()

                // Both parent and child should be active
                parent.isActive shouldBe true
                child.isActive shouldBe true

                // Stop parent
                lc.closeAndWait()
                idle()

                // Both should be stopped
                parent.isActive shouldBe false
                child.isActive shouldBe false
            }
        }
    }

    "Given a parent store with multiple compose calls" - {
        "When both child stores change state" - {
            "Then parent state reflects both changes" {
                val child1 = childStore("Child1")
                val child2 = childStore("Child2")

                val parent = store<ParentState, ParentIntent, ParentAction>(ParentState.DualContent()) {
                    configure {
                        debuggable = true
                        name = "MultiComposeStore"
                    }
                    transitions {
                        compose(child1, merge = { childState ->
                            when (this) {
                                is ParentState.DualContent -> copy(child1Data = childState)
                                else -> this
                            }
                        })
                        compose(child2, merge = { childState ->
                            when (this) {
                                is ParentState.DualContent -> copy(child2Data = childState)
                                else -> this
                            }
                        })
                    }
                }

                val lc = parent.start(this)
                lc.awaitStartup()
                idle()

                child1.emit(ChildIntent.SetData("from-child1"))
                idle()

                child2.emit(ChildIntent.SetData("from-child2"))
                idle()

                parent.states.value shouldBe ParentState.DualContent(
                    child1Data = ChildState.Loaded("from-child1"),
                    child2Data = ChildState.Loaded("from-child2"),
                )

                lc.closeAndWait()
            }
        }
    }
})
