# FSM (Finite State Machine) Integration Plan for FlowMVI

## Overview

This document describes the design and implementation plan for adding finite state machine (FSM) transitions to FlowMVI via a plugin-based DSL.

**The FSM plugin is a full replacement for `reduce {}`**, not a complement. Handlers inside `on<Intent> {}` receive a `TransitionScope` that **delegates to `PipelineContext`**, giving them access to every store API: `updateState`, `action()`, `intent()`, `launch {}`, `withState {}`, coroutine scope, config, subscriber count, and more. On top of that, `TransitionScope` provides a typed `state: T` property and convenience `transitionTo()` methods.

**Design decisions:**
- Plugin-based integration — installs via `onIntent` for dispatch, `onState` for enforcement
- Configurable enforcement — throw `InvalidStateException` in debuggable mode, silently veto in release
- Lives in `core/` module
- Handlers are imperative (`Unit` return) — they call `transitionTo`/`updateState`/`action`/`intent`/`launch` directly
- Intent-driven: `on<SomeIntent> { transitionTo(NewState) }`
- Focused API — no onEnter/onExit, guards, hierarchical states
- Not marked `@ExperimentalFlowMVIAPI`
- **Compose API** — `compose()` inside `transitions {}` replaces manual `delegate()` + `whileSubscribed` + `combine` boilerplate for child store composition

---

## 1. API Design

### 1.1 `TransitionScope` — The Handler Context (public)

The scope available inside `on<Intent> {}` handlers. Extends `PipelineContext` for full store API access and adds typed state + convenience `transitionTo()`.

```kotlin
/**
 * Scope available inside FSM [on] handlers.
 * Delegates to [PipelineContext] for full store API access, and adds
 * typed [state] and convenience [transitionTo] methods.
 *
 * Everything you can do in [reduce], you can do here — plus you get the current
 * state guaranteed to be of type [T], and a shorthand for state transitions.
 */
@FlowMVIDSL
@OptIn(ExperimentalSubclassOptIn::class)
@SubclassOptInRequired(NotIntendedForInheritance::class)
public interface TransitionScope<out T : S, S : MVIState, I : MVIIntent, A : MVIAction> :
    PipelineContext<S, I, A> {

    /**
     * The current state, guaranteed to be of type [T] at the time this handler was invoked.
     *
     * **Note**: If [StoreConfiguration.parallelIntents] is `true`, the actual store state may have
     * changed by the time you read this. Use [updateState] for atomic operations.
     * For most use cases this value is stable because the default is sequential intent processing.
     */
    public val state: T

    /**
     * Transition to [target] state. Shorthand for `updateState { target }`.
     * The transition will be validated by the FSM's enforcement rules via [onState].
     */
    @FlowMVIDSL
    public suspend fun transitionTo(target: S)

    /**
     * Transition to [target] state and emit [sideEffect] action.
     * Shorthand for `updateState { target }; action(sideEffect)`.
     */
    @FlowMVIDSL
    public suspend fun transitionTo(target: S, sideEffect: A)
}
```

### 1.2 `TransitionScopeImpl` — Internal Implementation

```kotlin
@PublishedApi
internal class TransitionScopeImpl<T : S, S : MVIState, I : MVIIntent, A : MVIAction>(
    private val pipeline: PipelineContext<S, I, A>,
    override val state: T,
) : TransitionScope<T, S, I, A>, PipelineContext<S, I, A> by pipeline {

    override suspend fun transitionTo(target: S) {
        updateState { target }
    }

    override suspend fun transitionTo(target: S, sideEffect: A) {
        updateState { target }
        action(sideEffect)
    }
}
```

Uses Kotlin's `by` delegation to forward all `PipelineContext` members to the real pipeline. Only `state`, `transitionTo()` are added.

### 1.3 `IntentHandler` — Internal Typealias

The stored handler type. Takes the `PipelineContext`, the untyped state snapshot, and the untyped intent. The `on<E>` builder wraps the user's typed lambda into this form.

```kotlin
/**
 * Internal handler function. Takes PipelineContext + current state + intent.
 * The PipelineContext is the receiver so handlers can call updateState/action/intent/launch.
 */
internal typealias IntentHandler<S, I, A> = suspend PipelineContext<S, I, A>.(state: S, intent: I) -> Unit
```

### 1.4 `TransitionsBuilder` — Top-level DSL

Collects `StateDefinition`s and builds a `TransitionGraph`.

```kotlin
/**
 * Builder that collects state definitions and compiles a [TransitionGraph].
 */
@FlowMVIDSL
public class TransitionsBuilder<S : MVIState, I : MVIIntent, A : MVIAction>
    @PublishedApi internal constructor() {

    @PublishedApi
    internal val definitions: MutableMap<KClass<out S>, StateDefinition<S, I, A>> = mutableMapOf()

    /**
     * Define intent handlers for state type [T].
     * Only intents registered via [StateTransitionsBuilder.on] will be handled when the store is in state [T].
     * Any state change to a type not reachable from a [state] block will be vetoed (or throw in debug mode).
     *
     * @throws IllegalArgumentException if [T] is already defined in this transitions block.
     */
    @FlowMVIDSL
    public inline fun <reified T : S> state(
        @BuilderInference block: StateTransitionsBuilder<T, S, I, A>.() -> Unit,
    ) {
        val stateClass = T::class
        require(stateClass !in definitions) {
            "State ${stateClass.simpleName} is already defined in this transitions block"
        }
        val builder = StateTransitionsBuilder<T, S, I, A>(stateClass).apply(block)
        definitions[stateClass] = builder.build()
    }

    @PublishedApi
    internal fun build(): TransitionGraph<S, I, A> = TransitionGraph(
        definitions = definitions.toMap(),
    )
}
```

### 1.5 `StateTransitionsBuilder` — Per-state Handler Registration

```kotlin
/**
 * Builder for declaring intent handlers within a specific state type [T].
 */
@FlowMVIDSL
public class StateTransitionsBuilder<T : S, S : MVIState, I : MVIIntent, A : MVIAction>
    @PublishedApi internal constructor(
        private val stateType: KClass<out S>,
    ) {

    @PublishedApi
    internal val handlers: MutableMap<KClass<out I>, IntentHandler<S, I, A>> = mutableMapOf()

    /**
     * Handle intent of type [E] when the store is in state [T].
     * Inside the lambda, you have full [PipelineContext] access plus typed [TransitionScope.state].
     * Use [TransitionScope.transitionTo] to change state, or call [updateState]/[action]/[intent]/[launch] directly.
     *
     * @throws IllegalArgumentException if [E] is already handled for state [T].
     */
    @FlowMVIDSL
    public inline fun <reified E : I> on(
        noinline block: suspend TransitionScope<T, S, I, A>.(E) -> Unit,
    ) {
        val intentClass = E::class
        require(intentClass !in handlers) {
            "Intent ${intentClass.simpleName} is already handled for state ${stateType.simpleName}"
        }
        handlers[intentClass] = handler@{ state, intent ->
            @Suppress("UNCHECKED_CAST")
            val scope = TransitionScopeImpl<T, S, I, A>(this, state as T)
            @Suppress("UNCHECKED_CAST")
            scope.block(intent as E)
        }
    }

    @PublishedApi
    internal fun build(): StateDefinition<S, I, A> = StateDefinition(
        stateType = stateType,
        handlers = handlers.toMap(),
    )
}
```

### 1.6 Entry Points

#### `StoreBuilder.transitions {}`

```kotlin
/**
 * Install a finite state machine plugin that handles intents based on the current state type.
 *
 * This is a full replacement for [reduce] — handlers have full [PipelineContext] access.
 * Intents not matched by any FSM handler pass through to downstream plugins.
 *
 * Name is hardcoded because usually multiple FSM plugins are not used.
 * Provide your own name if you want to have multiple FSM plugins.
 */
@IgnorableReturnValue
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.transitions(
    name: String = TransitionsPluginName,
    @BuilderInference block: TransitionsBuilder<S, I, A>.() -> Unit,
): Unit = install(transitionsPlugin(name, block))
```

#### `transitionsPlugin()` factory

```kotlin
public const val TransitionsPluginName: String = "TransitionsPlugin"

/**
 * Create a finite state machine plugin.
 *
 * @see transitions
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> transitionsPlugin(
    name: String = TransitionsPluginName,
    @BuilderInference block: TransitionsBuilder<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> {
    val graph = TransitionsBuilder<S, I, A>().apply(block).build()
    return plugin {
        this.name = name
        // ... onIntent + onState (see Section 4)
    }
}
```

### 1.7 Handler Signature Summary

