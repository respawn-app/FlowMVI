package pro.respawn.flowmvi.sample.features.simple

import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.features.simple.SimpleIntent.ClickedButton

data class SimpleState(val counter: Int = 0) : MVIState

sealed interface SimpleIntent : MVIIntent {

    data object ClickedButton : SimpleIntent
}

val simpleStore = store<_, _>(SimpleState()) {
    reduce { intent: SimpleIntent ->
        when (intent) {
            ClickedButton -> updateState {
                copy(counter = counter + 1)
            }
        }
    }
}
