package com.nek12.flowMVI.sample.di

import com.nek12.flowMVI.sample.compose.BaseClassViewModel
import com.nek12.flowMVI.sample.view.NoBaseClassViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { BaseClassViewModel() }
    viewModel { NoBaseClassViewModel() }
}
