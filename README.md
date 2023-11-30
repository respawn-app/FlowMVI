# FlowMVI 2.0

[![CI](https://github.com/respawn-app/FlowMVI/actions/workflows/ci.yml/badge.svg)](https://github.com/respawn-app/FlowMVI/actions/workflows/ci.yml)
![License](https://img.shields.io/github/license/respawn-app/flowMVI)
![GitHub last commit](https://img.shields.io/github/last-commit/respawn-app/FlowMVI)
![Issues](https://img.shields.io/github/issues/respawn-app/FlowMVI)
![GitHub top language](https://img.shields.io/github/languages/top/respawn-app/flowMVI)
[![CodeFactor](https://www.codefactor.io/repository/github/respawn-app/flowMVI/badge)](https://www.codefactor.io/repository/github/respawn-app/flowMVI)
[![AndroidWeekly #556](https://androidweekly.net/issues/issue-556/badge)](https://androidweekly.net/issues/issue-556/)

![badge][badge-android] ![badge][badge-jvm] ![badge][badge-js] ![badge][badge-nodejs] ![badge][badge-linux] ![badge][badge-windows] ![badge][badge-ios] ![badge][badge-mac] ![badge][badge-watchos] ![badge][badge-tvos]

FlowMVI is a Kotlin Multiplatform MVI library based on coroutines that has a few main goals:

1. Being simple to understand and use while staying powerful and flexible.
2. Featuring a clean and rich DSL.
3. Being thread-safe but asynchronous by design.

## Quickstart:

* Documentation:
  [![Docs](https://img.shields.io/website?down_color=red&down_message=Offline&label=Docs&up_color=green&up_message=Online&url=https%3A%2F%2Fopensource.respawn.pro%2FFlowMVI%2F%23%2F)](https://opensource.respawn.pro/FlowMVI/#/)
* KDoc:
  [![Javadoc](https://javadoc.io/badge2/pro.respawn.flowmvi/core/javadoc.svg)](https://opensource.respawn.pro/FlowMVI/javadocs)
* Latest version:
  [![Maven Central](https://img.shields.io/maven-central/v/pro.respawn.flowmvi/core?label=Maven%20Central)](https://central.sonatype.com/namespace/pro.respawn.flowmvi)

### Version Catalogs
```toml
[versions]
flowmvi = "< Badge above 👆🏻 >"

[dependencies]
flowmvi-core = { module = "pro.respawn.flowmvi:core", version.ref = "flowmvi" } # multiplatform
flowmvi-test = { module = "pro.respawn.flowmvi:test", version.ref = "flowmvi" }  # test DSL

flowmvi-android = { module = "pro.respawn.flowmvi:android", version.ref = "flowmvi" } # common android
flowmvi-view = { module = "pro.respawn.flowmvi:android-view", version.ref = "flowmvi" } # view-based android
flowmvi-compose = { module = "pro.respawn.flowmvi:android-compose", version.ref = "flowmvi" }  # compose
```
### Kotlin DSL
```kotlin
dependencies {
    val flowmvi = "< Badge above 👆🏻 >"
    commonMainImplementation("pro.respawn.flowmvi:core:$flowmvi")
    commonTestImplementation("pro.respawn.flowmvi:test:$flowmvi")

    androidMainImplementation("pro.respawn.flowmvi:android:$flowmvi")
    androidMainImplementation("pro.respawn.flowmvi:android-view:$flowmvi")
    androidMainImplementation("pro.respawn.flowmvi:android-compose:$flowmvi")
}
```

## Features:

Rich, plugin-based store DSL:

```kotlin
sealed interface CounterState : MVIState {
    data object Loading : CounterState
    data class Error(val e: Exception) : CounterState
    data class DisplayingCounter(
        val timer: Int,
        val counter: Int,
    ) : CounterState
}

sealed interface CounterIntent : MVIIntent {
    data object ClickedCounter : CounterIntent
}

sealed interface CounterAction : MVIAction {
    data class ShowMessage(val message: String) : CounterAction
}

class CounterContainer(
    private val repo: CounterRepository,
) {
    val store = store<CounterState, CounterIntent, CounterAction>(initial = Loading) {
        name = "CounterStore"
        parallelIntents = true
        actionShareBehavior = ActionShareBehavior.Distribute() // disable, share, distribute or consume side effects
        intentCapacity = 64

        install(
            platformLoggingPlugin(), // log to console, logcat or NSLog
            analyticsPlugin(name), // create custom plugins
            timeTravelPlugin(), // unit test stores and track changes
        )

        saveState {  // persist and restore state
            get = { repo.restoreStateFromFile() }
            set = { repo.saveStateToFile(this) }
        }

        val undoRedoPlugin = undoRedo(maxQueueSize = 10) // undo and redo any changes

        val jobManager = manageJobs() // manage named jobs

        init { // run actions when store is launched
            repo.startTimer()
        }

        whileSubscribed { // run a job while any subscribers are present
            repo.timer.onEach { timer: Int ->
                updateState<DisplayingCounter, _> { // update state safely between threads and filter by type
                    copy(timer = timer)
                }
            }.consume()
        }

        recover { e: Exception -> // recover from errors both in jobs and plugins
            action(ShowMessage(e.message)) // send side-effects
            null
        }

        reduce { intent: CounterIntent -> // reduce intents
            when (intent) {
                is ClickedCounter -> updateState<DisplayingCounter, _> {
                    copy(counter = counter + 1)
                }
            }
        }

        parentStore(repo.store) { state -> // one-liner to attach to any other store.
            updateState {
                copy(timer = state.timer)
            }
        }

        install { // build and install custom plugins on the fly

            onStop { // hook into various store events
                repo.stopTimer()
            }

            onState { old, new -> // veto changes, modify states, launch jobs, do literally anything
                new.withType<DisplayingCounter, _> {
                    if (counter >= 100) {
                        launch { repo.resetTimer() }
                        copy(counter = 0, timer = 0)
                    } else new
                }
            }
        }
    }
}
```

### Subscribe one-liner:

```kotlin
store.subscribe(
    scope = consumerCoroutineScope,
    consume = { action -> /* process side effects */ },
    render = { state -> /* render states */ },
)
```

### Custom plugins:

```kotlin
// Create plugins with a single line of code for any store or a specific one
val counterPlugin = plugin<CounterState, CounterIntent, CounterAction> {
    onStart {
        /*...*/
    }
    onIntent { intent ->
        /*...*/
    }
}
```

### Android support (Compose):

```kotlin
val module = module {
    // No more subclassing. Use StoreViewModel for everything and inject containers or stores directly.
    factoryOf(::CounterContainer)
    viewModel(qualifier<CounterContainer>()) { StoreViewModel(get<CounterContainer>()) }
}

// collect the store efficiently based on composable's lifecycle
@Composable
fun CounterScreen() {
    val store = getViewModel(qualifier<CounterContainer>())

    val state by store.subscribe { action -> // collect actions/states from composables
        when (action) {
            is ShowMessage -> {
                /* ... */
            }
        }
    }

    when (state) {
        is DisplayingCounter -> {
            Button(onClick = { store.intent(ClickedCounter) }) {
                Text("Counter: ${state.counter}")
            }
        }
    }
}
```

### Android support (View):

```kotlin
class ScreenFragment : Fragment() {

    private val vm by viewModel(qualifier<CounterContainer>())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // One-liner for store subscription. Lifecycle-aware and efficient.
        subscribe(vm, ::consume, ::render) 
    }

    private fun render(state: CounterState) {
        // update your views
    }

    private fun consume(action: CounterAction) {
        // handle actions
    }
}
```

### Testing DSL

```kotlin
// using Turbine + Kotest
testStore().subscribeAndTest {

    ClickedCounter resultsIn {
        states.test {
            awaitItem() shouldBe DisplayingCounter(counter = 1, timer = 0)
        }
        actions.test {
            awaitItem().shouldBeTypeOf<ShowMessage>()
        }
    }
}
```

Ready to try? Start with reading the [Quickstart Guide](https://opensource.respawn.pro/FlowMVI/#/quickstart).

## License

```
   Copyright 2022-2023 Respawn Team and contributors

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

[badge-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat

[badge-android-native]: http://img.shields.io/badge/support-[AndroidNative]-6EDB8D.svg?style=flat

[badge-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat

[badge-js]: http://img.shields.io/badge/-js-F8DB5D.svg?style=flat

[badge-js-ir]: https://img.shields.io/badge/support-[IR]-AAC4E0.svg?style=flat

[badge-nodejs]: https://img.shields.io/badge/-nodejs-68a063.svg?style=flat

[badge-linux]: http://img.shields.io/badge/-linux-2D3F6C.svg?style=flat

[badge-windows]: http://img.shields.io/badge/-windows-4D76CD.svg?style=flat

[badge-wasm]: https://img.shields.io/badge/-wasm-624FE8.svg?style=flat

[badge-apple-silicon]: http://img.shields.io/badge/support-[AppleSilicon]-43BBFF.svg?style=flat

[badge-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg?style=flat

[badge-mac]: http://img.shields.io/badge/-macos-111111.svg?style=flat

[badge-watchos]: http://img.shields.io/badge/-watchos-C0C0C0.svg?style=flat

[badge-tvos]: http://img.shields.io/badge/-tvos-808080.svg?style=flat
