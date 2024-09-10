package pro.respawn.flowmvi.debugger.server.arch.configuration

data object BuildFlags {
    val debuggable = System.getenv("DEBUG")?.toBooleanStrictOrNull() ?: false
}
