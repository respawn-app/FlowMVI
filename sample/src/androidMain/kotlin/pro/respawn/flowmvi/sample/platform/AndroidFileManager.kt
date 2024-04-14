package pro.respawn.flowmvi.sample.platform

import android.content.Context

class AndroidFileManager(context: Context) : FileManager {

    private val cacheDir = context.cacheDir

    override fun cacheDir(relative: String): String = cacheDir.resolve(relative).absolutePath
}
