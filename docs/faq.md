### Tips:

* Avoid using `sealed class`es and use `sealed interface`s whenever possible. Not only this reduces object allocations,
  but also prevents developers from putting excessive logic into their states and/or making private/protected
  properties. State is a simple typed data holder, so if you want to use protected properties or override functions,
  it is likely that something is wrong with your architecture.
* Use `data object`s instead of regular objects to make debugging easier and improve readability.
* Use nested class imports and import aliases to clean up your code, as Contract class names can be long sometimes.
* Use value classes to reduce object allocations if your Intents are being sent frequently, such as for text field
  value changes or scroll events.
    * You can use the `useState` function to optimize the performance of the store by bypassing all checks and plugins.
      Use for performance-critical operations only.
    * Overall, there are cases when changes are so frequent that you'll want to just leave some logic on the UI layer to
      avoid polluting the heap with garbage collected objects and keep the UI performant.
* Avoid subscribing to a bunch of flows in your Store. The best way to implement a reactive UI pattern is to
  use `combine(vararg flows...)` and merge all of your data streams into one flow, and then just use the `transform`
  block to handle the changes.
    * With this, you can be sure that your state is consistent even if you have 20 parallel data streams from different
      sources e.g. database cache, network, websockets and other objects.
* Avoid using platform-level imports and code in your Store/Container/ViewModel whenever possible. This is optional, but
  if you follow this rule, your **ViewModels can be multiplatform**! This is also very good for the architecture.
* There is an ongoing discussion about whether to name your intents starting with the verb or with the noun.
    * Case 1: `ClickedCounter`
    * Case 2: `CounterClicked`

      In general, this is up to your personal preference, just make sure you use a single style across all of your
      Contracts. I personally like to name intents starting with the verb (Case 1) for easier autosuggestions from the
      IDE.

### Opinionated naming design

Here's an example of rules we use at [Respawn](https://respawn.pro) to name our Contract classes:

* `MVIIntent` naming should be `<TypeOfActionInPastTense><Meaning>`.
  Example: `ClickedCounter`, `SwipedDismiss` (~~CounterClicked~~).
* `MVIAction`s should be named using verbs in present tense. Example: `ShowConfirmationPopup`, `GoBack`.
    * Navigation actions should be using the `GoTo` verb (~~NavigateTo, Open...~~) Example: `GoToHome`.
      Do not include `Screen` postfix. `GoToHome`~~Screen~~.
* `MVIState`s should be named using verbs in present tense using a gerund. Examples: `EditingGame`, `DisplayingSignIn`.

## FAQ

### My intents are not reduced! When I click buttons, nothing happens, the app just hangs.

Did you call `Store.start(scope: CoroutineScope)`?
Did you call `Store.subscribe()`?

### My Actions are not consumed, are dropped or missed.

1. Examine if you have `subscribe`d to the store correctly. Define the lifecycle state as needed.
2. Check your `ActionShareBehavior`. When using `Share`, if you have multiple subscribers and one of them is not
   subscribed yet, or if the View did not manage to subscribe on time, you will miss some Actions.
   This is a limitation of the Flow API and there are no potential
   resolutions at the moment of writing. Try to use `Distribute` instead.

### I made my store, but I want to wrap it in a ViewModel. How can I do that?

In the sample app, there is an example of how you can set up your DI with Koin.
Use `StoreViewModel` for a simple wrapper for the built Store, which you can inject using DI.
The biggest problem with this is that your generic types will be erased, and the only solution is to use qualifiers
to resolve your ViewModels/Stores.  
Contributions with examples for Hilt setup are welcome.

### In what order are intents and actions processed?

* Intents: FIFO or Parallel based on configuration parameter `parallelIntents`.
* Actions: FIFO.
* States: FIFO, but can be Parallel if using `useState`.

### When I consume an Action, the other actions are delayed or do not come.

Since actions are processed sequentially, make sure you launch a coroutine to not prevent other actions from coming and
suspending the scope.

### I want to expose a few public functions in my container for the store. Should I do that?

You shouldn't. Use an Intent / Action to follow the contract, unless you are using `LambdaIntent`s.

### How to use paging?

Well, this is a tricky one. `androidx.paging` breaks the architecture by invading all layers of your app with the UI
logic. The best solution we could come up with is just passing a PagingFlow as a property in the state.
This is not good, because the state becomes mutable and non-stable, but there's nothing better we could come up with,
but it does its job, as long as you are careful not to recreate the flow and pass it around between states.
If you have an idea or a working Paging setup, let us know and we can add it to the library!

### I have like a half-dozen various flows or coroutines and I want to make my state from those data streams. Do I subscribe to all of those flows in my store?

It's preferable to create a single flow using `combine(vararg flows...)` and produce your state based on that.
This will ensure that your state is consistent and that there are no unnecessary races in your logic.
As flows add up, it will be harder and harder to keep track of things if you use `updateState` and `collect`.

### How do I handle errors?

There are two ways to do this.

1. First one is using one of the Result wrappers, like `ApiResult`
   from [KMMUtils](https://github.com/respawn-app/kmmutils), a monad from Arrow.io or, as the last resort,
   a `kotlin.Result`.
2. Second one involves using a provided `recover` plugin that will be run when an exception is
   caught in plugins or child coroutines, but that works for "critical" errors only, because job's context is not
   present in that block.

### But that other library allows me to define 9000 handlers, actors, processors and whatnot - and I can reuse Intents. Why not do the same?

In general, a little boilerplate when duplicating intents is worth it to keep the consistency of actions and intents
of screens intact.
You usually don't want to reuse your actions and intents because they are specific to a given screen or flow.
That makes your logic way simpler, and the rest can be easily moved to your repository layer, usecases or just plain
top-level functions. This is where this library is opinionated, and where one of its main advantages - simplicity, comes
from.

### How to avoid class explosion?

1. Modularize your app. The library allows you to do that easily.
2. Use nested classes. For example, you can define an `object ScreenContract` and nest your state, intents, and actions
   inside, to make autocompletion easier.
3. Use `LambdaIntent`s. They don't require subclassing MVIIntent.
4. Disallow Actions for your store. Side effects are sometimes considered an anti-pattern, and you may want to disable
   them if you care about the architecture this much.

### What if I have sub-states or multiple Loading states for different parts of the screen?

Create nested classes and host them in your parent state.  
Example:

```kotlin
sealed interface NewsState {
    data class Loading : NewsState
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
  the `(this as? State)?.let { }` code can look ugly.
* Use `T.typed<Type>()` to perform a safe cast to the given state to clean up the code.

### I want to use a resource or a framework dependency in my store. How can I do that?

The best solution would be to avoid using platform dependencies such as string resources.
Instead, you could delegate to the UI layer - the one entity that **should** be handling data representation.
That would result in creating a few more intents and other classes, but it will be worth it to achieve better SoC.
If you are not convinced, you could try to use a resource wrapper. The implementation will vary depending on your needs.
