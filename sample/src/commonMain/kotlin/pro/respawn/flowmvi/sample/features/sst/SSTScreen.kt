@file:OptIn(ExperimentalMaterial3Api::class)

package pro.respawn.flowmvi.sample.features.sst

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.getString
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.compose.preview.EmptyReceiver
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.sst_charge_button
import pro.respawn.flowmvi.sample.sst_charged_snackbar
import pro.respawn.flowmvi.sample.sst_charging_snackbar
import pro.respawn.flowmvi.sample.sst_expired_snackbar
import pro.respawn.flowmvi.sample.sst_expires_in_label
import pro.respawn.flowmvi.sample.sst_feature_title
import pro.respawn.flowmvi.sample.sst_invoice_expired
import pro.respawn.flowmvi.sample.sst_invoice_label
import pro.respawn.flowmvi.sample.sst_payment_complete
import pro.respawn.flowmvi.sample.sst_receipt_label
import pro.respawn.flowmvi.sample.sst_start_over
import pro.respawn.flowmvi.sample.ui.theme.RespawnTheme
import pro.respawn.flowmvi.sample.ui.widgets.PreviewProvider
import pro.respawn.flowmvi.sample.ui.widgets.RErrorView
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.sample.util.rememberSnackbarHostState
import pro.respawn.kmmutils.compose.resources.string
import pro.respawn.kmmutils.datetime.asLocalTime
import pro.respawn.kmmutils.datetime.asString
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

@Composable
internal fun SSTScreen(
    navigator: AppNavigator,
) = with(container<SSTContainer, _, _, _>()) {
    val shs = rememberSnackbarHostState()
    val state by subscribe { action ->
        when (action) {
            SSTAction.ShowCharging -> shs.showSnackbar(getString(Res.string.sst_charging_snackbar))
            SSTAction.ShowCharged -> shs.showSnackbar(getString(Res.string.sst_charged_snackbar))
            SSTAction.ShowExpired -> shs.showSnackbar(getString(Res.string.sst_expired_snackbar))
        }
    }

    RScaffold(
        onBack = navigator.backNavigator,
        snackbarHostState = shs,
        title = Res.string.sst_feature_title.string()
    ) {
        SSTScreenContent(state)
    }
}

@Composable
private fun IntentReceiver<SSTIntent>.SSTScreenContent(
    state: SSTState,
) = TypeCrossfade(state) {
    when (this) {
        is SSTState.Error -> RErrorView(error)
        is SSTState.PendingInvoice -> PendingInvoiceContent(this)
        is SSTState.Charged -> ChargedContent(this)
        is SSTState.Expired -> Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = Res.string.sst_invoice_expired.string(),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { intent(SSTIntent.StartOver) }) {
                Text(Res.string.sst_start_over.string())
            }
        }
    }
}

@Composable
private fun IntentReceiver<SSTIntent>.PendingInvoiceContent(
    state: SSTState.PendingInvoice,
) = Column(
    verticalArrangement = Arrangement.SpaceEvenly,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            Res.string.sst_invoice_label.string(state.invoiceId.toString()),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))
        Text(Res.string.sst_expires_in_label.string(), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Crossfade(state.expiresIn) { expiresIn ->
            Text(
                text = expiresIn.asLocalTime.asString(addSecondsIfZero = true),
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
    Button(onClick = { intent(SSTIntent.ClickedPurchase) }) {
        Text(Res.string.sst_charge_button.string())
    }
    Spacer(Modifier.navigationBarsPadding())
}

@Composable
private fun IntentReceiver<SSTIntent>.ChargedContent(
    state: SSTState.Charged,
) = Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(Res.string.sst_payment_complete.string(), style = MaterialTheme.typography.headlineLarge)
    Spacer(Modifier.height(8.dp))
    Text(
        Res.string.sst_receipt_label.string(state.receiptId.toString()),
        style = MaterialTheme.typography.titleLarge
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = { intent(SSTIntent.StartOver) }) {
        Text(Res.string.sst_start_over.string())
    }
}

private class SSTScreenPreview : PreviewProvider<SSTState>(
    SSTState.PendingInvoice(Uuid.random(), 1.minutes),
    SSTState.Charged(Uuid.random()),
    SSTState.Expired,
)

@Composable
@Preview
private fun SSTScreenPreview(
    @PreviewParameter(SSTScreenPreview::class) state: SSTState,
) = EmptyReceiver {
    RespawnTheme {
        SSTScreenContent(state)
    }
}
