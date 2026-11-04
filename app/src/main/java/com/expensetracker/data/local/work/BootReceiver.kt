package com.expensetracker.data.local.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, rescheduling WorkManager tasks")
            rescheduleRecurringTransactionWork(context)
        }
    }

    private fun rescheduleRecurringTransactionWork(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val recurringWorkRequest = PeriodicWorkRequest.Builder(
            RecurringTransactionWorker::class.java,
            15,
            TimeUnit.MINUTES
        ).apply {
            setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .build()
            )
            setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            addTag(RecurringTransactionWorker.WORK_TAG)
            setInitialDelay(1, TimeUnit.MINUTES)
        }.build()

        workManager.enqueueUniquePeriodicWork(
            RecurringTransactionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            recurringWorkRequest
        )

        Log.d(TAG, "Recurring transaction work rescheduled successfully")
    }

    companion object {
        fun scheduleRecurringTransactionWork(context: Context) {
            val workManager = WorkManager.getInstance(context)

            val recurringWorkRequest = PeriodicWorkRequest.Builder(
                RecurringTransactionWorker::class.java,
                15,
                TimeUnit.MINUTES
            ).apply {
                setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .build()
                )
                setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                addTag(RecurringTransactionWorker.WORK_TAG)
                setInitialDelay(1, TimeUnit.MINUTES)
            }.build()

            workManager.enqueueUniquePeriodicWork(
                RecurringTransactionWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                recurringWorkRequest
            )

            Log.d(TAG, "Recurring transaction work scheduled manually")
        }

        fun cancelRecurringTransactionWork(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(RecurringTransactionWorker.WORK_NAME)
            Log.d(TAG, "Recurring transaction work cancelled")
        }
    }
}