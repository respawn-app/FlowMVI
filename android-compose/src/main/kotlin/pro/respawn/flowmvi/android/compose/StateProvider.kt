package pro.respawn.flowmvi.android.compose

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import pro.respawn.flowmvi.api.MVIState

/**
 * A collection preview param provider that provides [MVIState]
 * Created to avoid boilerplate related to Preview parameters.
 */
public open class StateProvider<S>(
    vararg states: S,
) : CollectionPreviewParameterProvider<S>(states.toList())
