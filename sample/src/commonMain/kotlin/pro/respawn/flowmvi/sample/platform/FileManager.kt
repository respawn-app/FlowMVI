package pro.respawn.flowmvi.sample.platform

interface FileManager {

    fun cacheFile(dir: String, filename: String): String
}
