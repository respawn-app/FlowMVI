# Implementation Plan: FSM Transitions Sample Screens

## Overview

Add 3 new sample screens to the FlowMVI sample app that showcase the FSM transitions plugin API. Each screen demonstrates a progressively more advanced usage pattern:

1. **Transitions** — Basic FSM transitions with an auth flow (replacement for `reduce {}`)
2. **Scoped Compose** — State-scoped child store composition (subscriptions active only in specific parent states)
3. **Top-Level Compose** — Always-active child store composition with a data class parent state

These screens teach developers how to use `transitions {}`, `state<T>`, `on<Intent>`, `transitionTo`, and `compose()` — the key FSM APIs in FlowMVI.

## Requirements

### Validated from codebase investigation:
- Sample app uses Decompose for navigation, Koin for DI, Compose Multiplatform for UI
- Each feature follows: Models (`*Models.kt`) → Container (`*Container.kt`) → Screen (`*Screen.kt`)
- Features registered in `FeatureModule.kt`, enum in `Destination.kt`, routed in `Destinations.kt`
- Navigation is via `AppNavigator` interface + `AppNavigatorImpl` class
- Home screen links defined in `HomeFeature` enum + title/icon/onClick mappings in `HomeScreen.kt`
- Container pattern: `Container<S, I, A>` with `override val store`
- Screens use `container<ContainerType, _, _, _>()` for scoped Koin injection
- Screens have Description text, CodeText with code snippet, and interactive UI
- String resources in `values/strings.xml`
- Icons are ImageVector extensions on `Icons` object in `ui/icons/`

### API availability confirmed:
- `transitions {}` builder with `state<T> {}` and `on<Intent> {}` — in `TransitionsPlugin.kt`
- `TransitionScope` with `state: T`, `transitionTo()`, full `PipelineContext` delegation — in `TransitionScope.kt`
- Top-level `compose(store, merge, consume)` — in `TransitionsBuilder.kt`
- State-scoped `compose(store, merge, consume)` — in `StateTransitionsBuilder`
- `childStorePlugin` auto-manages child store lifecycle — in `ComposePlugin.kt`

## Technical Approach

### Architecture Decisions
- Each screen is a standalone feature package under `pro.respawn.flowmvi.sample.features.*`
- Follow exact Container/Screen patterns from existing features (SST, LCE, Progressive)
- Repositories are simple classes with `delay()` to simulate network calls + `Random` for failures
- Child stores for compose screens are created as properties in the Container class (following Progressive pattern)
- Use `transitions {}` as the sole intent handler (no `reduce {}`) in all 3 screens
- Use `configure(configuration, "StoreName")` for store configuration consistency

### Icons
- Need 3 new icons. Recommended:
  - **Transitions**: `SwapHoriz` (material icon — horizontal swap, represents state transitions)
  - **Scoped Compose**: `FilterList` (material icon — filtered/scoped composition)
  - **Top-Level Compose**: `Dashboard` (material icon — always-visible dashboard)
- Alternative: reuse existing icons (`AccountTree` for transitions, `Layers` for scoped, `Refresh` for top-level) to avoid adding new icons. Trade-off: less distinct in the menu.
- **Decision**: Add 3 new icon files following the existing ImageVector pattern in `ui/icons/`.

---

## Tasks

### Phase 1: Shared Infrastructure

#### Task 1.1: Add Icon Files
- **Description**: Create 3 new Material icon ImageVector files
- **Files to create**:
  - `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/ui/icons/SwapHoriz.kt`
  - `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/ui/icons/FilterList.kt`
  - `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/ui/icons/Dashboard.kt`
- **Pattern**: Follow existing icon files (e.g., `AccountTree.kt`). Each defines an extension property on `Icons` returning an `ImageVector` built via `materialIcon { materialPath { ... } }`.
- **Acceptance criteria**: Icons compile and are accessible via `Icons.SwapHoriz`, `Icons.FilterList`, `Icons.Dashboard`

