package pro.respawn.flowmvi.savedstate.platform

import kotlinx.browser.localStorage

@PublishedApi
internal actual object FileAccess {

    actual suspend fun writeCompressed(data: String?, path: String) = write(data, path)

    actual suspend fun readCompressed(path: String): String? = read(path)

    actual suspend fun write(data: String?, path: String) =
        if (data != null) localStorage.setItem(path, data) else localStorage.removeItem(path)

    actual suspend fun read(path: String): String? = localStorage.getItem(path)
}
