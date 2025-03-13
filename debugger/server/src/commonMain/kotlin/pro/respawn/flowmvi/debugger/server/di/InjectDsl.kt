package pro.respawn.flowmvi.debugger.server.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

val LocalDestinationScope = staticCompositionLocalOf<DestinationScope> {
    error("Provide a destination scope")
}

@FlowMVIDSL
inline fun <reified T : Container<*, *, *>> Module.container(noinline definition: Definition<T>) {
    scope<DestinationScope> {
        scoped<T> { params ->
            definition(this, params).apply {
                store.start(get<CoroutineScope>())
            }
        }
    }
}

@FlowMVIDSL
@Composable
inline fun <reified T : Container<S, I, A>, S : MVIState, I : MVIIntent, A : MVIAction> container(
    noinline params: ParametersDefinition,
) = koinInject<T>(scope = LocalDestinationScope.current.scope, parameters = params).store

@FlowMVIDSL
@Composable
inline fun <reified T : Container<S, I, A>, S : MVIState, I : MVIIntent, A : MVIAction> container() = koinInject<T>(
    scope = LocalDestinationScope.current.scope
).store
