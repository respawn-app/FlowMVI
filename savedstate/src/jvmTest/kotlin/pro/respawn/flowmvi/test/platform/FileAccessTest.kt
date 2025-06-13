package pro.respawn.flowmvi.test.platform

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.io.files.SystemFileSystem
import pro.respawn.flowmvi.savedstate.platform.read
import pro.respawn.flowmvi.savedstate.platform.write
import java.util.UUID

class FileAccessTest : FreeSpec({
    val testDir = "build/tmp/test-${UUID.randomUUID()}"
    // TODO: Kotlinx.io does not support FakeFileSystem yet
    val fs = SystemFileSystem

    "Given a file system" - {
        val testPath = "$testDir/file.txt"

        "when writing data to a file" - {
            val testData = "Hello, World!"

            "then the file should be created with the data" {
                write(testData, testPath, fs)
                val result = read(testPath, fs)
                result shouldBe testData
            }
        }

        "when writing null data to an existing file" - {
            val testData = "Initial data"
            write(testData, testPath, fs)

            "then the file should be deleted" {
                write(null, testPath, fs)
                val result = read(testPath, fs)
                result shouldBe null
            }
        }

        "when writing to a nested path that doesn't exist" - {
            val nestedPath = "$testDir/deep/nested/directory/file.txt"
            val testData = "Nested file content"

            "then directories should be created automatically" {
                write(testData, nestedPath, fs)
                val result = read(nestedPath, fs)
                result shouldBe testData
            }
        }

        "when reading from a non-existent file" - {
            val nonExistentPath = "$testDir/non/existent/file.txt"

            "then it should return null" {
                val result = read(nonExistentPath, fs)
                result shouldBe null
            }
        }

        "when reading from a file with blank content" - {
            val blankPath = "$testDir/blank/file.txt"
            val blankData = "   \n\t  "

            "then it should return null for blank content" {
                write(blankData, blankPath, fs)
                val result = read(blankPath, fs)
                result shouldBe null
            }
        }

        "when reading from a file with empty content" - {
            val emptyPath = "$testDir/empty/file.txt"
            val emptyData = ""

            "then it should return null for empty content" {
                write(emptyData, emptyPath, fs)
                val result = read(emptyPath, fs)
                result shouldBe null
            }
        }

        "when writing and reading various data types" - {
            "then it should handle JSON-like strings" {
                val jsonData = """{"key": "value", "number": 42}"""
                val jsonPath = "$testDir/data/json.txt"
                write(jsonData, jsonPath, fs)
                val result = read(jsonPath, fs)
                result shouldBe jsonData
            }

            "then it should handle multiline strings" {
                val multilineData = """
                    Line 1
                    Line 2
                    Line 3
                """.trimIndent()
                val multilinePath = "$testDir/data/multiline.txt"
                write(multilineData, multilinePath, fs)
                val result = read(multilinePath, fs)
                result shouldBe multilineData
            }

            "then it should handle special characters" {
                val specialData = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?"
                val specialPath = "$testDir/data/special.txt"
                write(specialData, specialPath, fs)
                val result = read(specialPath, fs)
                result shouldBe specialData
            }
        }

        "when overwriting existing files" - {
            val overwritePath = "$testDir/overwrite/file.txt"
            val originalData = "Original content"
            val newData = "New content"

            "then the new data should replace the old data" {
                write(originalData, overwritePath, fs)
                write(newData, overwritePath, fs)
                val result = read(overwritePath, fs)
                result shouldBe newData
            }
        }
    }
})
