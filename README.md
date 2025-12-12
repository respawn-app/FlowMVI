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

#### FlowMVI is a Kotlin Multiplatform Architectural Framework.

It **adds a plug-in system** to your code that helps prevent crashes, handle errors, 
split responsibilities, reuse code, collect analytics, debug & log operations, monitor & improve performance,
achieve thread-safety, save state, manage background jobs, and more.

## ⚡️ Quickstart:

* Get Started in 10 mins:
  [![Quickstart](https://img.shields.io/website?down_color=red&down_message=Offline&label=Quickstart&up_color=green&up_message=Online&url=https%3A%2F%2Fopensource.respawn.pro%2FFlowMVI)](https://opensource.respawn.pro/FlowMVI/quickstart)
* Latest version:
  [![Maven Central](https://img.shields.io/maven-central/v/pro.respawn.flowmvi/core?label=Maven%20Central)](https://central.sonatype.com/namespace/pro.respawn.flowmvi)
* API Docs:
  [![Javadoc](https://javadoc.io/badge2/pro.respawn.flowmvi/core/javadoc.svg)](https://opensource.respawn.pro/FlowMVI/javadocs/index.html)
* Sample App + Showcase (Web):
  [![Sample](https://img.shields.io/badge/Click_Me-Click_Me?style=flat&color=00b147)](https://opensource.respawn.pro/FlowMVI/sample/)
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
# Performance metrics collection
flowmvi-metrics = { module = "pro.respawn.flowmvi:metrics", version.ref = "flowmvi" }
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
    // metrics collection & export
    commonMainImplementation("pro.respawn.flowmvi:metrics:$flowmvi")
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

Usually architecture frameworks mean boilerplate, restrictions, and support difficulty for marginal benefits
of "clean code". FlowMVI does not dictate what your code should do or look like.
Instead, this library focuses on **building a supporting infrastructure** to enable new possibilities for your app.

Here's what you get:

* Powerful plug-in system to **reuse any business logic** you desire. 
  Write your auth, error handling, analytics, logging, configuration, and any other code **once**
  and forget about it, focusing on more important things instead.

* Automatically **recover from exceptions**, prevent crashes, and report them to analytics.
* Automatically collect and view logs: forget about `Log.e("asdf")` sprinkling.
* **Collect 50+ performance metrics** with Prometheus, Grafana, OpenTelemetry export and 5 lines of setup.
* Manage concurrent, long-running **background jobs** with complete thread-safety.
* Debounce, retry, batch, throttle, conflate, **intercept any operations** automatically
* **Compress, persist, and restore state** automatically on any platform
* Create **compile-time safe state machines** with a readable DSL. Forget about casts, inconsistent states, and `null`s
* Share, distribute, disable, intercept, safely **manage side-effects**
* Build fully **async, reactive and parallel apps** - with no manual thread synchronization required!
* Write **simple, familiar MVVM+** code or follow MVI/Redux - no limits or requirements 
* Build restartable, reusable business logic components with **no external dependencies** or dedicated lifecycles
* No base classes, complicated abstractions, or factories of factories - write **simple, declarative logic** using a DSL
* Automatic multiplatform system **lifecycle handling**
* First class, one-liner **Compose Multiplatform support** with Previews and UI tests.
* Integrates with [Decompose](https://github.com/arkivanov/Decompose), Koin, Kodein, androidx.navigation, Nav3, and more
* Dedicated **IDE Plugin for debugging and codegen** and app for Windows, Linux, macOS
* The core **library has no dependencies** - just coroutines
* Extensively covered by **350+ tests**
* **Minimal performance overhead**, equal to using a simple Channel, with regular benchmarking
* **Test any business logic** using clean, declarative DSL
* Learn more by exploring the [sample app](https://opensource.respawn.pro/FlowMVI/sample/) in your browser
* 10 minutes to try by following the [Quickstart Guide](https://opensource.respawn.pro/FlowMVI/quickstart).

## 👀 Show me the code!

Here is an example of your new workflow:

### 1. Define a Contract:

```kotlin
sealed interface State : MVIState {

    data object Loading : State
    data class Error(val e: Exception) : State
    data class Content(val user: User?) : State
}

sealed interface Intent : MVIIntent {
    data object ClickedSignOut : Intent
}

sealed interface Action : MVIAction { // optional side-effect
    data class ShowMessage(val message: String) : Action
}
```

### 2. Declare your business logic:

```kotlin
val authStore = store(initial = State.Loading, coroutineScope) {
    recover { e: Exception -> // handle errors
        updateState { State.Error(e) }
        null
    }
    init { // load data
        updateState {
            State.Content(user = repository.loadUser())
        }
    }
    reduce { intent: Intent -> // respond to events
        when (intent) {
            is ClickedSignOut -> updateState<State.Content, _> {
                
                action(ShowMessage("Bye!"))
                copy(user = null)
            }
        }
    }
}
```

FlowMVI lets you scale your app in a way that does not increase complexity.
Adding a new feature is as simple as calling a function.

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
        analytics.logEngagementStart(config.name)
    }
    onUnsubscribe {
        analytics.logEngagementEnd(config.name)
    }
    onStop {
        analytics.logScreenLeave(config.name)
    }
}
```

Never write analytics, debugging, logging, or state persistence code again.

## Compose Multiplatform

Using FlowMVI with Compose is a matter of one line of code:

```kotlin
@Composable
fun AuthScreen() {
    // subscribe based on system lifecycle - on any platform
    val state by authStore.subscribe

    when (state) {
        is Content -> {
            Button(onClick = { store.intent(ClickedSignOut) }) {
                Text("Sign Out")
            }
        }
    }
}
```

Enjoy testable UI and free `@Preview`s.

## Testing DSL

Bundled Test Harness with minimal verbosity:

### Test Stores

```kotlin
authStore.subscribeAndTest {
    // turbine + kotest example
    
    intent(ClickedSignOut)
    
    states.test {
        awaitItem() shouldBe Content(user = null)
    }
    actions.test {
        awaitItem() shouldBe ShowMessage("Bye!")
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

Finally stop writing UI tests and replace them with unit tests.

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
   Copyright 2022-2026 Respawn Team and contributors

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
