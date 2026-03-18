package com.example.smarttrash.di

import com.example.smarttrash.data.repository.WasteRepository
import com.example.smarttrash.data.repository.WasteRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWasteRepository(
        impl: WasteRepositoryImpl
    ): WasteRepository
}