| Old Plan | Revised Plan |
|----------|-------------|
| `suspend TransitionContext<T, S, I, A>.(E) -> TransitionResult<S, A>` | `suspend TransitionScope<T, S, I, A>.(E) -> Unit` |

Key differences:
- **No `TransitionResult`** — handlers are imperative, calling `transitionTo`/`updateState`/`action`/etc.
- **No `TransitionContext`** — replaced by `TransitionScope` which extends `PipelineContext`
- **No `dontTransition()`** — just don't call `transitionTo()`, or call `action()` directly
- **Full `PipelineContext` access** — `launch {}`, `intent()`, `emit()`, `withState {}`, `config`, etc.

### 1.8 Compose API — Child Store Composition Inside Transitions

The `compose()` function integrates child store composition directly into the `transitions {}` DSL.
It replaces the manual `delegate()` + `whileSubscribed` + `combine` boilerplate pattern (as seen in `ProgressiveContainer.kt`).

**Two variants:**
1. **Top-level compose** — always-active subscriptions (for data-class states or permanent compositions)
2. **State-scoped compose** — subscriptions scoped to when parent is in a specific state type (for sealed hierarchies)

#### Top-level `compose()` in `TransitionsBuilder`

```kotlin
@FlowMVIDSL
public class TransitionsBuilder<S : MVIState, I : MVIIntent, A : MVIAction>
    @PublishedApi internal constructor() {

    // ... existing: state<T> {}, definitions, build()

    @PublishedApi
    internal val topLevelCompositions: MutableList<ComposeDefinition<S, I, A, *, *, *>> = mutableListOf()

    /**
     * Compose a child store into this store's state. The child's state changes are merged into the
     * parent state at all times while the parent store is active.
     *
     * The child store is automatically started as a lifecycle child of the parent store.
     *
     * @param store The child store to compose
     * @param merge Maps the child's state into the parent's state. Called each time the child state changes.
     *   Receiver is the current parent state; parameter is the new child state. Returns the new parent state.
     * @param consume Optional lambda to handle actions emitted by the child store. Runs in the parent's
     *   [PipelineContext], so you can call [action], [intent], [updateState], etc.
     */
    @FlowMVIDSL
    public fun <CS : MVIState, CI : MVIIntent, CA : MVIAction> compose(
        store: Store<CS, CI, CA>,
        merge: S.(childState: CS) -> S,
        consume: (suspend PipelineContext<S, I, A>.(CA) -> Unit)? = null,
    ) {
        topLevelCompositions += ComposeDefinition(
            store = store,
            merge = merge,
            consume = consume,
            scopedToState = null,  // null = always active
        )
    }
}
```

#### State-scoped `compose()` in `StateTransitionsBuilder`

```kotlin
@FlowMVIDSL
public class StateTransitionsBuilder<T : S, S : MVIState, I : MVIIntent, A : MVIAction>
    @PublishedApi internal constructor(
        private val stateType: KClass<out S>,
    ) {

    // ... existing: on<E> {}, handlers, build()

    @PublishedApi
    internal val compositions: MutableList<ComposeDefinition<S, I, A, *, *, *>> = mutableListOf()

    /**
     * Compose a child store into this store's state, scoped to state type [T].
     *
     * The child subscription is **only active while the parent store is in state [T]**.
     * When the parent transitions into [T], the subscription starts and the child's current state
     * is immediately merged. When the parent transitions out of [T], the subscription is cancelled.
     *
     * The child store is automatically started as a lifecycle child of the parent store
     * (it outlives individual state scopes and is stopped when the parent store stops).
     *
     * @param store The child store to compose
     * @param merge Maps the child's state into the parent's state. Receiver is the current parent state
     *   typed as [T]; returns the new parent state (may be any [S] to support transitions).
     * @param consume Optional lambda to handle actions emitted by the child store.
     */
    @FlowMVIDSL
    public fun <CS : MVIState, CI : MVIIntent, CA : MVIAction> compose(
        store: Store<CS, CI, CA>,
        merge: T.(childState: CS) -> S,
        consume: (suspend PipelineContext<S, I, A>.(CA) -> Unit)? = null,
    ) {
        @Suppress("UNCHECKED_CAST")
        compositions += ComposeDefinition(
            store = store,
            merge = merge as S.(Any) -> S,  // widened for storage; safety ensured by scoped activation
            consume = consume,
            scopedToState = stateType,
        )
    }

    @PublishedApi
    internal fun build(): StateDefinition<S, I, A> = StateDefinition(
        stateType = stateType,
        handlers = handlers.toMap(),
        compositions = compositions.toList(),
    )
}
```

#### Design Rationale

1. **`merge` receiver type**: Top-level `merge` receives `S` (generic parent state) because it runs regardless of state type. State-scoped `merge` receives `T` (typed parent state) because it only runs when parent is in state `T` — allowing `copy()` on data classes.

2. **`merge` return type is `S`**: Both variants return `S` (not `T`) because a merge could theoretically trigger a state type change. However, in practice most merges return the same type via `copy()`.

3. **`consume` runs in `PipelineContext<S, I, A>`**: This gives the action handler full parent store access — it can call `action()` to forward as parent action, `intent()` to trigger parent intents, `updateState {}`, `launch {}`, etc.

4. **Intent routing via normal `on<>` handlers**: No special API for sending intents to child stores. Users call `childStore.intent(ChildIntent.Foo)` inside `on<ParentIntent> {}` handlers. This keeps the API surface minimal and predictable.

### 1.9 `ComposeDefinition` — Internal Data Model

```kotlin
/**
 * Internal representation of a compose() call in the transitions DSL.
 * Captures the child store, merge function, optional action consumer, and scope constraint.
 */
internal class ComposeDefinition<S : MVIState, I : MVIIntent, A : MVIAction, CS : MVIState, CI : MVIIntent, CA : MVIAction>(
    /** The child store to subscribe to. */
    val store: Store<CS, CI, CA>,
    /** Merges child state into parent state. */
    val merge: S.(CS) -> S,
    /** Optional handler for child actions. */
    val consume: (suspend PipelineContext<S, I, A>.(CA) -> Unit)?,
    /** If non-null, the composition is only active while parent is in this state type.
     *  If null, the composition is always active (top-level). */
    val scopedToState: KClass<out S>?,
)
```

For storage in the heterogeneous lists, `ComposeDefinition` is type-erased to `ComposeDefinition<S, I, A, *, *, *>`. The type parameters `CS, CI, CA` are only needed at the point of creation and subscription.

---

## 2. Complete Usage Example

```kotlin
// --- State hierarchy ---
sealed interface ScreenState : MVIState {
    data object Loading : ScreenState
    data class Content(val items: List<Item>, val filter: String = "") : ScreenState
    data class Error(val message: String) : ScreenState
}

// --- Intents ---
sealed interface ScreenIntent : MVIIntent {
    data object Load : ScreenIntent
    data class DataLoaded(val items: List<Item>) : ScreenIntent
    data class LoadFailed(val message: String) : ScreenIntent
    data object Refresh : ScreenIntent
    data object Retry : ScreenIntent
    data class ItemClicked(val id: String) : ScreenIntent
    data class UpdateFilter(val filter: String) : ScreenIntent
}

// --- Actions ---
sealed interface ScreenAction : MVIAction {
    data class ShowError(val message: String) : ScreenAction
    data class NavigateToDetail(val id: String) : ScreenAction
}

// --- Store ---
val store = store<ScreenState, ScreenIntent, ScreenAction>(ScreenState.Loading) {
    configure {
        name = "ScreenStore"
        debuggable = BuildConfig.DEBUG
    }

    transitions {
        state<ScreenState.Loading> {
            on<ScreenIntent.Load> {
                // Full PipelineContext! Launch coroutines, call APIs, etc.
                launch {
                    val result = runCatching { repository.fetchData() }
                    result.fold(
                        onSuccess = { intent(ScreenIntent.DataLoaded(it)) },
                        onFailure = { intent(ScreenIntent.LoadFailed(it.message ?: "Unknown")) }
                    )
                }
            }
            on<ScreenIntent.DataLoaded> {
                transitionTo(ScreenState.Content(it.items))
            }
            on<ScreenIntent.LoadFailed> {
                transitionTo(ScreenState.Error(it.message))
                action(ScreenAction.ShowError(it.message))
            }
        }

        state<ScreenState.Content> {
            on<ScreenIntent.Refresh> {
                transitionTo(ScreenState.Loading)
                // Can launch async work after transition
                launch { repository.prefetch() }
            }
            on<ScreenIntent.ItemClicked> {
                // No state transition, just side effect
                action(ScreenAction.NavigateToDetail(it.id))
            }
            on<ScreenIntent.UpdateFilter> {
                // Typed state! state is ScreenState.Content, can use copy()
                transitionTo(state.copy(filter = it.filter))
            }
        }

        state<ScreenState.Error> {
            on<ScreenIntent.Retry> {
                transitionTo(ScreenState.Loading)
                intent(ScreenIntent.Load)  // re-emit Load intent
            }
        }
    }
}
```

