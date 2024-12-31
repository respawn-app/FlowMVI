---
sidebar_position: 2
---

# FAQ

### "Cannot inline bytecode" error

The library's minimum JVM target is set to 11 (sadly still not the default in Gradle).
If you encounter an error:

```
Cannot inline bytecode built with JVM target 11 into bytecode that
is being built with JVM target 1.8. Please specify proper '-jvm-target' option
```

Then configure your kotlin multiplatform compilation to target JVM 11 in your subproject's `build.gradle.kts`:

```kotlin
kotlin {
    androidTarget { // do the same for JVM/desktop target as well
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}


```

And in your android gradle files, set:

```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

If you support Android API \<26, you will also need to
enable [desugaring](https://developer.android.com/studio/write/java8-support).


### Tips:

* Avoid using `sealed class`es and use `sealed interface`s whenever possible. Not only this reduces object allocations,
  but also prevents developers from putting excessive logic into their states and/or making private/protected
  properties. State is a simple typed data holder, so if you want to use protected properties or override functions,
  it is likely that something is wrong with your architecture.
* Use nested class imports and import aliases to clean up your code, as contract class names can be long sometimes.
* Use value classes to reduce object allocations if your Intents are being sent frequently, such as for text field
  value changes or scroll events.
    * You can use the `updateStateImmediate` function to optimize the
      performance of the store by bypassing all checks and plugins.
    * Overall, there are cases when changes are so frequent that you'll want to just leave some logic on the UI layer to
      avoid polluting the heap with garbage collected objects and keep the UI performant.
* Avoid subscribing to a bunch of flows in your Store. The best way to implement a reactive UI pattern is to
  use `combine(vararg flows...)` and merge all of your data streams into one flow, and then just use the `transform`
  block to handle the changes.
    * With this, you can be sure that your state is consistent even if you have 20 parallel data streams from different
      sources e.g. database cache, network, websockets and other objects.
* Avoid using platform-level imports and code in your Store/Container/ViewModel whenever possible. This is optional, but
  if you follow this rule, your **Business logic can be multiplatform**! This is also very good for the architecture.
* There is an ongoing discussion about whether to name your intents starting with the verb or with the noun.
    * Case 1: `ClickedCounter`
    * Case 2: `CounterClicked`
      In general, this is up to your personal preference, just make sure you use a single style across all of your
      Contracts. I personally like to name intents starting with the verb (Case 1) for easier autosuggestions from the
      IDE.

### Opinionated naming design

Here's an example of rules we use at [Respawn](https://respawn.pro) to name our Contract classes:

* `MVIIntent` naming should be `<TypeOfActionInPastTense><Target>`.
  Example: `ClickedCounter`, `SwipedDismiss` (~~CounterClick~~).
* `MVIAction`s should be named using verbs in present tense. Example: `ShowConfirmationPopup`, `GoBack`.
    * Navigation actions should be using the `GoTo` verb (~~NavigateTo, Open...~~) Example: `GoToHome`.
      Do not include `Screen` postfix. `GoToHome`~~Screen~~.
* `MVIState`s should be named using verbs in present tense using a gerund. Examples: `EditingGame`, `DisplayingSignIn`.

## FAQ

### My intents are not reduced! When I click buttons, nothing happens, the app just hangs.

* Did you call `Store.start(scope: CoroutineScope)`?
* Did you call `Store.subscribe()`?

### My Actions are not consumed, are dropped or missed.

1. Examine if you have `subscribe`d to the store correctly. Define the lifecycle state as needed.
2. Check your `ActionShareBehavior`. When using `Share`, if you have multiple subscribers and one of them is not
   subscribed yet, or if the View did not manage to subscribe on time, you will miss some Actions.
   This is a limitation of the Flow API and there are no potential
   resolutions at the moment of writing. Try to use `Distribute` instead.
3. If one of the subscribers doesn't need to handle Actions, you can use another overload of `subscribe` that does not
   subscribe to actions.
4. Try to use an `onUndeliveredIntent` handler of a plugin or install a logging plugin to debug missed events.

### Why does `updateState` and `withState` not return the resulting state? Why is there no `state` property I can access?

FlowMVI is a framework that enables you to build highly parallel, multi-threaded systems. In such systems, multiple
threads may modify the state of the `Store` in parallel, leading to data races, thread races, live locks and other
nasty problems. To prevent that, FlowMVI implements a strategy called "transaction serialization" which only allows
**one** client at a time to read or modify the state. Because of that, you can be sure that your state won't change
unexpectedly while you're working with it. However, any state that you pass outside of the scope of `withState` or
`updateState` should be **considered invalid** immediately. You can read more about serializable state transactions in
the [article](https://proandroiddev.com/how-to-safely-update-state-in-your-kotlin-apps-bf51ccebe2ef).
Difficulties that you are facing because of this likely have an easy solution that requires a bit more thinking.
As you continue working with FlowMVI, updating states safely will come naturally to you.

### In what order are intents, plugins and actions processed?

* Intents: FIFO or undefined based on the configuration parameter `parallelIntents`.
* Actions: FIFO.
* States: FIFO.
* Plugins: FIFO (Chain of Responsibility) based on installation order.
* Decorators: FIFO, but after all of the regular plugins.

### When I consume an Action, the other actions are delayed or do not come.

Since actions are processed sequentially, make sure you launch a coroutine to not prevent other actions from coming and
suspending the scope. This is particularly obvious with things like snackbars that suspend in compose.

### I want to expose a few public functions in my container for the store. Should I do that?

You shouldn't. Use an Intent / Action to follow the contract, unless you are using `LambdaIntent`s.
In that case, expose the parent `ImmutableContainer` / `ImmutableStore` type to hide the `intent` function from
subscribers.

### How to use paging?

Well, this is a tricky one. `androidx.paging` breaks the architecture by invading all layers of your app with UI
logic. The best solution we could come up with is just passing a PagingFlow as a property in the state.
This is not good, because the state becomes mutable and non-stable, but there's nothing better we could come up with,
but it does its job, as long as you are careful not to recreate the flow and pass it around between states.
If you have an idea or a working Paging setup, let us know and we can add it to the library!

The Paging library also relies on the `cachedIn` operator which is tricky to use in `whileSubscribed`, because that
block is rerun on every subscription, recreating and re-caching the flow.
To fix this issue, use `cachePlugin` to cache the paginated flow, and then pass it to `whileSubscribed` block.
This will prevent any leaks that you would otherwise get if you created a new flow each time a subscriber appears.

```kotlin
val pagingFlow by cache {
    repo.getPagingDataFlow().cachedIn(this)
}
```

### I have like a half-dozen various flows or coroutines and I want to make my state from those data streams. Do I subscribe to all of those flows in my store?

It's preferable to create a single flow using `combine(vararg flows...)` and produce your state based on that.
This will ensure that your state is consistent and that there are no unnecessary races in your logic.
As flows add up, it will become harder and harder to keep track of things if you use `updateState` and `collect`.

### How do I handle errors?

There are two ways to do this.

1. First one is using one of the Result wrappers, like [ApiResult](https://github.com/respawn-app/apiresult), a monad
   from Arrow.io or, as the last resort, a `kotlin.Result`.
2. Second one involves using a provided `recover` plugin that will be run when an exception is
   caught in plugins or child coroutines, but the plugin will be run **after** the job was already cancelled, so you
   cannot continue the job execution anymore.

### But that other library allows me to define 9000 handlers, actors, processors and whatnot - and I can reuse reducers. Why not do the same?

In general, a little boilerplate when duplicating intents is worth it to keep the consistency of actions and intents
of screens intact.
You usually don't want to reuse your actions and intents because they are specific to a given screen or flow.
That makes your logic simpler, and the rest can be easily moved to your repository layer, use cases or just plain
top-level functions. This is where this library is opinionated, and where one of its main advantages - simplicity, comes
from. Everything you want to achieve with inheritance can already be achieved using plugins or child/parent
stores. For example, if you still want a reducer object and a plugin for it, all you have to do is:

```kotlin
fun interface Reducer<S : MVIState, I : MVIIntent> {

    operator fun S.invoke(intent: I): S
}

fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.reduce(
    reducer: Reducer<S, I>
) = reducePlugin<S, I, A>(consume = true) {
    updateState {
        with(reducer) { invoke(it) }
    }
}.install()
```

### How to avoid class explosion?

1. Modularize your app. The library allows you to do that easily.
2. Use nested classes. For example, you can define an `object ScreenContract` and nest your state, intents, and actions
   inside to make autocompletion easier.
3. Use `LambdaIntent`s. They don't require subclassing `MVIIntent`.
4. Disallow Actions for your store. Side effects are sometimes considered an anti-pattern, and you may want to disable
   them if you care about the architecture this much.

### What if I have sub-states or multiple Loading states for different parts of the screen?

Create nested classes and host them in your parent state.
Example:

```kotlin
sealed interface NewsState : MVIState {
    data object Loading : NewsState
    data class DisplayingNews(
        val suggestionsState: SuggestionsState,
        val feedState: FeedState,
    ) : NewsState {
        sealed interface SuggestionsState {
            data object Loading : SuggestionsState
            data class DisplayingSuggestions(val suggestions: List<Suggestion>) : SuggestionsState
        }

        sealed interface FeedState {
            data object Loading : FeedState
            data class DisplayingFeed(val news: List<News>) : FeedState
        }
    }
}
```

* Use `T.withType<Type>(block: Type.() -> Unit)` to cast your sub-states easier as
  the `(this as? State)?.let { } ?: this` code can look ugly.
* Use `T.typed<Type>()` to perform a safe cast to the given state to clean up the code.
* You don't have to have a top-level sealed interface. If it's simpler, you can just use a data class on the top level.

### I want to use a resource or a framework dependency in my store. How can I do that?

The best solution would be to avoid using platform dependencies such as string resources.
Instead, you could delegate to the UI layer - the one entity that **should** be handling data representation.
That would result in creating a few more intents and other classes, but it will be worth it to achieve better SoC.
If you are not convinced, you could try to use a resource wrapper. The implementation will vary depending on your needs.
