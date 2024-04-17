package pro.respawn.flowmvi.sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.scope.activityRetainedScope
import org.koin.core.annotation.KoinExperimentalAPI
import pro.respawn.flowmvi.sample.navigation.AndroidFeatureLauncher
import pro.respawn.flowmvi.sample.navigation.AppContent
import pro.respawn.flowmvi.sample.navigation.component.RootComponent
import pro.respawn.kmmutils.common.fastLazy

internal class MainActivity : ComponentActivity(), AndroidScopeComponent {

    override val scope by activityRetainedScope()
    private val launcher by fastLazy { AndroidFeatureLauncher(applicationContext) }

    @OptIn(ExperimentalDecomposeApi::class)
    private val root by fastLazy { retainedComponent { RootComponent(launcher, it) } }

    @OptIn(KoinExperimentalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent { KoinAndroidContext { AppContent(root) } }
    }
}
