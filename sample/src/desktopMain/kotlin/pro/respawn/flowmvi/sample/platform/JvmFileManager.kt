package pro.respawn.flowmvi.sample.platform

import java.io.File

class JvmFileManager : FileManager {

    private val cacheDir by lazy {
        File("cache").apply { mkdirs() }
    }

    override fun cacheDir(relative: String): String = cacheDir.resolve(relative).absolutePath
}
