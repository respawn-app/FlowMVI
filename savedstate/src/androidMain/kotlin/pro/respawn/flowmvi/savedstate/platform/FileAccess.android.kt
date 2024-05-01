package pro.respawn.flowmvi.savedstate.platform

import java.io.File
import java.io.InputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private fun File.outputStreamOrEmpty() = run {
    parentFile?.mkdirs()
    createNewFile()
    outputStream()
}

private fun InputStream.readOrNull() = bufferedReader().use { it.readText() }.takeIf { it.isNotBlank() }

internal actual suspend fun writeCompressed(data: String?, path: String) {
    val file = File(path)
    if (data == null) {
        file.delete()
        return
    }
    return file.outputStreamOrEmpty().let(::GZIPOutputStream).bufferedWriter().use { it.write(data) }
}

internal actual suspend fun readCompressed(path: String): String? {
    val file = File(path)
    if (!file.exists()) return null
    return file.inputStream().let(::GZIPInputStream).readOrNull()
}

internal actual suspend fun write(data: String?, path: String) {
    val file = File(path)
    if (data == null) {
        file.delete()
        return
    }
    return file.outputStreamOrEmpty().bufferedWriter().use { it.write(data) }
}

internal actual suspend fun read(path: String): String? {
    val file = File(path)
    if (!file.exists()) return null
    return file.inputStream().readOrNull()
}
