package com.thinkwik.whatsappclone.di

import com.thinkwik.whatsappclone.module.viewmodel.ViewModel
import com.thinkwik.whatsappclone.module.viewmodel.MainViewModel
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