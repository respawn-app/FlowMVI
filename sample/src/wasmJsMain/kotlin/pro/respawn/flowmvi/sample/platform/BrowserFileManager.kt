package pro.respawn.flowmvi.sample.platform

internal class BrowserFileManager : FileManager {

    override fun cacheFile(dir: String, filename: String): String = "cache/$dir/$filename"
}
