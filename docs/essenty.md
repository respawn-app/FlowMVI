# Essenty Integration

The library integrates with Essenty (Decompose) to support lifecycle and retaining store instances across configuration
changes. The integration supports all the artifacts that Essenty supports.

```toml
# Includes retained stores and coroutine scopes
flowmvi-essenty = { module = "pro.respawn.flowmvi:essenty", version.ref = "flowmvi" }
# Includes lifecycle support for store subscription
flowmvi-essenty-compose = { module = "pro.respawn.flowmvi:essenty-compose", version.ref = "flowmvi" } 
```

## Retaining stores

Creating a store that
is [retained](https://arkivanov.github.io/Decompose/component/instance-retaining/#instance-retaining) across
configuration changes is as simple as:

```kotlin
class CounterComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext, Container<State, Intent, Action> {

    val store = retainedStore(initial = Loading) {
        // build your store as usual
    }
}
```

The store that has been created will be started in a retained
coroutine scope upon creation. The `Container` interface serves as a marker and is optional.
You can override the scope by passing your own scope to the function:

```kotlin
val store = retainedStore(
    initial = Loading,
    scope = retainedScope(),
    key = "Store name by default",
) {
    // build or inject here
}
```

Pass `null` to the scope to not start the store upon creation. In this case, you'll have to start the store yourself.

!> Caveat: If you build a store that is retained, it will capture everything you pass into the `builder` closure. This
means that any parameters or outside properties you use in the builder will be captured **and retained** as well.
This is the same caveat that you have to be aware of when
using [Retained Components](https://arkivanov.github.io/Decompose/component/instance-retaining/#retained-components-since-v210-alpha-03).
If you don't want to retain your stores to prevent this from happening, just build the store
normally using a `store` builder. However, the store will be recreated and relaunched on configuration changes.

## Retained scopes

By default, a store is launched using a `retainedScope`. As the name says, it's retained across configuration changes
and will be stopped when the `InstanceKeeper`'s storage is cleared. If you want to relaunch the store on lifecycle
events instead, pass a regular `coroutineScope` from essenty coroutine extensions. All stores will share the same scope
by default.

```kotlin
class CounterComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {

    val scope = coroutineScope()

    val store = store<State, Intent, Action>(Loading, scope) { /* ... */ }
}
```

## Compose Lifecycle

The base `compose` artifact already provides you with everything that is necessary to implement lifecycle support.
The `essenty-compose` artifact simplifies the provision of lifecycle to the UI that subscribes to the stores.
When you create a component, it is assigned a new `Lifecycle` instance by Decompose.
This lifecycle will be used on the UI to correctly subscribe to the store.

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

?> The requirements for setting up lifecycle correctly are the same as in
the [Decompose docs](https://arkivanov.github.io/Decompose/component/lifecycle/).

If you want another approach, you can provide the lifecycle via a `CompositionLocal`:

```kotlin
@Composable
fun CounterScreen(component: CounterComponent) {
    
    // do this somewhere in your navigation logic
    ProvideSubscriberLifecycle(component) {
        val state by component.store.subscribe(requireLifecycle())
    }
}
```

The `requireLifecycle()` function will try to first find the lifecycle you provided. If not found, it will try to
resolve the system lifecycle, and if still not found, it will throw. If you don't want to throw and are willing to
sacrifice the lifecycle subscription if there is no lifecycle present, you can use `DefaultLifecycle` property
instead.