#### Task 1.2: Add String Resources
- **Description**: Add string resources for all 3 features
- **Files to modify**: `sample/src/commonMain/composeResources/values/strings.xml`
- **Strings to add**:
  ```xml
  <!-- Transitions feature -->
  <string name="transitions_feature_title">FSM Transitions</string>
  <string name="transitions_login_button">Log In</string>
  <string name="transitions_logout_button">Log Out</string>
  <string name="transitions_retry_button">Retry</string>
  <string name="transitions_username_label">Username</string>
  <string name="transitions_password_label">Password</string>
  <string name="transitions_authenticating_label">Authenticating…</string>
  <string name="transitions_welcome_label">Welcome, %1$s!</string>
  <string name="transitions_error_snackbar">Authentication failed: %1$s</string>
  
  <!-- Scoped Compose feature -->
  <string name="scoped_compose_feature_title">Scoped Compose</string>
  <string name="scoped_compose_loading_label">Loading dashboard…</string>
  <string name="scoped_compose_feed_title">Feed</string>
  <string name="scoped_compose_notifications_title">Notifications</string>
  <string name="scoped_compose_retry_button">Retry</string>
  <string name="scoped_compose_refresh_button">Refresh</string>
  <string name="scoped_compose_error_label">Something went wrong: %1$s</string>
  
  <!-- Top-Level Compose feature -->
  <string name="toplevel_compose_feature_title">Top-Level Compose</string>
  <string name="toplevel_compose_weather_title">Weather</string>
  <string name="toplevel_compose_clock_title">Clock</string>
  <string name="toplevel_compose_refresh_button">Refresh Weather</string>
  <string name="toplevel_compose_loading_label">Loading…</string>
  ```
- **Acceptance criteria**: All string resources resolve correctly via `Res.string.*`

#### Task 1.3: Add Destination Enum Entries
- **Description**: Register the 3 new destinations in the navigation enum
- **Files to modify**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/navigation/destination/Destination.kt`
- **Code**: Add before `Info`:
  ```kotlin
  Transitions("transitions"),
  ScopedCompose("scopedcompose"),
  TopLevelCompose("toplevelcompose"),
  ```
- **Acceptance criteria**: Enum entries exist with correct route strings

#### Task 1.4: Add AppNavigator Methods
- **Description**: Add navigation methods to the navigator interface and implementation
- **Files to modify**:
  - `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/navigation/AppNavigator.kt` — add 3 interface methods
  - `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/navigation/AppNavigatorImpl.kt` — implement 3 methods
- **Code** (interface):
  ```kotlin
  fun transitionsFeature()
  fun scopedComposeFeature()
  fun topLevelComposeFeature()
  ```
- **Code** (implementation):
  ```kotlin
  override fun transitionsFeature() = navigate(Destination.Transitions)
  override fun scopedComposeFeature() = navigate(Destination.ScopedCompose)
  override fun topLevelComposeFeature() = navigate(Destination.TopLevelCompose)
  ```
- **Acceptance criteria**: Navigator compiles, methods navigate to correct destinations

#### Task 1.5: Add HomeFeature Entries
- **Description**: Register the 3 features in the home screen menu
- **Files to modify**:
  - `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/home/HomeModels.kt` — add enum entries to `HomeFeature`
  - `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/home/HomeScreen.kt` — add title/icon/onClick mappings
- **Code** (HomeModels.kt — add to `HomeFeature` enum):
  ```kotlin
  Transitions, ScopedCompose, TopLevelCompose
  ```
  Add before `XmlViews` (so platform-specific features stay at the end).
- **Code** (HomeScreen.kt — add imports and mappings):
  ```kotlin
  // In GoToFeature action handler:
  HomeFeature.Transitions -> navigator.transitionsFeature()
  HomeFeature.ScopedCompose -> navigator.scopedComposeFeature()
  HomeFeature.TopLevelCompose -> navigator.topLevelComposeFeature()
  
  // In title mapping:
  Transitions -> Res.string.transitions_feature_title
  ScopedCompose -> Res.string.scoped_compose_feature_title
  TopLevelCompose -> Res.string.toplevel_compose_feature_title
  
  // In icon mapping:
  Transitions -> Icons.SwapHoriz
  ScopedCompose -> Icons.FilterList
  TopLevelCompose -> Icons.Dashboard
  ```
- **Acceptance criteria**: 3 new items appear in the home screen menu with correct titles and icons

#### Task 1.6: Add Routing in Destinations.kt
- **Description**: Route destination enum entries to screen composables
- **Files to modify**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/navigation/destination/Destinations.kt`
- **Code** (add to `when` block):
  ```kotlin
  Destination.Transitions -> TransitionsScreen(navigator)
  Destination.ScopedCompose -> ScopedComposeScreen(navigator)
  Destination.TopLevelCompose -> TopLevelComposeScreen(navigator)
  ```
