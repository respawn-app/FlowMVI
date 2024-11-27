package pro.respawn.flowmvi.debugger.server.ui.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

private val Timestamp = LocalDateTime.Format {
    date(LocalDate.Formats.ISO)
    char(' ')
    hour(Padding.ZERO)
    char(':')
    minute(Padding.ZERO)
    char(':')
    second(Padding.ZERO)
    char('.')
    secondFraction(3, 3)
}

val TimestampFormatter: (LocalDateTime) -> String = { it.format(Timestamp) }
