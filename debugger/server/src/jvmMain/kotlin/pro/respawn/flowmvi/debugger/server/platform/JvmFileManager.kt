package pro.respawn.flowmvi.debugger.server.platform

import java.io.File

internal class JvmFileManager : FileManager {

    private val cacheDir by lazy {
        File(".cache").apply { mkdirs() }
    }

    override fun cacheFile(dir: String, filename: String): String =
        cacheDir.resolve(dir).apply { mkdirs() }.resolve(filename).absolutePath
}
