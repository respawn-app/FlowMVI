package pro.respawn.flowmvi.android.compose

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import pro.respawn.flowmvi.MVIState

public open class StateProvider<S : MVIState>(
    vararg states: S,
) : CollectionPreviewParameterProvider<S>(states.toList())
