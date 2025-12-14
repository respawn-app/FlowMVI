---
sidebar_position: 4
sidebar_label: Testing
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Testing with the FlowMVI Test DSL

FlowMVI ships a small, coroutine-friendly test harness in the `pro.respawn.flowmvi:test` module.
It’s intended for unit-testing Stores and StorePlugins with safer concurrency and easier lifecycle management.

## Setup

Add the test module:

<Tabs>
  <TabItem value="toml" label="Version Catalogs" default>

```toml
[versions]
flowmvi = "<version>"

[libraries]
flowmvi-test = { module = "pro.respawn.flowmvi:test", version.ref = "flowmvi" }
```

  </TabItem>
  <TabItem value="kts" label="Gradle DSL">

```kotlin
dependencies {
    val flowmvi = "<version>"
    commonTestImplementation("pro.respawn.flowmvi:test:$flowmvi")
}
```

  </TabItem>
</Tabs>

For flow assertions, FlowMVI’s own tests use Turbine (`app.cash.turbine:turbine`), but any Flow test utility works.

## Keep test configuration separate (recommended)

It’s a good practice to keep a dedicated Store configuration for tests and provide it via DI, instead of inlining test-only
flags in every Store builder.
This keeps tests deterministic and makes it easy to switch concurrency knobs (e.g. `stateStrategy`, overflow behavior, logging).

The sample app demonstrates this pattern with a DI-provided `ConfigurationFactory` and a test variant:

- `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/arch/configuration/ConfigurationFactory.kt`
- `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/arch/configuration/TestConfigurationFactory.kt`

Typical usage inside a Store/Container builder:

```kotlin
class MyContainer(
    private val configuration: ConfigurationFactory,
) {
    val store = store(InitialState) {
        configuration(name = "MyStore")
        // plugins/reduce/etc
    }
}
```

To learn how to provide/override that dependency in your DI setup, see [Dependency Injection](/integrations/di).

## Testing Stores

There are two entry points:

- `Store.test { ... }` starts the store, runs your block, then closes the store (no subscription).
- `Store.subscribeAndTest { ... }` starts the store, subscribes, runs your block **inside the subscription scope**, then
  unsubscribes and closes the store.

### `test { }`: lifecycle-focused

Use `test { }` when you want to assert startup/shutdown behavior, subscription counts, or “store is still alive”
invariants.
The receiver is a `TestStore` (a `Store` plus `SubscriptionAware`).

```kotlin
store.test {
    isActive // StoreLifecycle
    subscriberCount.value // SubscriptionAware

    // send intents
    emit(MyIntent)
}
```

### `subscribeAndTest { }`: state/actions-focused

Use `subscribeAndTest { }` when you want to assert emitted state transitions and actions.
The receiver is `StoreTestScope`, which delegates both the `Store` and the subscription `Provider`, so you get:

- `states` (a `StateFlow<S>`) and `actions` (a `Flow<A>`) from the subscription
- `emit(...)` / `intent(...)` to send intents

Typical pattern with Turbine:

```kotlin
store.subscribeAndTest {
    states.test {
        awaitItem() shouldBe InitialState
        intent(MyIntent)
        awaitItem() shouldBe ExpectedState
    }

    actions.test {
        intent(MyIntentThatSendsAction)
        awaitItem() shouldBe ExpectedAction
    }
}
```

:::caution[Transient subscription validation]

`subscribeAndTest { ... }` intentionally **returns** from the subscription block (it unsubscribes when your test block
ends).
If your store runs with `debuggable = true`, make sure your store configuration allows transient subscriptions
(`allowTransientSubscriptions = true`), otherwise the store may treat the finished subscription as a bug.

:::

## Testing Plugins

Use `pro.respawn.flowmvi.test.plugin.test` on a `LazyPlugin`:

```kotlin
myPlugin.test(
    initial = InitialState,
    configuration = {
        debuggable = true
        // coroutineContext = ...
        // verifyPlugins = ...
        // name = ...
    },
) {
    // PluginTestScope
}
```

### What the plugin harness provides

Inside `test { ... }`, you get a `PluginTestScope` that is:

- a `PipelineContext` (so you can call `emit`, `intent`, `updateState`, `withState`, `send`/`action`, etc.)
- a `StorePlugin` (so you can directly call plugin callbacks like `onStart`, `onIntent`, `onState`, `onException`, …)
- a `ShutdownContext`/`StoreLifecycle` (so the scope can be cancelled via `closeAndWait()`; this happens automatically
  after the block)

The harness also installs a `TimeTravel` plugin (exposed as `timeTravel`) to observe store events the plugin causes.

### Lifecycle-driven plugins

The harness does **not** implicitly call lifecycle callbacks for you. If your plugin reacts to lifecycle, call them
explicitly:

```kotlin
plugin.test(initial = InitialState) {
    onStart()
    onSubscribe(1)
    // ...
    onUnsubscribe(0)
    onStop(null)
}
```

### Asserting via `timeTravel`

`timeTravel` is usually the simplest way to assert what happens inside your plugin from the outside of it:

```kotlin
plugin.test(initial = InitialState) {
    onStart()
    onIntent(MyIntent)
    timeTravel.actions.last() shouldBe ExpectedAction // plugin emitted a side-effect
}
```

### Configuration options

The `configuration` lambda is a `StoreConfigurationBuilder` and is applied to the mock pipeline context used in the
test.
Common toggles for tests:

- `debuggable`: enable extra checks and debug logging
- `coroutineContext`: provide dispatcher overrides if the plugin launches coroutines
- `verifyPlugins`: force plugin verification on/off
- `allowIdleSubscriptions` / `allowTransientSubscriptions`: relevant if your plugin depends on subscription rules

## Real examples

FlowMVI’s own test suite is a good set of reference patterns:

- Store tests using `subscribeAndTest`: `core/src/jvmTest/kotlin/pro/respawn/flowmvi/test/store/StoreEventsTest.kt`
- Store lifecycle tests using `test`: `core/src/jvmTest/kotlin/pro/respawn/flowmvi/test/store/StoreLaunchTest.kt`
- Plugin tests using `LazyPlugin.test`: `core/src/jvmTest/kotlin/pro/respawn/flowmvi/test/plugin/ReducePluginTest.kt`
- Subscription-driven plugin tests:
  `core/src/jvmTest/kotlin/pro/respawn/flowmvi/test/plugin/WhileSubscribedPluginTest.kt`
