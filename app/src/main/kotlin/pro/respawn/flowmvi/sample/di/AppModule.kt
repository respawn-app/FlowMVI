package pro.respawn.flowmvi.sample.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.androidx.compose.defaultExtras
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.compose.LocalKoinScope
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.qualifier
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import pro.respawn.flowmvi.android.StoreViewModel
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.sample.container.CounterContainer
import pro.respawn.flowmvi.sample.container.LambdaViewModel
import pro.respawn.flowmvi.sample.repo.CounterRepo

val appModule = module {
    singleOf(::CounterRepo)
    viewModelOf(::LambdaViewModel)

    factoryOf(::CounterContainer)
    storeViewModel<CounterContainer>()
}
