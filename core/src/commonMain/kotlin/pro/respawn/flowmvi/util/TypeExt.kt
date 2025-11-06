@file:MustUseReturnValue

package pro.respawn.flowmvi.util

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIState
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KMutableProperty0

private fun duplicatePropMessage(name: String) = """
    Value of $name has already been set. Setting the value of this property multiple times will override any previous
    invocations, which is likely not what you meant to do. 
    Please merge the logic from the second invocation with the first one.
""".trimIndent()

/**
 * Do the operation on [this] if the type of [this] is [T], and return [R], otherwise return [this]
 */
@FlowMVIDSL
@IgnorableReturnValue
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
@Deprecated("Usage of this function leads to some unintended consequences when enabling code obfuscation")
public inline fun <reified T : MVIState> nameByType(): String? = T::class.simpleName?.removeSuffix("State")

internal infix fun <T> KMutableProperty0<T?>.setOnce(value: T) {
    require(get() == null) { duplicatePropMessage(name) }
    set(value)
}
