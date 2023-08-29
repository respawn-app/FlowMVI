# Creating custom plugins

Plugin is a unit that can extend the business logic of the Store.
All stores are mostly based on plugins, and their behavior is entirely determined by them.

Plugins can influence subscription, stopping, and all other forms of store behavior.
Access the store's context and other functions through the `PipelineContext` receiver.
It is not recommended to implement the `StorePlugin` interface,
if you really need to subclass something, extend `AbstractStorePlugin` instead.
If you do override that interface, you **must** comply with the hashcode/equals contract of the plugin system,
described below.

## Step 1: Choose if your plugin will be generic or store-specific.

* If you want to create a typed plugin that is specific to a store, use `plugin` builder.
* If you want to create a plugin that works with any store, use `genericPlugin` builder. Generic plugins have limited
  functionality.

## Step 2 : Create the plugin.

Plugins are simply built:

```kotlin

val plugin = plugin<ScreenState, ScreenIntent, ScreenAction> {
    // dsl for intercepting is available
}

```

Here are all the dsl functions of a plugin:

### Name

```kotlin
val name: String? = null
```

The name can be used for logging purposes, but most importantly, to distinguish between different plugins.
Name is optional, when it is missing, the plugins will be compared **by reference**.
If you need to have the same plugin installed multiple times, consider giving plugins different names.
Plugins that have no name can be installed multiple times, assuming they are different instances of a plugin.

?> If you attempt to install the same plugin multiple times, or different plugins
with the same name, **an exception will be thrown**.

Consider the following examples:

``` kotlin
loggingPlugin("foo")
analyticsPlugin("foo") // -> will throw

loggingPlugin(null)
analyticsPlugin(null) // -> OK

loggingPlugin("plugin1")
loggingPlugin("plugin1") // -> will throw

loggingPlugin("plugin1")
loggingPlugin("plugin2") // -> OK, but same logs will be printed twice

loggingPlugin(null)
loggingPlugin(null) // -> OK, but same logs will be printed twice

val plugin = loggingPlugin(null)
install(plugin)
install(plugin) // -> will throw
```

So name your plugin based on whether you want it to be repeatable, i.e. installed multiple times.
For example, the library's `reduce` plugin **cannot** be installed multiple times.

### onState

```kotlin
 suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S? = new
```

A callback to be invoked each time `updateState` is called.
This callback is invoked **before** the state changes, but **after** the function's `block` is invoked.
Any plugin can veto (forbid) or modify the state change.

This callback is **not** invoked at all when state is changed through `useState`
or when `state` is obtained directly.

* Return null to cancel the state change. All plugins registered later when building the store will not receive
  this event.
* Return `new` to continue the chain of modification, or allow the state to change,
  if no other plugins change it.
* Return `old` to veto the state change, but allow next plugins in the queue to process the state.
* Execute other operations using `PipelineContext`, including jobs.

### onIntent

```kotlin
 suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I? = intent
```

A callback that is invoked each time an intent is received **and then begun** to be processed.

This callback is invoked **after** the intent is sent and **before** it is received,
sometimes the time difference can be significant if the store was stopped for some time
or even **never** if the store's buffer overflows or store is not ever used again.

* Return null to veto the processing and prevent other plugins from using the intent.
* Return another intent to replace `intent` with another one and continue with the chain.
* Return `intent` to continue processing, leaving it unmodified.
* Execute other operations using `PipelineContext`.

### onAction

```kotlin
 suspend fun PipelineContext<S, I, A>.onAction(action: A): A? = action
```

A callback that is invoked each time an `MVIAction` has been sent.

This is invoked **after** the action has been sent, but **before** the store handles it.
This function will always be invoked, even after the action is later dropped because of `ActionShareBehavior`,
and it will be invoked before the `action(action: A)` returns, if it has been suspended, so this handler may suspend the
parent coroutine that wanted to send the action.

* Return null to veto the processing and prevent other plugins from using the action.
* Return another action to replace `action` with another one and continue with the chain.
* Return `action` to continue processing, leaving it unmodified.
* Execute other operations using `PipelineContext`

### onException

```kotlin
suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception? = e
```

A callback that is invoked when Store catches an exception. It is invoked when either a coroutine launched inside the  
store throws, or when an exception occurs in any other plugins.

* If none of the plugins handles the exception (returns `null`), **the exception is rethrown and the store fails**.
* If you throw an exception in this block, **the store will fail**. Do not throw exceptions in this function.
* This is invoked **before** the exception is rethrown or otherwise processed.
* This is invoked **asynchronously in a background job** and after the job that has thrown was cancelled, meaning
  that some time may pass after the job is cancelled and the exception is handled.
* Handled exceptions do not result in the store being closed.
* You cannot prevent the job that threw an exception and all its nested jobs from failing. The job has already been
  canceled and can no longer continue. This does not apply to the store's context however.

-----

* Return `null` to signal that the exception has been handled and recovered from, continuing the flow's processing.
* Return `e` if the exception was **not** handled and should be passed to other plugins.
* Execute other operations using `PipelineContext`

### onStart

```kotlin
suspend fun PipelineContext<S, I, A>.onStart(): Unit = Unit
```

A callback that is invoked **each time** `Store.start` is called.

* suspending in this callback will **prevent** the store from starting until the plugin is finished.
* Execute any operations using `PipelineContext`.

### onSubscribe

```kotlin
fun PipelineContext<S, I, A>.onSubscribe(subscriberScope: CoroutineScope, subscriberCount: Int): Unit = Unit
```

A callback to be executed **each time** `Store.subscribe` is called.
This callback is executed **before** the subscriber gets access to the store and **before** the `subscriberCount`
is incremented. This means, for the first subscription, `subscriberCount` will be `0`.

This function is invoked in the store's scope, not the subscriber's scope.
To launch jobs in the subscriber's scope, use `subscriberScope`.
They are not affected by store's plugins.
They will be canceled when the subscriber unsubscribes.

### onUnsubscribe

```kotlin
fun PipelineContext<S, I, A>.onUnsubscribe(subscriberCount: Int): Unit = Unit
```

A callback to be executed when the subscriber cancels its subscription job (unsubscribes).

This callback is executed **after** the subscriber has been removed and **after** `subscriberCount` is
decremented. This means, for the last subscriber, the count will be `0`.
You cannot suspend in this function as the subscriber has already been lost, but you can launch jobs using
`PipelineContext`

### onStop

```kotlin
fun onStop(e: Exception?): Unit = Unit
```

Invoked when the store is closed.

* This is called **after** the store is already closed, and you cannot
  influence the outcome.
* This is invoked for both exceptional stops and normal stops.
* Will not be invoked when an `Error` is thrown.
* `e` is the exception the store is closed with. Will be null for normal completions.
