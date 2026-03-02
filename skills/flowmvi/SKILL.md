---
name: flowmvi
description: FlowMVI usage guidance. Use when working with FlowMVI stores/containers, plugin pipelines, composing stores, decorators, or authoring plugins.
---

## Overview

Use this to learn the API of FlowMVI. Prefer the bundled references for exact APIs and the live official docs for plugins/integrations/state.

Key references in this skill:
- `references/api-signatures.md` for core API signatures.
- `references/plugin-signatures.md` for all plugin/decorator signatures.
- `references/plugin-callbacks.md` for callback behavior and decorator semantics.
- `references/docs-index.md` for live docs URLs (plugins, integrations, state).

Use `rg` over `references/*signatures*.md` for discovery and open URLs from `references/docs-index.md` when you need full docs.

## Core loop

- Define a **Contract**: `MVIState`, `MVIIntent`, `MVIAction`.
- Build a **Store**: uses a plugin pipeline, handles intents, updates state, emits actions (side effects).
- Install **Plugins**: ordered chain of responsibility. Order changes behavior.
- Install **Decorators**: wrap the entire plugin chain and can short-circuit it.
- **Subscribers** render state and handle actions.

## Contract and state design

### State

- Make state immutable and comparable. Use `data class` / `data object`.
- Use sealed interfaces for LCE or multi-branch screens. Avoid placeholder fields.
- Use `EmptyState` if no state is needed.

Example (state family):

```kotlin
sealed interface CounterState : MVIState {
    data object Loading : CounterState
    data class Content(val count: Int) : CounterState
    data class Error(val cause: Exception) : CounterState
}
```

### Intent and action

- `MVIIntent`: inputs/events. Use sealed interfaces for MVI style.
- `MVIAction`: one-off effects, never rely on them for critical logic.
- If no actions, use `Nothing` or the no-action store overload.

```kotlin
sealed interface CounterIntent : MVIIntent {
    data object Tap : CounterIntent
    data class Set(val value: Int) : CounterIntent
}

sealed interface CounterAction : MVIAction {
    data class ShowToast(val text: String) : CounterAction
}
```

## Store creation patterns

### Container pattern

Prefer a container per screen/feature and expose the store as a property.

```kotlin
class CounterContainer : Container<CounterState, CounterIntent, CounterAction> {
    override val store = store(initial = CounterState.Loading) {
        configure { debuggable = BuildFlags.debuggable; name = "CounterStore" }
        reduce { intent ->
            when (intent) {
                is CounterIntent.Set -> updateState<_, CounterState.Content> { copy(count = intent.value) }
                is CounterIntent.Tap -> action(CounterAction.ShowToast("tap"))
            }
        }
    }
}
```

Use `lazyStore` when construction is heavy or should be deferred.

### MVVM+ Intents

Intents can be lambdas (Orbit MVI-style):

```kotlin
// contract
typealias CounterIntent = LambdaIntent<CounterState, CounterAction>

// sending
fun onTap() = store.intent { updateState<State.Content, _> { copy(count = count + 1) } }
```

When using LambdaIntent:
- Mandatory: Install `reduceLambdas` plugin to invoke intent blocks.
- Prefer `ImmutableStore`/`ImmutableContainer` at the edges to avoid leaking context.
- Plugins lose observability into intent contents, prefer regular intents unless codebase already uses lambdas.

## State management (SST)

FlowMVI serializes state transactions by default.

Use:
- `updateState { }` for atomic updates.
- `withState { }` for safe reads.
- Typed overloads for state families: `updateState<T, R> { }` and `withState<T, R> { }` (note always two parameters, of which first is target state, and second is always underscore `_`). Also these need an import.
- `updateStateImmediate` only for performance-critical hot paths; it bypasses plugins and safety.

## PipelineContext API

You get `PipelineContext` receiver inside most of the store DSLs and plugin callbacks. Key operations:

- `updateState { }`, `withState { }`, `updateStateImmediate { }`
- `action(action)` and `intent(intent)`
- `config` for store config and logging
- `Flow.consume(context)` for safe collection in store context

Exact signatures in `references/api-signatures.md`.

## Store configuration

Configure inside `store { configure { ... } }`. Defaults are defined in `StoreConfigurationBuilder`.

Key flags:
- `debuggable`: enable validations + verbose logging.
- `name`: used for logging/debugging and store identity.
- `parallelIntents`: process intents concurrently (state safety becomes critical).
- `coroutineContext`: merged with start scope.
- `actionShareBehavior`: `Distribute`, `Share`, `Restrict`, `Disabled`.
- `intentCapacity` + `onOverflow`: buffer sizing and backpressure.
- `stateStrategy`: `Atomic(reentrant = true/false)` or `Immediate`.
- `allowIdleSubscriptions`, `allowTransientSubscriptions`.
- `logger`, `verifyPlugins`.

See exact defaults in `references/api-signatures.md` and use `references/docs-index.md` for the latest state docs.

Exact signatures: `references/api-signatures.md`.

## Plugins (system)

Plugins are the core of FlowMVI. Order matters.

- Install plugins in the order you want them to intercept events.
- Plugins can consume, modify, or veto intents/actions/states.
- Use `reduce` early if you want later plugins to observe processed intents.

All plugin signatures are in `references/plugin-signatures.md`.
All callback signatures and behavior are in `references/plugin-callbacks.md`.

### Prebuilt plugin catalog

Open `references/docs-index.md` and `references/plugin-signatures.md` for the full list. Core plugins include:

- reduce, init, asyncInit, recover, whileSubscribed
- enableLogging
- cache / asyncCache
- manageJobs
- awaitSubscribers
- undoRedo
- disallowRestart
- timeTravel
- consumeIntents
- deinit, resetStateOnStop
- saved state plugins (savedstate module)
- remote debugger plugins (debugger modules)

## Creating custom plugins

Use the plugin DSL to intercept store events. Use `lazyPlugin` when you need `config` at creation time.

```kotlin
val analytics = plugin<State, Intent, Action> {
    onIntent { intent -> log { "intent = $intent" }; intent }
}
```
Rules:
- Do not call `updateState` inside `onState` to avoid loops.
- Never throw from `onUndeliveredIntent`/`onUndeliveredAction`.

Use `references/plugin-callbacks.md` for full behavior rules.

## Decorators (plugins for plugins)

Decorators wrap plugins and can short-circuit the entire chain.

- decorators run **after** plugins; they wrap all previously installed plugins.
- If you do not invoke `child.onX(...)` in a decorator, the chain stops there.
- Decorators return final values, not intermediate chain values.

Use decorators only when:
- You need to instrument, retry, debounce, or batch the **whole chain**.
- You want cross-cutting behavior that is hard to express as a plugin without chain control.

### Child stores

Tie lifecycle of child stores to a parent store:

```kotlin
val child = store(ChildState.Loading) { /* ... */ }
val parent = store(ParentState.Loading) {
    this hasChild child
}
```

### Delegation

Delegate state/actions from another store and project them:

```kotlin
val feedState by delegate(feedStore) { action -> /* handle */ }
whileSubscribed { feedState.collect { state -> /* render */ } }
```

Use `DelegationMode.Immediate` when you need always-hot projections.
Default `WhileSubscribed` mode yields stale projections when no subscribers.

Prefer building tree-like store hierarchies using children and delegates for complex business logic.

# Other 
Use savedstate module for persistence, metrics module to set up metrics, and essenty for decompose integration.

More info in `references/docs-index.md` 
