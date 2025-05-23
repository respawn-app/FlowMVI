package pro.respawn.flowmvi.debugger.server.ui.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.awtClipboard
import pro.respawn.apiresult.orNull
import pro.respawn.apiresult.runResulting
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalComposeUiApi::class)
fun Clipboard.setText(string: String) {
    val awtClipboard = runResulting { awtClipboard }.orNull()
    val selection = StringSelection(string)
    awtClipboard?.setContents(selection, selection)
}
