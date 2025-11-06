package pro.respawn.flowmvi.sample.features.sst

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.warn
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

internal data class Receipt(
    val invoiceId: Uuid,
    val receiptId: Uuid = Uuid.random(),
)

internal class PurchaseRepository {

    fun createInvoiceId(): Uuid = Uuid.random()

    fun checkoutExpirationTimer(
        duration: Duration = 2.minutes,
    ): Flow<Duration> = flow {
        repeat(duration.inWholeSeconds.toInt()) { index ->
            emit(duration - index.seconds)
            delay(1.seconds)
        }
        emit(Duration.ZERO)
    }

    suspend fun charge(
        invoiceId: Uuid,
        remaining: Duration,
    ): Receipt {
        delay(5.seconds)
        if (remaining <= Duration.ZERO) error("Invoice $invoiceId expired")
        return Receipt(invoiceId).also { receipt ->
            PlatformStoreLogger.warn { "Charged invoice ${receipt.invoiceId}, receipt ${receipt.receiptId}" }
        }
    }
}
