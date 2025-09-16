@docs/ai/tool-preferences.md

FlowMVI is a Kotlin Multiplatform MVI (Model-View-Intent) framework built on coroutines.
 
## Build Commands

- `./gradlew allTests` - Run all tests across platforms  
- `./gradlew detektFormat` - Auto-format code, use before completing ANY task.
- `./gradlew :core:jvmTest --tests "pro.respawn.flowmvi.test.StoreTestSuite"` - Run specific test class

## Main Directories:

- `core/src/commonMain/kotlin/pro/respawn/flowmvi` - core logic, main module
  - ./StoreImpl.kt - core store implementation
  - ./api/ - public API interfaces and contracts (Store, State, Intent, Action, etc.)
  - ./dsl/ - DSL builders for stores, plugins, and configurations
  - ./modules/ - internal store modules (intent, state, action, recovery, lifecycle)
  - ./plugins/ - built-in plugins (logging, undo/redo, caching, job management, etc.)
    - ./delegate/ - delegate plugins for composition
  - ./decorators/ - public api intent decorators (retry, batch, conflate, timeout)
  - ./decorator/ - decorator framework and builders
  - ./impl/ - internal implementations
    - ./plugin/ - plugin execution machinery
    - ./store/ - store internals
  - ./util/ - utility classes (locks, dispatchers, collections)
  - ./exceptions/ - custom exceptions
  - ./annotation/ - API annotations (@ExperimentalFlowMVIAPI, etc.)
  - ./logging/ - logging framework and implementations
- `android/` - Android ViewModel integration
- `compose/` - Compose Multiplatform integration with lifecycle-aware subscriptions
- `savedstate/` - Cross-platform state persistence
- `essenty/` - Decompose/Essenty integration for retained components
- `test/` - Testing DSL and utilities (actual tests are per-module)
- `debugger/` - Remote debugger for the lib (desktop app, IDE plugin, runtime plugin)
- `sample/` - Complete sample application (wasm, android, desktop). Usage examples here.
- `benchmarks/` - Benchmarks module
- `docs` - Documentation directory. Read documentation to self-educate on aspects of the library.

### Testing

- All business logic in `core` module must be covered by unit tests written using Kotest FreeSpec style. Examples: 
   - core/src/jvmTest/kotlin/pro/respawn/flowmvi/test/plugin/ReducePluginTest.kt - plugin test
   - core/src/jvmTest/kotlin/pro/respawn/flowmvi/test/store/ChildStoreTest.kt - store test

### Before you complete a task

1. Run `detektFormat`. It will autofix lint, and warn you about remaining issues to solve.
2. Assemble the target module.
3. Run unit tests with `gradle allTests`.

# Rules and notes 

- Never add comments.
- Document public code ONLY via kdocs.
- Do not run `assemble` on the entire project, that task can take hours, use more granular tasks
- Avoid source breaking changes. Deprecate public api instead of removing it.

## Commit Guidelines

Format: `<type>[!]: [description]` where scope is optional, `!` = breaking change

**Types:** `feat` (→🚀 Features), `fix` (→🐞 Fixes), `feat!`/`breaking`/`api` (→🧨 Breaking), `docs` (→📚 Docs), 
`perf`, `refactor`, `test`, `chore`, `build`, `ci`, `style`, `revert`

Examples: `feat: add state recovery`, `fix: resolve lifecycle conflict`, `feat!: change Saver API`
