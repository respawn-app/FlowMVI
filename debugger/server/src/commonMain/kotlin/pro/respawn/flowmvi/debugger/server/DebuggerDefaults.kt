package pro.respawn.flowmvi.debugger.server

import kotlinx.serialization.json.Json

object DebuggerDefaults {

    val PrettyPrintJson = Json {
        encodeDefaults = false
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    const val ReportIssueUrl =
        "${BuildFlags.ProjectUrl}/issues/new?labels=bug%2C+triage&projects=&template=bug_report.md"
}
