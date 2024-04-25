# Learn how to persist and restore State

The `savedstate` artifact contains plugins and API necessary to save and restore the state of a store to a place
that outlives its lifespan. This is useful in many cases and provides an unparalleled UX. For example, a person may
leave the app while the form they were filling is unfinished, then return to the app and see all of their data
being restored, continuing their work.

## 1. Adding a dependency

```toml
flowmvi-savedstate = { module = "pro.respawn.flowmvi:savedstate", version.ref = "flowmvi" } 
```

The artifact depends on:

* `kotlinx-io`, and as a consequence, on `okio`
* `kotlinx-serialization`, including Json
* `androidx-lifecycle-savedstate` on Android to parcelize the state.

The module depends on quite a few things, so it would be best to avoid adding it to all of your modules.
Instead, you can inject the plugin or savers using DI.

## 2. Defining `Saver`s

The basic building block of the module is the `Saver` interface/function. Saver defines **how** to save the state.
Use the `Saver` function to build a saver or implement the
interface to write your custom saving logic, or use one of the prebuilt ones:

* `MapSaver` for saving partial data.
* `TypedSaver` for saving a state of a particular subtype.
* `JsonSaver` for saving the state as a JSON.
* `FileSaver` for saving the state to a file. See `DefaultFileSaver` for custom file writing logic.
* `CompressedFileSaver` for saving the state to a file and compressing it.
* `NoOpSaver` for testing.

`Saver`s can be decorated and extended. For example, you can build a saver chain to store a particular type of the state
in a compressed Json file:

```kotlin
val saver = TypedSaver<DisplayingCounter, CounterState>(
    JsonSaver(
        json = Json,
        serializer = DisplayingCounter.serializer(),
        delegate = CompressedFileSaver(path),
    )
)
```

You can invoke the `save` method of the saver manually if you keep a reference to it.

## 3. Choosing `SaveBehavior`

For now there are two types of behaviors that you can use to decide **when** to save the state.

### `OnChange`

This behavior will save the state each time it is changed and after a specified `delay`.
If the state changes before or during the operation of saving the state, the delay will be restarted and the previous
job will be canceled.
In general, don't use multiple values of this behavior, because only the minimum delay value will be respected.

### `OnUnsubscribe`

This behavior will persist the state when a subscriber is removed and the store is left with a specified number of
`remainingSubscribers`.

This will happen, for example, when the app goes into the background.
Don't use multiple instances of this behavior, as only the maximum number of subscribers will be respected.

?> By default, both of these are used - on each change, with a sensible delay, and when all subscribers leave.
You can customize this via the `behaviors` parameter of the plugin.

## 4. Installing the plugin

To start saving the state, just install your preferred variation of the `saveState` plugin:

### Custom state savers

```kotlin
val store = store(initial = Loading) { // start with a default loading value as we still need it
    saveState(
        saver = CustomSaver(),
        context = Dispatchers.IO,
        resetOnException = true, // or false if you're brave
    )
}
```

### Serializing state to a file

You don't have to define your own savers if you don't need to. There is an overload of the `saveStatePlugin` that
provides sensible defaults for you, called `serializeState`:

```kotlin
serializeState(
    path = path, // (1)
    serializer = DisplayingCounter.serializer(), // (2)
    recover = NullRecover // (3)
)
```

1. Provide a path where the state will be saved.
    * It's best to use a subdirectory of your cache dir to prevent it from being fiddled with by other code.
   * On web platforms, the state will be saved to local storage.k
2. Mark your state class as `@Serializable` to generate the serializer for it.
    * It's best to store only a particular subset of states of the store because you don't want to restore the user
      to an error / loading state, do you?
3. Provide a way for the plugin to recover from errors when parsing, saving or reading the state. The bare minimum
   is to ignore all errors and not restore or save anything, but a better solution like logging the errors can be used
   instead. By default, the plugin will just throw and let the store (`recoverPlugin`) handle the exception.

### Storing the state in a bundle

If you're on android, there is the `parcelizeState` plugin that will store the state in a `SavedStateHandle`:

```kotlin
parcelizeState<DisplayingCounter, _, _, _>(
    handle = savedStateHandle,
    key = "CounterState",
)
```

* The `key` parameter will be derived from the store / class name if you don't specify it, but watch out for conflicts
* This plugin uses the `ParcelableSaver` by default, which you can use too.

!> Watch out for parcel size overflow exceptions! The library will not check the resulting parcel size for you.

?> According to the documentation, any writes to your saved state will only be restored if the activity
was killed by the OS. This means you will not see any state restoration results unless the OS kills the activity
itself (i.e. exiting the app will not result in the state being restored).

## 5. Caveats

### App updates

Saving state is great, but think about what will happen to your app when the app is updated and the resulting state
structure changes. For example, the name of the property may stay the same but its meaning may have changed.
This means, when the state will be restored, unpredictable behavior may occur. This does not necessarily mean
restoration will fail, but that the logic may be affected. On Android, the system will clear the saved state for you,
but if you persist the state to a file, you have to keep track of this yourself.

The best way to solve this would be to clear the saved state (for example, by deleting the directory) on each app
update.

You can do this by registering a broadcast receiver or checking if the app was updated upon startup.
Implementation of this logic is out of scope of the library.

### Crashes and failures

If the app fails for whatever reason, it's important that that may invalidate the state or even result in further
crashes. If your app crashes, make sure to invalidate the saved state as well, for example, by using Crashlytics,
overriding the main looper to catch fatal exceptions, or any other means.

The library will clear the state when an exception happens in the store and can let you recover from errors, but
that is not enough as crashes may happen in other places in your app, such as the UI layer.

### Saving sensitive data

If you save the state, you have to think about excluding sensitive data such as passwords and phone numbers
from it. Annotate the serializable state fields with `@Transient`, for example, or create a subset of the
state properties that you will save.
Unless you implement savers that encrypt the data, ensure the safety of the user by not storing sensitive data at all.
