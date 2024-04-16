package pro.respawn.flowmvi.sample.platform

interface FileManager {
    fun cacheDir(relative: String): String?
}