- **Note**: Imports for the screen composables will be added once the screen files are created
- **Acceptance criteria**: Navigation routes compile and display the correct screens

---

### Phase 2: Screen 1 — Transitions (Auth Flow)

#### Task 2.1: Create AuthRepository
- **Description**: Fake authentication repository with simulated delays and random failures
- **File to create**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/transitions/AuthRepository.kt`
- **Package**: `pro.respawn.flowmvi.sample.features.transitions`
- **Key code**:
  ```kotlin
  internal class AuthRepository {
      suspend fun authenticate(username: String, password: String): Result<String> {
          delay(2.seconds)
          return if (Random.nextFloat() < 0.3f) {
              Result.failure(IllegalStateException("Invalid credentials"))
          } else {
              Result.success(username)
          }
      }
  }
  ```
- **Acceptance criteria**: `authenticate()` returns success ~70% of the time after 2s delay

#### Task 2.2: Create TransitionsModels.kt
- **Description**: Define State, Intent, and Action sealed interfaces for the auth flow
- **File to create**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/transitions/TransitionsModels.kt`
- **Package**: `pro.respawn.flowmvi.sample.features.transitions`
- **Key code**:
  ```kotlin
  internal sealed interface TransitionsState : MVIState {
      data class Login(val username: String = "", val password: String = "") : TransitionsState
      data object Authenticating : TransitionsState
      data class Authenticated(val username: String) : TransitionsState
      data class Error(val message: String) : TransitionsState
  }

  internal sealed interface TransitionsIntent : MVIIntent {
      data class UpdateUsername(val value: String) : TransitionsIntent
      data class UpdatePassword(val value: String) : TransitionsIntent
      data object ClickedLogin : TransitionsIntent
      data object ClickedRetry : TransitionsIntent
      data object ClickedLogout : TransitionsIntent
  }

  internal sealed interface TransitionsAction : MVIAction {
      data class ShowError(val message: String) : TransitionsAction
  }
  ```
- **Acceptance criteria**: All model types compile, `@Immutable` annotated on sealed interfaces

