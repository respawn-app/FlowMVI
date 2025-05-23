package pro.respawn.flowmvi.sample.features

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesContainer
import pro.respawn.flowmvi.sample.features.diconfig.DiConfigContainer
import pro.respawn.flowmvi.sample.features.home.HomeContainer
import pro.respawn.flowmvi.sample.features.lce.LCEContainer
import pro.respawn.flowmvi.sample.features.lce.LCERepository
import pro.respawn.flowmvi.sample.features.logging.LoggingContainer
import pro.respawn.flowmvi.sample.features.progressive.ProgressiveContainer
import pro.respawn.flowmvi.sample.features.progressive.ProgressiveRepository
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateContainer
import pro.respawn.flowmvi.sample.features.undoredo.UndoRedoContainer

val featureModule = module {
    singleOf(::LCERepository)
    singleOf(::ProgressiveRepository)

    container { new(::HomeContainer) }
    container { new(::ProgressiveContainer) }
    container { new(::LCEContainer) }
    container { new(::SavedStateContainer) }
    container { new(::DiConfigContainer) }
    container { new(::LoggingContainer) }
    container { new(::UndoRedoContainer) }

    // decompose doesn't need to use scoped dsl
    factoryOf(::PagesContainer)
}
