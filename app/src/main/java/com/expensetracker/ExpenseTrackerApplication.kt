package com.expensetracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any global components here
    }
}