#### Task 2.3: Create TransitionsContainer.kt
- **Description**: Container using `transitions {}` as a full replacement for `reduce {}`
- **File to create**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/transitions/TransitionsContainer.kt`
- **Package**: `pro.respawn.flowmvi.sample.features.transitions`
- **Key code**:
  ```kotlin
  internal class TransitionsContainer(
      private val repo: AuthRepository,
      configuration: ConfigurationFactory,
  ) : Container<TransitionsState, TransitionsIntent, TransitionsAction> {

      override val store = store(TransitionsState.Login()) {
          configure(configuration, "TransitionsStore")
          
          transitions {
              state<TransitionsState.Login> {
                  on<TransitionsIntent.UpdateUsername> {
                      transitionTo(state.copy(username = it.value))
                  }
                  on<TransitionsIntent.UpdatePassword> {
                      transitionTo(state.copy(password = it.value))
                  }
                  on<TransitionsIntent.ClickedLogin> {
                      val (username, password) = state
                      transitionTo(TransitionsState.Authenticating)
                      launch {
                          repo.authenticate(username, password)
                              .onSuccess { transitionTo(TransitionsState.Authenticated(it)) }
                              .onFailure {
                                  action(TransitionsAction.ShowError(it.message ?: "Unknown error"))
                                  transitionTo(TransitionsState.Error(it.message ?: "Unknown error"))
                              }
                      }
                  }
              }
              state<TransitionsState.Error> {
                  on<TransitionsIntent.ClickedRetry> {
                      transitionTo(TransitionsState.Login())
                  }
              }
              state<TransitionsState.Authenticated> {
                  on<TransitionsIntent.ClickedLogout> {
                      transitionTo(TransitionsState.Login())
                  }
              }
          }
      }
  }
  ```
- **Showcases**: `transitions {}` replacing `reduce {}`, typed `state`, `transitionTo`, `launch {}`, `action()`, state-typed `on<>` handlers
- **Note**: `transitionTo` inside `launch {}` — the handler runs inside the FSM depth tracker's scope. The `launch {}` exits the tracker, but `updateState` (called by `transitionTo`) is unconstrained inside child coroutines because the `launch {}` is dispatched from within a handler. Actually, the `transitionTo` call in the launched coroutine will trigger `onState` enforcement. Since it's outside the handler depth tracker, it will be vetoed. **Correction**: The `launch {}` approach needs to use `updateState` directly rather than `transitionTo` from within a child coroutine, since the depth tracker won't cover it. Use `updateState { TransitionsState.Authenticated(it) }` inside launch instead. Alternatively, restructure so the async work completes and then re-emits an intent:
  ```kotlin
  on<TransitionsIntent.ClickedLogin> {
      val (username, password) = state
      transitionTo(TransitionsState.Authenticating)
      launch {
          repo.authenticate(username, password)
              .onSuccess { intent(TransitionsIntent.AuthSucceeded(it)) }  // re-emit
              .onFailure { intent(TransitionsIntent.AuthFailed(it.message ?: "Unknown error")) }
      }
  }
  ```
  Then define handlers in `state<Authenticating>`:
  ```kotlin
  state<TransitionsState.Authenticating> {
      on<TransitionsIntent.AuthSucceeded> {
          transitionTo(TransitionsState.Authenticated(it.username))
      }
      on<TransitionsIntent.AuthFailed> {
          action(TransitionsAction.ShowError(it.message))
          transitionTo(TransitionsState.Error(it.message))
      }
  }
  ```
  This is the correct FSM pattern — async results are fed back as intents. Add internal-only intents `AuthSucceeded(username)` and `AuthFailed(message)` to `TransitionsIntent`.
- **Acceptance criteria**: Container compiles, transitions graph covers Login→Authenticating→Authenticated/Error, async auth via intent re-emission

#### Task 2.4: Create TransitionsScreen.kt
- **Description**: Compose UI for the auth flow with 4 state views
- **File to create**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/transitions/TransitionsScreen.kt`
- **Package**: `pro.respawn.flowmvi.sample.features.transitions`
- **UI layout per state**:
  - `Login` → Username TextField + Password TextField + Login Button
  - `Authenticating` → CircularProgressIndicator + "Authenticating…" label
  - `Authenticated` → "Welcome, {username}!" + Logout Button
  - `Error` → Error message + Retry Button
- **Pattern**: Follow SST screen pattern with `container<TransitionsContainer, _, _, _>()`, `subscribe { action -> ... }`, `RScaffold`, `TypeCrossfade`
- **Includes**: Description text, CodeText snippet showing the transitions DSL
- **Acceptance criteria**: Screen renders all 4 states, form input works, actions display snackbar

---

### Phase 3: Screen 2 — Scoped Compose (Dashboard)

