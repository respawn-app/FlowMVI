package pro.respawn.flowmvi.util

import pro.respawn.flowmvi.api.FlowMVIDSL
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Do the operation on [this] if the type of [this] is [T], and return [R], otherwise return [this]
 */
@OverloadResolutionByLambdaReturnType
@FlowMVIDSL
public inline fun <reified T, R> R.withType(@BuilderInference block: T.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return (this as? T)?.let(block) ?: this
}
