package pro.respawn.flowmvi.savedstate.platform

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

@PublishedApi
internal actual object FileAccess {

    actual suspend fun writeCompressed(data: String?, path: String) = write(data, path)

    actual suspend fun readCompressed(path: String): String? = read(path)

    @OptIn(ExperimentalStdlibApi::class)
    actual suspend fun write(data: String?, path: String) {
        SystemFileSystem.run {
            val file = Path(path)
            if (data == null) {
                delete(file, false)
                return@run
            }
            sink(file).buffered().use { it.writeString(data) }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    actual suspend fun read(path: String): String? = SystemFileSystem
        .source(Path(path))
        .buffered()
        .use { it.readString() }
        .takeIf { it.isNotBlank() }
}