### 2.1 Comparison with `reduce {}`

The same logic written with `reduce {}`:

```kotlin
reduce { intent ->
    when (intent) {
        is ScreenIntent.Load -> {
            launch {
                val result = runCatching { repository.fetchData() }
                result.fold(
                    onSuccess = { intent(ScreenIntent.DataLoaded(it)) },
                    onFailure = { intent(ScreenIntent.LoadFailed(it.message ?: "Unknown")) }
                )
            }
        }
        is ScreenIntent.DataLoaded -> updateState {
            // Must manually cast or guard:
            when (this) {
                is ScreenState.Loading -> ScreenState.Content(intent.items)
                else -> this // ignore in wrong state
            }
        }
        is ScreenIntent.LoadFailed -> {
            updateState {
                when (this) {
                    is ScreenState.Loading -> ScreenState.Error(intent.message)
                    else -> this
                }
            }
            action(ScreenAction.ShowError(intent.message))
        }
        is ScreenIntent.Refresh -> updateState {
            when (this) {
                is ScreenState.Content -> {
                    launch { repository.prefetch() }
                    ScreenState.Loading
                }
                else -> this
            }
        }
        is ScreenIntent.ItemClicked -> {
            action(ScreenAction.NavigateToDetail(intent.id))
        }
        is ScreenIntent.UpdateFilter -> updateState {
            when (this) {
                is ScreenState.Content -> copy(filter = intent.filter)
                else -> this
            }
        }
        is ScreenIntent.Retry -> {
            updateState {
                when (this) {
                    is ScreenState.Error -> ScreenState.Loading
                    else -> this
                }
            }
            intent(ScreenIntent.Load)
        }
    }
}
```

| Aspect | `reduce {}` | `transitions {}` |
|--------|-------------|-------------------|
| State type checking | Manual `when(this)` + casts | Automatic — `state` is typed to `T` |
| Transition validation | None — any state can go anywhere | Runtime enforcement via `onState` |
| Intent scoping | All intents handled in one block | Intents scoped per state type |
| PipelineContext access | Full | Full (via delegation) |
| Async work | `launch {}`, `intent()` | Same |
| Side effects | `action()` | Same, plus `transitionTo(state, sideEffect)` shorthand |
| Learning curve | Minimal | Slightly higher (new DSL concepts) |

**Key takeaway**: Everything `reduce {}` can do, `transitions {}` handlers can do too — with the addition of typed state and transition enforcement.

### 2.2 Compose Usage Example — Sealed Hierarchy with State-Scoped Children

```kotlin
// --- Child stores (each with their own logic) ---

sealed interface FeedState : MVIState {
    data object Loading : FeedState
    data class Content(val items: List<FeedItem>) : FeedState
}
sealed interface FeedIntent : MVIIntent { data object Refresh : FeedIntent }
sealed interface FeedAction : MVIAction { data class ShowError(val message: String) : FeedAction }

val feedStore = store<FeedState, FeedIntent, FeedAction>(FeedState.Loading) {
    reduce { intent ->
        when (intent) {
            is FeedIntent.Refresh -> {
                updateState { FeedState.Loading }
                launch {
                    val items = repository.fetchFeed()
                    updateState { FeedState.Content(items) }
                }
            }
        }
    }
}

sealed interface AccountState : MVIState {
    data object Loading : AccountState
    data class Profile(val name: String) : AccountState
}
sealed interface AccountIntent : MVIIntent { data object Logout : AccountIntent }

val accountStore = store<AccountState, AccountIntent, Nothing>(AccountState.Loading) { /* ... */ }

// --- Parent store with composed FSM ---

sealed interface HomeState : MVIState {
    data object Loading : HomeState
    data class Content(
        val feed: FeedState = FeedState.Loading,
        val account: AccountState = AccountState.Loading,
    ) : HomeState
    data class Error(val message: String) : HomeState
}

sealed interface HomeIntent : MVIIntent {
    data object Initialize : HomeIntent
    data object RefreshFeed : HomeIntent
    data object Logout : HomeIntent
    data class GoToError(val message: String) : HomeIntent
    data object Retry : HomeIntent
}

sealed interface HomeAction : MVIAction {
    data class ShowToast(val message: String) : HomeAction
}

val homeStore = store<HomeState, HomeIntent, HomeAction>(HomeState.Loading) {
    transitions {
        state<HomeState.Loading> {
            on<HomeIntent.Initialize> {
                transitionTo(HomeState.Content())  // → starts child subscriptions
            }
        }

        state<HomeState.Content> {
            // State-scoped compose: only subscribed while in Content state
            compose(feedStore, merge = { childState -> copy(feed = childState) }) { feedAction ->
                when (feedAction) {
                    is FeedAction.ShowError -> action(HomeAction.ShowToast(feedAction.message))
                }
            }
            compose(accountStore, merge = { childState -> copy(account = childState) })

            // Intent routing via standard on<> handlers
            on<HomeIntent.RefreshFeed> {
                feedStore.intent(FeedIntent.Refresh)
            }
            on<HomeIntent.Logout> {
                accountStore.intent(AccountIntent.Logout)
            }
            on<HomeIntent.GoToError> {
                transitionTo(HomeState.Error(it.message))
                // ↑ transitions out of Content → child subscriptions cancelled
            }
        }

        state<HomeState.Error> {
            on<HomeIntent.Retry> {
                transitionTo(HomeState.Loading)
            }
        }
    }
}
```

### 2.3 Compose Usage Example — Data Class with Top-Level Compose

```kotlin
data class DashboardState(
    val feed: FeedState = FeedState.Loading,
    val notifications: NotificationState = NotificationState.Loading,
) : MVIState

val dashboardStore = store<DashboardState, DashboardIntent, DashboardAction>(DashboardState()) {
    transitions {
        // Top-level compose: always subscribed
        compose(feedStore, merge = { copy(feed = it) })
        compose(notificationStore, merge = { copy(notifications = it) })

        state<DashboardState> {
            on<DashboardIntent.RefreshAll> {
                feedStore.intent(FeedIntent.Refresh)
                notificationStore.intent(NotificationIntent.Refresh)
            }
        }
    }
}
```

### 2.4 Comparison with Current `delegate()` Pattern

Current manual approach (from `ProgressiveContainer.kt`):

```kotlin
val store by lazyStore(initial = ProgressiveState()) {
    val suggestionsState by delegate(suggestionStore)
    val feedState by delegate(feedStore) { action(it) }
    whileSubscribed {
        combine(suggestionsState, feedState) { suggestions, feed ->
            updateState { copy(feed = feed, suggestions = suggestions) }
        }.consume()
    }
    reduce { intent -> /* ... */ }
}
```

With `transitions {}` + `compose()`:

```kotlin
val store by lazyStore(initial = ProgressiveState()) {
    transitions {
        compose(suggestionStore, merge = { copy(suggestions = it) })
        compose(feedStore, merge = { copy(feed = it) }) { action(it) }
        state<ProgressiveState> {
            // intent handlers here
        }
    }
}
```

| Aspect | `delegate()` + `whileSubscribed` | `compose()` in `transitions {}` |
|--------|----------------------------------|----------------------------------|
| Lines of code | 7-10 lines of boilerplate | 2-3 lines |
| State merging | Manual `combine` + `updateState` | Declarative `merge` lambda |
| Action forwarding | Manual in `delegate` consume | Same — `consume` lambda |
| Lifecycle scoping | Manual `whileSubscribed` | Automatic (top-level or state-scoped) |
| Child lifecycle | Separate `installChild` or `delegate(start=true)` | Automatic |
| FSM integration | None — separate from transitions | Native — part of the same DSL |

---

## 3. Enforcement

### 3.1 `onState` Validation

The plugin uses the `onState(old, new)` callback to validate state transitions:

```
onState(old, new) logic:
  1. If self-initiated (handlerDepth > 0) → return new (allow)
  2. If old::class == new::class → return new (same-type updates always allowed)
  3. Otherwise → enforce:
     - config.debuggable == true → throw InvalidStateException(new::class.simpleName, old::class.simpleName)
     - config.debuggable == false → return old (veto silently)
```

