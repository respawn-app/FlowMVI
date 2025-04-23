---
sidebar_position: 3
sidebar_label: Contribution guide
---

# Contributing

* To build the project, you will need the following in your local.properties:
    ```properties
    # only required for publishing (maintainers)
    sonatypeUsername=...
    sonatypePassword=...
    signing.key=...
    signing.password=...
    keystore.password=...
    # always required
    sdk.dir=...
    release=false
    ```
  
* Make sure you have these installed:
    * Android Studio latest Stable or Beta, depending on the current project's AGP.
    * Kotlin Multiplatform suite (run `kdoctor` to verify proper setup)
    * Detekt plugin
    * Kotest plugin
    * Compose plugin
    * JDK 22+ (for both building and running gradle)
* To debug the project, use the tasks provided as IDEA run configurations
    * All tests to run tests
    * All benchmark to benchmark the library
    * Sample \<Web | Desktop | Android\> to test the sample app manually
    * Publish to Local to upload a maven local build to test in your project
* Before pushing, make sure the following tasks pass:
    * `gradle detektFormat`
    * `gradle assemble`
    * `gradle allTests`
* If you submit a PR that changes behavior, please add tests for the changes.
* All contributions are welcome, including your plugin ideas or plugins you used in your project.
* We're especially looking for people who use FlowMVI in an iOS-compatible KMP project because we would like to include
  the adapters and solutions people came up with
  to the core library to improve overall experience of library users out-of-the-box.
