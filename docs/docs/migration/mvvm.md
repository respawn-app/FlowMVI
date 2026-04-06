---
sidebar_position: 1
sidebar_label: From MVVM
---

# Migrating from MVVM to FlowMVI

This guide is for Android and Kotlin Multiplatform developers who use the traditional MVVM pattern
(`ViewModel` + `StateFlow`/`LiveData`) and want to adopt FlowMVI.
If you're starting a new project, you may prefer the [quickstart](../quickstart.md) instead.

## Concept Mapping

| MVVM | FlowMVI | Notes |
|------|---------|-------|
| `ViewModel` | `Container` / `ViewModel` implementing `ImmutableContainer` | You can keep ViewModels or use `ContainerViewModel`  |
| `MutableStateFlow` / `LiveData` | `MVIState` + `updateState {}` | Immutable, copy-based state |
| No direct analog for `StateFlow.value` | `withState {}` | Reads are always inside a transaction, no race-prone direct property access |
| `collectAsStateWithLifecycle` | `store.subscribe()` / Compose `subscribe {}` | `subscribe()` |
| Public ViewModel functions | `MVIIntent` sealed classes or `store.intent {}` lambdas | Choose one style per project |
| `SharedFlow` / `Channel` events | `action()` side effects | Send with `action()`, consume in `subscribe { consume { } }` |
| `init {}` block | `init` plugin | Runs each time the store starts |
| `viewModelScope.launch {}` | `PipelineContext.launch {}` | Structured pipeline |
| `combine()` / `collect()` on flows | `whileSubscribed {}` plugin | Auto-cancels with subscriber lifecycle |
| `try/catch` | `recover {}` plugin | Centralized, composable error handling |

## Before and After: User Profile Feature

Let's migrate a realistic feature that observes a user profile, handles errors, and shows a toast on save.

### Before: Traditional MVVM

```kotlin
class ProfileViewModel(
    private val repo: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeProfile().collect { profile ->
                _state.update { it.copy(profile = profile, isLoading = false) }
            }
        }
    }

    fun saveProfile(name: String) {
        viewModelScope.launch {
            try {
                repo.saveProfile(name)
                _state.update { it.copy(userMessage = "Profile saved") }
            } catch (e: Exception) {
                _state.update { it.copy(userMessage = "Save failed: ${e.message}") }
            }
        }
    }

    // UI must call this after showing the message
    fun userMessageShown() {
        _state.update { it.copy(userMessage = null) }
    }
}

data class ProfileUiState(
    val profile: Profile? = null,
    val isLoading: Boolean = true,
    val userMessage: String? = null, // One-off event reduced to state
)
```

### After: FlowMVI

```kotlin
sealed interface ProfileState : MVIState {
    data object Loading : ProfileState
    data class Error(val message: String) : ProfileState
    data class DisplayingProfile(val profile: Profile) : ProfileState
}

sealed interface ProfileAction : MVIAction {
    data class ShowToast(val message: String) : ProfileAction
}

class ProfileViewModel(
    private val repo: ProfileRepository,
) : ViewModel(), ImmutableContainer<ProfileState, LambdaIntent<ProfileState, ProfileAction>, ProfileAction> {

    override val store by lazyStore(
        initial = ProfileState.Loading,
        scope = viewModelScope,
    ) {
        configure {
            debuggable = BuildFlags.debuggable
            name = "ProfileStore"
        }

        recover { e ->
            updateState { ProfileState.Error(e.message ?: "Unknown error") }
            null
        }

        whileSubscribed {
            repo.observeProfile().collect { profile ->
                updateState { ProfileState.DisplayingProfile(profile) }
            }
        }

        reduceLambdas()
    }

    fun saveProfile(name: String) = store.intent {
        repo.saveProfile(name)
        action(ProfileAction.ShowToast("Profile saved"))
    }
}
```

## UI Layer Migration

### Compose

**Before** — collecting state and consuming events-as-state manually:

```kotlin
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.userMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.userMessageShown()
        }
    }

    if (state.isLoading) {
        CircularProgressIndicator()
    } else {
        state.profile?.let { ProfileContent(it, onSave = viewModel::saveProfile) }
    }
}
```

