package pro.respawn.flowmvi.sample.features.toplevelcompose

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.transitions
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class TopLevelComposeContainer(
    configuration: ConfigurationFactory,
) : Container<DashboardState, DashboardIntent, DashboardAction> {

    // Private child intent types
    private sealed interface WeatherIntent : MVIIntent {
        data object Refresh : WeatherIntent
    }

    private sealed interface ClockIntent : MVIIntent

    private val weatherStore: Store<WeatherState, WeatherIntent, MVIAction> =
        store<WeatherState, WeatherIntent, MVIAction>(WeatherState.Loading) {
            configure {
                name = "WeatherStore"
                debuggable = true
            }
            init {
                launch {
                    delay(1500.milliseconds)
                    val conditions = listOf("Sunny", "Cloudy", "Rainy", "Snowy", "Windy")
                    updateState { WeatherState.Loaded(Random.nextInt(-10, 35), conditions.random()) }
                }
            }
            reduce { intent ->
                when (intent) {
                    WeatherIntent.Refresh -> {
                        updateState { WeatherState.Loading }
                        launch {
                            delay(1500.milliseconds)
                            val conditions = listOf("Sunny", "Cloudy", "Rainy", "Snowy", "Windy")
                            updateState { WeatherState.Loaded(Random.nextInt(-10, 35), conditions.random()) }
                        }
                    }
                }
            }
        }

    private val clockStore: Store<ClockState, ClockIntent, MVIAction> =
        store<ClockState, ClockIntent, MVIAction>(ClockState()) {
            configure {
                name = "ClockStore"
                debuggable = true
            }
            whileSubscribed {
                while (true) {
                    updateState { copy(time = currentTimeFormatted()) }
                    delay(1.seconds)
                }
            }
        }

    override val store: Store<DashboardState, DashboardIntent, DashboardAction> = store(DashboardState()) {
        configure(configuration, "TopLevelComposeStore")

        transitions {
            // Top-level compose: always active while parent store runs
            compose(weatherStore, merge = { childState -> copy(weather = childState) })
            compose(clockStore, merge = { childState -> copy(clock = childState) })

            state<DashboardState> {
                on<DashboardIntent.ClickedRefresh> {
                    weatherStore.intent(WeatherIntent.Refresh)
                }
            }
        }
    }

    private fun currentTimeFormatted(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val h = now.hour.toString().padStart(2, '0')
        val m = now.minute.toString().padStart(2, '0')
        val s = now.second.toString().padStart(2, '0')
        return "$h:$m:$s"
    }
}
