package pro.respawn.flowmvi.debugger.server.arch.configuration

import pro.respawn.flowmvi.debugger.server.BuildFlags

val BuildFlags.debuggable by lazy { System.getenv("DEBUG")?.toBooleanStrictOrNull() ?: false }
