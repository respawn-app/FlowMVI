package pro.respawn.flowmvi.sample.platform

import java.io.File

class JvmFileManager : FileManager {

    private val cacheDir by lazy { File(".cache").apply { mkdirs() } }

    override fun cacheFile(dir: String, filename: String): String = cacheDir.resolve(dir).resolve(filename).absolutePath
}
