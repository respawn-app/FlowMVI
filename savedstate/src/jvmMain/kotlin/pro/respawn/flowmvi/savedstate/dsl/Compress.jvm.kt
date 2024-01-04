package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

// TODO: Can't share sources between jvm and android yet

@Suppress("INVISIBLE_MEMBER") // we want to access the JVM api of kotlinx.io...
internal actual suspend fun writeCompressed(data: String, to: Path) = withContext(Dispatchers.IO) {
    GZIPOutputStream(FileOutputStream(to.file)).writer().use { it.write(data) }
}

@Suppress("INVISIBLE_MEMBER") // we want to access the JVM api of kotlinx.io...
internal actual suspend fun readCompressed(from: Path): String? = withContext(Dispatchers.IO) {
    GZIPInputStream(FileInputStream(from.file)).reader().use { it.readText() }
}
