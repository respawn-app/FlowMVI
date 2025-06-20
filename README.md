![FlowMVI Framework Banner](https://opensource.respawn.pro/FlowMVI/banner.webp)

[![CI](https://github.com/respawn-app/FlowMVI/actions/workflows/ci.yml/badge.svg)](https://github.com/respawn-app/FlowMVI/actions/workflows/ci.yml)
![License](https://img.shields.io/github/license/respawn-app/flowMVI)
![GitHub last commit](https://img.shields.io/github/last-commit/respawn-app/FlowMVI)
![Issues](https://img.shields.io/github/issues/respawn-app/FlowMVI)
![GitHub top language](https://img.shields.io/github/languages/top/respawn-app/flowMVI)
[![AndroidWeekly #563](https://androidweekly.net/issues/issue-563/badge)](https://androidweekly.net/issues/issue-563/)
[![Slack channel](https://img.shields.io/badge/Chat-Slack-orange.svg?style=flat&logo=slack)](https://kotlinlang.slack.com/messages/flowmvi/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/respawn-app/FlowMVI)


![badge][badge-android] ![badge][badge-jvm] ![badge][badge-js] ![badge][badge-nodejs] ![badge][badge-linux] ![badge][badge-windows] ![badge][badge-ios] ![badge][badge-mac] ![badge][badge-watchos] ![badge][badge-tvos] ![badge][badge-wasm]

FlowMVI is a Kotlin Multiplatform architectural framework based on coroutines.
It enables you to extend your business logic with reusable plugins, handle errors,
achieve thread-safety, and more. It takes about 10 minutes to get started.

## ⚡️ Quickstart:

* Get Started in 10 minutes:
  [![Quickstart](https://img.shields.io/website?down_color=red&down_message=Offline&label=Quickstart&up_color=green&up_message=Online&url=https%3A%2F%2Fopensource.respawn.pro%2FFlowMVI)](https://opensource.respawn.pro/FlowMVI/quickstart)
* Latest version:
  [![Maven Central](https://img.shields.io/maven-central/v/pro.respawn.flowmvi/core?label=Maven%20Central)](https://central.sonatype.com/namespace/pro.respawn.flowmvi)
* API Docs:
  [![Javadoc](https://javadoc.io/badge2/pro.respawn.flowmvi/core/javadoc.svg)](https://opensource.respawn.pro/FlowMVI/javadocs/index.html)
* Sample App in your browser:
  [![Static Badge](https://img.shields.io/badge/Click_Me-Click_Me?style=flat&color=00b147)](https://opensource.respawn.pro/FlowMVI/sample/)
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

## 🚀 Why FlowMVI?

Usually architecture frameworks mean boilerplate and support difficulty for marginal benefits of "clean code".
FlowMVI does not dictate what your code should do or look like.
Instead, this library focuses on building a supporting infrastructure to enable new possibilities for your app.

Here's what you get:

* Powerful Plug-In system to automate processes and **reuse any business logic** you desire
    * Create automatic analytics handlers, websocket connections, error handling mechanisms, or anything else **once**
      and reuse them throughout your whole project automatically
* Automatically **recover from any errors** and report them to analytics.
* Build fully **async, reactive and parallel apps** - with no manual thread synchronization required!
* Create **multiplatform business logic** components with pluggable UI
* Automatic multiplatform system **lifecycle handling**
* Out of the box **debugging, logging, caching and long-running tasks** support
* Debounce, retry, batch, throttle, conflate, monitor, **modify any operations** automatically
* **Compress, persist, and restore state** automatically on any platform
* **No base classes, complicated interfaces**, or factories of factories - logic is declarative and built with a DSL
* Build **Restartable, reusable business logic components** with no external dependencies or dedicated lifecycles
* Create **compile-time safe state machines** with a readable DSL. Forget about casts, inconsistent states, and `null`s
* First class **Compose Multiplatform support** optimized for performance and ease of use
* Use both **MVVM+** (functional) or **MVI** (model-driven) style of programming
* Share, distribute, disable, **manage side-effects** based on your team's needs
* Dedicated **IDE Plugin for debugging and codegen** and app for Windows, Linux, MacOS
* **Integration with popular libraries**, such as [Decompose (Essenty)](https://github.com/arkivanov/Decompose), Koin, Kodein, androidx.navigation, and more.
* The core **library has no dependencies** except kotlin coroutines.
* Core library is fully covered by **hundreds of tests**
* **Minimal performance overhead**, equal to using a simple Channel, with regular benchmarking
* **Test any business logic** using clean, declarative DSL.
* Learn more by exploring the [sample app](https://opensource.respawn.pro/FlowMVI/sample/) in your browser 
* 10 minutes to try by following the [Quickstart Guide](https://opensource.respawn.pro/FlowMVI/quickstart).

## 👀 Show me the code!

Here is an example of your new workflow:

### 1. Define a Contract:

```kotlin
sealed interface State : MVIState {

    data object Loading : State
    data class Error(val e: Exception) : State
    data class Content(val counter: Int = 0) : State
}

sealed interface Intent : MVIIntent {
    data object ClickedCounter : Intent
}

sealed interface Action : MVIAction {
    data class ShowMessage(val message: String) : Action
}
```

### 2. Declare your business logic:

```kotlin
val counterStore = store(initial = State.Loading, scope = coroutineScope) {

    install(analyticsPlugin) // install custom plugins

    recover { e: Exception -> // recover from errors
        updateState { State.Error(e) }
        null
    }
    init { // load data
        updateState {
            State.Content(counter = repository.loadCounter())
        }
    }
    reduce { intent: Intent -> // respond to events
        when (intent) {
            is ClickedCounter -> updateState<State.Content, _> {
                action(ShowMessage("Incremented!"))

                copy(counter = counter + 1)
            }
        }
    }
}

store.intent(ClickedCounter)
```

### 3. Scale your app

FlowMVI lets you scale your app in a way that does not increase complexity.
Adding a new feature is as simple as calling a function.

<details>
<summary>Advanced configuration example</summary>

```kotlin
class CounterContainer(
    private val repo: CounterRepository, // inject dependencies
) {
    val store = store<CounterState, CounterIntent, CounterAction>(initial = Loading) {

        configure {
            // use various side-effect strategies
            actionShareBehavior = Distribute()

            // checks and verifies your business logic for you
            debuggable = true

            // make the store fully async, parallel and thread-safe
            parallelIntents = true
            coroutineContext = Dispatchers.Default
            stateStrategy = Atomic()
        }

        // out of the box logging
        enableLogging()

        // debug using the IDE plugin
        enableRemoteDebugging()

        // undo / redo any operation
        val undoRedo = undoRedo()

        // manage long-running jobs
        val jobManager = manageJobs<CounterJob>()

        // save and restore the state automatically
        serializeState(
            path = repo.cacheFile("counter"),
            serializer = DisplayingCounter.serializer(),
        )

        // perform long-running tasks on startup
        init {
            repo.startTimer()
        }

        // save resources when there are no subscribers
        whileSubscribed {
            repo.timer.collect {
                updateState<DisplayingCounter, _> {
                    copy(timer = timer)
                }
            }
        }

        // lazily evaluate and cache values, even when the method is suspending.
        val pagingData by cache {
            repo.getPagedDataSuspending()
        }

        // testable reducer as a function
        reduce { intent: CounterIntent ->
            when (intent) {
                // typed state update prevents races and allows using sealed class hierarchies for LCE
                is ClickedCounter -> updateState<DisplayingCounter, _> {
                    copy(counter = counter + 1)
                }
            }
        }

        // cleanup resources
        deinit {
            repo.stopTimer()
        }
    }
}
```

</details>

## Extend your logic with Plugins

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

## Compose Multiplatform Support

![badge][badge-android] ![badge][badge-ios] ![badge][badge-mac] ![badge][badge-jvm] ![badge][badge-wasm] ![badge][badge-js]

Using FlowMVI with Compose is a matter of one line of code:

```kotlin
@Composable
fun CounterScreen() {
    val store = counterStore

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

Enjoy testable UI and free `@Preview`s.

### Android Support

No more subclassing `ViewModel`. Use `StoreViewModel` instead and make your business logic multiplatform.

```kotlin
val module = module { // Koin example
    factoryOf(::CounterContainer)
    viewModel(qualifier<CounterContainer>()) { ContainerViewModel(get<CounterContainer>()) }
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

Finally stop writing UI tests and replace them with unit tests:

### Test Stores

```kotlin
store.subscribeAndTest {
    // turbine + kotest example
    ClickedCounter resultsIn {
        states.test {
            awaitItem() shouldBe State(counter = 1)
        }
        actions.test {
            awaitItem() shouldBe ShowMessage
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

IDE plugin generates code and lets you debug and control your app remotely:
[![Plugin](https://img.shields.io/jetbrains/plugin/v/25766?style=flat)](https://plugins.jetbrains.com/plugin/25766-flowmvi)

<video
  src='https://github.com/user-attachments/assets/05f8efdb-d125-4c4a-9bda-79875f22578f'
  controls
  width="100%"
  alt="FlowMVI IDE Plugin Demo">
  Your browser does not support the video element. You can view the demo at our website.
</video>

## People love the library:

<a href="https://star-history.com/#respawn-app/flowmvi&Date">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=respawn-app/flowmvi&type=Date&theme=dark" />
    <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=respawn-app/flowmvi&type=Date" />
    <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=respawn-app/flowmvi&type=Date" />
  </picture>
</a>

## Ready to try?

Begin by reading the [Quickstart Guide](https://opensource.respawn.pro/FlowMVI/quickstart).

----

## License

```
   Copyright 2022-2025 Respawn Team and contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

[badge-android]: https://img.shields.io/badge/-android-6EDB8D.svg?style=flat

[badge-android-native]: https://img.shields.io/badge/support-[AndroidNative]-6EDB8D.svg?style=flat

[badge-jvm]: https://img.shields.io/badge/-jvm-DB413D.svg?style=flat

[badge-js]: https://img.shields.io/badge/-js-F8DB5D.svg?style=flat

[badge-js-ir]: https://img.shields.io/badge/support-[IR]-AAC4E0.svg?style=flat

[badge-nodejs]: https://img.shields.io/badge/-nodejs-68a063.svg?style=flat

[badge-linux]: https://img.shields.io/badge/-linux-2D3F6C.svg?style=flat

[badge-windows]: https://img.shields.io/badge/-windows-4D76CD.svg?style=flat

[badge-wasm]: https://img.shields.io/badge/-wasm-624FE8.svg?style=flat

[badge-apple-silicon]: https://img.shields.io/badge/support-[AppleSilicon]-43BBFF.svg?style=flat

[badge-ios]: https://img.shields.io/badge/-ios-CDCDCD.svg?style=flat

[badge-mac]: https://img.shields.io/badge/-macos-111111.svg?style=flat

[badge-watchos]: https://img.shields.io/badge/-watchos-C0C0C0.svg?style=flat

[badge-tvos]: https://img.shields.io/badge/-tvos-808080.svg?style=flat