**Allowed transitions**: All cross-state-type transitions must go through the FSM's `on<Intent>` handlers. Transitions initiated outside the FSM (e.g., from `init {}` or a coexisting `reduce {}` plugin) will be blocked unless they are same-type updates. This is the desired strict enforcement behavior.

If the user needs to allow specific external transitions in the future, an `allowTransition<From, To>()` API can be added to `TransitionsBuilder`.

### 3.2 Self-Transition Tracking Mechanism

To distinguish FSM-initiated state changes from external ones, the plugin uses an `AtomicInt` counter tracking the depth of active FSM handlers:

```kotlin
// Inside the plugin's closure
val handlerDepth: AtomicInt = atomic(0)
```

**How it works inside `onIntent`:**

```kotlin
onIntent { intent ->
    val currentState = states.value  // read snapshot for type dispatch
    val stateClass = currentState::class
    val definition = graph.definitions[stateClass]
        ?: return@onIntent intent  // no FSM rules for this state, pass through
    val handler = definition.handlers[intent::class]
        ?: return@onIntent intent  // no handler for this intent, pass through

    // Set self-initiated flag. All updateState calls within the handler
    // will trigger onState, which will see handlerDepth > 0 and allow.
    handlerDepth.incrementAndGet()
    try {
        handler.invoke(this, currentState, intent)
    } finally {
        handlerDepth.decrementAndGet()
    }

    null  // consume the intent
}
```

And in `onState`:

```kotlin
onState { old, new ->
    if (handlerDepth.value > 0) return@onState new  // FSM-initiated, allow
    if (old::class == new::class) return@onState new  // same-type update, allow
    if (config.debuggable) {
        throw InvalidStateException(new::class.simpleName, old::class.simpleName) 
    }
    old  // veto in release
}
```

### 3.3 Thread Safety of the Self-Initiated Flag

**With `parallelIntents = false` (default):** Intents are processed sequentially. The `onIntent → handler → updateState → onState` chain runs on a single coroutine. The counter is incremented before the handler and decremented after. Since `onState` is called synchronously within `updateState` (inside the state mutex in `StateModule.useState`), the counter is guaranteed to be > 0 when `onState` runs. No race conditions.

**With `parallelIntents = true`:** Multiple intents can be processed concurrently. Two FSM handlers could run in parallel, both incrementing the counter. However, `updateState` acquires the state mutex (`StateStrategy.Atomic`, the default), so `onState` callbacks are serialized. Consider:

1. Intent A (FSM) increments `handlerDepth` to 1
2. Intent B (FSM) increments `handlerDepth` to 2
3. Intent A's `updateState` → `onState` runs (sees `2` > 0 ✓)
4. Intent B's `updateState` → `onState` runs (sees `2` or `1` > 0 ✓)
5. They decrement back to 0

This works correctly because both are FSM-initiated. The only edge case:

- During FSM handler execution (`handlerDepth > 0`), an external `updateState` from a `launch {}` in a different coroutine triggers `onState` and sees `handlerDepth > 0` → incorrectly allowed.

This is acceptable because:
1. The `launch {}` was started by the FSM handler, so it's logically part of FSM work
2. True external transitions (from other plugins) are unlikely to coincide with active handler execution
3. For stricter enforcement, a coroutine context element could replace the counter in a future iteration

---

## 4. Implementation Architecture

### 4.1 Plugin Implementation — `onIntent`

```kotlin
onIntent { intent ->
    // Read current state for type-based dispatch.
    // states.value is available via PipelineContext → ImmediateStateReceiver → StateProvider
    val currentState = states.value
    val stateClass = currentState::class

    val definition = graph.definitions[stateClass]
        ?: return@onIntent intent  // no FSM rules for this state, pass through

    val handler = definition.handlers[intent::class]
        ?: return@onIntent intent  // no handler for this intent in current state, pass through

    // Execute handler with self-initiated tracking
    handlerDepth.incrementAndGet()
    try {
        handler.invoke(this, currentState, intent)
    } finally {
        handlerDepth.decrementAndGet()
    }

    null  // consume the intent
}
```

**Reading current state**: `PipelineContext` extends `ImmediateStateReceiver<S>` which extends `StateProvider<S>` which has `val states: StateFlow<S>`. Reading `states.value` gives the current state snapshot. This is a non-atomic, non-blocking read used only for type dispatch. The actual state mutations go through `updateState {}` which is atomic.

**TOCTOU consideration**: The state could change between `states.value` and the handler calling `updateState`. With `parallelIntents = false` (default), intents are sequential so this can't happen. With `parallelIntents = true`, this is the same trade-off as `reduce {}` — the user opted into concurrency. The `state` property on `TransitionScope` is a snapshot from dispatch time; for atomic operations, handlers should use `updateState {}`.

### 4.2 Plugin Implementation — `onState`

```kotlin
onState { old, new ->
    // Allow transitions initiated by FSM handlers
    if (handlerDepth.value > 0) return@onState new

    // Same-type updates always allowed (e.g., copy() on a data class)
    if (old::class == new::class) return@onState new

    // External cross-type transition — enforce
    if (config.debuggable) {
        throw InvalidStateException(new::class.simpleName, old::class.simpleName)
    }
    old  // veto silently in release mode
}
```

### 4.3 Full Plugin Assembly

When **no compose definitions** are present, the plugin is a simple `plugin {}` (no composite overhead):

```kotlin
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> transitionsPlugin(
    name: String = TransitionsPluginName,
    @BuilderInference block: TransitionsBuilder<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> {
    val builder = TransitionsBuilder<S, I, A>().apply(block)
    val graph = builder.build()
    val topLevelCompositions = builder.topLevelCompositions.toList()
    val scopedCompositions = graph.definitions.values.flatMap { it.compositions }

    val fsmPlugin = buildFsmPlugin<S, I, A>(graph, name)

    // If no compositions, return the simple FSM plugin
    if (topLevelCompositions.isEmpty() && scopedCompositions.isEmpty()) return fsmPlugin

    // Otherwise, assemble composite: child lifecycle + compose subscriptions + FSM
    val allChildStores = (topLevelCompositions + scopedCompositions)
        .map { it.store }
        .toSet()

    val childPlugin = childStorePlugin<S, I, A>(allChildStores, force = null, blocking = false)
    val composePlugin = buildComposePlugin(topLevelCompositions, scopedCompositions)

    return compositePlugin(
        plugins = listOfNotNull(childPlugin, composePlugin, fsmPlugin),
        name = name,
    )
}
```

The `buildFsmPlugin` extracts the core FSM `onIntent` + `onState` logic:

```kotlin
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> buildFsmPlugin(
    graph: TransitionGraph<S, I, A>,
    name: String,
): StorePlugin<S, I, A> = plugin {
    this.name = name

    val handlerDepth = atomic(0)

    onIntent { intent ->
        val currentState = states.value
        val stateClass = currentState::class
        val definition = graph.definitions[stateClass]
            ?: return@onIntent intent
        val handler = definition.handlers[intent::class]
            ?: return@onIntent intent

        handlerDepth.incrementAndGet()
        try {
            handler.invoke(this, currentState, intent)
        } finally {
            handlerDepth.decrementAndGet()
        }

        null
    }

    onState { old, new ->
        if (handlerDepth.value > 0) return@onState new
        if (old::class == new::class) return@onState new
        if (config.debuggable) {
            throw InvalidStateException(new::class.simpleName, old::class.simpleName)
        }
        old
    }
}
```

### 4.4 How State Reading Works in the Plugin

The chain of interfaces that makes `states.value` available inside `onIntent`:

```
PipelineContext<S, I, A>
    extends StateReceiver<S>
        extends ImmediateStateReceiver<S>
            extends StateProvider<S>
                val states: StateFlow<S>   ← states.value gives current S
```

In `PipelineModule.kt`, `PipelineContext` is constructed with `ImmediateStateReceiver<S> by states` (where `states` is the `StateModule`). The `StateModule` holds a `MutableStateFlow` and exposes it as `StateFlow<S>` via the `states` property.

Inside `onIntent`, `this` is `PipelineContext<S, I, A>`, so `this.states.value` gives the current state.

Note: The `@DelicateStoreApi` extension `StateProvider<S>.state` (in `StateDsl.kt`) is shorthand for `states.value`. We use `states.value` directly to avoid the opt-in annotation.

### 4.5 Coexistence with `reduce {}`

The `transitions {}` plugin:
- **Consumes intents** that match a registered `on<E>` handler for the current state type (returns `null` from `onIntent`).
- **Passes through intents** that don't match any handler (returns the intent from `onIntent`).

