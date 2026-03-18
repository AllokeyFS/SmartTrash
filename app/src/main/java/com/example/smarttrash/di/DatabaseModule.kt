package com.example.smarttrash.di

import android.content.Context
import androidx.room.Room
import com.example.smarttrash.data.local.AppDatabase
import com.example.smarttrash.data.local.dao.RecyclingBinDao
import com.example.smarttrash.data.local.dao.WasteItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWasteItemDao(database: AppDatabase): WasteItemDao =
        database.wasteItemDao()

    @Provides
    @Singleton
    fun provideRecyclingBinDao(database: AppDatabase): RecyclingBinDao =
        database.recyclingBinDao()
}