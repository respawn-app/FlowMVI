package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.io.files.Path

internal actual suspend fun writeCompressed(data: String, to: Path) = write(data, to)

internal actual suspend fun readCompressed(from: Path): String? = read(from)
