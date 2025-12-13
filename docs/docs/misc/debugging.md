---
sidebar_position: 1
---


# Remote Debugger Setup

FlowMVI comes with a remote debugging setup with a dedicated Jetbrains IDEs / Android Studio plugin and a desktop app
for Windows, Linux, and MacOS.

<iframe width="384px" height="319px" src="https://plugins.jetbrains.com/embeddable/card/25766"></iframe>

## Step 1: Install the plugin on **debug builds only**

:::danger[Don't install the debugger on prod builds!]

It pollutes your app with unnecessary code, introduces serious security
risks and degrades performance. If possible on your platform, don't include the debugging code in the release build or
use minification/obfuscation to remove the debugging code.

:::

### 1.1 Set up a module for store configurations

To keep the source set structure simple, you can create a separate module for your store configuration logic and then
inject configurations using DI.

First, create a separate module where you'll keep the Store configuration.

```
<project_root>/
├─ common-arch/
│  ├─ src/
│  │  ├─ androidDebug/
│  │  │  ├─ InstallDebugger.kt
│  │  ├─ commonMain/
│  │  │  ├─ InstallDebugger.kt
│  │  ├─ nativeMain/
│  │  │  ├─ InstallDebugger.kt
│  │  ├─ androidRelease/
│  │  │  ├─ InstallDebugger.kt
|  ├─ build.gradle.kts
```

```kotlin title="common-arch/build.gradle.kts"
dependencies {
    debugImplementation(libs.flowmvi.debugger) // android Debug (name is incorrect on the kotlin plugin side)
    nativeMainImplementation(libs.flowmvi.debugger) // other platforms
    implementation(libs.flowmvi.core)
}
```

### 1.2 Set up source set overrides

Now we're going to create an expect-actual fun to:

1. Install the real remote debugger in `androidDebug` source set
2. Do nothing in `androidRelease` source set
3. Conditionally install the debugger on other platforms where build types are not supported.

```kotlin
// commonMain -> InstallDebugger.kt
expect fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger()

// androidDebug -> InstallDebugger.kt
actual fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger(
) = install(debuggerPlugin())

// androidRelease -> InstallDebugger.kt
actual fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger() = Unit

// conditional installation for other platforms:
actual fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger() {
    enableRemoteDebugging()
}
```

:::info[About Indexing]

As of the date of writing, the Android Studio will not index the `androidRelease` source set correctly, but it _will_
be picked up by the compiler. We'll have to resort to "notepad-style coding" for that set unfortunately.

:::

### 1.3 Set up store configuration injection

:::tip

If you're building a small pet project, you may omit this complicated setup and just use a simple extension
if you know the risks you are taking.

:::

Set up config injection using a factory pattern using your DI framework:

```kotlin
interface ConfigurationFactory {

    operator fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.invoke(name: String)
}

inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.configure(
    configuration: StoreConfiguration,
    name: String,
) = with(configuration) {
    invoke(name = name)
}
```

:::tip

You can also use this to inject other plugins, such as the Saved State plugin or your custom plugins.

:::

Now we'll create a configuration factory.
You can create more based on your needs, such as for testing stores or app flavors.

```kotlin
internal class DefaultConfigurationFactory(
    analytics: Analytics,
) : ConfigurationFactory {

    override operator fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.invoke(
        name: String,
    ) {
        configure {
            this.name = name
            debuggable = BuildFlags.debuggable // set up using an expect-actual
            actionShareBehavior = ActionShareBehavior.Distribute()
            onOverflow = SUSPEND
            parallelIntents = true
            logger = CustomLogger
        }
        enableLogging()
        enableRemoteDebugging()

        install(analyticsPlugin(analytics)) // custom plugins
    }
}
```

Finally, inject your config:

```kotlin
internal class CounterContainer(
    configuration: ConfigurationFactory,
) : Container<State, Intent, Action> {

    override val store = store(Loading) {
        configure(configuration, "Counter")
    }
}
```

Setting up injection is covered in the [DI Guide](/integrations/di.md)

## Step 2: Connect the client on Android

:::info

You can skip this step if you don't target Android

:::

On all platforms except Android, we can just use the default host and port for debugging (localhost). But if you
use an external device or an emulator on Android, you need to configure the host yourself.

For emulators, the plugin will use the emulator host by default (`10.0.2.2`). We will need to allow cleartext traffic on
that host and our local network hosts

In your `common-arch` module we created earlier, or in the `app` module, create a network security configuration
**for debug builds only**.

```xml title="app/src/debug/AndroidManifest.xml"
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools">

    <application android:networkSecurityConfig="@xml/network_security_config"
            tools:node="merge">

    </application>

</manifest>
```

```xml title="app/src/debug/res/xml/network_security_config.xml"
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.*</domain>
    </domain-config>
</network-security-config>
```

:::warning

Please don't do this for release builds.

:::

## Step 3.1: Install and run the debugger app for a single device

Either install the IDE plugin by clicking the card on top, or install the desktop app from the Artifacts section of the
repository.
You can find the latest archive on the [releases](https://github.com/respawn-app/FlowMVI/releases) page on GitHub.

Run the debugger. The panel will ask you to configure host and port. Unless you are using a physical external device,
you can just use the defaults. Your devices must be on the same network to connect successfully.

![setup.png](/debugger_setup_1.png)

Run the server and the client, or click the panel icon in the IDE.
After a few seconds, your devices should connect and you can start debugging.

## Step 3.2 External device configuration

If you are connected to an external device via ADB that is on the same network, you can set up the debugger to work with
that.
The setup is a little bit more complicated, but in short, it involves:

1. Assign a static IP address to both your PC and development device on your Wi-Fi network for convenience.
2. Use the IP address of your PC as the host address when running the debugger plugin.
3. Provide the IP address of the PC to the debugger store plugin (in the code) to let it know to which address to connect to using the
   plugin parameters.
4. Make sure the debugging port you are using is open on both devices.

## Step 4: Visualizing Metrics (Optional)

The debugger can also display [metrics](/plugins/metrics.md) collected from your stores in real-time.
This gives you insight into store performance characteristics like intent throughput, state transition latency,
queue times, and subscription counts directly in the IDE plugin or desktop app.

### 4.1 Add the metrics dependency

```toml
flowmvi-metrics = { module = "pro.respawn.flowmvi:metrics", version.ref = "flowmvi" }
```

```kotlin
commonMainImplementation("pro.respawn.flowmvi:metrics:<version>")
```

### 4.2 Configure metrics collection with DebuggerSink

Use `DebuggerSink` to send metrics to the debugger. You can combine it with other sinks using `CompositeSink`:

```kotlin
import pro.respawn.flowmvi.debugger.plugin.DebuggerSink
import pro.respawn.flowmvi.metrics.CompositeSink
import pro.respawn.flowmvi.metrics.LoggingJsonMetricsSink
import pro.respawn.flowmvi.metrics.dsl.collectMetrics
import pro.respawn.flowmvi.metrics.dsl.reportMetrics

val store = store(Initial) {
    val metrics = collectMetrics(reportingScope = applicationScope)
    reportMetrics(
        metrics = metrics,
        interval = 10.seconds,
        sink = CompositeSink(
            LoggingJsonMetricsSink(json, tag = name), // optional: also log to console
            DebuggerSink { e -> logger.error(e) },    // send to debugger
        ),
    )
    // ... other plugins
}
```

::::tip[Platform-specific setup]
Like `remoteDebugger()`, you should use expect/actual declarations to provide `DebuggerSink` only in debug builds
and a no-op sink (like `NoopSink`) in release builds.
::::

Once configured, the debugger will display metrics for each connected store alongside the event timeline,
allowing you to correlate performance data with specific intents and state changes.

For more details on metrics collection and available sinks, see the [Metrics Plugin](/plugins/metrics.md) documentation.
