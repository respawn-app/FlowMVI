package pro.respawn.flowmvi.sample.features.sst

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import kotlin.time.Duration
import kotlin.uuid.Uuid

internal sealed interface SSTState : MVIState {
    data class PendingInvoice(
        val invoiceId: Uuid,
        val expiresIn: Duration,
    ) : SSTState

    data class Charged(val receiptId: Uuid) : SSTState
    data object Expired : SSTState
    data class Error(val error: Exception?) : SSTState
}

internal sealed interface SSTIntent : MVIIntent {
    data object ClickedPurchase : SSTIntent
    data object StartOver : SSTIntent
}

internal sealed interface SSTAction : MVIAction {
    data object ShowCharging : SSTAction
    data object ShowCharged : SSTAction
    data object ShowExpired : SSTAction
}
