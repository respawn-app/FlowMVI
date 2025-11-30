package pro.respawn.flowmvi.test.decorator

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.configure

@OptIn(ExperimentalFlowMVIAPI::class)
class DecoratorBuilderTest : FreeSpec({
    configure()

    "given decorator builder" - {
        "when callbacks set once" - {
            "then decorator builds successfully" {
                shouldNotThrowAny {
                    decorator<TestState, TestIntent, TestAction> {
                        onStart { child -> child.run { onStart() } }
                        onIntent { _, intent -> intent }
                        onState { _, old, _ -> old }
                    }
                }
            }
        }
        "when onIntentEnqueue assigned twice" - {
            "then builder throws" {
                shouldThrowExactly<IllegalArgumentException> {
                    decorator<TestState, TestIntent, TestAction> {
                        onIntentEnqueue { _, intent -> intent }
                        onIntentEnqueue { _, intent -> intent }
                    }
                }
            }
        }
        "when onActionDispatch assigned twice" - {
            "then builder throws" {
                shouldThrowExactly<IllegalArgumentException> {
                    decorator<TestState, TestIntent, TestAction> {
                        onActionDispatch { _, action -> action }
                        onActionDispatch { _, action -> action }
                    }
                }
            }
        }
        "when onIntent assigned twice" - {
            "then builder throws" {
                shouldThrowExactly<IllegalArgumentException> {
                    decorator<TestState, TestIntent, TestAction> {
                        onIntent { _, intent -> intent }
                        onIntent { _, intent -> intent }
                    }
                }
            }
        }
        "when onStop assigned twice" - {
            "then builder throws" {
                shouldThrowExactly<IllegalArgumentException> {
                    decorator<TestState, TestIntent, TestAction> {
                        onStop { child, _ -> child.run { onStop(null) } }
                        onStop { _, _ -> }
                    }
                }
            }
        }
    }
})
