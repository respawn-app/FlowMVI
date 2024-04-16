package pro.respawn.flowmvi.sample.platform

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

class NativeFileManager : FileManager {

    private val cacheDir = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).first()

    override fun cacheDir(relative: String): String = "$cacheDir/$relative"
}
