package pro.respawn.flowmvi.sample.features.xmlactivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityAction.ShowIncrementedSnackbar
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityIntent.ClickedIncrementCounter
import pro.respawn.flowmvi.sample.features.xmlactivity.XmlActivityState.DisplayingCounter

internal class XmlActivityContainer(
    configuration: ConfigurationFactory,
) : Container<XmlActivityState, XmlActivityIntent, XmlActivityAction> {

    override val store = store(DisplayingCounter(0)) {
        configure(configuration, "XmlActivityStore")
        recover {
            updateState { XmlActivityState.Error(it) }
            null
        }
        reduce { intent ->
            when (intent) {
                is ClickedIncrementCounter -> updateState<DisplayingCounter, _> {
                    launch {
                        delay(1000L)
                        action(ShowIncrementedSnackbar)
                        updateState { DisplayingCounter(counter + 1) }
                    }
                    XmlActivityState.Loading
                }
            }
        }
    }
}
