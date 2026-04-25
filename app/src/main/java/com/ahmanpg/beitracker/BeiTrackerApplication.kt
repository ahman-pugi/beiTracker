package com.ahmanpg.beitracker

import android.app.Application
import com.ahmanpg.beitracker.worker.NotificationHelper
import com.ahmanpg.beitracker.worker.PriceCheckWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BeiTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        NotificationHelper.createChannels(this)

        // Schedule background price checking
        PriceCheckWorker.schedule(this, intervalHours = 6)
    }
}