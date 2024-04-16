package pro.respawn.flowmvi.sample.arch.di

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinScopeComponent

@Stable
interface DestinationScope : KoinScopeComponent {

    val coroutineScope: CoroutineScope
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
