## Migrating from v1.0

Unfortunately in FlowMVI 2.0, virtually everything from the old API was deprecated to allow for the new architecture.
However, most of the deprecations you will face are simple renames and will specify what to replace them with.
You can simply run a context action in the IDE: `Code -> Code Cleanup -> Run` to replace everything that can be
replaced.

## Migrating MVIViewModel

The hardest thing to migrate will be the `MVIViewModel`. Let's say we have the following:

```kotlin
class CounterViewModel(
    repo: CounterRepository,
) : MVIViewModel<CounterState, CounterIntent, CounterAction>(CounterState.Loading) {

    init {
        incrementCounter(-1)

        repo.getTimer()
            .onEach(::produceState)
            .recover()
            .flowOn(Dispatchers.Default)
            .consume()
    }

    override fun recover(from: Exception) = DisplayingCounter(0, 0, "")

    override suspend fun reduce(intent: CounterIntent) {
        when (intent) {

            is CounterIntent.ClickedCounter -> updateState<DisplayingCounter> {
                incrementCounter(current = counter, timer)

                CounterState.Loading
            }
        }
    }

    private suspend fun produceState(value: Int) = updateState<DisplayingCounter> { copy(timer = value) }

    private fun incrementCounter(current: Int, timer: Int? = null) = launchRecovering {

        updateState {
            DisplayingCounter(
                counter = current + 1,
                timer = timer ?: (this as? DisplayingCounter)?.timer ?: 0,
                param = "",
            )
        }
    }
}
```

### 1. Replace `MVIViewModel` with `Container`

```kotlin
import pro.respawn.flowmvi.api.Container

// ---- old
class CounterViewModel(
    repo: CounterRepo,
) : MVIViewModel<ComposeState, ComposeIntent, ComposeAction>(initialState = Loading)


// ---- new
class CounterViewModel(
    repo: CounterRepo,
) : ViewModel(), Container<ComposeState, ComposeIntent, ComposeAction>
```

### 2. Override the new `store` property.

* Move everything from the `recover` function to the `recover` plugin.
  Add an `updateState` call and return null from the block.
* Move everything from the `reduce` function to the `reduce` plugin
* Move everything from the `init` block to the `init` plugin

```kotlin
import pro.respawn.flowmvi.dsl.lazyStore
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.dsl.updateState

class CounterViewModel(
    repo: CounterRepo,
) : ViewModel(), Container<ComposeState, ComposeIntent, ComposeAction> {

    override val store by lazyStore(CounterState.Loading) {

        // Order of plugins matters!
        init {
            incrementCounter(-1)

            // TODO: Consider moving this to whileSubscribed { } to not leak resources when the app is in background.
            repo.getTimer()
                .onEach(::timerToState)
                .recover()
                .flowOn(Dispatchers.Default)
                .consume()
        }
        recover {
            // add this updateState block
            updateState {
                DisplayingCounter(0, 0, "")
            }
            null // return null if you handled the exception
        }
        reduce { intent ->
            when (intent) {
                is CounterIntent.ClickedCounter -> updateState<DisplayingCounter> {
                    incrementCounter(current = counter, timer)
                    
                    CounterState.Loading
                }
            }
        }
    }
}
```

### 3. Migrate private functions

* Define a typealias for `PipelineContext`
* Add `PipelineContext` receiver to your custom private functions
* Replace `launchRecovering` with `launch`
* Add a second type parameter to `updateState` (like this: `updateState<DisplayingCounter, _>()`)

```kotlin
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.PipelineContext

private typealias Ctx = PipelineContext<CounterState, CounterIntent, CounterAction>

class CounterViewModel(
    repo: CounterRepository,
) : ViewModel(), Container<ComposeState, ComposeIntent, ComposeAction> {

    /* <...> */

    private suspend fun Ctx.timerToState(value: Int) = updateState<DisplayingCounter, _> { copy(timer = value) }

    private fun Ctx.incrementCounter(current: Int, timer: Int? = null) = launch {

        updateState {
            DisplayingCounter(
                counter = current + 1,
                timer = timer ?: (this as? DisplayingCounter)?.timer ?: 0,
                param = "",
            )
        }
    }
}
```

### 4. Migrate your Views

* For Compose, you hopefully won't have to change anything.
* For views, migrate to the new MVIView interface using auto-replacement. Except for the rename (provider -> container),
  everything should be the same.

### 5. (Optional) Add new plugins

You can now improve your VM using new features of the library. You now can do the following:

* Add the `parcelizeState` plugin to save state across process death
* Add the `androidLoggingPlugin` to get logcat logs
* Set `debuggable = BuildConfig.DEBUG` to get more validations and logging
* Set `parallelIntents = true` to make intents faster (make sure you do not depend on the ordering of intents!)
* Use `whileSubscribed` to avoid resource leaks while the VM is in the background
* Write custom plugins to set up analytics and the like.

### 6. (Optional) Migrate to StoreViewModels or MVVM+

* If you want to make your ViewModels multiplatform, you will need to migrate away from subclassing ViewModel at all.
* If you have no plans on using KMP, you can now use the MVVM+ approach.

See the [Android guide](android.md) and [Quickstart](quickstart.md) to learn how to do these.
