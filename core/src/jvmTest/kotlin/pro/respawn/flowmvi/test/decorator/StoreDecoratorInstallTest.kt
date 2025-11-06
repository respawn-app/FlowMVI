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
import pro.respawn.flowmvi.util.testStore

@OptIn(ExperimentalFlowMVIAPI::class)
class StoreDecoratorInstallTest : FreeSpec({
    configure()

    "given store builder decorator registration" - {
        "when same decorator instance installed twice" - {
            "then installation fails" {
                shouldThrowExactly<IllegalArgumentException> {
                    testStore {
                        val decorator = decorator<TestState, TestIntent, TestAction> { }
                        decorator.install()
                        decorator.install()
                    }
                }
            }
        }
        "when decorators share a name" - {
            "then installation fails" {
                shouldThrowExactly<IllegalArgumentException> {
                    testStore {
                        decorate {
                            name = "dup"
                        }
                        decorate {
                            name = "dup"
                        }
                    }
                }
            }
        }
        "when decorators have unique identity" - {
            "then installation succeeds" {
                shouldNotThrowAny {
                    testStore {
                        decorate { name = "first" }
                        decorate { name = "second" }
                        decorate { }
                        decorate { }
                    }
                }
            }
        }
    }
})
