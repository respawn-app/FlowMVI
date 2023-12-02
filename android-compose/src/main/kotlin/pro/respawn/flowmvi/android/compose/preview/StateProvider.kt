package pro.respawn.flowmvi.android.compose.preview

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import pro.respawn.flowmvi.android.compose.ComposeArtifactMessage
import pro.respawn.flowmvi.api.MVIState

/**
 * A collection preview param provider that provides [MVIState]
 * Created to avoid boilerplate related to Preview parameters.
 */
@Deprecated(ComposeArtifactMessage)
public open class StateProvider<S>(
    vararg states: S,
) : CollectionPreviewParameterProvider<S>(states.toList())
