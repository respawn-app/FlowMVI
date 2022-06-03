package com.nek12.flowMVI.sample.di

import com.nek12.flowMVI.sample.compose.BaseClassViewModel
import com.nek12.flowMVI.sample.repo.CounterRepo
import com.nek12.flowMVI.sample.view.NoBaseClassViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::CounterRepo)
    viewModelOf(::BaseClassViewModel)
    viewModelOf(::NoBaseClassViewModel)
}
