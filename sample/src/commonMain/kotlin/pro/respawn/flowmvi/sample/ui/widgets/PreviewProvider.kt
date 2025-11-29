package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class PreviewProvider<T>(vararg items: T) : PreviewParameterProvider<T> {

    override val values: Sequence<T> = items.asSequence()
}
