package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.plugins.reduce
import kotlin.jvm.JvmInline

/**
 * An intent that is a holder for a [block] that acts as this intent's action.
 * LambdaIntents enable MVVM+ -style declaration of your [pro.respawn.flowmvi.api.Store].
 * Use [reduceLambdas] to handle lambda intents in your store.
 * Use [send] to send lambda intents and define a block for them to be processed.
 * **When using lambda intents, some plugins wil not work correctly, and some will become useless,
 * e.g. logging of lambda intents**
 */
@JvmInline
public value class LambdaIntent<S : MVIState, A : MVIAction>(
    private val block: suspend PipelineContext<S, LambdaIntent<S, A>, A>.() -> Unit
) : MVIIntent {

    /**
     * Invoke the [block] of this intent
     */
    internal suspend operator fun PipelineContext<S, LambdaIntent<S, A>, A>.invoke(): Unit = block.invoke(this)
}

/**
 * An alias for [pro.respawn.flowmvi.api.IntentReceiver.send] ([LambdaIntent] ([block]))
 */
public fun <S : MVIState, A : MVIAction> IntentReceiver<LambdaIntent<S, A>>.send(
    @BuilderInference block: suspend PipelineContext<S, LambdaIntent<S, A>, A>.() -> Unit
): Unit = intent(LambdaIntent(block))

/**
 * Install a new [pro.respawn.flowmvi.plugins.reducePlugin] that is tailored for [LambdaIntent]s.
 */
@FlowMVIDSL
public fun <S : MVIState, A : MVIAction> StoreBuilder<S, LambdaIntent<S, A>, A>.reduceLambdas(): Unit = reduce {
    with(it) { invoke() }
}
