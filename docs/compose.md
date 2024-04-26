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

Then configure compose compiler to account for the definitions in your root `build.gradle.kts`:

<details>
<summary>/build.gradle.kts</summary>

```kotlin
allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=" +
                        "${rootProject.rootDir.absolutePath}/stability_definitions.txt"
            )
        }
    }
}
```

</details>

Now the states/intents you create will be stable in compose. Immutability of these classes is already required by the
library, so this will ensure you get the best performance.

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

        val state by subscribe(DefaultLifecycle) { action ->
            when (action) {
                is ShowMessage -> {
                    /* ... */
                }
            }
        }

        CounterScreenContent(state)
    }
```

Under the hood, the `subscribe` function will efficiently subscribe to the store (it is lifecycle-aware) and
use the composition scope to process your events. Event processing will stop when the UI is no longer visible (by
default). When the UI is visible again, the function will re-subscribe. Your composable will recompose when the state
changes.

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
define our `PreviewParameterProvider` and the Preview composable.

```kotlin
// vararg preview provider for convenience
open class PreviewProvider<T>(
    vararg values: T,
) : CollectionPreviewParameterProvider<T>(values.toList())

private class StateProvider : PreviewProvider<CounterState>(
    DisplayingCounter(counter = 1),
    Loading,
)

@Composable
@Preview
private fun CounterScreenPreview(
    @PreviewParameter(StateProvider::class) state: CounterState,
) = EmptyReceiver {
    ComposeScreenContent(state)
}
```
