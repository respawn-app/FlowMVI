package pro.respawn.flowmvi.savedstate.dsl

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

internal expect suspend fun writeCompressed(data: String, to: Path)
internal expect suspend fun readCompressed(from: Path): String?

@OptIn(ExperimentalStdlibApi::class)
internal fun write(data: String, to: Path) {
    SystemFileSystem.sink(to).buffered().use { it.writeString(data) }
}

@OptIn(ExperimentalStdlibApi::class)
internal fun read(from: Path): String = SystemFileSystem.source(from).buffered().use { it.readString() }
