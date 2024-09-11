package pro.respawn.flowmvi.debugger.server.ui.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format

val TimestampFormatter: (LocalDateTime) -> String = { it.format(LocalDateTime.Formats.ISO) }
