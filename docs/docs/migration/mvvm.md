---
sidebar_position: 1
sidebar_label: From MVVM
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Migrating from MVVM to FlowMVI

This guide is for Android and Kotlin Multiplatform developers who use the traditional MVVM pattern
(`ViewModel` + `StateFlow`/`LiveData`) and want to adopt FlowMVI.
If you're starting a new project, you may prefer the [quickstart](../quickstart.md) instead.

## Concept Mapping

| MVVM | FlowMVI | Notes |
|------|---------|-------|
| `ViewModel` | `Container` / `ViewModel` implementing `ImmutableContainer` | You can keep your ViewModels on Android |
| `MutableStateFlow` / `LiveData` | `MVIState` + `updateState {}` | Immutable, copy-based state |
| `StateFlow.value` | `withState {}` | Thread-safe read access |
| Exposed `StateFlow` | `store.subscribe()` / Compose `subscribe {}` | Lifecycle-aware |
| Public ViewModel functions | `MVIIntent` sealed classes or `store.intent {}` lambdas | Choose one style per project |
| `SharedFlow` / `Channel` events | `MVIAction` side effects | Guaranteed delivery with `Distribute()` |
| `init {}` block | `init` plugin | Runs each time the store starts |
| `viewModelScope.launch {}` | Logic in `reduce {}`, `init {}`, `whileSubscribed {}` | Structured pipeline |
| `combine()` / `collect()` on flows | `whileSubscribed {}` plugin | Auto-cancels with subscriber lifecycle |
| `try/catch` | `recover {}` plugin | Centralized, composable error handling |

## Before and After: User Profile Feature

Let's migrate a realistic feature that loads a user profile, handles errors, supports refresh, and shows a toast on save.

### Before: Traditional MVVM

```kotlin
class ProfileViewModel(
    private val repo: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = ProfileState.Loading
            try {
                val profile = repo.getProfile()
                _state.value = ProfileState.DisplayingProfile(profile)
            } catch (e: Exception) {
                _state.value = ProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveProfile(name: String) {
        viewModelScope.launch {
            try {
                repo.saveProfile(name)
                _events.send(ProfileEvent.ShowToast("Profile saved"))
            } catch (e: Exception) {
                _events.send(ProfileEvent.ShowToast("Save failed: ${e.message}"))
            }
        }
    }
}

sealed interface ProfileState {
    data object Loading : ProfileState
    data class Error(val message: String) : ProfileState
    data class DisplayingProfile(val profile: Profile) : ProfileState
}

sealed interface ProfileEvent {
    data class ShowToast(val message: String) : ProfileEvent
}
```

### After: FlowMVI

<Tabs>
  <TabItem value="mvi" label="MVI Style" default>

```kotlin
// Contract
sealed interface ProfileState : MVIState {
    data object Loading : ProfileState
    data class Error(val message: String) : ProfileState
    data class DisplayingProfile(val profile: Profile) : ProfileState
}

sealed interface ProfileIntent : MVIIntent {
    data object ClickedLoad : ProfileIntent
    data class ClickedSave(val name: String) : ProfileIntent
}

sealed interface ProfileAction : MVIAction {
    data class ShowToast(val message: String) : ProfileAction
}

// Container
class ProfileContainer(
    private val repo: ProfileRepository,
) : Container<ProfileState, ProfileIntent, ProfileAction> {

    override val store = store(initial = ProfileState.Loading) {
        configure {
            debuggable = BuildFlags.debuggable
            name = "ProfileStore"
        }

        recover { e ->
            updateState { ProfileState.Error(e.message ?: "Unknown error") }
            null
        }

        init {
            loadProfile()
        }

        reduce { intent ->
            when (intent) {
                is ClickedLoad -> loadProfile()
                is ClickedSave -> saveProfile(intent.name)
            }
        }
    }

    private suspend fun PipelineContext<ProfileState, ProfileIntent, ProfileAction>.loadProfile() {
        updateState { ProfileState.Loading }
        val profile = repo.getProfile()  // errors caught by recover {}
        updateState { ProfileState.DisplayingProfile(profile) }
    }

    private suspend fun PipelineContext<ProfileState, ProfileIntent, ProfileAction>.saveProfile(name: String) {
        repo.saveProfile(name)  // errors caught by recover {}
        action(ProfileAction.ShowToast("Profile saved"))
    }
}
```