This means users **can** use both `transitions {}` and `reduce {}` in the same store. Intents not handled by the FSM fall through to the reduce plugin. For a pure-FSM store, `transitions {}` alone is sufficient.

Plugin ordering matters: install `transitions {}` **before** `reduce {}` if using both, so the FSM gets first crack at intents.

### 4.6 Thread Safety Summary

| Concern | Mitigation |
|---------|-----------|
| Concurrent `updateState` | Handled by FlowMVI's `StateStrategy.Atomic` (default mutex) |
| Self-transition detection | `AtomicInt` counter — tracks FSM handler depth |
| Parallel intents | Same TOCTOU trade-off as `reduce {}` — acceptable |
| Graph immutability | `TransitionGraph` is built once and never modified |
| Handler concurrency | Handlers run in `onIntent` context; state mutations serialized by mutex |
| Compose active jobs map | `SynchronizedObject` lock (see 4.8) |

### 4.7 Compose Plugin Architecture

The `transitions {}` builder collects both FSM definitions **and** compose definitions. The `transitionsPlugin()` factory assembles them into a single `compositePlugin`:

```kotlin
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> transitionsPlugin(
    name: String = TransitionsPluginName,
    @BuilderInference block: TransitionsBuilder<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> {
    val builder = TransitionsBuilder<S, I, A>().apply(block)
    val graph = builder.build()  // returns TransitionGraph
    val topLevelCompositions = builder.topLevelCompositions.toList()
    // Scoped compositions are collected from each StateDefinition inside the graph
    val scopedCompositions = graph.definitions.values.flatMap { it.compositions }

    val allChildStores = (topLevelCompositions + scopedCompositions)
        .map { it.store }
        .toSet()

    val fsmPlugin = buildFsmPlugin(graph, name)  // existing onIntent + onState enforcement plugin
    val composePlugin = buildComposePlugin(topLevelCompositions, scopedCompositions)
    val childPlugin = if (allChildStores.isNotEmpty()) {
        childStorePlugin(allChildStores, force = null, blocking = false)
    } else null

    val plugins = listOfNotNull(childPlugin, composePlugin, fsmPlugin)

    return if (plugins.size == 1) plugins.single()
    else compositePlugin(plugins = plugins, name = name)
}
```

**Key architectural decisions:**

1. **Single `compositePlugin`**: The FSM enforcement plugin and compose subscription plugin are bundled into one composite. This ensures they share a name and appear as a single plugin to the store.

2. **Child store lifecycle via `childStorePlugin`**: All composed child stores (both top-level and scoped) are started as lifecycle children of the parent store. They are started once when the parent starts and stopped when the parent stops. State-scoped compose controls the *subscription* lifecycle, not the *store* lifecycle.

3. **Plugin ordering within composite**: `childStorePlugin` first (starts children), then `composePlugin` (subscribes), then `fsmPlugin` (handles intents + enforces). This ensures children are started before subscriptions begin, and subscriptions are set up before intent processing starts.

### 4.8 Compose Plugin Implementation

#### Top-Level Compose (Always Active)

Top-level compositions start subscriptions in `onStart` and run for the lifetime of the parent store:

```kotlin
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> buildComposePlugin(
    topLevel: List<ComposeDefinition<S, I, A, *, *, *>>,
    scoped: List<ComposeDefinition<S, I, A, *, *, *>>,
): StorePlugin<S, I, A>? {
    if (topLevel.isEmpty() && scoped.isEmpty()) return null

    return plugin {
        // --- Top-level: subscribe once in onStart ---
        onStart {
            for (def in topLevel) {
                launchComposeSubscription(def)
            }

            // --- Scoped: monitor parent state type changes ---
            if (scoped.isNotEmpty()) {
                launchScopedCompositions(scoped)
            }
        }
    }
}
```

#### `launchComposeSubscription` — Subscribes to a Child Store

```kotlin
@Suppress("UNCHECKED_CAST")
private fun <S : MVIState, I : MVIIntent, A : MVIAction> PipelineContext<S, I, A>.launchComposeSubscription(
    def: ComposeDefinition<S, I, A, *, *, *>,
): Job = launch {
    val typedDef = def as ComposeDefinition<S, I, A, MVIState, MVIIntent, MVIAction>
    typedDef.store.collect {
        // Subscribe to child state changes → merge into parent state
        launch {
            states.collect { childState ->
                updateState { typedDef.merge(this, childState) }
            }
        }
        // Subscribe to child actions (if consume provided)
        typedDef.consume?.let { consume ->
            launch { actions.collect { consume(this@launchComposeSubscription, it) } }
        }
        awaitCancellation()
    }
}
```

This follows the same pattern as `StoreDelegate.subscribeChild()` — it calls `store.collect {}` which internally calls `store.subscribe {}` and provides a `Provider<CS, CI, CA>` with `states` and `actions` flows.

#### State-Scoped Compose — Monitoring Parent State Type Changes

```kotlin
private fun <S : MVIState, I : MVIIntent, A : MVIAction> PipelineContext<S, I, A>.launchScopedCompositions(
    scoped: List<ComposeDefinition<S, I, A, *, *, *>>,
) {
    // Track active subscription jobs per compose definition
    val activeJobs = SynchronizedObject()
    val jobMap = mutableMapOf<ComposeDefinition<S, I, A, *, *, *>, Job>()

    launch {
        // Monitor parent state changes via states flow
        states.collect { parentState ->
            synchronized(activeJobs) {
                for (def in scoped) {
                    val shouldBeActive = def.scopedToState?.isInstance(parentState) == true
                    val currentJob = jobMap[def]

                    if (shouldBeActive && (currentJob == null || !currentJob.isActive)) {
                        // Entering scoped state → start subscription
                        jobMap[def] = launchComposeSubscription(def)
                    } else if (!shouldBeActive && currentJob != null) {
                        // Leaving scoped state → cancel subscription
                        currentJob.cancel()
                        jobMap.remove(def)
                    }
                }
            }
        }
    }
}
```

#### How State-Scoped Compose Handles Key Scenarios

**1. Initial merge on state entry:**
When the parent transitions into the scoped state, `launchComposeSubscription` starts a subscription to the child's `states` flow. Since `StateFlow` always has a current value, the `collect` lambda fires immediately with the child's current state, triggering a `updateState { merge(this, childState) }`. This ensures the parent state is immediately updated with the child's latest state upon entering the scoped state.

**2. Leaving the scoped state:**
When the parent transitions out of the scoped state, the subscription job is cancelled. Any in-flight child state changes that haven't been merged yet are dropped. This is correct behavior — the parent is no longer in a state that cares about the child's state.

**3. Re-entering the scoped state:**
A new subscription is created, and the child's current state is merged again immediately. The child store itself is still running (its lifecycle is tied to the parent store, not the scope), so it may have continued processing while the parent was in a different state.

**4. Interaction with `handlerDepth` enforcement:**
The `updateState` calls from compose subscriptions happen **outside** of `onIntent` handlers, so `handlerDepth` is 0. These are same-type updates (e.g., `copy(feed = newFeed)` on a data class), so `old::class == new::class` and they pass the `onState` enforcement check.

**Important constraint**: The `merge` lambda in state-scoped compose should return the **same state type** as the current state (e.g., `copy()` on a data class). If it returns a different state type, it will be blocked by `onState` enforcement. This is by design — state type transitions should only happen through explicit `on<Intent>` handlers.

**5. Thread safety of `jobMap`:**
The `jobMap` is mutated only inside `states.collect {}`, which runs on a single coroutine (the `launch` in `onStart`). However, `launchComposeSubscription` returns immediately (it launches a new coroutine), so there's no suspension between `jobMap` reads and writes within a single `collect` invocation. The `SynchronizedObject` provides belt-and-suspenders safety, particularly if `states` were to emit from a different dispatcher. In practice, `StateFlow.collect` delivers emissions on the collector's coroutine context, so the lock is rarely contended.

---

## 5. Data Model

### 5.1 `TransitionGraph<S, I, A>` (internal)

```kotlin
/**
 * Immutable transition graph that maps (StateType, IntentType) → handler.
 * Also holds compose definitions collected from state blocks.
 */
internal class TransitionGraph<S : MVIState, I : MVIIntent, A : MVIAction>(
    /** Map from state KClass to its [StateDefinition]. */
    val definitions: Map<KClass<out S>, StateDefinition<S, I, A>>,
)
```

### 5.2 `StateDefinition<S, I, A>` (internal)