#### Task 3.1: Create ScopedComposeModels.kt
- **Description**: Define parent and child state/intent/action types
- **File to create**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/scopedcompose/ScopedComposeModels.kt`
- **Package**: `pro.respawn.flowmvi.sample.features.scopedcompose`
- **Key code**:
  ```kotlin
  // Child states (shared by child stores)
  internal sealed interface ListState : MVIState {
      data object Loading : ListState
      data class Loaded(val items: List<String>) : ListState
      data class Error(val message: String) : ListState
  }
  
  // Parent state
  internal sealed interface ScopedComposeState : MVIState {
      data object Loading : ScopedComposeState
      data class Content(
          val feed: ListState = ListState.Loading,
          val notifications: ListState = ListState.Loading,
      ) : ScopedComposeState
      data class Error(val message: String) : ScopedComposeState
  }

  internal sealed interface ScopedComposeIntent : MVIIntent {
      data object ClickedRetry : ScopedComposeIntent
      data object ClickedRefreshFeed : ScopedComposeIntent
      data object ClickedRefreshNotifications : ScopedComposeIntent
      // internal intents for data loaded results
      data object DataReady : ScopedComposeIntent
      data class LoadFailed(val message: String) : ScopedComposeIntent
  }

  internal sealed interface ScopedComposeAction : MVIAction {
      data class ShowError(val message: String) : ScopedComposeAction
  }
  
  // Child intents
  internal sealed interface ListIntent : MVIIntent {
      data object Refresh : ListIntent
  }
  
  // Child actions (forwarded to parent)
  internal sealed interface ListAction : MVIAction {
      data class ShowLoaded(val label: String) : ListAction
  }
  ```
- **Acceptance criteria**: All types compile, clean separation between parent and child types

#### Task 3.2: Create ScopedComposeContainer.kt
- **Description**: Parent container with 2 child stores using state-scoped `compose()`
- **File to create**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/scopedcompose/ScopedComposeContainer.kt`
- **Package**: `pro.respawn.flowmvi.sample.features.scopedcompose`
- **Key code**:
  ```kotlin
  internal class ScopedComposeContainer(
      configuration: ConfigurationFactory,
  ) : Container<ScopedComposeState, ScopedComposeIntent, ScopedComposeAction> {

      private val feedStore = store<ListState, ListIntent, ListAction>(ListState.Loading) {
          configure { name = "FeedStore"; debuggable = true; actionShareBehavior = ActionShareBehavior.Distribute() }
          init { launch { loadFeed() } }
          reduce { intent -> when (intent) { ListIntent.Refresh -> { updateState { ListState.Loading }; launch { loadFeed() } } } }
      }

      private val notificationsStore = store<ListState, ListIntent, ListAction>(ListState.Loading) {
          configure { name = "NotificationsStore"; debuggable = true; actionShareBehavior = ActionShareBehavior.Distribute() }
          init { launch { loadNotifications() } }
          reduce { intent -> when (intent) { ListIntent.Refresh -> { updateState { ListState.Loading }; launch { loadNotifications() } } } }
      }

      override val store = store(ScopedComposeState.Loading) {
          configure(configuration, "ScopedComposeStore")
          
          transitions {
              state<ScopedComposeState.Loading> {
                  on<ScopedComposeIntent.DataReady> {
                      transitionTo(ScopedComposeState.Content())
                  }
                  on<ScopedComposeIntent.LoadFailed> {
                      transitionTo(ScopedComposeState.Error(it.message))
                  }
              }
              state<ScopedComposeState.Content> {
                  // State-scoped compose: active only while in Content
                  compose(feedStore, merge = { childState -> copy(feed = childState) })
                  compose(notificationsStore, merge = { childState -> copy(notifications = childState) })
                  
                  on<ScopedComposeIntent.ClickedRefreshFeed> {
                      feedStore.intent(ListIntent.Refresh)
                  }
                  on<ScopedComposeIntent.ClickedRefreshNotifications> {
                      notificationsStore.intent(ListIntent.Refresh)
                  }
              }
              state<ScopedComposeState.Error> {
                  on<ScopedComposeIntent.ClickedRetry> {
                      transitionTo(ScopedComposeState.Loading)
                  }
              }
          }
          
          // Simulate initial data loading
          init {
              launch {
                  delay(1500)
                  intent(ScopedComposeIntent.DataReady) // transition to Content after loading
              }
          }
      }
  }
  ```
- **Showcases**: State-scoped `compose()` — subscriptions activate when parent enters `Content`, deactivate when leaving. Child store intent routing via `feedStore.intent()`.
- **Acceptance criteria**: Container compiles, child stores only merge into parent while in `Content`, retry resets to Loading

