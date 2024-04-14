package pro.respawn.flowmvi.sample.navigation.component

import androidx.compose.runtime.Stable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnDestroy
import org.koin.core.component.getScopeId
import org.koin.core.qualifier.qualifier
import pro.respawn.flowmvi.sample.arch.di.DestinationScope
import pro.respawn.flowmvi.sample.navigation.destination.Destination
import pro.respawn.flowmvi.util.typed

@Stable
interface DestinationComponent : ComponentContext, DestinationScope

internal fun destinationComponent(
    destination: Destination?,
    context: ComponentContext
): DestinationComponent = object : ComponentContext by context, DestinationComponent {
    override val scope = getKoin().getOrCreateScope(
        scopeId = destination?.getScopeId() ?: "RootComponent",
        qualifier = qualifier<DestinationScope>(),
        source = context as? DestinationScope
    )
    override val coroutineScope = coroutineScope()

    init {
        scope.declare(instance = coroutineScope, allowOverride = false)
        context.typed<DestinationScope>()?.let { scope.linkTo(it.scope) }
        doOnDestroy { scope.close() }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is DestinationComponent) return false
        return scope.id == other.scope.id
    }

    override fun hashCode(): Int = scope.hashCode()
}
