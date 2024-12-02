# Decorators = Plugins for Plugins

!> Decorators are currently experimental because their DSL is limited by Kotlin features and they
are less safe/performant to use. There will be a breaking change with them within a few updates.
They are also not tested enough as of 3.1 Beta releases.

Decorators are very similar to plugins, so the way they work may seem confusing at first.

In short,

> A Decorator **wraps** another plugin and _manages_ it **manually**.

If plugins are executed automatically as a Chain of Responsibility, Decorators instead decide whether to call
plugin methods **themselves**. If you return `null` from a plugin callback, you can halt the chain, but you cannot
"wrap" it, watch over the entire chain of plugins, or skip the execution altogether. That's where you would use a
decorator.

Plugins can be used in two ways:

## Decorating Plugins

A straightforward way is to create a decorator and call `decorate` on a **single** plugin:

```kotlin
val plugin = plugin<State, Intent, Action> {
    onIntent {
        // does stuff
        it
    }
}

val decorator = decorator<State, Intent, Action> {
    name = "FilterInvalidDecorator"
    onIntent { chain, intent ->
        if (intent is InvalidIntent) return@onIntent null
        chain.run { onIntent(intent) } // returns the result of the chain
    }
}

val decoratedPlugin = decorator decorates plugin
```

As you can see, there is an additional parameter `chain` in the decorator callback. It is the plugin that we
wrap. Decorators can be applied to any plugin, so you can't know which exact plugin you are wrapping except by querying
its `name`.

?> The `chain` parameter is temporary and will become a receiver with a future Kotlin release. Right now,
calling the chain is awkward because you have to always wrap it in a `run` block to bring the `PipelineContext` into
the scope.

!> If you don't call the corresponding plugin method, it will be skipped entirely! This can result in very dangerous
behaviors if, for example, plugins initialize some resources in `onStart` and expect to have them elsewhere.
Always auto-complete and use the `chain` parameter in some way.

* The value you return from the decorator callback will behave in the opposite way to plugins. You should consider it
  the "final" value, not the "next" or intermediate value that will go further down the chain.
  A safe bet is to return from the decorator whatever the `chain` invocation returns. This means that you did
  not want to make modifications to the result.
* If you don't define a particular decorator callback, it will be "transparent" (not skipped).
* You can decorate decorators because after you decorate a plugin, you get a plugin as a result.

## Decorating Stores

This section makes my brain explode each time, but I will try to explain it as best as I can.

The interesting thing about FlowMVI is that Store is just a decorator.
What it does is receive a plugin chain, wrapped in a Composite pattern after it is built, so that Store thinks it
always only has a single plugin installed. It then, like a decorator, decides when and whether to call its plugin
methods, and does things (sets states etc.) based on the value returned by the chain.

Previously I said that when you decorate a plugin, you get a plugin as a result. What this means is that

1. Store is a Plugin of _itself_
2. Store is a Decorator of _itself_
3. Store decorates Decorators
4. Decorators decorate Plugins

I know that makes 0 sense whatsoever. But practically, when you decorate a Store, you can think of it like this:

1. First all of the installed plugins are merged into a single plugin.
2. The first decorator you install decorates that chain.
3. Then each one of your decorators decorates the previous ones in the order of installation.
4. Then the Store decorates the last decorator in the chain and becomes a Plugin as a result.

So,

* Decorators are installed **after** all plugins, no matter where they are declared.
* Decorators are installed in order among themselves. The one declared lower wraps the upper one
* Whatever the last returned decorator returns from a callback becomes the final value passed to the Store.

## Pre-made Decorators

The power of decorators enables some awesome features. You can take them as an example by examining their source code.

### BatchIntentsDecorator

```kotlin
fun batchIntentsDecorator(
    mode: BatchingMode,
    queue: BatchQueue<I> = BatchQueue(),
    name: String? = "BatchIntentsDecorator"
): PluginDecorator<S, I, A>
```

This one intercepts the intents coming through it and puts them in a queue. Based on the `BatchingMode` it will either
accumulate a given `Amount` of intents in the queue before flushing them as soon as the queue overflows, or
intercept all intents and flush them every `Time` interval.

It is useful when you want to save some resources and want to do computations in bursts.

By default, it can only be installed once per Store.
Install with `batchIntents(mode)`.

### ConflateDecorator

```kotlin
fun <S : MVIState, I : MVIIntent, A : MVIAction> conflateIntentsDecorator(
    name: String? = "ConflateIntents",
    crossinline compare: ((it: I, other: I) -> Boolean) = MVIIntent::equals,
): PluginDecorator<S, I, A>
```

* Out of the box, Intents and Actions, unlike States, are not conflated. That means that if you send the same Intent
  twice, it will trigger two rounds of processing. If you don't want that, you can install the decorator using
  `conflateIntents()` or `conflateActions()`.
* Using provided `compare` function, it will drop the **second** intent if the previous was the same as this (second)
  one,
  if the function returns `true`.

It can be useful if you have a Store (Plugin) where the same intents/actions can be spammed a lot and you don't
want them processed repeatedly.

### IntentTimeoutDecorator

```kotlin
fun <S : MVIState, I : MVIIntent, A : MVIAction> intentTimeoutDecorator(
    timeout: Duration,
    name: String? = "IntentTimeout",
    crossinline onTimeout: suspend PipelineContext<S, I, A>.(I) -> I? = { throw StoreTimeoutException(timeout) },
): PluginDecorator<S, I, A>
```

* This decorator will measure the time it takes to execute `onIntent` and (by default) throw an exception if processing
  takes longer than the `timeout` value.
* When the `onTimeout` block is invoked, the execution has already been canceled, so you cannot continue it.
  It works with both `parallelIntents` and regular ones, but does not measure the time it takes to run a job `launch`ed
  inside any of the chain links.

It can be useful when you want to prevent the Store from being stuck processing an Intent for long because of some heavy
operation or a bug. In the block, you can resend the intent to retry, or report an error, for example.

### RetryDecorator

Speaking of retry:

```kotlin
fun <S : MVIState, I : MVIIntent, A : MVIAction> retryIntentsDecorator(
    strategy: RetryStrategy,
    name: String? = null,
    selector: (intent: I, e: Exception) -> Boolean = { _, _ -> true },
): StoreDecorator<S, I, A>
```

* This one will use the `selector` first to decide whether it should retry execution of the `onIntent` callback, and if
  the block returns `true`, it will retry processing it using the provided `strategy`.
* If the `strategy` includes a delay of any kind, the decorator will move the processing to a separate coroutine to not
  prevent other intents from being processed.
* You can use the following strategies:
    * `RetryStrategy.ExponentialDelay` - each delay will be multiplied by the exponent. By default 2, 4, 8...
    * `RetryStrategy.FixedDelay` - each delay will be the same length up to a max `retries`.
    * `RetryStrategy.Once` - just retry once immediately without running asynchronously.
    * `RetryStrategy.Immediate` - retry immediately (while blocking other intents, if `parallelIntents` is not used),
      for up to `retries` times.
    * `RetryStrategy.Infinite` - retry indefinitely and immediately until store is closed or succeeded. Very dangerous.

---- 

?> The word "Decorator" has been said 60 times in this document. Uhm, I meant, 61.
