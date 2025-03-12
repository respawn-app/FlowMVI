---
sidebar_position: 4
sidebar_label: Dependency Injection
---

# Dependency Injection

DI is mostly out of scope of the library, however, setting up your own injection is very easy and can be done
with a single file because Containers and Stores are just like any other dependency: they can be injected as factories
or scoped. The only problem is with injecting Stores in a way that survives config changes in Android and follows
navigation graph lifecycle, which is solved below.

To inject a store that survives configuration changes, we can use (now multiplatform) `androidx.viewmodel` setup.
Jetpack navigation and DI frameworks already provide everything we need. If you are using a different navigation
library, your setup will be different, just make sure that the Stores are actually scoped to destinations and survive
configuration changes.

:::info

The examples below use the `Container` interface for convenience, but if you don't, your setup will be a bit different,
with an added manual call to `store.start()` upon creation of your ViewModel.

:::

## Koin 4.1.x

We only need 2 functions - one to declare, and another to inject the container:

```kotlin
@FlowMVIDSL
inline fun <reified T : Container<S, I, A>, S : MVIState, I : MVIIntent, A : MVIAction> Module.container(
    crossinline definition: Definition<T>,
) = viewModel(qualifier<T>()) { params ->
    ContainerViewModel<T, _, _, _>(container = definition(params))
}

@FlowMVIDSL
@NonRestartableComposable
@Composable
inline fun <reified T : Container<S, I, A>, S : MVIState, I : MVIIntent, A : MVIAction> container(
    key: String? = null,
    scope: Scope = currentKoinScope(),
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
    extras: CreationExtras = defaultExtras(viewModelStoreOwner),
    noinline params: ParametersDefinition? = null,
): T = koinViewModel<ContainerViewModel<T, S, I, A>>(
    qualifier = qualifier<T>(),
    parameters = params,
    key = key,
    scope = scope,
    viewModelStoreOwner = viewModelStoreOwner,
    extras = extras
).container
```

And our DI code is now 2 lines:

```kotlin
val accountModule = module {
    container { new(::GoogleSignInContainer) }
    container { new(::SignInContainer) }
}

@Composable
fun SignInScreen(
    email: String,
    container: SignInContainer = container { parametersOf(email) }, // parameters are passed to the container
) {
    // or as a field
    val googleSignIn: GoogleSignInContainer = container()
}
```

:::tip

Specify the return type to avoid defining 4 type parameters of the function manually.

:::

## [Kodein](https://github.com/kosi-libs/Kodein) 7.x

First of all, make sure you set up Kodein as in [docs](https://kosi-libs.org/kodein/7.25/framework/compose.html) and
provide DI using `withDI()`.

Then, with Kodein, the setup is also very simple, albeit a little bit scary-looking:

```kotlin
inline fun <reified T : Container<S, I, A>, S : MVIState, I : MVIIntent, A : MVIAction> DI.Builder.container(
    @BuilderInference crossinline definition: NoArgBindingDI<Any>.() -> T
) = bind<ContainerViewModel<T, S, I, A>>() with provider { ContainerViewModel(definition()) }

@Suppress("INVISIBLE_REFERENCE", "INDENTATION")
@kotlin.internal.LowPriorityInOverloadResolution
inline fun <
        reified T : Container<S, I, A>,
        reified P : Any,
        S : MVIState,
        I : MVIIntent,
        A : MVIAction
        > DI.Builder.container(
    @BuilderInference crossinline definition: BindingDI<Any>.(P) -> T
) = bind<ContainerViewModel<T, S, I, A>>() with factory { params: P -> ContainerViewModel(definition(params)) }

@Composable
@NonRestartableComposable
inline fun <reified T : Container<S, I, A>, S : MVIState, I : MVIIntent, A : MVIAction> container(): T {
    val vm by rememberViewModel<ContainerViewModel<T, S, I, A>>()
    return vm.container
}

@Suppress("INVISIBLE_REFERENCE", "INDENTATION") // put in a separate package to remove the need for this suppress
@kotlin.internal.LowPriorityInOverloadResolution
@NonRestartableComposable
@Composable
inline fun <reified T : Container<S, I, A>, reified P : Any, S : MVIState, I : MVIIntent, A : MVIAction> container(
    param: P,
): T {
    val vm by rememberViewModel<P, ContainerViewModel<T, S, I, A>>(arg = param)
    return vm.container
}
```

Then we can inject things with 2 lines of code:

```kotlin
val accountModule by DI.Module {
    container { new(::GoogleSignInContainer) }
    container { email: String -> new(email, ::SignInContainer) } // added in Kodein 7.26
}

@Composable
fun SignInScreen(
    email: String,
    container: SignInContainer = container(email),
) {
    val googleSignIn: GoogleSignInContainer = container()
}
```

:::info

We need `@kotlin.internal.LowPriorityInOverloadResolution` to resolve the ambiguity between two of our functions.
If we add an annotation, we can specify parameter as shown in the example with `{ param: Int -> }` instead of
specifying all of 5 type arguments, and if we don't, the function will resolve to the no-argument version instead
of showing an error. This is currently a lacking implementation of proper overload resolution in Kotlin.
Also in Kodein 7.26+ a `new()` function with parameter support will be added instead of having to use `instance()`.

:::

## Hilt / Kotlin-inject

Unfortunately the authors are not currently using those libraries, so no examples can be provided.

If you are setting up those, you can join the chat on Slack to receive support.

If you already have a working setup, you can help other people by opening an issue and providing your code to add to
this page.
