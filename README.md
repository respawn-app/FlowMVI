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

FlowMVI is a Kotlin Multiplatform architectural framework based on coroutines.
It enables you to extend your business logic with reusable plugins, handle errors,
achieve thread-safety, and more. It takes about 100 lines of code to get started.

## Quickstart:

* Sample App ![badge-wasm]: 
  [![Static Badge](https://img.shields.io/badge/Click_Me-Click_Me?style=flat&color=00b147)](https://opensource.respawn.pro/FlowMVI/sample/)
* Documentation:
  [![Docs](https://img.shields.io/website?down_color=red&down_message=Offline&label=Docs&up_color=green&up_message=Online&url=https%3A%2F%2Fopensource.respawn.pro%2FFlowMVI%2F%23%2F)](https://opensource.respawn.pro/FlowMVI/#/)
* KDoc:
  [![Javadoc](https://javadoc.io/badge2/pro.respawn.flowmvi/core/javadoc.svg)](https://opensource.respawn.pro/FlowMVI/javadocs/index.html)
* Latest version:
  [![Maven Central](https://img.shields.io/maven-central/v/pro.respawn.flowmvi/core?label=Maven%20Central)](https://central.sonatype.com/namespace/pro.respawn.flowmvi)
* Ask questions on
  [![Slack](https://img.shields.io/badge/Chat-Slack-orange.svg?style=flat&logo=slack)](https://kotlinlang.slack.com/messages/flowmvi/)

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

Usually architecture frameworks mean boilerplate and support difficulty for marginal benefits of "clean code".
This library focuses instead on building a supporting infrastructure to enable new possibilities for your app.

Here's what you get:

* Powerful Plug-In system to automate processes and **reuse any business logic** you desire
    * Create automatic analytics handlers, websocket connections, error handling mechanisms, or anything else once and
      reuse them throughout your whole project automatically
* Automatically **recover from any errors** and prevent crashes
* Build fully **async, reactive and parallel apps** - with no manual thread synchronization required!
* Create business logic components with pluggable UI using **0 platform code**
* Automatic multiplatform system **lifecycle handling**
* Out of the box **debugging, logging, testing, undo/redo, caching and long-running tasks** support
* Debounce, retry, batch, throttle, conflate any operations automatically.
* **Compress, persist, and restore state** automatically on any platform
* No base classes, complicated interfaces, or factories of factories - logic is **declarative and built with a DSL**
* Restartable, reusable business logic components with no external dependencies or dedicated lifecycles.
* Create compile-time safe state machines with a readable DSL. Forget about casts, inconsistent states, and `null`s
* First class Compose Multiplatform support optimized for performance and ease of use
* Use both MVVM+ (functional) or MVI (model-driven) style of programming
* Share, distribute, or disable side-effects based on your team's needs
* Dedicated remote debugger IDEA/AS plugin and app for Windows, Linux, MacOS
* Integration with popular libraries, such as [Decompose (Essenty)](https://github.com/arkivanov/Decompose)
* The core library depends on kotlin coroutines. Nothing else
* Core library is fully covered by tests
* Minimal performance overhead, equal to using a simple Channel. Benchmarks are run regularly
* Learn more by exploring the [sample app](https://opensource.respawn.pro/FlowMVI/sample/) in your browser

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

Then define your business logic:

```kotlin
class CounterContainer(
    private val repo: CounterRepository,
) {
    val store = store<CounterState, CounterIntent, CounterAction>(initial = Loading) {

        configure {
            actionShareBehavior = ActionShareBehavior.Distribute()
            debuggable = true

            // makes the store fully async, parallel and thread-safe
            parallelIntents = true
            coroutineContext = Dispatchers.Default
            atomicStateUpdates = true
        }

        enableLogging()
        enableRemoteDebugging()

        // allows to undo any operation
        val undoRedo = undoRedo()

        // manages long-running jobs
        val jobManager = manageJobs()

        // saves and restores the state automatically
        serializeState(
            path = repo.cacheFile("counter"),
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

        // saves resources when there are no subscribers
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

        // testable reducer as a function
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

### ...And extend it with plugins!

Powerful DSL allows you to hook into various events and amend any part of your logic:

```kotlin
fun analyticsPlugin(analytics: Analytics) = plugin<MVIState, MVIIntent, MVIAction> {
    onStart {
        analytics.logScreenView(config.name) // name of the screen
    }
    onIntent { intent ->
        analytics.logUserAction(intent.name)
    }
    onException { e ->
        analytics.logError(e)
    }
    onSubscribe {
        analytics.logEngagementStart()
    }
    onUnsubscribe {
        analytics.logEngagementEnd()
    }
    onStop {
        analytics.logScreenLeave()
    }
}
```

Never write analytics, debugging, or state persistence code again.

### Compose Multiplatform:

![badge][badge-android] ![badge][badge-ios] ![badge][badge-mac] ![badge][badge-jvm] ![badge][badge-wasm] ![badge][badge-js]

Using FlowMVI with Compose is a matter of one line of code:

```kotlin
@Composable
fun CounterScreen() {
    val store = inject<CounterContainer>().store

    // subscribe to store based on system lifecycle - on any platform
    val state by store.subscribe { action ->
        when (action) {
            is ShowMessage -> /* ... */
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

Enjoy unit-testable UI and free `@Previews`.

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

Finally stop doing UI tests and replace them with a unit test DSL:

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

## Debugger IDE Plugin + App

[![Plugin](https://img.shields.io/jetbrains/plugin/v/25766?style=flat)](https://plugins.jetbrains.com/plugin/25766-flowmvi)

https://github.com/user-attachments/assets/05f8efdb-d125-4c4a-9bda-79875f22578f


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
