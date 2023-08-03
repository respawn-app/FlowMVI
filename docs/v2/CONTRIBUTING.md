# Contributing

* To build the project, you will need the following in your local.properties:
    ```properties
    # only required for publishing
    sonatypeUsername=...
    sonatypePassword=...
    signing.key=...
    signing.password=...
    # always required
    sdk.dir=...
    release=false
    ```
* Make sure you these installed:
    * Android Studio latest Canary or Beta, depending on the current project's AGP (yes, we're on the edge).
    * Kotlin Multiplatform suite (run `kdoctor` to verify proper setup)
    * Detekt plugin
    * Kotest plugin
    * Compose plugin

* Before pushing, make sure the following tasks pass:
    * `gradle detektFormat`
    * `gradle assemble`
    * `gradle allTests`

* If you submit a PR that changes behavior or adds a new plugin, please add tests for it.
* All contributions are welcome, including your plugin ideas or plugins you used in your project.
* We're especially looking for people who use FlowMVI in an iOS-compatible KMP project because we would like to include
  the adapters and solutions people to the core library to improve overall experience of library users out-of-the-box.
