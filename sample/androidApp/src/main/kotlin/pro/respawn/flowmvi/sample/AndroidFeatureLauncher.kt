package pro.respawn.flowmvi.sample

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import pro.respawn.flowmvi.sample.platform.PlatformFeatureLauncher

internal class AndroidFeatureLauncher(private val context: Context) : PlatformFeatureLauncher {

    override fun xmlActivity() = context.startActivity(
        Intent(context, XmlActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
        }
    )
}
