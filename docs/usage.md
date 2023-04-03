### Tips:

* Avoid using `sealed class`es and use `sealed interface`s whenever possible. Not only this reduces object allocations,
  but also prevents developers from putting excessive logic into their states and/or making private/protected
  properties. State is a simple typed data holder, so if you want to use protected properties or override functions,
  it is likely that something is wrong with your architecture.
* Use `data object`s instead of regular objects to make debugging easier and improve readability.
* Use nested class imports and import aliases to clean up your code, as Contract class names can be long sometimes.
* Use value classes to reduce object allocations if your Intents are being sent frequently, such as for text field
  value changes or scroll events.
    * Overall, there are cases when changes are so frequent that you'll want to just leave some logic on the UI layer to
      avoid polluting the heap with garbage collected objects and keep the UI performant. Use only when necessary, as
      any logic in the UI layer is not great for the architecture.
* Avoid subscribing to a bunch of flows in your Store / ViewModel. The best way to implement a reactive UI pattern is to
  use `combine(vararg flows...)` and merge all of your data streams into one flow, and then just use the `transform`
  block to handle the changes. Example:

Example of advanced reactive UI from [Respawn](https://respawn.pro)

```kotlin
internal class AccountViewModel(
    private val repo: UserRepository,
    private val billingRepo: BillingRepository,
) : MVIViewModel<AccountState, AccountIntent, AccountAction>(Loading) {

    init {
        combine(
            repo.userState,
            billingRepo.subscriptionState,
            transform = ::produceState,
        )
            .onStart { launchGetUserInfo() } // launch initial data load
            .recover() // MVIViewModel helper functions to recover from errors and subscribe to flows
            .flowOn(Dispatchers.Default) // offload to background thread
            .consume()
    }

    override suspend fun reduce(intent: AccountIntent) {
        /* ... */
    }

    private suspend fun produceState(user: User?, subscription: SubscriptionStatus) = updateState {
        val currentState = this as? DisplayingLoginPrompt  // merge with an existing state like this

        if (user == null) return@updateState DisplayingLoginPrompt(currentState?.canSignIn ?: true)

        DisplayingAccount(
            name = user.name,
            email = user.email,
            avatarUrl = user.avatarUri,
            isOffline = !user.isRemote,
            isSubscribed = subscription.isSubscribedOrNull
        ) // ^updateState
    }
}
```

* With this, you can be sure that your state is consistent even if you have 20 parallel data streams from different
  sources e.g. database cache, network, websockets and other objects.
* Avoid returning the state from the `transform` block. Instead, return `Unit` and use `updateState` to prevent races
  when your state is changing. `updateState` already returns `Unit`.


* Avoid using platform-level imports and code in your Provider whenever possible. This is optional, but if you follow
  this rule, your **ViewModels can be multiplatform**! This is also very good for the architecture.
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
* `MVIState`s should be named using verbs in present tense using a gerund. Example `EditingGame`, `DisplayingSignIn`

## Common problems

### Q: My intents are not reduced! When I click buttons, nothing happens, the app just hangs.

A: Did you call `MVIStore.start(scope: CoroutineScope)`?

### Q: My Actions are not consumed, dropped, or missed.

A:

1. Examine if you `subscribe`d to the store correctly. Define the lifecycle state as needed.
2. Check your `ActionShareBehavior`. When using `Share`, if you have multiple subscribers and one of them is not
   subscribed yet, or if the View did not manage
   to subscribe on time, you will miss some Actions. This is a limitation of the Flow API and there are no potential
   resolutions at them moment of writing. Try to use `Distribute`.

### Q: I made my provider, but I want to wrap it in a ViewModel. How can I do that?

A: In the sample app, there is an example of how you can set up your DI with Koin. The biggest problem is that your
generic types will be erased, and the only solution is to use qualifiers to resolve your ViewModels/Providers
Contributions with examples for Hilt setup are welcome. Use `StoreViewModel` for a simple wrapper.

### Q: In what order are intents and actions processed?

A: FIFO. Launch coroutines to parallelize the processing as needed.

### Q: When I consume an Action, the other actions are delayed or do not come.

A: Since actions are processed FIFO, make sure you launch a coroutine to not prevent other actions from coming and being
handled.

### Q: I want to expose a few public functions in my Provider. Should I do that?

A: You shouldn't. Use an Intent / Action to follow the contract.

### Q: How to use paging?

A: Well this is a tricky one. androidx.paging breaks the architecture by invading all layers of your app with the UI
logic. The best solution we could come up with is just passing a Paging Flow as a property in the state.
This is not good, because the state becomes mutable and non-stable, but there's nothing better we could come up with,
but it does it's job, as long as you are careful not to recreate the flow and pass it around between states.
If you have an idea or a working Paging setup, let us know and we can add it to the library!

### I have like a half-dozen various flows or coroutines and I want to make my state from those data streams. Do I subscribe to all of those flows in my Provider?

A: It's preferable to create a single flow using `combine(vararg flows...)` and produce your state based on that.
This will ensure that your state is consistent and that there are no unnecessary races in your logic.
As flows add up, it will be harder and harder to keep track of things if you use `updateState` and `collect`.

### How do I handle errors?

A: There are two ways to do this.

1. First one is using one of the Result wrappers, like `ApiResult`
   from [KMMUtils](https://github.com/respawn-app/kmmutils), an monad from Arrow.io or, as the last resort,
   a `kotlin.Result`
2. Second one involves using a provided `recover` function of the store / provider that will be run when an exception is
   caught in `reduce` or child coroutines, but that works for "critical" errors only, because current state is hard to
   obtain in that block. Use `state` property at your own risk.

### But that other library allows me to define 9000 handlers to reuse my Actions and Intents. Why not do the same?

A: In general, a little boilerplate when duplicating intents is worth it to keep the consistency of actions and intents
of screens intact.
You usually don't want to reuse your actions and intents because they are specific to a given screen or flow.
That makes your logic way simpler, and the rest can be easily moved to your repository layer, usecases or just plain
top-level functions. This is where this library is opinionated, and where one of its main advantages - simplicity, comes
from.

### How to avoid class explosion?

A:

1. Modularize your app. The library allows you to do that easily
2. Use nested classes. For example, you can define a `object ScreenContract` and nest your state, intents, and actions
   inside, to make the life of autocompletion easier.

### What if I have substates or multiple Loading states for different parts of the screen?

A:
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

Use `T.withType<Type>(block: () -> Unit)` to cast your substates easier as the (this as? State)?.let{ } looks ugly.

### I have an API call or other coroutine I want to run, but I don't have a coroutine scope to launch it in at the time of store creation. How do I do that?

A: The best solution would be to follow reactive app modeling practices, go reactive and make a flow out of your suspend
function, and then use `combine`.

Or you could try to implement `lazySuspend` property and delegate to that.
Example implementation (no guarantees on how it will work):

```kotlin
class SuspendLazy<T>(
    private val block: suspend CoroutineScope.() -> T,
) {

    private val value = atomic<Deferred<T>?>(null) // atomicfu

    suspend operator fun invoke(): T = (
            value.value
                ?: coroutineScope {
                    value.updateAndGet { actual ->
                        actual ?: async { block() }
                    }!!
                }
            ).await()
}

/**
 * Use this like a lazy, but because operator getValue cannot
 * be suspend, you'll have to invoke this object instead in a
 * suspend context to receive the value.
 */
fun <T> suspendLazy(initializer: suspend CoroutineScope.() -> T) = SuspendLazy(initializer)
```

### I want to use a resource or a framework dependency in my provider. How can I do that?

A: The best solution would be to avoid using platform dependencies such as string resources.
Instead, you could delegate to the UI layer - the one entity that **should** be handling data representation.
That would result in creating a few more intents and other classes, but it will be worth it to achieve better SoC.
If you are not convinced, you could try to use a resource wrapper. The implementation will vary according to your needs.
