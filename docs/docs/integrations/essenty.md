---
sidebar_position: 5
sidebar_label: Essenty
---

# Essenty Integration

The library integrates with Essenty (Decompose) to support lifecycle and retaining store instances across configuration
changes. The integration supports all the artifacts that Essenty supports.

```toml
# Includes retained stores and coroutine scopes
flowmvi-essenty = { module = "pro.respawn.flowmvi:essenty", version.ref = "flowmvi" }
# Includes lifecycle support for store subscription
flowmvi-essenty-compose = { module = "pro.respawn.flowmvi:essenty-compose", version.ref = "flowmvi" }
```

## Retaining Stores

Creating a store that
is [retained](https://arkivanov.github.io/Decompose/component/instance-retaining/#instance-retaining) across
configuration changes is ideally done as follows:

```kotlin
// inject dependencies and write your logic as usual
class FeatureContainer(
    private val repo: CounterRepository,
) {

    val store = store<CounterState, CounterIntent, CounterAction>(Loading) {
        // ...
    }
}

class CounterComponent(
    context: ComponentContext,
    container: () -> FeatureContainer = { inject() },  // inject via DI as a factory or provide manually
) : ComponentContext by context,
    Store<CounterState, CounterIntent, CounterAction> by context.retainedStore(factory = container) {

    init {
        subscribe {
            actions.collect { action: CounterAction ->

            }
        }
    }
}
```

The store that has been created will be started in a retained coroutine scope upon creation.
If you are using the `Container` interface, you can delegate that one as well.
You can override the scope by passing your own scope to the function:

```kotlin
retainedStore(
    initial = Loading,
    scope = retainedScope(),
    key = "Type of the State class by default",
    factory = { /* inject here */ },
)
```

Pass `null` to the scope to not start the store upon creation. In this case, you'll have to start the store yourself.

:::warning Caveat:

If you do not use the factory DSL and instead build a store that is retained, it will capture everything you
pass into the `builder` closure. This means that any parameters or outside properties you use in the builder will be
captured **and retained** as well. This is the same caveat that you have to be aware of when
using [Retained Components](https://arkivanov.github.io/Decompose/component/instance-retaining/#retained-components-since-v210-alpha-03).
If you don't want to retain your stores to prevent this from happening, just build the store
normally using a `store` builder. However, the store will be recreated and relaunched on configuration changes.
If you are using retained components already, you can opt-in to the warning annotation issues by the library using a
compiler flag or just not use retained stores.

:::

## Retaining Coroutine Scopes

By default, a store is launched using a `retainedScope`. As the name says, it's retained across configuration changes
and will be stopped when the `InstanceKeeper`'s storage is cleared. If you want to relaunch the store on lifecycle
events instead, pass a regular `coroutineScope` from essenty coroutine extensions. All stores will share the same scope
by default.

```kotlin
class CounterComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {

    private val scope = coroutineScope()

    val store = store<State, Intent, Action>(Loading, scope) { /* ... */ }
}
```

## Subscribing in Components

You can subscribe in the `init` of your component as shown above. The subscription will follow the component's
lifecycle. It's preferable to collect `MVIAction`s in one place only (either UI or the component) because otherwise you
will have to use `ActionShareBehavior.Share()` and manage multiple subscribers manually.

## Subscribing in the UI

The base `compose` artifact already provides you with everything that is necessary to implement lifecycle support.
The `essenty-compose` artifact simplifies the provision of lifecycle to the UI that subscribes to the stores.
When you create a component, it is assigned a new `Lifecycle` instance by Decompose.
This lifecycle can be used on the UI to correctly subscribe to the store.

```kotlin
@Composable
fun CounterScreen(component: CounterComponent) {

    // when overriding Container
    val state by component.subscribe()

    // when just using a property
    val state by component.store.subscribe(component)
}
```

Optionally, you can override the `Lifecycle.State` parameter to specify when the store should unsubscribe.
By default, the store will unsubscribe when:

* The component goes into the backstack
* The composable goes out of the composition
* The lifecycle reaches the `STOPPED` state, such as when the UI is no longer visible, but is still composed.

:::tip

 The requirements for setting up lifecycle correctly are the same as in
the [Decompose docs](https://arkivanov.github.io/Decompose/component/lifecycle/).

:::

If you want another approach, you can provide the lifecycle via a `CompositionLocal`:

```kotlin
@Composable
fun CounterScreen(component: CounterComponent) {

    // do this somewhere in your navigation logic
    ProvideSubscriberLifecycle(component) {
        val state by component.store.subscribe(DefaultLifecycle)
    }
}
```

The `DefaultLifecycle` property will try to first find the lifecycle you provided. If not found, it will try to
resolve the system lifecycle (which should always be present since compose 1.6.10).
