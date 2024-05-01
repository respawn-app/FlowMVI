package pro.respawn.flowmvi.savedstate.platform

internal expect suspend fun writeCompressed(data: String?, path: String)

internal expect suspend fun readCompressed(path: String): String?

internal expect suspend fun write(data: String?, path: String)

internal expect suspend fun read(path: String): String?
