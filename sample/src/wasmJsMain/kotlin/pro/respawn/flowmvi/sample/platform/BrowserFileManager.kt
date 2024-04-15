package pro.respawn.flowmvi.sample.platform

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

internal class BrowserFileManager : FileManager {

    override fun cacheDir(relative: String): String = SystemFileSystem.resolve(Path(relative)).name
}
