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

### Compose:

* Make sure you use compose `@Immutable` on your Contract interfaces to prevent compose from making your
  composables [non-skippable](https://developer.android.com/jetpack/compose/performance/bestpractices). They must be
  immutable anyway, so feel free to always use the annotation.
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
