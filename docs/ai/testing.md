# Testing Guidance

- The project uses Kotest FreeSpec for all tests with Given/And/When/Then syntax.
- For tests involving coroutines/reactivity, Turbine can be used, otherwise tests can be flaky.
 
- If tests require changing the API or production code, then that is allowed. You can make refactors that will make the
  code more easily testable if that doesn't affect the public API.
- Prefer splitting code into testable functions/chunks if that doesn't affect the public API. 
- If you're writing repeated code chunks even in tests then prefer lightweight DSLs over functions.
- Aim for comprehensive coverage of logic without coverage-farming or neglect. 
- Tests should cover as many scenarios and edge cases as possible **without** duplicating existing tests.

- Tests for plugins, decorators, and Stores should be dogfed with the harness from the `test` module. Do not attempt to rawdog plugin/store tests w/o the harness!
- All business logic in all modules except sample/debugger must be covered by unit tests written using Kotest FreeSpec style.
- Note: we don't do screenshot/ui/instrumented tests.

- Test examples:
    - core/src/jvmTest/kotlin/pro/respawn/flowmvi/test/plugin/ReducePluginTest.kt - plugin test
    - core/src/jvmTest/kotlin/pro/respawn/flowmvi/test/store/ChildStoreTest.kt - store test