:::info[What is PipelineContext?]

`PipelineContext` is the receiver that gives your functions access to `updateState`, `action`, `intent`,
and the store's `CoroutineScope`. Using it as a receiver (instead of injecting the store) keeps your
business logic decoupled from any specific store instance.

:::

  </TabItem>
  <TabItem value="mvvmplus" label="MVVM+ Style">

```kotlin
// Contract (same states and actions, but no intent classes needed)
sealed interface ProfileState : MVIState {
    data object Loading : ProfileState
    data class Error(val message: String) : ProfileState
    data class DisplayingProfile(val profile: Profile) : ProfileState
}

sealed interface ProfileAction : MVIAction {
    data class ShowToast(val message: String) : ProfileAction
}

// Container using lambda intents
class ProfileContainer(
    private val repo: ProfileRepository,
) : ImmutableContainer<ProfileState, LambdaIntent<ProfileState, ProfileAction>, ProfileAction> {

    override val store = store(initial = ProfileState.Loading) {
        configure {
            debuggable = BuildFlags.debuggable
            name = "ProfileStore"
        }

        recover { e ->
            updateState { ProfileState.Error(e.message ?: "Unknown error") }
            null
        }

        init {
            val profile = repo.getProfile()
            updateState { ProfileState.DisplayingProfile(profile) }
        }

        reduceLambdas()
    }

    fun loadProfile() = store.intent {
        updateState { ProfileState.Loading }
        val profile = repo.getProfile()
        updateState { ProfileState.DisplayingProfile(profile) }
    }

    fun saveProfile(name: String) = store.intent {
        repo.saveProfile(name)
        action(ProfileAction.ShowToast("Profile saved"))
    }
}
```

  </TabItem>
</Tabs>

:::tip[What changed?]

- **Error handling** moved from `try/catch` in every function to a single `recover {}` plugin
- **Events** changed from a manually managed `Channel` to type-safe `MVIAction`s with guaranteed delivery
- **Coroutine management** moved from manual `viewModelScope.launch {}` to the store's structured pipeline
- **State updates** are thread-safe by default — no need to worry about concurrent writes

:::

## UI Layer Migration

### Compose

**Before** — collecting state and events manually:

```kotlin
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ShowToast -> { /* show snackbar */ }
            }
        }
    }

    when (state) {
        is Loading -> CircularProgressIndicator()
        is Error -> ErrorContent(state.message, onRetry = viewModel::loadProfile)
        is DisplayingProfile -> ProfileContent(state.profile, onSave = viewModel::saveProfile)
    }
}
```

**After** — using FlowMVI's `subscribe`:

```kotlin
@Composable
fun ProfileScreen(container: ProfileContainer) = with(container.store) {
    val state by subscribe { action ->
        when (action) {
            is ShowToast -> { /* show snackbar */ }
        }
    }

    when (state) {
        is Loading -> CircularProgressIndicator()
        is Error -> ErrorContent(state.message, onRetry = { intent(ClickedLoad) })
        is DisplayingProfile -> ProfileContent(state.profile, onSave = { intent(ClickedSave(it)) })
    }
}
```

`subscribe` handles both state observation and action consumption in one call. It automatically respects
the composition lifecycle — no `LaunchedEffect` needed.

For best practices on extracting pure content composables, Compose compiler stability, and previews,
see the [Compose guide](../integrations/compose.md).

<details>
<summary>Compose Multiplatform (KMP)</summary>

The `subscribe` API works identically on all Compose Multiplatform targets — Android, iOS, Desktop, and Web.
Your Container is defined in `commonMain`, and the same composable works everywhere:

