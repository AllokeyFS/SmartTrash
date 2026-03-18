package com.example.smarttrash

import android.app.Application
import com.example.smarttrash.data.remote.FirestoreSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SmartTrashApp : Application() {

    @Inject
    lateinit var seeder: FirestoreSeeder

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { seeder.seedIfEmpty() }
        }
    }
}