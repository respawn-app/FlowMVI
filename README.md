![](docs/images/banner.png)

[![CI](https://github.com/respawn-app/FlowMVI/actions/workflows/ci.yml/badge.svg)](https://github.com/respawn-app/FlowMVI/actions/workflows/ci.yml)
![License](https://img.shields.io/github/license/respawn-app/flowMVI)
![GitHub last commit](https://img.shields.io/github/last-commit/respawn-app/FlowMVI)
![Issues](https://img.shields.io/github/issues/respawn-app/FlowMVI)
![GitHub top language](https://img.shields.io/github/languages/top/respawn-app/flowMVI)
[![CodeFactor](https://www.codefactor.io/repository/github/respawn-app/flowMVI/badge)](https://www.codefactor.io/repository/github/respawn-app/flowMVI)
[![AndroidWeekly #556](https://androidweekly.net/issues/issue-556/badge)](https://androidweekly.net/issues/issue-556/)
[![Slack channel](https://img.shields.io/badge/Chat-Slack-orange.svg?style=flat&logo=slack)](https://kotlinlang.slack.com/messages/flowmvi/)

![badge][badge-android] ![badge][badge-jvm] ![badge][badge-js] ![badge][badge-nodejs] ![badge][badge-linux] ![badge][badge-windows] ![badge][badge-ios] ![badge][badge-mac] ![badge][badge-watchos] ![badge][badge-tvos] ![badge][badge-wasm]

FlowMVI is a Kotlin Multiplatform architectural framework based on coroutines with an extensive feature set, powerful
plugin system and a rich DSL.

## Quickstart:

* Sample App ![badge-wasm] :
  [![Static Badge](https://img.shields.io/badge/Click_Me-Click_Me?style=for-the-badge&color=00b147)](https://opensource.respawn.pro/FlowMVI/sample)
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
# Core KMP module
flowmvi-core = { module = "pro.respawn.flowmvi:core", version.ref = "flowmvi" }
# Test DSL
flowmvi-test = { module = "pro.respawn.flowmvi:test", version.ref = "flowmvi" }
# Compose multiplatform
flowmvi-compose = { module = "pro.respawn.flowmvi:compose", version.ref = "flowmvi" }
# Android (common + view-based)
flowmvi-android = { module = "pro.respawn.flowmvi:android", version.ref = "flowmvi" }
# Multiplatform state preservation
flowmvi-savedstate = { module = "pro.respawn.flowmvi:savedstate", version.ref = "flowmvi" }
# Remote debugging client
flowmvi-debugger-client = { module = "pro.respawn.flowmvi:debugger-plugin", version.ref = "flowmvi" }
# Essenty (Decompose) integration
flowmvi-essenty = { module = "pro.respawn.flowmvi:essenty", version.ref = "flowmvi" }
flowmvi-essenty-compose = { module = "pro.respawn.flowmvi:essenty-compose", version.ref = "flowmvi" } 
```

</details>

<details>
<summary>Gradle DSL</summary>

```kotlin
dependencies {
    val flowmvi = "< Badge above 👆🏻 >"
    // Core KMP module
    commonMainImplementation("pro.respawn.flowmvi:core:$flowmvi")
    // compose multiplatform
    commonMainImplementation("pro.respawn.flowmvi:compose:$flowmvi")
    // saving and restoring state
    commonMainImplementation("pro.respawn.flowmvi:savedstate:$flowmvi")
    // essenty integration
    commonMainImplementation("pro.respawn.flowmvi:essenty:$flowmvi")
    commonMainImplementation("pro.respawn.flowmvi:essenty-compose:$flowmvi")
    // testing DSL
    commonTestImplementation("pro.respawn.flowmvi:test:$flowmvi")
    // android integration
    androidMainImplementation("pro.respawn.flowmvi:android:$flowmvi")
    // remote debugging client
    androidDebugImplementation("pro.respawn.flowmvi:debugger-plugin:$flowmvi")
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
* No base classes, complicated interfaces or factories of factories - store is built using a simple DSL
* Use both MVVM+ (functional) or MVI (model-driven) style of programming
* Share, distribute, or disable side-effects based on your team's needs
* Dedicated remote debugger app for Windows, Linux, MacOS.
* Built for Compose: The library lets you achieve the best performance with Compose out of the box.
* The core library depends on kotlin coroutines. Nothing else.
* Integration with popular libraries, such as [Decompose (Essenty)](https://github.com/arkivanov/Decompose)
* 60+% unit test coverage of core library code

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

        actionShareBehavior = ActionShareBehavior.Distribute()

        // makes the store fully async, parallel and thread-safe
        parallelIntents = true
        coroutineContext = Dispatchers.Default
        atomicStateUpdates = true

        // enables debugging features such as logging and remote connection
        debuggable = true
        enableLogging()
        enableRemoteDebugging()

        // allows to undo any operation
        val undoRedo = undoRedo()

        // manages long-running jobs
        val jobManager = manageJobs()

        // saves and restores the state automatically
        serializeState(
            dir = repo.cacheDir,
            json = Json,
            serializer = DisplayingCounter.serializer(),
        )

        // performs long-running tasks on startup
        init {
            repo.startTimer()
        }

        // handles any errors
        recover { e: Exception ->
            action(ShowMessage(e.message))
            null
        }

        // hooks into subscriber lifecycle
        whileSubscribed {
            repo.timer.collect {
                updateState<DisplayingCounter, _> {
                    copy(timer = timer)
                }
            }
        }

        // lazily evaluates and caches values, even when the method is suspending.
        val pagingData by cache {
            repo.getPagedDataSuspending()
        }

        reduce { intent: CounterIntent ->
            when (intent) {
                is ClickedCounter -> updateState<DisplayingCounter, _> {
                    copy(counter = counter + 1)
                }
            }
        }

        // builds custom plugins on the fly
        install {
            onStop { repo.stopTimer() }
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

### Plugins:

Powerful DSL allows to hook into store events and amend any store's logic with reusable plugins.

```kotlin
val counterPlugin = plugin<CounterState, CounterIntent, CounterAction> {
    
    onStart { }
    
    onStop { }
    
    onIntent { intent -> }
    
    onState { old, new -> }
    
    onAction { action -> }
    
    onSubscribe { subs -> }
    
    onUnsubscribe { subs -> }
    
    onException { e -> }
}
```

### Compose Multiplatform:

![badge][badge-android] ![badge][badge-ios] ![badge][badge-mac] ![badge][badge-jvm] ![badge][badge-wasm] ![badge][badge-js]

```kotlin
@Composable
fun CounterScreen() {
    val store = inject<CounterContainer>().store

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

No more subclassing `ViewModel`. Use `StoreViewModel` instead and make your business logic multiplatform.

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

    // time travel keeps track of all plugin operations for you
    assert(timeTravel.starts == 1) 
    assert(state is DisplayingCounter)
    assert(timer.isStarted)

    onStop(null)

    assert(!timer.isStarted)
}
```

## Debugger App

<img src="docs/images/debugger.gif" width="1200" alt="">

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
