package pro.respawn.flowmvi.logging

internal fun defaultLogger(debuggable: Boolean) = if (debuggable) PlatformStoreLogger else NoOpStoreLogger