```kotlin
// commonMain — this composable runs on all platforms
@Composable
fun ProfileScreen(container: ProfileContainer) = with(container.store) {
    val state by subscribe { action ->
        when (action) {
            is ShowToast -> { /* platform-specific snackbar */ }
        }
    }

    when (state) {
        is Loading -> CircularProgressIndicator()
        is Error -> ErrorContent(state.message, onRetry = { intent(ClickedLoad) })
        is DisplayingProfile -> ProfileContent(state.profile, onSave = { intent(ClickedSave(it)) })
    }
}
```

:::tip

For iOS targets, Compose Multiplatform is the recommended UI integration path — FlowMVI does not
provide a native SwiftUI bridge. The Container and store logic are fully multiplatform; only platform
entry points differ. See the [Compose guide](../integrations/compose.md) for stability configuration.

:::

</details>

<details>
<summary>Android Views</summary>

**Before:**

```kotlin
class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by viewModels()
    private val binding by viewBinding<ProfileFragmentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }
}
```

**After:**

```kotlin
class ProfileFragment : Fragment() {
    private val container: ProfileContainer by container()
    private val binding by viewBinding<ProfileFragmentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(container, ::consume, ::render)
    }

    private fun render(state: ProfileState) { /* update all views */ }
    private fun consume(action: ProfileAction) { /* handle side effects */ }
}
```

One call to `subscribe` replaces all the `repeatOnLifecycle` boilerplate.
See the [Android guide](../integrations/android.md) for the full setup.

</details>

## Incremental Adoption Strategy

You don't have to migrate everything at once. Here's a practical path:

### 1. Start with one feature

Pick a simple, self-contained screen with clear state transitions. A settings page or a detail screen
works well. Avoid complex screens with pagination or deep navigation for the first migration.

### 2. Keep your ViewModels

You can use FlowMVI inside existing Android ViewModels. Implement `ImmutableContainer` and delegate to a store:

```kotlin
class ProfileViewModel(
    repo: ProfileRepository,
) : ViewModel(), ImmutableContainer<ProfileState, ProfileIntent, ProfileAction> {

    override val store by lazyStore(
        initial = ProfileState.Loading,
        scope = viewModelScope,
    ) {
        configure { debuggable = BuildConfig.DEBUG }
        reduceLambdas()
    }

    fun loadProfile() = store.intent { /* ... */ }
}
```

This approach keeps your DI, navigation, and lifecycle integration unchanged.
See the [Android guide](../integrations/android.md) for the full pattern.

:::tip

Containers and Stores are regular dependencies — they work with any DI framework.
For Koin and Kodein setup with `ContainerViewModel`, see the [DI guide](../integrations/di.md).

:::

### 3. Adopt MVVM+ style first

If your team is used to calling ViewModel functions from the UI, the MVVM+ (lambda intent) style is the
smallest conceptual shift. Your UI code barely changes — just swap `viewModel.doThing()` for
`container.doThing()`.

**Use MVVM+ (lambda intents) when:**
- Migrating incrementally and you want minimal UI changes
- The feature has simple, linear intent flow
- Your team prefers calling functions over dispatching sealed classes

**Graduate to MVI style when:**
- You need exhaustive `when` handling for intents (compiler-checked)
- Multiple UI surfaces dispatch the same intents
- You want full logging/debugging visibility — lambda intents log as `LambdaIntent`, not descriptive names
- You are using Compose — lambda intents are unstable for the Compose compiler

:::warning

`LambdaIntent` is a value class wrapping a lambda, which makes it inherently unstable for the Compose
compiler. If you are using Compose, prefer the MVI style with sealed class intents for better
recomposition performance. See the [Compose guide](../integrations/compose.md) for details.

:::

### 4. Extract Containers gradually

Once comfortable, move store logic out of ViewModels into standalone `Container` classes. Use
`StoreViewModel` delegation to keep the ViewModel as a thin Android lifecycle wrapper.
See the [Android guide](../integrations/android.md) for `StoreViewModel` examples.