#### Task 3.3: Create ScopedComposeScreen.kt
- **Description**: Dashboard UI with feed + notifications sections
- **File to create**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/scopedcompose/ScopedComposeScreen.kt`
- **Package**: `pro.respawn.flowmvi.sample.features.scopedcompose`
- **UI layout per state**:
  - `Loading` → CircularProgressIndicator + "Loading dashboard…"
  - `Content` → Feed section (list or loading) + Notifications section (list or loading) + Refresh buttons
  - `Error` → Error message + Retry button
- **Pattern**: Follow existing screen patterns with `TypeCrossfade`
- **Includes**: Description text explaining state-scoped composition, CodeText snippet
- **Acceptance criteria**: Screen renders all states, refresh buttons trigger child store reloads, child subscriptions visually stop when transitioning to Error

---

### Phase 4: Screen 3 — Top-Level Compose (Dashboard)

#### Task 4.1: Create TopLevelComposeModels.kt
- **Description**: Data class parent state with child state types
- **File to create**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/toplevelcompose/TopLevelComposeModels.kt`
- **Package**: `pro.respawn.flowmvi.sample.features.toplevelcompose`
- **Key code**:
  ```kotlin
  // Weather child state
  internal sealed interface WeatherState : MVIState {
      data object Loading : WeatherState
      data class Loaded(val temperature: Int, val condition: String) : WeatherState
  }

  // Clock child state
  internal data class ClockState(val time: String = "--:--:--") : MVIState

  // Parent state — single data class, always-active composition target
  internal data class DashboardState(
      val weather: WeatherState = WeatherState.Loading,
      val clock: ClockState = ClockState(),
  ) : MVIState

  internal sealed interface DashboardIntent : MVIIntent {
      data object ClickedRefresh : DashboardIntent
  }

  internal sealed interface DashboardAction : MVIAction
  // No actions needed — pure state-driven UI
  ```
- **Acceptance criteria**: All types compile, `DashboardState` is a data class (not sealed)

#### Task 4.2: Create TopLevelComposeContainer.kt
- **Description**: Parent container with always-active child stores using top-level `compose()`
- **File to create**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/toplevelcompose/TopLevelComposeContainer.kt`
- **Package**: `pro.respawn.flowmvi.sample.features.toplevelcompose`
- **Key code**:
  ```kotlin
  internal class TopLevelComposeContainer(
      configuration: ConfigurationFactory,
  ) : Container<DashboardState, DashboardIntent, DashboardAction> {

      // Child intents/actions not exposed to parent
      private sealed interface WeatherIntent : MVIIntent { data object Refresh : WeatherIntent }
      private sealed interface ClockIntent : MVIIntent

      private val weatherStore = store<WeatherState, WeatherIntent, Nothing>(WeatherState.Loading) {
          configure { name = "WeatherStore"; debuggable = true }
          init { launch { loadWeather() } }
          reduce { intent ->
              when (intent) {
                  WeatherIntent.Refresh -> { updateState { WeatherState.Loading }; launch { loadWeather() } }
              }
          }
      }

      private val clockStore = store<ClockState, ClockIntent, Nothing>(ClockState()) {
          configure { name = "ClockStore"; debuggable = true }
          whileSubscribed {
              while (true) {
                  updateState { copy(time = currentTimeFormatted()) }
                  delay(1.seconds)
              }
          }
      }

      override val store = store(DashboardState()) {
          configure(configuration, "TopLevelComposeStore")
          
          transitions {
              // Top-level compose: always active while parent store runs
              compose(weatherStore) { copy(weather = it) }
              compose(clockStore) { copy(clock = it) }
              
              state<DashboardState> {
                  on<DashboardIntent.ClickedRefresh> {
                      weatherStore.intent(WeatherIntent.Refresh)
                  }
              }
          }
      }

      private suspend fun PipelineContext<WeatherState, *, *>.loadWeather() {
          delay(1500)
          val conditions = listOf("Sunny", "Cloudy", "Rainy", "Snowy", "Windy")
          updateState { WeatherState.Loaded(Random.nextInt(-10, 35), conditions.random()) }
      }
      
      private fun currentTimeFormatted(): String { /* format current time HH:mm:ss */ }
  }
  ```
- **Showcases**: Top-level `compose()` — always active, `copy(weather = it)` merge syntax, single data class state, clock ticking every second
- **Note**: `clockStore` uses `whileSubscribed` to tick. Since it's auto-started as a child, the parent's compose subscription acts as the subscriber that keeps it active.
- **Acceptance criteria**: Container compiles, weather loads with delay, clock ticks every second, refresh reloads weather

#### Task 4.3: Create TopLevelComposeScreen.kt
- **Description**: Dashboard UI with weather card + live clock
- **File to create**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/toplevelcompose/TopLevelComposeScreen.kt`
- **Package**: `pro.respawn.flowmvi.sample.features.toplevelcompose`
- **UI layout**:
  - Weather card: temperature + condition (or loading spinner)
  - Clock card: live-updating time string
  - Refresh button for weather
