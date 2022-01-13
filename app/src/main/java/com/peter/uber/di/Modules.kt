package com.peter.awesomenews.di

import com.peter.uber.pojo.Location
import com.peter.uber.service.FirestoreUber

import com.peter.uber.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainViewModelModule = module {
    viewModel { MainViewModel(get(), get()) }
}
val fireStoreModule = module {
    single { FirestoreUber(get()) }
}

val locationModule = module {
    factory { Location("",0.0,0.0) }
}


