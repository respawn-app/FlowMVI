package pro.respawn.flowmvi.debugger.server.platform

interface FileManager {
    fun cacheFile(dir: String, filename: String): String
}
