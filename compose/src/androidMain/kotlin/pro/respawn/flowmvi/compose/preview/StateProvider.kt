package pro.respawn.flowmvi.compose.preview

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider

/**
 * Preview provider that takes a vararg argument for convenience
 */
@Deprecated(
    """
    FlowMVI will no longer provide preview functionality as it is platform-dependent and out of scope of the library. 
    Please copy and paste the code of the provider to your repository if you need it.
""",
    ReplaceWith(
        "CollectionPreviewParameterProvider<T>(states.asList())",
        "androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider",
    )
)
public open class StateProvider<T>(vararg states: T) : CollectionPreviewParameterProvider<T>(states.asList())
