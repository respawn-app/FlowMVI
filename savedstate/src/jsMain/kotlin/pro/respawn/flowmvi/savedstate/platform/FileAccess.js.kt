package pro.respawn.flowmvi.savedstate.platform

import kotlinx.browser.localStorage

internal actual suspend fun writeCompressed(data: String?, path: String) = write(data, path)

internal actual suspend fun readCompressed(path: String): String? = read(path)

internal actual suspend fun write(
    data: String?, path: String
) = if (data != null) localStorage.setItem(path, data) else localStorage.removeItem(path)

internal actual suspend fun read(path: String): String? = localStorage.getItem(path)
