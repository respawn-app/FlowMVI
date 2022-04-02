# Flow MVI

This is an MVI implementation based on coroutines with a few main goals:

1. Being simple to understand, implement and use
2. Following the Principle of Least Responsibility - all communication happens through strictly defined contract
3. Featuring a clean and readable DSL

## Let's get started:

Choose your ~~fighter~~ dependency:  
[![](https://jitpack.io/v/Nek-12/FlowMVI.svg)](https://jitpack.io/#Nek-12/FlowMVI)

```kotlin
val flowVersion = /* look at the widget */
implementation("com.github.Nek-12.FlowMVI:core:${flowVersion}") //does not depend on any particular platform
implementation("com.github.Nek-12.FlowMVI:android-compose:${flowVersion}") //For Jetpack Compose Android projects
implementation("com.github.Nek-12.FlowMVI:android-view:${flowVersion}") //For View-based Android projects
```

## Core:

```kotlin
sealed class ScreenState : MVIState {
    object Loading : ScreenState()
    data class Error(e: Exception) : ScreenState()
    data class DisplayingCounter(
        val counter: Int,
    ) : ScreenState()
}

sealed class ScreenIntent : MVIIntent {
    object ClickedCounter : ScreenIntent()
}

sealed class ScreenAction : MVIAction {
    data class ShowMessage(val message: String) : ScreenAction()
}


val store = MVIStore<ScreenState, ScreenIntent, ScreenAction>(
    scope = viewModelScope,
    initialState = DisplayingCounter(0),
    reduce = { intent -> /*...*/ }
)

//somewhere in the ui layer

store.subscribe(
    coroutineScope,
    consume = { action -> /* ... */ },
    render = { state -> /* ... */ }
)
```

## Android (Compose):

```kotlin

class ScreenViewModel : MVIViewModel<ScreenState, ScreenIntent, ScreenAction>() {

    override val initialState get() = Loading
    override fun recover(from: Exception) = Error(from) //optional

    override suspend fun reduce(intent: ScreenIntent) = when (intent) {

        //no-op if state is not DisplayingCounter
        is ClickedCounter -> withState<DisplayingCounter> { //this -> DisplayingCounter

            send(ShowMessage("So simple!"))

            copy(counter = counter + 1)
        }
        /* ... */
    }
}

@Composable
fun ComposeScreen() = MVIComposable(
    provider = getViewModel<ScreenViewModel>(), //use your fav DI framework
) { state ->

    consume { action ->
        when (action) {
            is ShowMessage -> { /* ... */
            }
        }
    }

    when (state) {
        is DisplayingCounter -> {
            Button(onClick = { send(ClickedCounter) }) {
                Text("Counter: ${state.counter}") //render state,
            }
        }
    }
}
```

If you don't want to use MVIComposable, just collect the actions flow using coroutineScope and render states
using `viewModel.states.collectAsState()`

## Android (View):

```kotlin

//ViewModel and Model classes have not changed

class ScreenFragment : Fragment(), MVIView<ScreenState, ScreenIntent, ScreenAction> {

    override val provider by viewModel<ScreenViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribe() //one-liner for store subscription. Lifecycle-aware and efficient.
    }

    override fun render(state: ScreenState) {
        //update your views
    }

    override fun consume(action: ScreenAction) {
        //handle actions
    }
}
```

And that's it!   
If you don't like base classes, interfaces or abstraction, there always are ways to avoid inheritance and use
composition. You are not required in any way to extend MVIView or MVIViewModel, or even MVIProvider. Everything is
possible with a couple of lambdas. For examples of such implementations,
see [sample app](/app/src/main/java/com/nek12/flowMVI/sample/view/NoBaseClassViewModel.kt) or read java docs.

For more information and more elaborate examples, see the sample app.

## License

```
Copyright 2022 Nikita Vaizin

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
