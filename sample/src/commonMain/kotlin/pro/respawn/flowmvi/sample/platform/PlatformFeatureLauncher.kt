package pro.respawn.flowmvi.sample.platform

interface PlatformFeatureLauncher {

    fun xmlActivity()
}

object NoOpPlatformFeatureLauncher : PlatformFeatureLauncher {

    override fun xmlActivity() = Unit
}
