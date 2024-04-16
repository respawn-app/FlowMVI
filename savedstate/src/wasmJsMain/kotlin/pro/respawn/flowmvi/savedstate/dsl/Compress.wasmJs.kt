package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.browser.localStorage
import kotlinx.io.files.Path

internal actual suspend fun writeCompressed(data: String, to: Path) = localStorage.setItem(to.name, data)

internal actual suspend fun readCompressed(from: Path): String? = localStorage.getItem(from.name)
