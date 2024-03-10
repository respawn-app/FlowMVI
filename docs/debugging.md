# Remote Debugger Setup

FlowMVI 2.5.0 now comes with a remote debugging setup with a dedicated desktop app for Windows and MacOS, and an
(upcoming in alphas) IDE plugin.

## Step 1: Install the plugin on **debug builds only**

!> Don't install the debugger on prod builds! It pollutes your app with unnecessary code, introduces serious security
risks and degrades performance. If possible on your platform, don't include the debugging code in the release build or
use minification/obfuscation to remove the debugging code.

### 1.1 Set up a module for store configurations

To keep the source set structure simple, you can create a separate module for your store configuration logic and then
inject configurations using DI.

First, create a separate module where you'll keep the debug-only store configuration.

```
project_root/
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

```kotlin
// common-arch/build.gradle.kts. 
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
) = install(debuggerPlugin(name ?: "Store"))

// androidRelease -> InstallDebugger.kt
actual fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger() = Unit

// conditional installation for other platforms: 
actual fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger() {
    if (debuggable) enableRemoteDebugging()
}
```

?> As of the date of writing, the Android Studio will not index the `androidRelease` source set correctly, but it _will_
be picked up by the compiler. We'll have to resort to "notepad-style coding" for now unfortunately.

### 1.3 Set up store configuration injection

?> If you're building a small pet project, you may omit this complicated setup and just use conditional
installation if you know the risks you are taking.

Set up config injection using a factory pattern using your DI framework:

```kotlin
interface StoreConfiguration {

    operator fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.invoke(name: String)
}

inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.configure(
    configuration: StoreConfiguration,
    name: String = requireNotNull(nameByType<S>()), // automatically provide store name with reflection if needed
) = with(configuration) {
    invoke(name = name)
}
```

?> You can also use this to inject other plugins, such as the Saved State plugin or your custom plugin.

Now we'll create a configuration factory.
You can create more based on your needs, such as for testing stores and other source sets.

```kotlin
internal class DefaultStoreConfiguration(
    analytics: Analytics,
) : StoreConfiguration {

    override operator fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.invoke(
        name: String,
    ) {
        this.name = name
        debuggable = BuildFlags.debuggable // set up using an expect-actual and BuildConfig.DEBUG
        actionShareBehavior = ActionShareBehavior.Distribute()
        onOverflow = SUSPEND
        parallelIntents = true
        if (debuggable) {
            enableLogging()
            remoteDebugger()
        }

        install(analyticsPlugin(analytics, name)) // custom plugins
    }
}
```

Finally, inject your config:

```kotlin
val commonArchModule = module {
    singleOf(::DefaultStoreConfiguration) bind StoreConfiguration::class
}

// feature-counter module
internal class CounterContainer(
    configuration: StoreConfiguration,
) : Container<State, Intent, Action> {

    override val store = store(Loading) {
        configure(configuration)
    }
}
```

## Step 2: Connect the client on Android

?> You can skip this step if you don't target Android

On all platforms except Android, we can just use the default host and port for debugging (localhost). But if you
use an external device or an emulator on Android, you need to configure the host yourself.

For emulators, the plugin will use the emulator host by default (`10.0.2.2`). We will need to allow cleartext traffic on
that host and our local network hosts

In your `common-arch` module we created earlier, or in the `app` module, create a network security configuration
**for debug builds only**.

In your `app/src/debug/AndroidManifest.xml`:

```xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools">

    <application android:networkSecurityConfig="@xml/network_security_config"
            tools:node="merge">

    </application>

</manifest>
```

In your `app/src/debug/res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.*</domain>
    </domain-config>
</network-security-config>
```

!> Please don't do this for release builds.

## Step 3.1: Install and run the debugger app for a single device

Right now the debugger is packaged as a standalone app, with an IDE plugin upcoming.
Choose a distribution for your platform and install the app like you would do for any other app.
You can find the latest archive on the [releases](https://github.com/respawn-app/FlowMVI/releases) page on GitHub.

Run the debugger app. The app will ask you to configure host and port. Unless you are using a physical external device,
you can just use the defaults. Your devices must be on the same network to connect successfully.

![setup.png](images/debugger_setup_1.png)

Run the server and the app. After a few seconds, your devices should connect and you can start debugging.

![debugger](images/debugger.gif | width=1280)

## Step 3.2 External device configuration

If you are connected to an external device via ADB that is on the same network, you can set up the debugger to work with
that.
The setup is a little bit more complicated, but in short, it involves:

1. Assign a static IP address to both your PC and development device on your Wi-Fi network for convenience.
2. Use the IP address of your PC as the host address when running the debugger app.
3. Provide the IP address of the PC to the debugger plugin to let it know to which address to connect to using the
   plugin parameters.
4. Make sure the debugging port you are using is open on both devices.

## Next Steps

Right now the debugging setup includes only the essentials.

Feel free to create an issue for a feature you want to be added.

You can also check out the debugger
app [implementation](https://github.com/respawn-app/FlowMVI/tree/34236773e21e7138a330d7d0fb6c5d0eba21b61e/debugger/server/src/commonMain/kotlin/pro/respawn/flowmvi/debugger/server)
to see how flowMVI can be used to build multiplatform apps.