```kotlin
/**
 * All intent handlers and compose definitions registered for a particular state type.
 */
internal class StateDefinition<S : MVIState, I : MVIIntent, A : MVIAction>(
    /** The KClass of the state this definition applies to. */
    val stateType: KClass<out S>,
    /** Map from intent KClass to handler. */
    val handlers: Map<KClass<out I>, IntentHandler<S, I, A>>,
    /** Compose definitions scoped to this state type. */
    val compositions: List<ComposeDefinition<S, I, A, *, *, *>> = emptyList(),
)
```

### 5.3 `IntentHandler<S, I, A>` (internal typealias)

```kotlin
internal typealias IntentHandler<S, I, A> =
    suspend PipelineContext<S, I, A>.(state: S, intent: I) -> Unit
```

### 5.4 `ComposeDefinition<S, I, A, CS, CI, CA>` (internal)

```kotlin
/**
 * Internal representation of a compose() call. Captures all information needed
 * to set up and manage a child store subscription.
 */
internal class ComposeDefinition<S : MVIState, I : MVIIntent, A : MVIAction, CS : MVIState, CI : MVIIntent, CA : MVIAction>(
    /** The child store to subscribe to. */
    val store: Store<CS, CI, CA>,
    /** Merges child state into parent state. Receiver is the current parent state. */
    val merge: S.(CS) -> S,
    /** Optional handler for child actions, running in parent's PipelineContext. */
    val consume: (suspend PipelineContext<S, I, A>.(CA) -> Unit)?,
    /** If non-null, composition is active only while parent is in this state type.
     *  If null, always active (top-level). */
    val scopedToState: KClass<out S>?,
)
```

### 5.5 Public Types Summary

| Type | Visibility | Description |
|------|-----------|-------------|
| `TransitionScope<T, S, I, A>` | public interface | Handler context — extends `PipelineContext`, adds `state: T`, `transitionTo()` |
| `TransitionScopeImpl` | `@PublishedApi internal` | Delegates to `PipelineContext` |
| `TransitionsBuilder` | public class | Top-level DSL builder (`state<T> {}`, top-level `compose()`) |
| `StateTransitionsBuilder` | public class | Per-state DSL builder (`on<E> {}`, state-scoped `compose()`) |

### 5.6 Internal Types Summary

| Type | Description |
|------|-------------|
| `TransitionGraph<S, I, A>` | Immutable graph of state definitions |
| `StateDefinition<S, I, A>` | Per-state handler map + compose definitions |
| `IntentHandler<S, I, A>` | Handler function typealias |
| `ComposeDefinition<S, I, A, CS, CI, CA>` | Child store composition specification |

### 5.7 Removed Types (vs. Old Plan)

| Removed | Replaced By |
|---------|-------------|
| `TransitionResult<S, A>` | Imperative calls — handlers return `Unit` |
| `TransitionContext` | `TransitionScope` (extends `PipelineContext`) |
| `dontTransition()` | Just don't call `transitionTo()` |
| `allowedTransitions: Set<Pair<...>>` | Self-initiated flag mechanism |

---

## 6. Comparison with `reduce {}`

Side-by-side: the same logic in `reduce {}` vs `transitions {}`.

| Aspect | `reduce {}` | `transitions {}` |
|--------|-------------|-------------------|
| State type checking | Manual `when(this)` + casts | Automatic — `state` is typed to `T` |
| Transition validation | None — any state can go anywhere | Runtime enforcement via `onState` |
| Intent scoping | All intents in one `when` block | Intents scoped per state type |
| PipelineContext access | Full | Full (via delegation) |
| Async work | `launch {}`, `intent()` | Same |
| Side effects | `action()` | Same, plus `transitionTo(state, sideEffect)` shorthand |
| Learning curve | Minimal | Slightly higher (new DSL) |
| `state` typed access | Manual casting | `state` is `T` automatically |

**Key takeaway**: Everything `reduce {}` can do, FSM handlers can do too — with the addition of typed state and transition enforcement.

---

## 7. File Layout

### New Files

| File | Description |
|------|-------------|
| `core/src/commonMain/kotlin/pro/respawn/flowmvi/plugins/TransitionsPlugin.kt` | Plugin factory `transitionsPlugin()`, `StoreBuilder.transitions()` extension, `TransitionsPluginName` const |
| `core/src/commonMain/kotlin/pro/respawn/flowmvi/dsl/TransitionsBuilder.kt` | `TransitionsBuilder`, `StateTransitionsBuilder` builder classes (including `compose()`) |
| `core/src/commonMain/kotlin/pro/respawn/flowmvi/dsl/TransitionScope.kt` | `TransitionScope` public interface, `TransitionScopeImpl` internal class |
| `core/src/commonMain/kotlin/pro/respawn/flowmvi/plugins/TransitionGraph.kt` | `TransitionGraph`, `StateDefinition`, `IntentHandler`, `ComposeDefinition` internal types |
| `core/src/commonMain/kotlin/pro/respawn/flowmvi/plugins/ComposePlugin.kt` | `buildComposePlugin()`, `launchComposeSubscription()`, `launchScopedCompositions()` internal functions |
| `core/src/jvmTest/kotlin/pro/respawn/flowmvi/test/TransitionsPluginTest.kt` | Unit tests for FSM logic |
| `core/src/jvmTest/kotlin/pro/respawn/flowmvi/test/ComposePluginTest.kt` | Unit tests for compose() functionality |

### Modified Files

None. The FSM is entirely additive.

### File Placement Rationale

- Plugin file in `plugins/` — follows `ReducePlugin.kt`, `InitPlugin.kt`, `LoggingPlugin.kt` pattern
- Builder DSL classes in `dsl/` — follows `StoreBuilder.kt`, `StorePluginBuilder.kt`, `StateDsl.kt` pattern
- `TransitionScope` in `dsl/` — it's a DSL scope interface, similar to `LambdaIntent.kt` in `dsl/`
- Internal graph types in `plugins/` — tightly coupled to the plugin
- `ComposePlugin.kt` in `plugins/` — internal compose subscription management, paired with `TransitionsPlugin.kt`
- `ComposeDefinition` in `TransitionGraph.kt` — co-located with other internal data types
- `@file:MustUseReturnValues` annotation on `TransitionsPlugin.kt` (following `ReducePlugin.kt`)

---

## 8. Task Breakdown

### Task 1: Data Model & TransitionScope

**Files**: `TransitionScope.kt`, `TransitionGraph.kt`

**Description**: Create the `TransitionScope` public interface, `TransitionScopeImpl` internal class, and internal types (`TransitionGraph`, `StateDefinition`, `IntentHandler`).

**Acceptance Criteria**:
- `TransitionScope` extends `PipelineContext<S, I, A>` with `val state: T`, `transitionTo(S)`, `transitionTo(S, A)`
- `TransitionScopeImpl` delegates to `PipelineContext` via `by`
- `TransitionScope` annotated with `@FlowMVIDSL`, `@SubclassOptInRequired(NotIntendedForInheritance::class)`
- Internal types are `internal` visibility
- All public API has KDoc
- Compiles on all targets

### Task 2: DSL Builders

**Files**: `TransitionsBuilder.kt`

**Description**: Implement `TransitionsBuilder` and `StateTransitionsBuilder`. Both annotated with `@FlowMVIDSL`.

**Acceptance Criteria**:
- `state<T> {}` uses reified type params, throws on duplicate state registration
- `on<E> {}` uses reified type params, throws on duplicate intent registration
- `on<E>` handler signature: `suspend TransitionScope<T, S, I, A>.(E) -> Unit`
- `on<E>` wraps the user's typed lambda into `IntentHandler<S, I, A>` with proper casts
- All public API has KDoc
- Constructors are `@PublishedApi internal`

### Task 3: Plugin Implementation

**Files**: `TransitionsPlugin.kt`

**Description**: Implement `transitionsPlugin()` factory and `StoreBuilder.transitions()` extension.

**Acceptance Criteria**:
- `onIntent` dispatches to handlers based on `states.value::class` and `intent::class`
- `onIntent` returns `null` (consumes) for handled intents, returns intent unchanged for unhandled
- `onState` validates transitions using `handlerDepth` counter
- Debug mode throws `InvalidStateException` for invalid external transitions
- Release mode silently vetoes invalid external transitions (returns `old`)
- Self-initiated transitions (via `handlerDepth > 0`) always allowed
- Same-type updates always allowed
- Plugin has default name `TransitionsPluginName`
- Extension function has `@IgnorableReturnValue`
- File has `@file:MustUseReturnValues` annotation
- Follows `reducePlugin()` pattern closely

### Task 3b: Compose Data Model

