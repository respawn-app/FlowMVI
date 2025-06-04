package pro.respawn.flowmvi.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityRetainedScope
import org.koin.core.annotation.KoinExperimentalAPI
import pro.respawn.flowmvi.sample.navigation.AppContent
import pro.respawn.flowmvi.sample.navigation.component.RootComponent
import pro.respawn.kmmutils.common.fastLazy

internal class MainActivity : ComponentActivity(), AndroidScopeComponent {

    override val scope by activityRetainedScope()

    @OptIn(ExperimentalDecomposeApi::class)
    private val root by fastLazy { retainedComponent { RootComponent(it) } }

    @OptIn(KoinExperimentalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppContent(root) }
    }
}
