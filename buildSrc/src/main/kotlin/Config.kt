object Config {

    val kotlinCompilerArgs = listOf(
        "-opt-in=kotlin.RequiresOptIn",
        "-Xjvm-default=all",
        "-Xbackend-threads=0", // parallel IR compilation
        "-opt-in=kotlin.Experimental",
        "-opt-in=kotlin.RequiresOptIn",
    )

    val stabilityLevels = listOf("preview", "eap", "alpha", "beta", "m", "cr", "rc")
}
