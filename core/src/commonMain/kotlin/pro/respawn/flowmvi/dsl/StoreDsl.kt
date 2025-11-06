@file:MustUseReturnValue

package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import kotlin.jvm.JvmName

/**
 * Build a new [Store] using [StoreBuilder].
 * The store is created eagerly, with all its plugins.
 * However, the store is not launched upon creation until called "launch", which means,
 * no heavy operations will be performed at creation time.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> store(
    initial: S,
    @BuilderInference configure: BuildStore<S, I, A>,
): Store<S, I, A> = StoreBuilder<S, I, A>(initial).apply(configure).invoke()

/**
 * Build a new [Store] using [StoreBuilder].
 * The store is created eagerly and then launched in the given [scope] immediately.
 **/
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> store(
    initial: S,
    scope: CoroutineScope,
    @BuilderInference configure: BuildStore<S, I, A>,
): Store<S, I, A> = store(initial, configure).apply { start(scope) }

/**
 * * Build a new [Store] using [StoreBuilder] but disallow using [MVIAction]s.
 * The store is **not** launched, but is created eagerly, with all its plugins.
 *
 * If your code doesn't compile, you are looking for another overload with three type parameters, i.e:
 * `store<_, _, _>()`
 */
@FlowMVIDSL
@JvmName("noActionStore")
// https://youtrack.jetbrains.com/issue/KT-16255
@Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
)
@kotlin.internal.LowPriorityInOverloadResolution
public inline fun <S : MVIState, I : MVIIntent> store(
    initial: S,
    @BuilderInference configure: BuildStore<S, I, Nothing>,
): Store<S, I, Nothing> = store(initial) {
    configure()
    configure {
        actionShareBehavior = ActionShareBehavior.Disabled
    }
}

/**
 * * Build a new [Store] using [StoreBuilder] but disallow using [MVIAction]s.
 * The store is **not** launched, but is created eagerly, with all its plugins.
 *
 * If your code doesn't compile, you are looking for another overload with three type parameters, i.e:
 * `store<_, _, _>()`
 */
@FlowMVIDSL
@JvmName("noActionStore")
// https://youtrack.jetbrains.com/issue/KT-16255
@Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
)
@kotlin.internal.LowPriorityInOverloadResolution
public inline fun <S : MVIState, I : MVIIntent> store(
    initial: S,
    scope: CoroutineScope,
    @BuilderInference configure: BuildStore<S, I, Nothing>,
): Store<S, I, Nothing> = store(initial, scope) {
    configure()
    configure {
        actionShareBehavior = ActionShareBehavior.Disabled
    }
}

/**
 * Build a new [Store] using [StoreBuilder].
 *  The store is created lazily, with all its plugins.
 *  The store is **not** launched.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    initial: S,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference crossinline configure: BuildStore<S, I, A>,
): Lazy<Store<S, I, A>> = lazy(mode) { store(initial, configure) }

/**
 * Build a new [Store] using [StoreBuilder].
 * The store is built **lazily** and launched on **first access** (i.e. immediately after creation).
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    initial: S,
    scope: CoroutineScope,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference crossinline configure: BuildStore<S, I, A>,
): Lazy<Store<S, I, A>> = lazy(mode) { store(initial, configure).apply { start(scope) } }
