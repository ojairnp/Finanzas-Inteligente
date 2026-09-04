package com.example.worker

import android.content.Context
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.FinanzaDatabase
import com.example.data.remote.BackupPayload
import com.example.data.remote.GoogleDriveSheetsService
import com.example.util.BackupConverterUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker de WorkManager para ejecutar la exportación y guardado periódico de datos
 * en formatos JSON y CSV dentro del almacenamiento interno del dispositivo.
 */
class LocalBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = FinanzaDatabase.getDatabase(
                applicationContext,
                CoroutineScope(Dispatchers.IO)
            )
            val dao = database.finanzaDao()

            val expenses = dao.getAllExpenses().first()
            val goals = dao.getAllSavingsGoals().first()
            val cards = dao.getAllCreditCards().first()
            val stocks = dao.getAllStockPositions().first()
            val assets = dao.getAllAssetsLiabilities().first()

            val payload = BackupPayload(
                userEmail = "ojairnp@gmail.com",
                expenses = expenses,
                savingsGoals = goals,
                creditCards = cards,
                stockPositions = stocks,
                assetsLiabilities = assets,
                timestampMillis = System.currentTimeMillis()
            )

            val service = GoogleDriveSheetsService()
            val localResult = service.saveLocalBackupFiles(applicationContext, payload)

            if (localResult.isSuccess) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "✅ WorkManager: Exportación programada de archivos CSV y JSON completada exitosamente.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME_PERIODIC = "FinanzaInteligente_PeriodicLocalBackup"
        const val WORK_NAME_ONETIME = "FinanzaInteligente_OneTimeLocalBackup"

        /**
         * Programa la tarea periódica diaria (cada 24 horas) para exportar automáticamente
         * los datos de Room a archivos JSON y CSV locales.
         */
        fun scheduleDailyBackup(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val dailyWorkRequest = PeriodicWorkRequestBuilder<LocalBackupWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        }

        /**
         * Ejecuta una exportación inmediata en segundo plano utilizando WorkManager.
         */
        fun triggerImmediateBackup(context: Context) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<LocalBackupWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeRequest)
            Toast.makeText(context, "🚀 Tarea de WorkManager programada: Exportando CSV/JSON...", Toast.LENGTH_SHORT).show()
        }

        /**
         * Cancela la tarea programada si el usuario así lo requiere.
         */
        fun cancelScheduledBackup(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
        }
    }
}
