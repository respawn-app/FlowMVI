package pro.respawn.flowmvi.android.compose.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIIntent

@Immutable
public object EmptyReceiver : IntentReceiver<MVIIntent> {

    override fun send(intent: MVIIntent): Unit = Unit
    override suspend fun emit(intent: MVIIntent): Unit = Unit
}

@Composable
public inline fun <I : MVIIntent> EmptyReceiver(
    @BuilderInference call: @Composable IntentReceiver<I>.() -> Unit,
): Unit = call(EmptyReceiver as IntentReceiver<I>)
