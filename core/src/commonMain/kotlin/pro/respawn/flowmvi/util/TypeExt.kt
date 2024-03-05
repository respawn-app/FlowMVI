package pro.respawn.flowmvi.util

import pro.respawn.flowmvi.api.FlowMVIDSL
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Do the operation on [this] if the type of [this] is [T], and return [R], otherwise return [this]
 */
@FlowMVIDSL
public inline fun <reified T : R, R> R.withType(@BuilderInference block: T.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return typed<T>()?.let(block) ?: this
}

/**
 * Cast this to type [T] or return null.
 *
 * Just an alias for `(this as? T)`
 */
@FlowMVIDSL
public inline fun <reified T> Any?.typed(): T? = this as? T

/**
 * Get the name of the class, removing the "State" suffix, if present.
 */
public inline fun <reified T> nameByType(): String? = T::class.simpleName?.removeSuffix("State")
