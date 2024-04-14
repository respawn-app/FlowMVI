package pro.respawn.flowmvi.sample.navigation.util

import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.instancekeeper.getOrCreateSimple
import pro.respawn.flowmvi.sample.navigation.destination.Destination
import pro.respawn.kmmutils.common.fastLazy

internal infix fun Destination.duplicateOf(
    other: Destination
) = if (singleTop) this::class == other::class else this == other

internal inline fun <reified T> InstanceKeeperOwner.retained(
    crossinline block: () -> T
) = fastLazy { instanceKeeper.getOrCreateSimple<T> { block() } }
