package pro.respawn.flowmvi.sample.features.sst

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.StateStrategy
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val DemoStrategy = StateStrategy.Immediate
// private val DemoStrategy = StateStrategy.Atomic(reentrant = true)

internal class SSTContainer(
    private val repo: PurchaseRepository,
    private val containerScope: CoroutineScope,
) : Container<SSTState, SSTIntent, SSTAction> {

    private val initialDuration = 8.seconds

    @OptIn(InternalFlowMVIAPI::class, ExperimentalCoroutinesApi::class)
    override val store = store(initial = PendingInvoice()) {
        configure {
            stateStrategy = DemoStrategy
        }

        recover { e ->
            updateState { SSTState.Error(e) }
            null
        }

        whileSubscribed {
            repo.checkoutExpirationTimer(initialDuration).collect { remaining ->
                updateState {
                    when (this) {
                        is SSTState.PendingInvoice ->
                            if (remaining > Duration.ZERO) copy(expiresIn = remaining) else SSTState.Expired
                        else -> this
                    }
                }
            }
        }

        reduce { intent ->
            when (intent) {
                is SSTIntent.ClickedPurchase -> launch {
                    updateState<SSTState.PendingInvoice, _> {
                        if (expiresIn <= Duration.ZERO) {
                            action(SSTAction.ShowExpired)
                            return@updateState SSTState.Expired
                        }
                        action(SSTAction.ShowCharging)
                        val receipt = repo.charge(invoiceId, expiresIn)
                        action(SSTAction.ShowCharged)
                        SSTState.Charged(receipt.receiptId)
                    }
                }
                is SSTIntent.StartOver -> {
                    updateStateImmediate { PendingInvoice() }
                    restart()
                }
            }
        }
    }

    private fun restart() {
        containerScope.launch {
            store.closeAndWait()
            store.start(containerScope)
        }
    }

    private fun PendingInvoice() = SSTState.PendingInvoice(
        invoiceId = repo.createInvoiceId(),
        expiresIn = initialDuration
    )
}
