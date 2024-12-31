---
sidebar_position: 1
sidebar_label: Updating State
---

# Managing State

State management in FlowMVI is slightly different from what we are used to in MVVM.
This guide will teach you everything about how to manage and update application state in a safe, fast, and extensible manner.

## Understanding State

In FlowMVI, state is represented by classes implementing the `MVIState` marker interface. States must be:

-   **Immutable** - State object must never change after it is created
-   **Comparable** - State object must implement a stable and valid `hashCode`/`equals` contract

To adhere to the requirements above, the only thing you need to do in most cases is to use `data class`es or `data object`s that will generate `equals` and `hashCode` for you.

The simplest state looks like this:

```kotlin
data class CounterState(
    val counter: Int = 0,
    val isLoading: Boolean = false
) : MVIState
```

The marker interface `MVIState` is needed to **enforce** the requirements above at compilation time. If you do not define `equals`, your IDE will complain.
It also prevents you from using unintended objects (like 3rd party interfaces or network response objects) as your state.

:::warning[Do not mutate the state directly!]

Do not use mutable properties like `var` or mutable collections.
Always create new instances using `copy()` and ensure the collections you pass are **new** ones, not just `Mutable` collections upcasted to their parent type,
otherwise your state updates will **not be reflected**.

:::

:::tip[Empty state]

If your store does not have a `State`, you can use an `EmptyState` object provided by the library.

:::

## State Families

The state in the example above can be used by itself, but most apps can be in more than one state at a time. The best example is LCE (loading-content-error) state family.
The key things we want from such state are that:

-   There are no unused/junk/placeholder values for each state.
    -   For example, during an `Error` state, there is no data because it failed loading, and vice versa, when the state is `Content`, we don't have an error to report.
        Having these properties unnecessarily pollutes the code and fosters many bugs.
-   State clients who use it cannot gain access to unwanted data.
    -   This means that if we pass our state to the UI to render it, we want to avoid accidentally showing **both** an error message **and** the list of items.
        The only way to reliably prevent that is to **enforce** a compile-time error on clients, making them handle all different types of states.

To achieve our goals above, FlowMVI supports **State Families**, represented as sealed interfaces:

```kotlin
sealed interface LCEState : MVIState {

    data object Loading : LCEState
    data class Error(val e: Exception) : LCEState
    data class Content(val items: List<Item>) : LCEState
}
```

:::tip[Why an interface?]

Using `sealed interface` instead of `class` improves performance by reducing allocations and prevents state classes from having any logic (private members).

:::

Now not only must the client handle all states using an exhaustive `when` block, but we no longer expose any properties that shouldn't be present, such as having `items: List<Item>? = null` or `e: Exception? = null`.

However, the code above introduces some complexity to handling state types, such as needing to cast or check the state's type before updating it:

```kotlin
val current = state.value as? Content ?: return
current.items // use the property
```

For that, the library provides a nice and simple DSL consisting of two functions:

```kotlin
updateState<Content, _> { // this: LCEState.Content
    copy(items = items + loadMoreItems())
}

withState<Error, _> { // this: LCEState.Error
    action(ShowErrorMessage(exception = this.e))
}
```

These functions first check the type of the state, and if it is not of the first type parameter, they skip the operation inside the `block` entirely.
The latter one only checks the state but does not change it.

:::tip[Fail Fast]

If you want to throw an exception instead of skipping the operation, there are `updateStateOrThrow` / `withStateOrThrow` functions.

:::

Using the functions above not only simplifies our code but also prevents various bugs due to the asynchronous nature of state updates, such as the user spamming buttons during
an animation, leading to, for example, the app retrying failed data loading multiple times.

### Nested State Families

Of course, you can mix and match the approaches above or introduce multiple nesting levels to your states. For example, to implement progressive content loading, you can create
a common state data class with multiple state families nested inside:

```kotlin
sealed interface FeedState: MVIState {
    data object Loading: FeedState
    data class Content(val items: List<Item>): FeedState
}

// implementing `MVIState` for nested states is not required but beneficial
sealed interface RecommendationsState: MVIState { /* ... */ }

data class ProgressiveState(
    val feed: FeedState = FeedState.Loading,
    val recommended: RecommendationsState = RecommendationsState.Loading,
    /* ... */
): MVIState
```