**After** — using FlowMVI's `subscribe`:

```kotlin
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) = with(viewModel.store) {
    val state by subscribe { action ->
        when (action) {
            is ShowToast -> { /* show snackbar */ }
        }
    }

    when (state) {
        is Loading -> CircularProgressIndicator()
        is DisplayingProfile -> ProfileContent(state.profile, onSave = viewModel::saveProfile)
        is Error -> ErrorContent(state.message)
    }
}
```

`subscribe` handles both state observation and action consumption in one call. It automatically respects
the composition lifecycle — no `LaunchedEffect` needed.

For best practices on extracting pure content composables, Compose compiler stability, and previews,
see the [Compose guide](../integrations/compose.md).

:::tip[Compose Multiplatform]

The `subscribe` API and ViewModel logic work identically on all Compose Multiplatform targets —
Android, iOS, Desktop, and Web. The code above runs on all platforms without changes.

:::

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
    private val viewModel: ProfileViewModel by viewModels()
    private val binding by viewBinding<ProfileFragmentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(viewModel.store, ::consume, ::render)
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

### 2. Use MVVM+ with your existing ViewModels

The example above already shows this — implement `ImmutableContainer` on your ViewModel,
delegate to a `lazyStore`, and expose functions via `store.intent {}`. Your DI, navigation, and
lifecycle integration stay unchanged. The only UI change is calling `subscribe` instead of
`collectAsStateWithLifecycle`.

See the [Android guide](../integrations/android.md) for the full pattern and
the [DI guide](../integrations/di.md) for Koin and Kodein setup.

### 3. Graduate to sealed intents when needed

MVVM+ (lambda intents) works well for simple screens. Consider switching to sealed class intents when:

- You need exhaustive `when` handling (compiler-checked)
- Multiple UI surfaces dispatch the same intents
- You want full logging/debugging visibility — lambda intents log as `LambdaIntent`, not descriptive names
- You are using Compose — MVVM-style `viewModel.doThis()` function references capture state and
  are inherently unstable for the Compose compiler, causing unnecessary recompositions

### 4. Add plugins incrementally

Start with `recover`, `whileSubscribed`, and `reduceLambdas` — then layer in more as needed:

- `whileSubscribed` for reactive data streams — use this from day one
- `enableLogging()` — set this up once in your shared DI configuration, not per-store
- `saveState` / `parcelizeState` for [state persistence](../state/savedstate.md)

:::tip

Once comfortable, you can extract store logic out of ViewModels into standalone `Container` classes.
See the [Android guide](../integrations/android.md) for `StoreViewModel` examples.

:::

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

**Problem:** FlowMVI stores have their own lifecycle and concurrency constraints, which require
specific setup in tests.

**Solution:** Create a store with your test dependencies, then use `subscribeAndTest` — it handles
start, subscription, and cleanup:

```kotlin
val store = store(ProfileState.Loading) {
    recover { e -> updateState { ProfileState.Error(e.message ?: "Unknown") }; null }
    // configure plugins under test
}

store.subscribeAndTest {
    states.test {
        awaitItem() shouldBe Loading
        awaitItem() shouldBe DisplayingProfile(expectedProfile)
    }
}
```

See the [testing guide](../integrations/testing.md) for the full API and assertion patterns.

### Store lifecycle and scoping

**Problem:** "When does the store start and stop?"

**Solution:** The store lifecycle matches whatever `CoroutineScope` you provide. When using
`viewModelScope`, the store lives as long as the ViewModel — identical behavior to what you already have.
You can also start/stop stores manually for finer control.

## Next Steps

- [Quickstart](../quickstart.md) — full setup walkthrough
- [Plugins](../plugins/prebuilt.md) — explore built-in plugins
- [Compose Integration](../integrations/compose.md) — Compose best practices
- [Android Integration](../integrations/android.md) — ViewModel patterns and View-based UI
- [Test Harness](../integrations/testing.md) — testing DSL and patterns
- [Saved State](../state/savedstate.md) — state persistence across platforms
