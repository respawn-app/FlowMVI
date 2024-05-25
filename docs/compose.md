# Compose and Lifecycle Integration

## Step 1: Add Dependencies

![Maven Central](https://img.shields.io/maven-central/v/pro.respawn.flowmvi/core?label=Maven%20Central)

```toml
[versions]
flowmvi = "< Badge above 👆🏻 >"

[dependencies]
flowmvi-compose = { module = "pro.respawn.flowmvi:compose", version.ref = "flowmvi" }
```

## Step 2: Configure the Compiler

Set up stability definitions for your project:

<details>
<summary>project_root/stability_definitions.txt</summary>

```text
pro.respawn.flowmvi.api.MVIIntent
pro.respawn.flowmvi.api.MVIState
pro.respawn.flowmvi.api.MVIAction
pro.respawn.flowmvi.api.Store
pro.respawn.flowmvi.api.Container
pro.respawn.flowmvi.api.ImmutableStore
pro.respawn.flowmvi.dsl.LambdaIntent
pro.respawn.flowmvi.api.SubscriberLifecycle
pro.respawn.flowmvi.api.IntentReceiver
```

</details>

Then configure compose compiler to account for the definitions in your feature's `build.gradle.kts`:

<details>
<summary>feature-module/build.gradle.kts</summary>

```kotlin
composeCompiler {
    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_definitions.txt")
}
```

</details>

Now the states/intents you create will be stable in compose. Immutability of these classes is already required by the
library, so this will ensure you get the best performance. See the project's gradle configuration if you want to learn
how to set compose compiler configuration globally and/or in gradle conventions.

## Step 3: Subscribe to Stores

!> Compose does not play well with MVVM+ style because of the instability of the `LambdaIntent` and `ViewModel` classes.
It is discouraged to use Lambda intents with Compose as that will not only leak the context of the store but
also degrade performance.

Subscribing to a store is as simple as calling `subscribe()`

```kotlin
@Composable
fun CounterScreen(
    container: CounterContainer,
) = with(container.store) {

        val state by subscribe { action ->
            when (action) {
                is ShowMessage -> {
                    /* ... */
                }
            }
        }

        CounterScreenContent(state)
    }
```

Under the hood, the `subscribe` function will efficiently subscribe to the store and
use the composition scope to process your events. Event processing will stop when the UI is no longer visible (by
default). When the UI is visible again, the function will re-subscribe. Your composable will recompose when the state
changes. By default, the function will use your system's default lifecycle, provided by the
compose `LocalLifecycleOwner`. If you are using a custom lifecycle implementation e.g. provided by the navigation
library, you can use that lifecycle by providing it as a `LocalSubscriberLifecycle` composition local or passing it as
a parameter to the `subscribe` function.

Use the lambda parameter of `subscribe` to subscribe to `MVIActions`. Those will be processed as they arrive and
the `consume` lambda will **suspend** until an action is processed. Use a receiver coroutine scope to
launch new coroutines that will parallelize your flow (e.g. for snackbars).

## Step 4: Create Pure UI Composables

A best practice is to make your state handling (UI redraw composable) a pure function and extract it to a separate
Composable such as `ScreenContent(state: ScreenState)` to keep your `*Screen` function clean, as shown below.
It will also enable smart-casting by the compiler. If you want to send `MVIIntent`s from a nested composable, just
use `IntentReceiver` as a context or pass a function reference:

```kotlin
@Composable
private fun IntentReceiver<CounterIntent>.CounterScreenContent(state: CounterState) {
    when (state) {
        is DisplayingCounter -> {
            Button(onClick = { intent(ClickedCounter) }) { // intent() available from the receiver parameter
                Text("Counter: ${state.counter}")
            }
        }
        /* ... */
    }
}
```

## Step 5: Create Previews

When you have defined your `*Content` function, you will get a composable that can be easily used in previews.
That composable will not need DI, Local Providers from compose, or anything else for that matter, to draw itself.
But there's a catch: It has an `IntentReceiver<I>` as a parameter. To deal with this, there is an `EmptyReceiver`
composable. EmptyReceiver does nothing when an intent is sent, which is exactly what we want for previews. We can now
define our `PreviewParameterProvider` and the Preview composable. You won't need an `EmptyReceiver` if you pass the
`intent` callback manually.

```kotlin
private class StateProvider : CollectionPreviewParameterProvider<CounterState>(
    listOf(DisplayingCounter(counter = 1), Loading)
)

@Composable
@Preview
private fun CounterScreenPreview(
    @PreviewParameter(StateProvider::class) state: CounterState,
) = EmptyReceiver {
    CounterScreenContent(state)
}
```
