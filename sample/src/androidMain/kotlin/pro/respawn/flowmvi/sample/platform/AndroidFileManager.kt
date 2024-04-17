package pro.respawn.flowmvi.sample.platform

import android.content.Context

class AndroidFileManager(context: Context) : FileManager {

    private val cacheDir = context.cacheDir

    override fun cacheFile(dir: String, filename: String): String = cacheDir.resolve(dir).resolve(filename).absolutePath
}