### 5. Add plugins incrementally

Start with the essentials — `reduce`, `recover`, `init` — then layer in more as needed:

- `enableLogging()` for debug visibility
- `saveState` / `parcelizeState` for [state persistence](../state/savedstate.md)
- `whileSubscribed` for reactive data streams
- `collectMetrics()` for performance tracking

## Common Challenges

### Side effect delivery

**Problem:** `Channel`-based events can be lost if collected too late or by the wrong subscriber.

**Solution:** FlowMVI uses `ActionShareBehavior.Distribute()` by default, which queues actions in a fan-out
FIFO fashion. Actions wait for a subscriber — they are not dropped.

### Threading and concurrency

**Problem:** Manually switching dispatchers with `Dispatchers.IO` and protecting `MutableStateFlow` from races.

**Solution:** `updateState {}` is thread-safe by default (using `Atomic` state strategy). For dispatcher
overrides, set `coroutineContext` in `configure {}` once:

```kotlin
configure {
    coroutineContext = Dispatchers.Default
}
```

### State restoration

**Problem:** Wiring `SavedStateHandle` manually in every ViewModel.

**Solution:** One plugin call:

```kotlin
parcelizeState(handle) // Android
// or
serializeState(path, serializer) // KMP
```

See the [saved state guide](../state/savedstate.md) for details.

### Testing

**Problem:** Manually constructing ViewModels, injecting mocks, and collecting state with Turbine.

**Before** — MVVM test with manual setup:

```kotlin
@Test
fun `loads profile on init`() = runTest {
    val repo = FakeProfileRepository(expectedProfile)
    val viewModel = ProfileViewModel(repo)

    viewModel.state.test {
        awaitItem() shouldBe Loading
        awaitItem() shouldBe DisplayingProfile(expectedProfile)
    }
}
```

**After** — FlowMVI's `subscribeAndTest` handles store start, subscription, and cleanup:

```kotlin
@Test
fun `loads profile on init`() = runTest {
    store.subscribeAndTest {
        states.test {
            awaitItem() shouldBe Loading
            awaitItem() shouldBe DisplayingProfile(expectedProfile)
        }
    }
}
```

No manual lifecycle management — `subscribeAndTest` starts the store, subscribes, runs your
assertions, then stops and cleans up. You can also send intents and assert on actions:

```kotlin
store.subscribeAndTest {
    intent(ClickedSave("New Name"))
    actions.test {
        awaitItem() shouldBe ShowToast("Profile saved")
    }
}
```

See the [test harness guide](../integrations/testing.md) for the full API.

### Store lifecycle and scoping

**Problem:** "When does the store start and stop?"

**Solution:** The store lifecycle matches whatever `CoroutineScope` you provide. When using
`viewModelScope`, the store lives as long as the ViewModel — identical behavior to what you already have.
You can also start/stop stores manually for finer control.

## What You Gain

- **Centralized error handling** — one `recover {}` replaces scattered `try/catch` blocks
- **Built-in logging and debugging** — `enableLogging()` plus the [remote debugger](../plugins/debugging.md)
- **KMP readiness** — Containers are multiplatform; only the ViewModel wrapper is Android-specific
- **Structured testability** — `subscribeAndTest {}` with no manual lifecycle management
- **One-line state persistence** — `parcelizeState()` or `serializeState()` replaces manual `SavedStateHandle` code
- **Compose performance** — stable state types with proper `subscribe` integration
- **Plugin composability** — add logging, metrics, undo/redo, and more without changing business logic

## Next Steps

- [Quickstart](../quickstart.md) — full setup walkthrough
- [Plugins](../plugins/prebuilt.md) — explore built-in plugins
- [Compose Integration](../integrations/compose.md) — Compose best practices
- [Android Integration](../integrations/android.md) — ViewModel patterns and View-based UI
- [Test Harness](../integrations/testing.md) — testing DSL and patterns
- [Saved State](../state/savedstate.md) — state persistence across platforms
