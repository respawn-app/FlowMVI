package pro.respawn.flowmvi.sample.features.scopedcompose

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.transitions
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val ErrorChance = 0.15f
private const val ItemCount = 10
private val LoadDelay = 1500.milliseconds

internal class ScopedComposeContainer(
    configuration: ConfigurationFactory,
) : Container<ScopedComposeState, ScopedComposeIntent, ScopedComposeAction> {

    private val feedStore: Store<ListState, ListIntent, ListAction> =
        store<ListState, ListIntent, ListAction>(ListState.Loading) {
            configure {
                name = "FeedStore"
                debuggable = true
                actionShareBehavior = ActionShareBehavior.Distribute()
            }
            init {
                launch { updateState { loadItems("Feed") } }
            }
            reduce { intent ->
                when (intent) {
                    ListIntent.Refresh -> {
                        updateState { ListState.Loading }
                        launch { updateState { loadItems("Feed") } }
                    }
                }
            }
        }

    private val notificationsStore: Store<ListState, ListIntent, ListAction> =
        store<ListState, ListIntent, ListAction>(ListState.Loading) {
            configure {
                name = "NotificationsStore"
                debuggable = true
                actionShareBehavior = ActionShareBehavior.Distribute()
            }
            init {
                launch { updateState { loadItems("Notification") } }
            }
            reduce { intent ->
                when (intent) {
                    ListIntent.Refresh -> {
                        updateState { ListState.Loading }
                        launch { updateState { loadItems("Notification") } }
                    }
                }
            }
        }

    override val store: Store<ScopedComposeState, ScopedComposeIntent, ScopedComposeAction> =
        store<ScopedComposeState, ScopedComposeIntent, ScopedComposeAction>(ScopedComposeState.Loading) {
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
                    compose<ListState, ListIntent, ListAction>(
                        feedStore,
                        merge = { childState -> copy(feed = childState) },
                    )
                    compose<ListState, ListIntent, ListAction>(
                        notificationsStore,
                        merge = { childState -> copy(notifications = childState) },
                    )

                    on<ScopedComposeIntent.ClickedRefreshFeed> {
                        feedStore.intent(ListIntent.Refresh)
                    }
                    on<ScopedComposeIntent.ClickedRefreshNotifications> {
                        notificationsStore.intent(ListIntent.Refresh)
                    }
                    on<ScopedComposeIntent.ClickedRefreshAll> {
                        feedStore.intent(ListIntent.Refresh)
                        notificationsStore.intent(ListIntent.Refresh)
                    }
                }
                state<ScopedComposeState.Error> {
                    on<ScopedComposeIntent.ClickedRetry> {
                        transitionTo(ScopedComposeState.Loading)
                        launch {
                            delay(LoadDelay)
                            intent(ScopedComposeIntent.DataReady)
                        }
                    }
                }
            }

            // Simulate initial data loading
            init {
                launch {
                    delay(LoadDelay)
                    intent(ScopedComposeIntent.DataReady)
                }
            }
        }

    private suspend fun loadItems(prefix: String): ListState {
        delay(1.seconds)
        return if (Random.nextFloat() < ErrorChance) {
            ListState.Error("Failed to load $prefix")
        } else {
            ListState.Loaded(List(ItemCount) { "$prefix item ${it + 1}" })
        }
    }
}
