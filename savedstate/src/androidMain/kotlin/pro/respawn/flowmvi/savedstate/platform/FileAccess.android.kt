package pro.respawn.flowmvi.savedstate.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

@PublishedApi
internal actual object FileAccess {

    private fun File.outputStreamOrEmpty() = run {
        mkdirs()
        createNewFile()
        outputStream()
    }

    private fun InputStream.readOrNull() = reader().use { it.readText() }.takeIf { it.isNotBlank() }

    actual suspend fun writeCompressed(data: String?, path: String) = withContext(Dispatchers.IO) {
        val file = File(path)
        if (data == null) {
            file.delete()
            return@withContext
        }
        file.outputStreamOrEmpty().let(::GZIPOutputStream).writer().use { it.write(data) }
    }

    actual suspend fun readCompressed(path: String): String? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) return@withContext null
        file.inputStream().let(::GZIPInputStream).readOrNull()
    }

    actual suspend fun write(data: String?, path: String) = withContext(Dispatchers.IO) {
        val file = File(path)
        if (data == null) {
            file.delete()
            return@withContext
        }
        file.outputStreamOrEmpty().writer().use { it.write(data) }
    }

    actual suspend fun read(path: String): String? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) return@withContext null
        file.inputStream().readOrNull()
    }
}
