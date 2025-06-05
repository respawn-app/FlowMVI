# FlowMVI Development Guidelines

This document provides essential information for developers working on the FlowMVI project.

### Project Structure

The project is organized into several modules:
- `core`: The main library module
- `compose`: Compose Multiplatform integration
- `android`: Android-specific integration
- `essenty`: Integration with Essenty (Decompose)
- `savedstate`: State preservation utilities
- `test`: Testing utilities
- `debugger`: Remote debugging tools
- `sample`: Sample app
- `docs`: Extensive project documentation

## Building and Testing 

To build the entire project (substitute :module:):

```bash
./gradlew :module:assembleDebug --no-configuration-cache
# or
./gradlew :module:assembleRelease --no-configuration-cache
```

* Avoid using the generic `assemble` command (no module prefix and build variant) - it can take a very long time and will time out.
* Do NOT use any other commands, as they may include lint checks or other tasks that aren't necessarily needed.
* Ignore any warnings about experimental features or unsupported features. They are simply informational messages.
* Your development environment may not support mac/iOS targets fully. If builds fail, try building for android, desktop, or jvm only.

To run tests:

```bash
./gradlew :module:jvmTest --tests=path/to/test
```

* Warning: Default `test` tool call will not work for you. Use only the command above to run tests instead of your tool.* The test command above does not support running individual test cases. Rerun the entire test class, it's fine.
* Ignore any warnings issued by this command. You're interested in whether the tests passed or failed: 
  "BUILD FAILED: There were failing tests.". 

FlowMVI uses Kotest for testing, primarily with the FreeSpec style. Tests are written in a nested, descriptive style:

```kotlin
class MyTest : FreeSpec({
    "given some context" - {
        "when something happens" - {
            "then expect this result" {
                // Test code
            }
        }
    }
})
```

### Creating and Running Tests

1. Create a test file in the appropriate module's test directory:
   - `<module>/src/jvmTest/kotlin/pro/respawn/flowmvi/test/`

2. Extend `FreeSpec` and use the testing DSL:

```kotlin
class MyStoreTest : FreeSpec({
    "given a store" - {
        val store = testStore {
            // Configure store
        }

        "when an intent is emitted" - {
            "then the state is updated correctly" {
                store.subscribeAndTest {
                    // Emit an intent
                    emit(MyIntent)
                    // Assert the state
                    state shouldBe ExpectedState
                }
            }
        }
    }
})
```

### Code Style

The project uses detekt for static code analysis with a comprehensive set of rules:

- Public API must be documented (classes, functions, properties)
- Run lint checks via `./gradlew detektAll` at the end of each task.
- Comments over code are not written unless explicitly asked for. Only kDoc comments are allowed.

## Additional Resources

- Project documentation: https://opensource.respawn.pro/FlowMVI/

### Build Configuration

The project uses Gradle with Kotlin DSL for build configuration. Key configuration files:

- `build.gradle.kts`: Root project configuration
- `buildSrc/src/main/kotlin/Config.kt`: Central configuration constants
- `buildSrc/src/main/kotlin/ConfigureMultiplatform.kt`: Multiplatform target configuration

### Dependencies

Dependencies are managed through version catalogs in `gradle/libs.versions.toml`.
