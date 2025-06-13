package pro.respawn.flowmvi.savedstate.platform

import kotlinx.io.files.SystemFileSystem

internal actual suspend fun writeCompressed(data: String?, path: String) = write(data, path)

internal actual suspend fun readCompressed(path: String): String? = read(path)

internal actual suspend fun write(data: String?, path: String) = write(data, path, SystemFileSystem)

internal actual suspend fun read(path: String): String? = read(path, SystemFileSystem)