In that case, you will not need the typed versions of `updateState`, but rather want to use two other functions provided by the library:

-   `value.typed<T>()` to cast the `value` to type `T` or return `null` otherwise (just like the operator `as?`)
-   `value.withType<T, _> { }` to operate on `value` only if it's of type `T`, or just return it otherwise

If you represent the state this way, you will never have to write ugly `null`-ridden code again to manage states with placeholders,
and your stores' subscribers will never have the problem of rendering an inconsistent or invalid state.

But next, let's talk about thread safety.

## Serialized State Transactions (SSTs)

The key difference that FlowMVI has over conventional approaches is that state transactions (changes) are **serialized**.
This has nothing to do with JSON or networking, but rather, the term comes from the [Database Architecture](<https://en.wikipedia.org/wiki/Isolation_(database_systems)>).
In simple terms:

> Store's `state` is changed **sequentially** to prevent data races.

Consider the following:

```kotlin
val state = MutableStateFlow(State(items = emptyList()))

suspend fun loadMoreItems() {
    val lastIndex = state.value.items.lastIndex // (1)
    val newItems = repository.requestItems(atIndex = lastIndex, amount = 20)
    state.value = state.value.copy(items = items + newItems) // (2)
}
```

This code contains multiple race conditions:

1. The code obtains the index to the last item to load more items, then executes a long operation, during which **the state may have already changed**.
   When trying to add new items, the item list could have already been modified, and we'll get duplicate or stale values appended. This is a **data race**.
2. When mutating the state using `state.value`, while the right-hand side of the expression is being executed, the left-hand side
   (`state.value`) could have already been changed by another thread, leading to the right-hand side overwriting the state with stale data. This is a **thread race**.

The first problem arises only when we need to **read the current state** to make a decision on which logic to execute next.
In the example above, we need the current items to load the next page, but this is apparent in many other cases as well,
such as form validation, conditional logic, or LCE-based logic.

You may have never encountered these issues before, but this is only due to luck - because the state is modified on the main thread and sequentially.
FlowMVI, unlike many other libraries, allows the state to be modified on **any thread** to enable much better performance and reactiveness of your apps.

To address a common objection to this argument that sounds like:

> But I can just read the state in the `update` block because it's thread-safe!

Study the [documentation](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-mutable-state-flow/#1996476265%2FFunctions%2F1975948010) of the update function:

> `function` may be evaluated multiple times, if `value` is being concurrently updated.

This means that if the starting and ending states do not match, your `function` block will be executed **multiple times**.
This not only wastes resources but can be detrimental if you call a function that is not _idempotent_ (has side effects) inside the `function` block.
For example, if you are making a banking app and perform a transaction, then the code above can lead to the user's credit card being charged multiple times.
Pretty awful, don't you agree?

Even if you always modify the state on the main thread, or use `update { }`, the parallel nature of coroutines does not prevent you from having data races.

To combat this, the library uses SSTs by default when you gain any access to the `state`.
This is also why you can't just access `state` wherever you wish.
Instead, you only have 2 functions to gain access to the state: `updateState` and `withState`.

The typed functions we discussed in the previous section are just aliases to the more generic ones, and they work by making sure that while the `block` is executing,
**no one else can read or change the state**. Whenever you call `updateState`, the transaction is synchronized using a Mutex, and all other code that tries to also
call `updateState` will wait in a FIFO queue until the `block` finishes execution and the state is updated.

This means that when you have access to the state, you can be absolutely sure it will not change to something unexpected inside the lambda.
You can execute any suspending and long-running code inside the block and it will only be executed **once** and on the most up-to-date value.
So even in systems with hundreds of parallel state updates, it will always stay consistent.

:::info

When you use `updateState`, your store's plugins will receive both the initial and the resulting value, so that they can also respond to the update or modify it.
This does not happen when `withState` is called as the state does not change.

:::

### Reentrant vs Non-reentrant

By default, SSTs are **reentrant**, which means that you are allowed to write code like this:

```kotlin
updateState {
    updateState {

    }
}
```

In that case, if you are already inside a state transaction, a new one will **not be created** for nested update blocks.
Otherwise, you would get a **deadlock** as the inner transaction waits for the outer state update to finish, which waits for the inner update to finish.

This, unfortunately, has a performance penalty due to context switching, which makes the reentrant transactions ~1600% slower.
For most apps, this is negligible as the time is still measured in microseconds, so as of 3.x SSTs are reentrant by default.

When configuring a store, it is possible to change the `stateStrategy` property to make the transactions non-reentrant:

```kotlin
val store = store(initial = Loading) {
    configure {
        stateStrategy = Atomic(reentrant = false)
        debuggable = true
    }
}
```

In this case, while the store is `debuggable`, the library will check the transaction for you so that instead of a deadlock you at least get a crash.

:::info[New Default in 4.0]

In the future, non-reentrant transactions may become the default for the simple reason of redundancy.
Since you are in the transaction, can just use `this` property, which is already the most up-to-date state.

:::

### Bypassing SSTs

Although non-reentrant transactions are already very fast, they are still ~2x slower due to locking overhead.

Only if you absolutely **must** squeeze the last drop of performance from the store, and you are **sure** you can avoid the pitfalls discussed above manually,
you can use one of two ways to override the synchronization routines:

-   `updateStateImmediate` function, which avoids all locks, **plugins** and thread safety, or
-   `StateStrategy.Immediate` which disables SSTs entirely.

:::danger

`updateStateImmediate` **bypasses all plugins** in addition to lacking thread safety!

:::

One example where that may be needed is Compose's text fields:

```kotlin
data class State(val input: String = "") : MVIState

// composable:
TextField(
    value = state.input,
    onValueChange = { intent(ChangedInput(it)) },
)

// container:
val store = store(State()) {
    reduce { intent ->
        when(intent) {
            is ChangedInput -> updateStateImmediate {
                copy(input = intent.value)
            }
        }
    }
}
```

Due to flaws in Compose's `TextField` implementation, if you do not update the state immediately, the UI will have jank.
This will be addressed in future Compose releases with `BasicTextField2`.

:::warning[Do not leak the state]

Despite all the effort put into safety, it's still possible to leak the state.
For example, assigning it to external variables or launching coroutines while in an SST will **leak** the current state outside the transaction.
If you do leak the state, always assume that **any** state outside the transaction is **invalid and outdated**.

:::

## Reactive State Management

With MVVM, a best practice is to produce the state from several upstream flows using `combine`, then the `stateIn` operator to make the flow hot.

A key distinction of MVI compared to MVVM is that a BLoC always has a single, hot, mutable state.
To avoid resource leaks and redundant work, the state must only be updated **while the subscribers are present** (otherwise who is there to render the state?).
FlowMVI provides the API for that in the form of the `whileSubscribed` plugin:

```kotlin
val store = store(ProgressiveState()) { // initial value just like stateIn

    whileSubscribed {
        combine(
            repo.getFeedFlow(),
            repo.getRecommendationsFlow(),
        ) { feed, recommendations ->
            updateState {
                copy(
                    feed = FeedState.Content(feed),
                    recommended = RecommendationsState.Content(recommendations),
                )
            }
        // don't forget to collect the flow
        // highlight-next-line
        }.consume(Dispatchers.Default)
    }
}
```

So make sure that instead of collecting long-running or infinite flows in the `init` plugin, you observe data streams only when subscribers are present, unless your
internal store logic depends on the state being up-to-date in the background.

Additionally, whenever you produce your state, such as in the `combine` lambda, you must **consider the current state**.
For example, if your state has an in-memory value, such as a text input, and you use a State Family, you must "persist" the previous value so that it is not overridden:

```kotlin
sealed interface State : MVIState {

    data object Loading : State
    data class Content(
        val items: List<Item>,
        val searchQuery: String = "", // in-memory value
    ) : State
}

val store = store(State.Loading) {

    whileSubscribed {
        repo.getItems().collect { items ->
            updateState {
                State.Content(
                    items = items,
                    // highlight-next-line
                    searchQuery = typed<Content>()?.searchQuery ?: "" // preserve the input
                )
            }
        }
    }
}
```

In the code above, we use the `typed` function to check the type of the previous state, and if it was already `Content`, we preserve the `searchQuery` value.

The framework has no preference over whether to keep a separate flow like in MVVM, or to keep the value in the state directly,
but the state-based approach has the advantage of using SSTs and state families to achieve greater safety.

If your concern is the boilerplate, you can extract your in-memory data into a separate `data class`, which only needs one type-check to preserve.
