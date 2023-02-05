# Contributing

* To build the project, you will need the following in your local.properties:
    ```properties
    # only required for publishing
    sonatypeUsername=...
    sonatypePassword=...
    signing.keyId=...
    signing.password=...
    # --- 
    sdk.dir=...
    release=false
    ```

* Make sure you have proper plugins installed:
    * Detekt
    * Kotest
    * Kotlin Multiplatform (run `kdoctor` to verify proper setup)
    * Compose


* Before you push, make sure that Detekt passes. Run `gradle detektFormat` to see the report.