- **Pattern**: Single state (`DashboardState` is a data class, no TypeCrossfade needed — use direct composition)
- **Includes**: Description text explaining top-level composition, CodeText snippet
- **Acceptance criteria**: Weather loads after delay, clock ticks in real-time, refresh button reloads weather

---

### Phase 5: Integration & Polish

#### Task 5.1: Register Containers in FeatureModule.kt
- **Description**: Register all new containers and repositories with Koin
- **Files to modify**: `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/features/FeatureModule.kt`
- **Code to add**:
  ```kotlin
  // Imports
  import pro.respawn.flowmvi.sample.features.transitions.AuthRepository
  import pro.respawn.flowmvi.sample.features.transitions.TransitionsContainer
  import pro.respawn.flowmvi.sample.features.scopedcompose.ScopedComposeContainer
  import pro.respawn.flowmvi.sample.features.toplevelcompose.TopLevelComposeContainer

  // In module block:
  singleOf(::AuthRepository)
  container { new(::TransitionsContainer) }
  container { new(::ScopedComposeContainer) }
  container { new(::TopLevelComposeContainer) }
  ```
- **Acceptance criteria**: All containers resolve via Koin scoped injection

#### Task 5.2: Run detektFormat
- **Description**: Run `./gradlew detektFormat` and fix any remaining lint issues
- **Acceptance criteria**: detektFormat passes with no errors

#### Task 5.3: Compile Sample App
- **Description**: Run `./gradlew :sample:assemble` (or platform-specific task) to verify compilation
- **Acceptance criteria**: Sample module compiles without errors

#### Task 5.4: Manual Testing
- **Description**: Run the sample app and verify:
  - All 3 features appear in the home screen menu
  - Navigation to each feature works
  - Back navigation works
  - **Transitions**: Login form → authenticating spinner → success/error → retry/logout cycle works
  - **Scoped Compose**: Loading → content with feed + notifications → error → retry cycle works
  - **Top-Level Compose**: Weather loads, clock ticks, refresh button works
- **Acceptance criteria**: All features functional on at least one platform (desktop/wasm/android)

---

## Task Dependencies

```
Phase 1 (Infrastructure)
├── Task 1.1 (Icons)           ─── no deps
├── Task 1.2 (Strings)         ─── no deps
├── Task 1.3 (Destinations)    ─── no deps
├── Task 1.4 (Navigator)       ─── depends on 1.3
├── Task 1.5 (HomeFeature)     ─── depends on 1.1, 1.2
└── Task 1.6 (Routing)         ─── depends on 1.3, Phase 2-4 screens

Phase 2 (Transitions Screen)
├── Task 2.1 (AuthRepository)  ─── no deps
├── Task 2.2 (Models)          ─── no deps
├── Task 2.3 (Container)       ─── depends on 2.1, 2.2
└── Task 2.4 (Screen)          ─── depends on 2.2, 2.3

Phase 3 (Scoped Compose Screen)
├── Task 3.1 (Models)          ─── no deps
├── Task 3.2 (Container)       ─── depends on 3.1
└── Task 3.3 (Screen)          ─── depends on 3.1, 3.2

Phase 4 (Top-Level Compose Screen)
├── Task 4.1 (Models)          ─── no deps
├── Task 4.2 (Container)       ─── depends on 4.1
└── Task 4.3 (Screen)          ─── depends on 4.1, 4.2

Phase 5 (Integration & Polish)
├── Task 5.1 (FeatureModule)   ─── depends on 2.3, 3.2, 4.2
├── Task 5.2 (detektFormat)    ─── depends on all above
├── Task 5.3 (Compile)         ─── depends on 5.2
└── Task 5.4 (Manual Testing)  ─── depends on 5.3
```

