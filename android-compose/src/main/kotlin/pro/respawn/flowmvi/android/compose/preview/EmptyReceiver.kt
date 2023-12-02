package pro.respawn.flowmvi.android.compose.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.android.compose.ComposeArtifactMessage
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIIntent

@Deprecated(ComposeArtifactMessage)
@Immutable
private object EmptyReceiver : IntentReceiver<MVIIntent> {

    override fun intent(intent: MVIIntent): Unit = Unit
    override suspend fun emit(intent: MVIIntent): Unit = Unit
}

/**
 * An [IntentReceiver] that does nothing and ignores all intents. Most often used for Composable previews.
 */
@Composable
@Deprecated(ComposeArtifactMessage)
public fun <I : MVIIntent> EmptyReceiver(
    @BuilderInference call: @Composable IntentReceiver<I>.() -> Unit,
): Unit = call(EmptyReceiver as IntentReceiver<I>)
