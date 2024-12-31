---
sidebar_position: 2
---

# FAQ

### How to fix "Cannot inline bytecode" error?

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

### How to name Intents, States, Actions?

There is an ongoing discussion on how to name Intents/States/Actions.
Here's an example of rules we use at [Respawn](https://respawn.pro) to name our Contract classes:

* `MVIIntent` naming should be `<TypeOfActionInPastTense><Target>`.
  Example: `ClickedCounter`, `SwipedDismiss` (~~CounterClick~~).
* `MVIAction`s should be named using verbs in present tense. Example: `ShowConfirmationPopup`, `GoBack`.
    * Navigation actions should be using the `GoTo` verb (~~NavigateTo, Open...~~) Example: `GoToHome`.
      Do not include `Screen` postfix. `GoToHome`~~Screen~~.
* `MVIState`s should be named using verbs in present tense using a gerund. Examples: `EditingGame`, `DisplayingSignIn`.

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

### In what order are intents, plugins and actions processed?

* Intents: FIFO or undefined based on the configuration parameter `parallelIntents`
* Actions: FIFO
* States: FIFO
* Plugins: FIFO (Chain of Responsibility) based on installation order
* Decorators: FIFO, but after all of the regular plugins

### When I consume an Action, the other actions are delayed or do not come

Since actions are processed sequentially, make sure you launch a coroutine to not prevent other actions from coming and
suspending the scope. This is particularly obvious with things like snackbars that suspend in compose.

### I want to expose a few public functions in my container for the store. Should I do that?

You shouldn't. Use an Intent / Action to follow the contract, unless you are using `LambdaIntent`s.
In that case, expose the parent `ImmutableContainer` / `ImmutableStore` type to hide the `intent` function from
subscribers.

### How to use androidx.paging?

Well, this is a tricky one. `androidx.paging` breaks the architecture by invading all layers of your app with UI
logic. The best solution we could come up with is just passing a PagingFlow as a property in the state.
This is not good, because the state becomes mutable and non-stable, but there's nothing better we could come up with,
but it does its job, as long as you are careful not to recreate the flow and pass it around between states.

The Paging library also relies on the `cachedIn` operator which is tricky to use in `whileSubscribed`, because that
block is rerun on every subscription, recreating and re-caching the flow.
To fix this issue, use `cachePlugin` to cache the paginated flow, and then pass it to `whileSubscribed` block.
This will prevent any leaks that you would otherwise get if you created a new flow each time a subscriber appears.

```kotlin
val pagingFlow by cache {
    repo.getPagingDataFlow().cachedIn(this)
}
```

### I have a lot of data streams. Do I subscribe to all of the flows in my store?

It's preferable to create a single flow using `combine(vararg flows...)` and produce your state based on that.
This will ensure that your state is consistent and that there are no unnecessary races in your logic.
As flows add up, it will become harder and harder to keep track of things if you use `updateState` and `collect`.

### But that other library has 9000 handlers, reducers and whatnot. Why not do the same?

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

1. Modularize the app. The library allows to do that easily.
2. Use nested classes. For example, define an `object ScreenContract` and nest your state, intents, and actions
   inside to make autocompletion easier.
3. Use `LambdaIntent`s. They don't require subclassing `MVIIntent`.
4. Disallow Actions for your store. Side effects are sometimes considered an anti-pattern, and you may want to disable
   them if you care about the architecture this much.

### I want to use a resource or a framework dependency in my store. How can I do that?

The best solution would be to avoid using platform dependencies such as string resources.
Instead, you could delegate to the UI layer - the one entity that **should** be handling data representation.
That would result in creating a few more intents and other classes, but it will be worth it to achieve better SoC.
If you are not convinced, you could try to use a resource wrapper. The implementation will vary depending on your needs.
