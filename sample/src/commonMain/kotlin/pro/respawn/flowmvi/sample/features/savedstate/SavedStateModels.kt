@file:UseSerializers(InputSerializer::class)

package pro.respawn.flowmvi.sample.features.savedstate

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.sample.util.InputSerializer
import pro.respawn.kmmutils.inputforms.Input
import pro.respawn.kmmutils.inputforms.dsl.input
import kotlin.jvm.JvmInline

@Immutable
internal sealed interface SavedStateFeatureState : MVIState {

    // sealed class for demonstration purposes - plugin will only save the type for which serializer is provided

    @Serializable
    data class DisplayingInput(
        val input: Input = input(),
    ) : SavedStateFeatureState
}

@Immutable
internal sealed interface SavedStateIntent : MVIIntent {

    @JvmInline
    value class ChangedInput(val value: String) : SavedStateIntent
}
