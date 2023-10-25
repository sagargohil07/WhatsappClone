package com.thinkwik.communimate.di

import com.thinkwik.communimate.module.viewmodel.ViewModel
import com.thinkwik.communimate.module.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        ViewModel()
    }
    viewModel {
        MainViewModel()
    }
}