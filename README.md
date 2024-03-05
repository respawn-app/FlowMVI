![](docs/images/banner.png)

[![CI](https://github.com/respawn-app/FlowMVI/actions/workflows/ci.yml/badge.svg)](https://github.com/respawn-app/FlowMVI/actions/workflows/ci.yml)
![License](https://img.shields.io/github/license/respawn-app/flowMVI)
![GitHub last commit](https://img.shields.io/github/last-commit/respawn-app/FlowMVI)
![Issues](https://img.shields.io/github/issues/respawn-app/FlowMVI)
![GitHub top language](https://img.shields.io/github/languages/top/respawn-app/flowMVI)
[![CodeFactor](https://www.codefactor.io/repository/github/respawn-app/flowMVI/badge)](https://www.codefactor.io/repository/github/respawn-app/flowMVI)
[![AndroidWeekly #556](https://androidweekly.net/issues/issue-556/badge)](https://androidweekly.net/issues/issue-556/)
[![Slack channel](https://img.shields.io/badge/Chat-Slack-orange.svg?style=flat&logo=slack)](https://kotlinlang.slack.com/messages/flowmvi/)

![badge][badge-android] ![badge][badge-jvm] ![badge][badge-js] ![badge][badge-nodejs] ![badge][badge-linux] ![badge][badge-windows] ![badge][badge-ios] ![badge][badge-mac] ![badge][badge-watchos] ![badge][badge-tvos]

FlowMVI is a Kotlin Multiplatform MVI library based on coroutines that has a few main goals:

1. Being simple to understand and use while staying powerful and flexible.
2. Featuring a clean and rich DSL.
3. Being thread-safe but asynchronous by design.

## Quickstart:

* Documentation:
  [![Docs](https://img.shields.io/website?down_color=red&down_message=Offline&label=Docs&up_color=green&up_message=Online&url=https%3A%2F%2Fopensource.respawn.pro%2FFlowMVI%2F%23%2F)](https://opensource.respawn.pro/FlowMVI/#/)
* KDoc:
  [![Javadoc](https://javadoc.io/badge2/pro.respawn.flowmvi/core/javadoc.svg)](https://opensource.respawn.pro/FlowMVI/javadocs/index.html)
* Latest version:
  [![Maven Central](https://img.shields.io/maven-central/v/pro.respawn.flowmvi/core?label=Maven%20Central)](https://central.sonatype.com/namespace/pro.respawn.flowmvi)

<details>
<summary>Version catalogs</summary>

```toml
[versions]
flowmvi = "< Badge above 👆🏻 >"

[dependencies]
# core KMP module
flowmvi-core = { module = "pro.respawn.flowmvi:core", version.ref = "flowmvi" }
# test DSL
flowmvi-test = { module = "pro.respawn.flowmvi:test", version.ref = "flowmvi" }
# compose multiplatform
flowmvi-compose = { module = "pro.respawn.flowmvi:compose", version.ref = "flowmvi" }
# common android
flowmvi-android = { module = "pro.respawn.flowmvi:android", version.ref = "flowmvi" }
# view-based android
flowmvi-view = { module = "pro.respawn.flowmvi:android-view", version.ref = "flowmvi" }
# Multiplatform state preservation
flowmvi-savedstate = { module = "pro.respawn.flowmvi:savedstate", version.ref = "flowmvi" }
# Remote debugging client
flowmvi-debugger-client = { module = "pro.respawn.flowmvi:debugger-plugin", version.ref = "flowmvi" } 
```

</details>

<details>
<summary>Gradle DSL</summary>

```kotlin
dependencies {
    val flowmvi = "< Badge above 👆🏻 >"
    commonMainImplementation("pro.respawn.flowmvi:core:$flowmvi")
    commonMainImplementation("pro.respawn.flowmvi:compose:$flowmvi")
    commonMainImplementation("pro.respawn.flowmvi:savedstate:$flowmvi")
    commonTestImplementation("pro.respawn.flowmvi:test:$flowmvi")

    androidDebugImplementation("pro.respawn.flowmvi:debugger-plugin:$flowmvi")
    androidMainImplementation("pro.respawn.flowmvi:android:$flowmvi")
    androidMainImplementation("pro.respawn.flowmvi:android-view:$flowmvi")
}
```

</details>

## Why FlowMVI?

* Fully async and parallel business logic - with no manual thread synchronization required!
* Automatically recover from any errors and avoid runtime crashes with one line of code
* Automatic platform-independent system lifecycle handling
* Build fully-multiplatform business logic with pluggable UI
* Out of the box debugging, logging, testing and long-running task management support
* Restartable, reusable stores with no external dependencies or dedicated lifecycles.
* Compress, persist, and restore state automatically with a single line of code - on any platform
* Create compile-time safe state machines with a readable DSL. Forget about `state as? ...` casts
* Decompose stores into plugins, split responsibilities, and modularize the project easily
* No base classes or complicated interfaces - store is built using a simple DSL
* Use both MVVM+ (functional) or MVI (model-driven) style of programming
* Share, distribute, or disable side-effects based on your team's needs
* Create parent-child relationships between stores and delegate responsibilities
* Built for Compose: The library lets you achieve the best performance with Compose out of the box.
* The core library depends on kotlin coroutines. That's it. Nothing else.
* 70+% unit test coverage of core library code
* Dedicated remote debugger app for Windows, Linux, MacOS (in alpha)

## How does it look?

<details>
<summary>Define a contract</summary>

```kotlin
sealed interface CounterState : MVIState {
    data object Loading : CounterState
    data class Error(val e: Exception) : CounterState

    @Serializable
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
```

</details>

```kotlin
class CounterContainer(
    private val repo: CounterRepository,
) {
    val store = store<CounterState, CounterIntent, CounterAction>(initial = Loading) {
        parallelIntents = true
        coroutineContext = Dispatchers.Default
        actionShareBehavior = ActionShareBehavior.Distribute()
        intentCapacity = 64

        install(
            platformLoggingPlugin(),
            timeTravelPlugin(),
            undoRedoPlugin(),
        )

        val jobManager = manageJobs()

        serializeState(
            dir = repo.cacheDir,
            json = Json,
            serializer = DisplayingCounter.serializer(),
        )

        init { repo.startTimer() }

        recover { e: Exception ->
            action(ShowMessage(e.message))
            null
        }

        whileSubscribed {
            repo.timer.collect {
                updateState<DisplayingCounter, _> {
                    copy(timer = timer)
                }
            }
        }

        reduce { intent: CounterIntent ->
            when (intent) {
                is ClickedCounter -> updateState<DisplayingCounter, _> {
                    copy(counter = counter + 1)
                }
            }
        }

        parentStore(repo.store) { state ->
            updateState {
                copy(timer = state.timer)
            }
        }

        // lazily evaluate and cache values, even when the method is suspending.
        val pagingData by cache {
            repo.getPagedDataSuspending()
        }

        install { // build custom plugins on the fly
            onStop {
                repo.stopTimer()
            }
        }
    }
}
```

### Subscribe one-liner:

```kotlin
store.subscribe(
    scope = coroutineScope,
    consume = { action -> /* process side effects */ },
    render = { state -> /* render states */ },
)
```

### Custom plugins:

Create plugins with a single line of code for any store or a specific one, then hook into any store events:

```kotlin
val counterPlugin = plugin<CounterState, CounterIntent, CounterAction> {
    onStart {
        /*...*/
    }
    onIntent { intent ->
        /*...*/
    }
}
```

### Compose Multiplatform:

![badge][badge-android] ![badge][badge-ios] ![badge][badge-mac] ![badge][badge-jvm]  

```kotlin
@Composable
fun CounterScreen() {
    val store = inject<CounterContainer>()

    // subscribe to store based on system lifecycle - on any platform
    val state by store.subscribe { action ->
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

### Android support:

No more subclassing `ViewModel`. Use `StoreViewModel` instead and inject stores directly.

```kotlin
val module = module {
    factoryOf(::CounterContainer)
    viewModel(qualifier<CounterContainer>()) { StoreViewModel(get<CounterContainer>()) }
}

class ScreenFragment : Fragment() {

    private val vm by viewModel(qualifier<CounterContainer>())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

## Testing DSL

### Test Stores

```kotlin
counterStore().subscribeAndTest {

    // turbine + kotest example

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

### Test plugins

```kotlin
val timer = Timer()
timerPlugin(timer).test(Loading) {

    onStart()

    assert(timeTravel.starts == 1) // keeps track of all plugin operations
    assert(state is DisplayingCounter)
    assert(timer.isStarted)

    onStop(null)

    assert(!timer.isStarted)
}
```

Ready to try? Start with reading the [Quickstart Guide](https://opensource.respawn.pro/FlowMVI/#/quickstart).

## Star History

<a href="https://star-history.com/#respawn-app/flowmvi&Date">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=respawn-app/flowmvi&type=Date&theme=dark" />
    <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=respawn-app/flowmvi&type=Date" />
    <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=respawn-app/flowmvi&type=Date" />
  </picture>
</a>

## License

```
   Copyright 2022-2024 Respawn Team and contributors

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
