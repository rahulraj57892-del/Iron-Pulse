package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.GymDatabase
import com.example.data.GymRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IronPulseApp : Application() {

    lateinit var database: GymDatabase
    lateinit var repository: GymRepository

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Build premium local database
        database = Room.databaseBuilder(
            this,
            GymDatabase::class.java,
            "iron_pulse_db"
        )
        .fallbackToDestructiveMigration()
        .build()

        repository = GymRepository(database.gymDao())

        // Proactively insert high-quality presets (workouts, community stories, notifications)
        CoroutineScope(Dispatchers.IO).launch {
            repository.loadInitialPresets()
        }
    }

    companion object {
        lateinit var instance: IronPulseApp
            private set
    }
}