**Files**: `TransitionGraph.kt` (additions)

**Description**: Add `ComposeDefinition` to the internal data model. Update `StateDefinition` to include `compositions` list.

**Acceptance Criteria**:
- `ComposeDefinition<S, I, A, CS, CI, CA>` captures store, merge, consume, and scopedToState
- `StateDefinition` gains `compositions: List<ComposeDefinition<S, I, A, *, *, *>>` property
- Both are `internal` visibility
- Type erasure to `*` for heterogeneous storage works correctly

### Task 3c: Compose DSL in Builders

**Files**: `TransitionsBuilder.kt` (additions)

**Description**: Add `compose()` to both `TransitionsBuilder` (top-level) and `StateTransitionsBuilder` (state-scoped).

**Acceptance Criteria**:
- `TransitionsBuilder.compose()` adds to `topLevelCompositions` list
- `StateTransitionsBuilder.compose()` adds to per-state `compositions` list
- Both annotated with `@FlowMVIDSL`
- `merge` lambda signature: `S.(CS) -> S` for top-level, `T.(CS) -> S` for state-scoped
- `consume` is optional (`null` by default)
- All public API has KDoc
- `TransitionsBuilder.build()` updated to expose both graph and compositions

### Task 3d: Compose Plugin Implementation

**Files**: `ComposePlugin.kt`

**Description**: Implement the internal compose subscription management: `buildComposePlugin()`, `launchComposeSubscription()`, `launchScopedCompositions()`.

**Acceptance Criteria**:
- Top-level compose subscriptions start in `onStart` and run for store lifetime
- State-scoped compose subscriptions activate when parent enters scoped state type
- State-scoped compose subscriptions cancel when parent leaves scoped state type
- Child state changes trigger `updateState { merge(this, childState) }` in parent
- Child actions trigger `consume` lambda in parent's `PipelineContext` (if provided)
- Uses `Store.collect {}` (which internally calls `subscribe`) — same pattern as `StoreDelegate`
- `SynchronizedObject` protects `jobMap` for scoped compositions
- Returns `null` when no compositions are registered (avoid empty plugin)

### Task 3e: Composite Assembly in `transitionsPlugin()`

**Files**: `TransitionsPlugin.kt` (update)

**Description**: Update `transitionsPlugin()` to assemble FSM plugin + compose plugin + child store plugin via `compositePlugin`.

**Acceptance Criteria**:
- Collects all child stores from compositions and creates a single `childStorePlugin`
- Plugin ordering: `childStorePlugin` → `composePlugin` → `fsmPlugin`
- Falls back to single plugin (no composite) when no compositions exist
- `compositePlugin` name matches `TransitionsPluginName`

### Task 4: Tests

**Files**: `TransitionsPluginTest.kt`, `ComposePluginTest.kt`

**Description**: Comprehensive tests. Read `docs/ai/testing.md` before writing.

**Acceptance Criteria (FSM — `TransitionsPluginTest.kt`)**:
- Test valid transition via `transitionTo()`
- Test `transitionTo(state, sideEffect)` emits the action
- Test handler can call `updateState {}` directly (not just `transitionTo`)
- Test handler can call `action()` directly
- Test handler can call `intent()` to re-emit
- Test handler can call `launch {}` for async work
- Test handler receives correctly typed `state`
- Test invalid external transition is vetoed in non-debug mode
- Test invalid external transition throws `InvalidStateException` in debug mode
- Test same-type state update is always allowed (from external source)
- Test intent passthrough for unhandled intents (no handler for current state)
- Test intent passthrough for unregistered state types
- Test coexistence with `reduce {}` (unhandled intents fall through)
- Test duplicate state definition throws `IllegalArgumentException` at build time
- Test duplicate intent handler throws `IllegalArgumentException` at build time
- Test handler `state` property matches the current state type
- Test `TransitionScope` has access to `config`, `CoroutineScope`, etc.

**Acceptance Criteria (Compose — `ComposePluginTest.kt`)**:
- Test top-level compose merges child state into parent state
- Test top-level compose updates parent when child state changes
- Test top-level compose with consume lambda receives child actions
- Test top-level compose without consume lambda (actions ignored)
- Test state-scoped compose activates when parent enters scoped state
- Test state-scoped compose deactivates when parent leaves scoped state
- Test state-scoped compose re-activates on re-entry to scoped state
- Test state-scoped compose merges child's current state immediately on activation
- Test multiple compose() calls (multiple children) work correctly
- Test compose() with child store that has its own FSM
- Test compose-triggered updateState passes onState enforcement (same-type check)
- Test child store lifecycle is tied to parent store (started/stopped together)
- Test scoped compose cancels in-flight merges when parent transitions away
- Test compose with action forwarding (child action → parent action)

### Task 5: Documentation & Skills

**Files**: Update docs, update skills

**Description**:
- KDoc on all public symbols (done in Tasks 1-3)
- Update `skills/flowmvi` with new API signatures
- Consider adding a doc page in `docs/docs/`

### Sequencing

```
Task 1 (Data Model & TransitionScope)
    ↓
Task 2 (DSL Builders) — depends on Task 1 for TransitionScope, IntentHandler
    ↓
Task 3 (Plugin) — depends on Task 1 + 2
    ↓
Task 3b (Compose Data Model) — depends on Task 1
    ↓
Task 3c (Compose DSL) — depends on Task 2 + 3b
    ↓
Task 3d (Compose Plugin) — depends on Task 3b
    ↓
Task 3e (Composite Assembly) — depends on Task 3 + 3c + 3d
    ↓
Task 4 (Tests) — depends on Task 3e      Task 5 (Docs) — depends on Task 3e
         ↘                                  ↙
          (can run in parallel)
```

**Practical ordering**: Tasks 3b-3e can be implemented incrementally within a single implementation pass after Tasks 1-3. They are separated here for clarity of scope, not necessarily for separate PRs.

---

## 9. Risks & Mitigations

### Risk 1: Handler State Staleness with Parallel Intents

**Risk**: When `parallelIntents = true`, the state might change between reading `states.value` for handler dispatch and the handler reading `state` or calling `updateState`.

**Mitigation**: Same trade-off as `reduce {}`. The `state` property on `TransitionScope` is a snapshot taken at dispatch time. For atomic operations, handlers should use `updateState {}` which receives the actual current state inside the lock. Document this clearly.

### Risk 2: Self-Initiated Flag with Parallel Intents

**Risk**: With `parallelIntents = true`, the `handlerDepth` counter could be > 0 when an external `updateState` is triggered, incorrectly allowing it.

**Mitigation**: `AtomicInt` counter. During FSM handler execution, the counter is > 0. The brief window of relaxed enforcement is acceptable because `launch {}` from handlers is logically FSM work. For stricter enforcement, a coroutine context element could replace the counter in a future iteration.

### Risk 3: `NotIntendedForInheritance` on `TransitionScope`

**Risk**: `TransitionScope` extends `PipelineContext` which has `@SubclassOptInRequired(NotIntendedForInheritance)`. Our interface and impl need to opt in.

**Mitigation**: Apply `@OptIn(NotIntendedForInheritance::class)` on `TransitionScopeImpl`. Apply `@SubclassOptInRequired(NotIntendedForInheritance::class)` on `TransitionScope` itself to prevent external subclassing.

### Risk 4: Runtime Type Erasure on Kotlin/JS & WASM

**Risk**: `KClass` comparisons using `::class` might not work on all targets.

**Mitigation**: Kotlin's `KClass` and `::class` work correctly on all targets (JVM, JS, WASM, Native). Verify in CI with `allTests`.

### Risk 5: `onState` Hook Order with Other Plugins

**Risk**: If other plugins also use `onState`, the FSM's enforcement might interfere.

**Mitigation**: Plugins execute in installation order. The FSM plugin's `onState` runs at its position in the chain. Document that `transitions {}` should be installed as the primary intent handler (same position as `reduce {}`).

### Risk 6: Blocking/Long-Running Handlers

**Risk**: Handlers are `suspend` functions called from `onIntent`. Long-running handlers delay intent processing.

**Mitigation**: Identical to `reduce {}`. Document that handlers should dispatch long-running work via `launch {}` and use `intent()` to send results back, as shown in the usage example.

### Risk 7: `atomicfu` Dependency

**Risk**: The `handlerDepth` counter uses `AtomicInt`. This may require an additional dependency.

**Mitigation**: FlowMVI already depends on `kotlinx-coroutines` and the `kotlinx-atomicfu` compiler plugin is commonly used in KMP projects. Check the existing dependency tree. Alternatively, use `kotlin.concurrent.AtomicInt` (Kotlin 2.1+) or a simple `@Volatile var` — given that `onState` is called under the state mutex, a volatile int is sufficient.

