package pro.respawn.flowmvi.savedstate.platform

@PublishedApi
internal expect object FileAccess {

    suspend fun writeCompressed(data: String?, path: String)
    suspend fun readCompressed(path: String): String?
    suspend fun write(data: String?, path: String)
    suspend fun read(path: String): String?
}
