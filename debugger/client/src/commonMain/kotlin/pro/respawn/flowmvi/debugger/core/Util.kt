package pro.respawn.flowmvi.debugger.core

import pro.respawn.flowmvi.api.EmptyState
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.LambdaIntent

internal inline val <T : MVIIntent> T.name: String
    get() = when (this) {
        is LambdaIntent<*, *> -> "LambdaIntent"
        else -> this::class.simpleName?.removeSuffix("Intent") ?: "Anonymous"
    }

internal inline val <T : MVIAction> T.name: String get() = this::class.simpleName?.removeSuffix("Action") ?: "Anonymous"

internal inline val <T : MVIState> T.name: String
    get() = when (this) {
        is EmptyState -> "Empty"
        else -> this::class.simpleName?.removeSuffix("State") ?: "Anonymous"
    }

internal inline val <T : Exception> T.name: String
    get() = this::class.simpleName?.removeSuffix("Exception") ?: "Anonymous"