### Risk 8: Compose — State Update Loop

**Risk**: A compose subscription calls `updateState { merge(this, childState) }` on the parent. If the parent's `onState` or another plugin re-triggers a state change that re-triggers the compose subscription, an infinite loop could occur.

**Mitigation**: `StateFlow.collect` in the compose subscription monitors the **child** store's `states`, not the parent's. The child's state only changes when the child processes its own intents. The parent's `updateState` from merge does not affect the child. Therefore, no feedback loop is possible through the compose mechanism itself. If the merge lambda somehow triggers a child intent (which it shouldn't — it's a pure mapping function), that would be a user error. Document that `merge` must be a pure state mapping.

### Risk 9: Compose — `onState` Enforcement Blocking Compose Merges

**Risk**: When a compose subscription calls `updateState { merge(this, childState) }`, the `onState` callback runs. If `handlerDepth` is 0 (no FSM handler active) and the merge produces a different state type, the enforcement will veto or throw.

**Mitigation**: This is by design. Compose merges should always produce the same state type (e.g., `copy(feed = newFeed)` returns the same data class type). Cross-type transitions must go through explicit `on<Intent>` handlers. Document this clearly in the `compose()` KDoc. State-scoped compose inherently guarantees this because the merge lambda's receiver is typed as `T` — the caller would naturally use `copy()`.

### Risk 10: Compose — Scoped Subscription Timing with Fast State Changes

**Risk**: If the parent state changes rapidly (e.g., `Loading → Content → Error` in quick succession), the scoped compose monitoring job (`states.collect`) may process events with delay, leading to brief windows where a subscription is active for a state that has already been exited.

**Mitigation**: The compose subscription uses `launch` which starts asynchronously. Even if the subscription starts for a state that's already been exited, the next `states.collect` emission will cancel it. The worst case is one extra merge call with a stale child state, which is harmless because the parent has already moved to a different state type — and the `onState` enforcement will veto the cross-type update attempt. The `SynchronizedObject` on `jobMap` prevents concurrent modification during rapid transitions.

### Risk 11: Compose — Multiple Children Updating Same State Field

**Risk**: Two compose subscriptions both call `updateState` concurrently. The state mutex serializes them, but one could overwrite the other's merge if they modify different fields on the same data class.

**Mitigation**: Each `updateState { merge(this, childState) }` reads the latest parent state inside the mutex and applies its merge. Because the state is read fresh inside the lock, each merge sees the result of the previous one. This is identical to any concurrent `updateState` calls in FlowMVI — the mutex ensures serializable reads. As long as each `merge` only touches its own field (e.g., `copy(feed = childState)` vs `copy(notifications = childState)`), there's no lost update.

### Risk 12: Compose — Child Store Must Be Started Before Subscribing

**Risk**: `Store.collect {}` (which calls `subscribe`) requires the store to be active. If the compose subscription starts before the child store is started, it might miss initial state or fail.

**Mitigation**: The `compositePlugin` ordering guarantees `childStorePlugin` runs first (in `onStart`), starting all child stores before the compose plugin's `onStart` runs. Additionally, `childStorePlugin` can be configured with `blocking = false` — the child's `start()` call returns immediately and the store begins processing. The compose subscription's `states.collect` on the child will receive the initial `StateFlow` value immediately (StateFlow always has a value), so no state is missed.

---

## Appendix A: Full API Signatures Summary

```kotlin
// ═══════════════════════════════════════════════════════════
// PUBLIC API
// ═══════════════════════════════════════════════════════════

// --- TransitionScope.kt (dsl/) ---

@FlowMVIDSL
@SubclassOptInRequired(NotIntendedForInheritance::class)
public interface TransitionScope<out T : S, S : MVIState, I : MVIIntent, A : MVIAction> :
    PipelineContext<S, I, A> {

    public val state: T
    public suspend fun transitionTo(target: S)
    public suspend fun transitionTo(target: S, sideEffect: A)
}

// --- TransitionsBuilder.kt (dsl/) ---

@FlowMVIDSL
public class TransitionsBuilder<S : MVIState, I : MVIIntent, A : MVIAction> {

    public inline fun <reified T : S> state(
        block: StateTransitionsBuilder<T, S, I, A>.() -> Unit,
    )

    /** Top-level compose — child state merged into parent at all times. */
    public fun <CS : MVIState, CI : MVIIntent, CA : MVIAction> compose(
        store: Store<CS, CI, CA>,
        merge: S.(childState: CS) -> S,
        consume: (suspend PipelineContext<S, I, A>.(CA) -> Unit)? = null,
    )
}

@FlowMVIDSL
public class StateTransitionsBuilder<T : S, S : MVIState, I : MVIIntent, A : MVIAction> {

    public inline fun <reified E : I> on(
        noinline block: suspend TransitionScope<T, S, I, A>.(E) -> Unit,
    )

    /** State-scoped compose — subscribed only while parent is in state T. */
    public fun <CS : MVIState, CI : MVIIntent, CA : MVIAction> compose(
        store: Store<CS, CI, CA>,
        merge: T.(childState: CS) -> S,
        consume: (suspend PipelineContext<S, I, A>.(CA) -> Unit)? = null,
    )
}

// --- TransitionsPlugin.kt (plugins/) ---

public const val TransitionsPluginName: String = "TransitionsPlugin"

public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> transitionsPlugin(
    name: String = TransitionsPluginName,
    block: TransitionsBuilder<S, I, A>.() -> Unit,
): StorePlugin<S, I, A>

@IgnorableReturnValue
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.transitions(
    name: String = TransitionsPluginName,
    block: TransitionsBuilder<S, I, A>.() -> Unit,
): Unit

// ═══════════════════════════════════════════════════════════
// INTERNAL API
// ═══════════════════════════════════════════════════════════

// --- TransitionScope.kt (dsl/) ---

@PublishedApi
internal class TransitionScopeImpl<T : S, S : MVIState, I : MVIIntent, A : MVIAction>(
    pipeline: PipelineContext<S, I, A>,
    override val state: T,
) : TransitionScope<T, S, I, A>, PipelineContext<S, I, A> by pipeline

// --- TransitionGraph.kt (plugins/) ---

internal class TransitionGraph<S : MVIState, I : MVIIntent, A : MVIAction>(
    val definitions: Map<KClass<out S>, StateDefinition<S, I, A>>,
)

internal class StateDefinition<S : MVIState, I : MVIIntent, A : MVIAction>(
    val stateType: KClass<out S>,
    val handlers: Map<KClass<out I>, IntentHandler<S, I, A>>,
    val compositions: List<ComposeDefinition<S, I, A, *, *, *>> = emptyList(),
)

internal typealias IntentHandler<S, I, A> =
    suspend PipelineContext<S, I, A>.(state: S, intent: I) -> Unit

internal class ComposeDefinition<S : MVIState, I : MVIIntent, A : MVIAction, CS : MVIState, CI : MVIIntent, CA : MVIAction>(
    val store: Store<CS, CI, CA>,
    val merge: S.(CS) -> S,
    val consume: (suspend PipelineContext<S, I, A>.(CA) -> Unit)?,
    val scopedToState: KClass<out S>?,
)

// --- ComposePlugin.kt (plugins/) ---

internal fun <S : MVIState, I : MVIIntent, A : MVIAction> buildComposePlugin(
    topLevel: List<ComposeDefinition<S, I, A, *, *, *>>,
    scoped: List<ComposeDefinition<S, I, A, *, *, *>>,
): StorePlugin<S, I, A>?
```

## Appendix B: Comparison with Tinder StateMachine

| Aspect | Tinder StateMachine | FlowMVI FSM |
|--------|---------------------|-------------|
| Standalone vs Plugin | Standalone state machine class | Plugin within existing Store |
| Type parameters | `State, Event, SideEffect` | Uses Store's `S, I, A` |
| `state<T> { on<E> {} }` | ✅ | ✅ (same pattern) |
| `transitionTo(state, sideEffect)` | ✅ | ✅ |
| Handler power | Pure — no side effects beyond transition | Full `PipelineContext` — `launch`, `intent`, `action`, `updateState` |
| Enforcement | Hard (throws) | Configurable (debug=throw, release=veto) |
| Async handlers | ❌ Synchronous only | ✅ Full coroutine support |
| Integration cost | Separate from MVI, manual bridging | Zero — native plugin |
| `onEnter`/`onExit` | ✅ | ❌ (not in scope, can be added later) |
