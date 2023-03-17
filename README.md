# Flow MVI
[![CI](https://github.com/respawn-app/FlowMVI/actions/workflows/ci.yml/badge.svg)](https://github.com/respawn-app/FlowMVI/actions/workflows/ci.yml)
![Docs](https://img.shields.io/website?down_color=red&down_message=Offline&label=Docs&up_color=green&up_message=Online&url=https%3A%2F%2Fopensource.respawn.pro%2FFlowMVI%2F%23%2F)
[![Javadoc](https://javadoc.io/badge2/pro.respawn.flowmvi/core/javadoc.svg)](https://javadoc.io/doc/pro.respawn.flowmvi/core)
![License](https://img.shields.io/github/license/respawn-app/flowMVI)
![GitHub last commit](https://img.shields.io/github/last-commit/respawn-app/FlowMVI)
![Issues](https://img.shields.io/github/issues/respawn-app/FlowMVI)
![GitHub top language](https://img.shields.io/github/languages/top/respawn-app/flowMVI)
[![CodeFactor](https://www.codefactor.io/repository/github/respawn-app/flowMVI/badge)](https://www.codefactor.io/repository/github/respawn-app/flowMVI)

FlowMVI is a Kotlin Multiplatform MVI implementation based on coroutines with a few main goals:

1. Being simple to understand, implement and use
2. Following the Principle of Least Responsibility - all communication happens through strictly defined contract
3. Featuring a clean and readable DSL
4. Being thread-safe but asynchronous

* Documentation is at [https://opensource.respawn.pro/FlowMVI/](https://opensource.respawn.pro/FlowMVI/)  
* KDocs are at [FlowMVI/javadocs](https://opensource.respawn.pro/FlowMVI/javadocs/)

## Let's get started:

![Maven Central](https://img.shields.io/maven-central/v/pro.respawn.flowmvi/core?label=Maven%20Central)

```toml
[versions]
flowmvi = "< Badge above 👆🏻 >"

[dependencies]
flowmvi-core = { module = "pro.respawn.flowmvi:core", version.ref = "flowmvi" } # multiplatform
flowmvi-android = { module = "pro.respawn.flowmvi:android", version.ref = "flowmvi" } # common android
flowmvi-view = { module = "pro.respawn.flowmvi:android-view", version.ref = "flowmvi" } # view-based android
flowmvi-compose = { module = "pro.respawn.flowmvi:android-compose", version.ref = "flowmvi" }  # compose
```

## Core:

```kotlin
sealed interface ScreenState : MVIState {
    data object Loading : ScreenState
    data class Error(e: Exception) : ScreenState
    data class DisplayingCounter(val counter: Int) : ScreenState
}

sealed interface ScreenIntent : MVIIntent {
    data object ClickedCounter : ScreenIntent
}

sealed interface ScreenAction : MVIAction {
    data class ShowMessage(val message: String) : ScreenAction
}


val store by launchedStore<ScreenState, ScreenIntent, ScreenAction>(
    scope = eventProcessingCoroutineScope,
    initial = Loading,
    reduce = { intent -> /*...*/ },
)

// somewhere in the ui layer...
store.subscribe(
    consumerCoroutineScope,
    consume = { action -> /* ... */ },
    render = { state -> /* ... */ },
)
```

## Android (Compose):

```kotlin
class ScreenViewModel : MVIViewModel<ScreenState, ScreenIntent, ScreenAction>(initialState = Loading) {

    override suspend fun reduce(intent: ScreenIntent): Unit = when (intent) {
        is ClickedCounter -> updateState<DisplayingCounter> { //this -> DisplayingCounter

            ShowMessage("Incremented counter").send()

            copy(counter = counter + 1)
        }
        /* ... */
    }
}

@Composable
fun ComposeScreen() = MVIComposable(
    provider = getViewModel<ScreenViewModel>(),
) { state ->

    consume { action ->
        when (action) {
            is ShowMessage -> {
                /* ... */
            }
        }
    }

    when (state) {
        is DisplayingCounter -> {
            Button(onClick = { ClickedCounter.send() }) {
                Text("Counter: ${state.counter}") // render state,
            }
        }
    }
}
```
## Android (View):

```kotlin

// ViewModel and Model classes have not changed

class ScreenFragment: Fragment(), MVIView<ScreenState, ScreenIntent, ScreenAction> {

    override val provider by viewModel<ScreenViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribe() // One-liner for store subscription. Lifecycle-aware and efficient.
    }

    override fun render(state: ScreenState) {
        // update your views
    }

    override fun consume(action: ScreenAction) {
        // handle actions
    }
}
```

And that's it!   
For more information and sample code, see the [Documentation](https://opensource.respawn.pro/FlowMVI).

## License

```
   Copyright 2022 Respawn Team and contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

```
