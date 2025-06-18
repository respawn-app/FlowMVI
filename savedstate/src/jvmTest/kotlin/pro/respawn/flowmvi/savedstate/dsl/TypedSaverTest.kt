package pro.respawn.flowmvi.savedstate.dsl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

sealed class TestState {
    data class StateA(val value: String) : TestState()
    data class StateB(val number: Int) : TestState()
}

class TypedSaverTest : FreeSpec({

    "Given a MapSaver" - {

        "when mapping between compatible types" - {
            "then it should transform data correctly" {
                var savedInt: Int? = null

                val intDelegate = Saver(
                    save = { savedInt = it },
                    restore = { savedInt }
                )

                val stringToIntSaver = MapSaver(
                    delegate = intDelegate,
                    from = { it.toString() },
                    to = { it?.toIntOrNull() }
                )

                stringToIntSaver.save("42")
                savedInt shouldBe 42

                val result = stringToIntSaver.restore()
                result shouldBe "42"
            }
        }

        "when saving null state" - {
            "then to function should receive null and delegate should save null" {
                var savedInt: Int? = null
                var toFunctionCalled = false
                var toFunctionValue: String? = "not-null"

                val intDelegate = Saver(
                    save = { savedInt = it },
                    restore = { savedInt }
                )

                val stringToIntSaver = MapSaver(
                    delegate = intDelegate,
                    from = { it.toString() },
                    to = { value ->
                        toFunctionCalled = true
                        toFunctionValue = value
                        value?.toIntOrNull()
                    }
                )

                stringToIntSaver.save(null)
                toFunctionCalled shouldBe true
                toFunctionValue shouldBe null
                savedInt shouldBe null
            }
        }

        "when delegate returns null during restore" - {
            "then restore should return null without calling from function" {
                var fromFunctionCalled = false

                val intDelegate = Saver<Int>(
                    save = { },
                    restore = { null }
                )

                val stringToIntSaver = MapSaver(
                    delegate = intDelegate,
                    from = { value ->
                        fromFunctionCalled = true
                        value.toString()
                    },
                    to = { it?.toIntOrNull() }
                )

                val result = stringToIntSaver.restore()
                result shouldBe null
                fromFunctionCalled shouldBe false
            }
        }

        "when from function returns null" - {
            "then restore should return null" {
                var savedInt: Int? = 42

                val intDelegate = Saver(
                    save = { savedInt = it },
                    restore = { savedInt }
                )

                val stringToIntSaver = MapSaver<String, Int>(
                    delegate = intDelegate,
                    from = { _: Int -> null }, // Always return null
                    to = { it?.toIntOrNull() }
                )

                val result = stringToIntSaver.restore()
                result shouldBe null
            }
        }

        "when to function returns null" - {
            "then delegate should save null" {
                var savedInt: Int? = null

                val intDelegate = Saver(
                    save = { savedInt = it },
                    restore = { savedInt }
                )

                val stringToIntSaver = MapSaver(
                    delegate = intDelegate,
                    from = { it.toString() },
                    to = { null } // Always return null
                )

                stringToIntSaver.save("42")
                savedInt shouldBe null
            }
        }

        "when delegate throws exception during save" - {
            "then exception should propagate after transformation" {
                val testException = RuntimeException("Save failed")
                var transformedValue: Int? = null

                val intDelegate = Saver<Int>(
                    save = { throw testException },
                    restore = { 0 }
                )

                val stringToIntSaver = MapSaver(
                    delegate = intDelegate,
                    from = { it.toString() },
                    to = { value ->
                        transformedValue = value?.toIntOrNull()
                        transformedValue
                    }
                )

                val thrownException = shouldThrow<RuntimeException> {
                    stringToIntSaver.save("42")
                }
                thrownException shouldBe testException
                transformedValue shouldBe 42 // Transformation should have happened
            }
        }

        "when delegate throws exception during restore" - {
            "then exception should propagate" {
                val testException = RuntimeException("Restore failed")

                val intDelegate = Saver<Int>(
                    save = { },
                    restore = { throw testException }
                )

                val stringToIntSaver = MapSaver(
                    delegate = intDelegate,
                    from = { it.toString() },
                    to = { it?.toIntOrNull() }
                )

                val thrownException = shouldThrow<RuntimeException> {
                    stringToIntSaver.restore()
                }
                thrownException shouldBe testException
            }
        }
    }

    "Given a TypedSaver" - {

        "when filtering compatible types" - {
            "then it should only save and restore matching types" {
                var savedStateA: TestState.StateA? = null

                val stateADelegate = Saver(
                    save = { savedStateA = it },
                    restore = { savedStateA }
                )

                val typedSaver = TypedSaver<TestState.StateA, TestState>(stateADelegate)

                val stateA = TestState.StateA("test")
                val stateB = TestState.StateB(42)

                // Save StateA - should work
                typedSaver.save(stateA)
                savedStateA shouldBe stateA

                val restoredA = typedSaver.restore()
                restoredA shouldBe stateA

                // Save StateB - should save null since it's not StateA
                typedSaver.save(stateB)
                savedStateA shouldBe null

                val restoredB = typedSaver.restore()
                restoredB shouldBe null
            }
        }

        "when saving null state" - {
            "then it should pass null to delegate" {
                var savedStateA: TestState.StateA? = TestState.StateA("not-null")

                val stateADelegate = Saver(
                    save = { savedStateA = it },
                    restore = { savedStateA }
                )

                val typedSaver = TypedSaver<TestState.StateA, TestState>(stateADelegate)

                typedSaver.save(null)
                savedStateA shouldBe null
            }
        }

        "when delegate returns null during restore" - {
            "then restore should return null" {
                val stateADelegate = Saver<TestState.StateA>(
                    save = { },
                    restore = { null }
                )

                val typedSaver = TypedSaver<TestState.StateA, TestState>(stateADelegate)

                val result = typedSaver.restore()
                result shouldBe null
            }
        }

        "when delegate throws exception during save" - {
            "then exception should propagate" {
                val testException = RuntimeException("Save failed")

                val stateADelegate = Saver<TestState.StateA>(
                    save = { throw testException },
                    restore = { TestState.StateA("test") }
                )

                val typedSaver = TypedSaver<TestState.StateA, TestState>(stateADelegate)

                val thrownException = shouldThrow<RuntimeException> {
                    typedSaver.save(TestState.StateA("test"))
                }
                thrownException shouldBe testException
            }
        }

        "when delegate throws exception during restore" - {
            "then exception should propagate" {
                val testException = RuntimeException("Restore failed")

                val stateADelegate = Saver<TestState.StateA>(
                    save = { },
                    restore = { throw testException }
                )

                val typedSaver = TypedSaver<TestState.StateA, TestState>(stateADelegate)

                val thrownException = shouldThrow<RuntimeException> {
                    typedSaver.restore()
                }
                thrownException shouldBe testException
            }
        }
    }
})
