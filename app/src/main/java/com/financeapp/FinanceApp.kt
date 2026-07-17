package com.financeapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FinanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Schedule recurring transaction worker on first launch
        com.financeapp.domain.RecurringTransactionWorker.schedule(this)
    }
}