**Parallelizable**: Phases 2, 3, 4 are independent and can be implemented in parallel. Phase 1 tasks 1.1–1.3 are independent. Task 1.6 (routing) should be done last after all screens exist.

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| `transitionTo` inside `launch {}` is vetoed by FSM enforcement | High — auth flow broken | Use intent re-emission pattern: async result → `intent(AuthSucceeded)` → handler calls `transitionTo`. This is the correct FSM pattern. |
| Child store lifecycle in compose screens — child stores need to be started | Medium — child stores silently do nothing | `childStorePlugin` (part of `transitions {}` composite) auto-starts child stores. Verified in `ComposePlugin.kt`. |
| `whileSubscribed` in clockStore may not trigger without explicit subscriber | Medium — clock doesn't tick | The compose subscription from the parent acts as a subscriber. If this doesn't work, use `init { launch { ... } }` with manual flow collection instead. |
| Icon ImageVector paths are complex to create manually | Low — icons don't render | Copy structure from existing icons (e.g., `AccountTree.kt`), use Material Icons reference for path data. |
| New string resources may not be generated until Gradle sync | Low — compile error on `Res.string.*` | Run Gradle sync after adding strings. Compose resources plugin generates accessors automatically. |
| `ScopedComposeContainer` child stores use `store()` (eager) vs `lazyStore()` — eager stores aren't started at construction | Low — child stores not running | `childStorePlugin` handles starting. The stores are passed to `compose()` which registers them in the child plugin. |

---

## Files Summary

### Files to Create (12)
| File | Phase |
|------|-------|
| `sample/.../ui/icons/SwapHoriz.kt` | 1 |
| `sample/.../ui/icons/FilterList.kt` | 1 |
| `sample/.../ui/icons/Dashboard.kt` | 1 |
| `sample/.../features/transitions/AuthRepository.kt` | 2 |
| `sample/.../features/transitions/TransitionsModels.kt` | 2 |
| `sample/.../features/transitions/TransitionsContainer.kt` | 2 |
| `sample/.../features/transitions/TransitionsScreen.kt` | 2 |
| `sample/.../features/scopedcompose/ScopedComposeModels.kt` | 3 |
| `sample/.../features/scopedcompose/ScopedComposeContainer.kt` | 3 |
| `sample/.../features/scopedcompose/ScopedComposeScreen.kt` | 3 |
| `sample/.../features/toplevelcompose/TopLevelComposeModels.kt` | 4 |
| `sample/.../features/toplevelcompose/TopLevelComposeContainer.kt` | 4 |
| `sample/.../features/toplevelcompose/TopLevelComposeScreen.kt` | 4 |

### Files to Modify (7)
| File | Phase |
|------|-------|
| `sample/.../composeResources/values/strings.xml` | 1 |
| `sample/.../navigation/destination/Destination.kt` | 1 |
| `sample/.../navigation/AppNavigator.kt` | 1 |
| `sample/.../navigation/AppNavigatorImpl.kt` | 1 |
| `sample/.../features/home/HomeModels.kt` | 1 |
| `sample/.../features/home/HomeScreen.kt` | 1 |
| `sample/.../navigation/destination/Destinations.kt` | 1 |
| `sample/.../features/FeatureModule.kt` | 5 |

*All paths relative to `sample/src/commonMain/kotlin/pro/respawn/flowmvi/sample/`*
