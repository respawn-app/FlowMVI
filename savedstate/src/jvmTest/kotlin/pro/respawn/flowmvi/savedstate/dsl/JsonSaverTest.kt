package pro.respawn.flowmvi.savedstate.dsl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
data class TestData(val value: String, val number: Int)

@Suppress("AssignedValueIsNeverRead")
class JsonSaverTest : FreeSpec({

    "Given a JsonSaver" - {

        "when serializing and deserializing valid data" - {
            "then it should work correctly" {
                var savedJson: String? = null
                val testData = TestData("test", 42)

                val stringDelegate = Saver(
                    save = { savedJson = it },
                    restore = { savedJson }
                )

                val jsonSaver = JsonSaver(
                    json = Json,
                    serializer = TestData.serializer(),
                    delegate = stringDelegate
                )

                jsonSaver.save(testData)
                savedJson shouldBe """{"value":"test","number":42}"""

                val result = jsonSaver.restore()
                result shouldBe testData
            }
        }

        "when saving null state" - {
            "then delegate should receive null" {
                var savedJson: String? = "not-null"

                val stringDelegate = Saver(
                    save = { savedJson = it },
                    restore = { savedJson }
                )

                val jsonSaver = JsonSaver(
                    json = Json,
                    serializer = TestData.serializer(),
                    delegate = stringDelegate
                )

                jsonSaver.save(null)
                savedJson shouldBe null
            }
        }

        "when delegate returns null during restore" - {
            "then restore should return null" {
                val stringDelegate = Saver<String>(
                    save = { },
                    restore = { null }
                )

                val jsonSaver = JsonSaver(
                    json = Json,
                    serializer = TestData.serializer(),
                    delegate = stringDelegate
                )

                val result = jsonSaver.restore()
                result shouldBe null
            }
        }

        "when delegate returns invalid JSON during restore" - {
            "then it should throw SerializationException" {
                val stringDelegate = Saver<String>(
                    save = { },
                    restore = { "invalid-json" }
                )

                val jsonSaver = JsonSaver(
                    json = Json,
                    serializer = TestData.serializer(),
                    delegate = stringDelegate
                )

                shouldThrow<SerializationException> {
                    jsonSaver.restore()
                }
            }
        }

        "when using custom Json configuration" - {
            "then it should respect the configuration" {
                var savedJson: String? = null
                val testData = TestData("test", 42)

                val customJson = Json {
                    prettyPrint = true
                }

                val stringDelegate = Saver(
                    save = { savedJson = it },
                    restore = { savedJson }
                )

                val jsonSaver = JsonSaver(
                    json = customJson,
                    serializer = TestData.serializer(),
                    delegate = stringDelegate
                )

                jsonSaver.save(testData)
                // Pretty printed JSON should contain newlines
                savedJson?.contains("\n") shouldBe true

                val result = jsonSaver.restore()
                result shouldBe testData
            }
        }

        "when delegate throws exception during save" - {
            "then exception should propagate" {
                val testException = RuntimeException("Save failed")
                val testData = TestData("test", 42)

                val stringDelegate = Saver<String>(
                    save = { throw testException },
                    restore = { "restored" }
                )

                val jsonSaver = JsonSaver(
                    json = Json,
                    serializer = TestData.serializer(),
                    delegate = stringDelegate
                )

                val thrownException = shouldThrow<RuntimeException> {
                    jsonSaver.save(testData)
                }
                thrownException shouldBe testException
            }
        }

        "when delegate throws exception during restore" - {
            "then exception should propagate" {
                val testException = RuntimeException("Restore failed")

                val stringDelegate = Saver<String>(
                    save = { },
                    restore = { throw testException }
                )

                val jsonSaver = JsonSaver(
                    json = Json,
                    serializer = TestData.serializer(),
                    delegate = stringDelegate
                )

                val thrownException = shouldThrow<RuntimeException> {
                    jsonSaver.restore()
                }
                thrownException shouldBe testException
            }
        }

        "when serializing complex nested data" - {
            "then it should handle nested structures correctly" {
                @Serializable
                data class NestedData(val inner: TestData, val list: List<String>)

                var savedJson: String? = null
                val nestedData = NestedData(
                    inner = TestData("nested", 123),
                    list = listOf("a", "b", "c")
                )

                val stringDelegate = Saver(
                    save = { savedJson = it },
                    restore = { savedJson }
                )

                val jsonSaver = JsonSaver(
                    json = Json,
                    serializer = NestedData.serializer(),
                    delegate = stringDelegate
                )

                jsonSaver.save(nestedData)
                val result = jsonSaver.restore()
                result shouldBe nestedData
            }
        }
    }
})
