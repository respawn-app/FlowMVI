package pro.respawn.flowmvi.debugger.server.navigation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import pro.respawn.flowmvi.debugger.server.navigation.destination.Destination
import pro.respawn.flowmvi.debugger.server.navigation.util.NavResult
import pro.respawn.flowmvi.debugger.server.navigation.util.Navigator
import pro.respawn.flowmvi.debugger.server.navigation.util.duplicateOf
import pro.respawn.flowmvi.debugger.server.navigation.util.retained

@Stable
open class StackComponent(
    context: ComponentContext,
) : Navigator {

    val results by context.retained { MutableStateFlow<Set<NavResult<*>>>(emptySet()) }
    val stackNav = StackNavigation<Destination>()
    val stack = context.childStack(
        source = stackNav,
        serializer = Destination.serializer(),
        initialConfiguration = Destination.Connect,
        handleBackButton = true,
        childFactory = ::destinationComponent,
    )

    fun navigate(
        destination: Destination,
        filter: (Destination) -> Boolean = { false },
    ) = stackNav.navigate inner@{ stack ->
        if (destination.topLevel) return@inner listOf(destination)
        stack.asSequence()
            .filterNot { destination duplicateOf it && !it.topLevel }
            .filterNot(filter)
            .plus(destination)
            .toList()
    }

    @Composable
    override fun rememberBackNavigationState(): State<Boolean> {
        val stack by stack.subscribeAsState()
        return remember { derivedStateOf { stack.backStack.isNotEmpty() } }
    }

    override fun back() = popBackStack()

    inline fun <reified R> sendResult(result: R) {
        val destination = stack.active.configuration
        // bring desired page to the front
        popBackStack {
            results.update {
                sequence {
                    yield(NavResult(destination, result))
                    yieldAll(it.take(MaxNavResults - 1))
                }.toSet()
            }
        }
    }

    suspend inline fun <reified D : Destination, reified R> subscribe(
        noinline onResult: suspend (result: R) -> Unit
    ) = results.collectLatest { list ->
        val matching = list.asSequence().filter { it.from is D && it.value is R }.toSet()
        results.update { it.asSequence().minus(matching).take(MaxNavResults).toSet() }
        matching.forEach { onResult(it.value as R) }
    }

    inline fun popBackStack(crossinline onSuccess: () -> Unit = {}) = when {
        stack.value.backStack.isEmpty() && !stack.value.active.configuration.topLevel -> navigate(Destination.Timeline)
        else -> stackNav.pop { if (it) onSuccess() }
    }

    companion object {

        const val MaxNavResults = 10
    }
}
