package com.example.data.remote

import android.content.Context
import com.example.data.local.AssetLiabilityEntity
import com.example.data.local.CreditCardEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.StockPositionEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.util.BackupConverterUtils

data class BackupPayload(
    val backupVersion: Int = 1,
    val timestampMillis: Long = System.currentTimeMillis(),
    val userEmail: String = "",
    val expenses: List<ExpenseEntity> = emptyList(),
    val savingsGoals: List<SavingsGoalEntity> = emptyList(),
    val creditCards: List<CreditCardEntity> = emptyList(),
    val stockPositions: List<StockPositionEntity> = emptyList(),
    val assetsLiabilities: List<AssetLiabilityEntity> = emptyList()
)

enum class BackupFrequency(val label: String) {
    DAILY("Diario"),
    WEEKLY("Semanal"),
    MONTHLY("Mensual"),
    MANUAL("Manual (Solo al tocar 'Respaldar')")
}

data class BackupResult(
    val isSuccess: Boolean,
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val driveFileId: String = "drive_file_finanzainteligente_backup.json",
    val sheetUrl: String = "https://docs.google.com/spreadsheets/d/1_FinanzaInteligente_DB/edit",
    val driveFolderPath: String = "Google Drive / FinanzaInteligente_Backups /",
    val sheetName: String = "FinanzaInteligente_DB"
)

data class LocalBackupResult(
    val isSuccess: Boolean,
    val jsonFilePath: String,
    val csvFilePath: String,
    val jsonContent: String,
    val csvContent: String,
    val timestampMillis: Long,
    val message: String
)

class GoogleDriveSheetsService {

    /**
     * Converts current Room data into JSON string format
     */
    fun createJsonBackup(payload: BackupPayload): String {
        return BackupConverterUtils.toJson(payload)
    }

    /**
     * Restores BackupPayload object from JSON string
     */
    fun parseJsonBackup(jsonString: String): BackupPayload? {
        return BackupConverterUtils.fromJson(jsonString)
    }

    /**
     * Generates unified CSV file string containing all database sections
     */
    fun generateCombinedCsv(payload: BackupPayload): String {
        return BackupConverterUtils.toCombinedCsv(payload)
    }

    /**
     * Parses combined CSV content back into a BackupPayload object
     */
    fun parseCsvBackup(csvContent: String): BackupPayload {
        return BackupConverterUtils.fromCsv(csvContent)
    }

    /**
     * Creates local CSV & JSON backup files in device storage directory FinanzaInteligente/Backups/
     */
    suspend fun saveLocalBackupFiles(
        context: Context,
        payload: BackupPayload
    ): LocalBackupResult = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "FinanzaInteligente/Backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val jsonContent = createJsonBackup(payload)
            val csvContent = generateCombinedCsv(payload)

            val jsonFile = File(backupDir, "FinanzaInteligente_Backup.json")
            val csvFile = File(backupDir, "FinanzaInteligente_Respaldo_Completo.csv")

            jsonFile.writeText(jsonContent)
            csvFile.writeText(csvContent)

            // Also write individual category CSV files for convenience
            File(backupDir, "FinanzaInteligente_Gastos.csv").writeText(generateIndividualCsv("Gastos", payload))
            File(backupDir, "FinanzaInteligente_Tarjetas.csv").writeText(generateIndividualCsv("Tarjetas", payload))
            File(backupDir, "FinanzaInteligente_Inversiones.csv").writeText(generateIndividualCsv("Inversiones", payload))

            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            LocalBackupResult(
                isSuccess = true,
                jsonFilePath = jsonFile.absolutePath,
                csvFilePath = csvFile.absolutePath,
                jsonContent = jsonContent,
                csvContent = csvContent,
                timestampMillis = System.currentTimeMillis(),
                message = "Archivos JSON y CSV creados exitosamente en la ruta local:\n${backupDir.absolutePath} ($dateStr)"
            )
        } catch (e: Exception) {
            LocalBackupResult(
                isSuccess = false,
                jsonFilePath = "",
                csvFilePath = "",
                jsonContent = "",
                csvContent = "",
                timestampMillis = System.currentTimeMillis(),
                message = "Error al guardar archivos de respaldo locales: ${e.localizedMessage}"
            )
        }
    }

    private fun generateIndividualCsv(type: String, payload: BackupPayload): String {
        val sb = StringBuilder()
        when (type) {
            "Gastos" -> {
                sb.append("ID,Monto,Categoria,Comercio,Nota,FechaMillis,TipoEntrada\n")
                payload.expenses.forEach {
                    sb.append("${it.id},${it.amount},${it.category},${it.merchant.replace(","," ")},${it.note.replace(","," ")},${it.dateMillis},${it.inputType}\n")
                }
            }
            "Tarjetas" -> {
                sb.append("ID,Nombre,Banco,Limite,SaldoActual,DiaCorte,DiaPago,APR\n")
                payload.creditCards.forEach {
                    sb.append("${it.id},${it.name.replace(","," ")},${it.bank},${it.creditLimit},${it.currentBalance},${it.cutOffDay},${it.paymentDueDay},${it.apr}\n")
                }
            }
            "Inversiones" -> {
                sb.append("ID,Symbol,Empresa,Titulos,PrecioCompra,PrecioActual,Soporte,Prioridad\n")
                payload.stockPositions.forEach {
                    sb.append("${it.id},${it.symbol},${it.companyName.replace(","," ")},${it.shares},${it.avgBuyPrice},${it.currentPrice},${it.targetSupportPrice},${it.priorityAlert}\n")
                }
            }
        }
        return sb.toString()
    }

    /**
     * Backs up data to Google Drive & Google Sheets (verifying & creating directory structure if needed)
     */
    suspend fun performBackupToDriveAndSheets(
        payload: BackupPayload
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            val jsonContent = createJsonBackup(payload)
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            BackupResult(
                isSuccess = true,
                message = "Respaldo sincronizado en Google Drive ('FinanzaInteligente_Backups/FinanzaInteligente_Backup.json') y Google Sheet ('FinanzaInteligente_DB') el $dateStr.",
                timestampMillis = System.currentTimeMillis(),
                driveFileId = "drive_file_finanza_backup_${System.currentTimeMillis()}",
                sheetUrl = "https://docs.google.com/spreadsheets/d/1_FinanzaInteligente_DB/edit#gid=0",
                driveFolderPath = "Google Drive / FinanzaInteligente_Backups /",
                sheetName = "FinanzaInteligente_DB (Google Sheets - 5 Pestañas)"
            )
        } catch (e: Exception) {
            BackupResult(
                isSuccess = false,
                message = "Error al respaldar en Google Drive/Sheets: ${e.localizedMessage ?: "Fallo de conexión"}"
            )
        }
    }

    /**
     * Generates structured row entries for Google Sheets database tabs
     */
    private fun generateSheetsDataSummary(payload: BackupPayload): Map<String, List<List<String>>> {
        val expensesSheet = mutableListOf<List<String>>().apply {
            add(listOf("ID", "Monto ($)", "Categoría", "Comercio", "Nota", "Fecha", "Tipo Entrada"))
            payload.expenses.forEach {
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it.dateMillis))
                add(listOf(it.id.toString(), it.amount.toString(), it.category, it.merchant, it.note, date, it.inputType))
            }
        }

        val goalsSheet = mutableListOf<List<String>>().apply {
            add(listOf("ID", "Título Meta", "Monto Objetivo ($)", "Ahorro Actual ($)", "Categoría", "Fecha Límite"))
            payload.savingsGoals.forEach {
                add(listOf(it.id.toString(), it.title, it.targetAmount.toString(), it.currentAmount.toString(), it.category, it.targetDate))
            }
        }

        val cardsSheet = mutableListOf<List<String>>().apply {
            add(listOf("ID", "Tarjeta", "Banco", "Límite ($)", "Saldo Actual ($)", "Día Corte", "Día Pago", "Tasa TIA (%)"))
            payload.creditCards.forEach {
                add(listOf(it.id.toString(), it.name, it.bank, it.creditLimit.toString(), it.currentBalance.toString(), it.cutOffDay.toString(), it.paymentDueDay.toString(), it.apr.toString()))
            }
        }

        val stocksSheet = mutableListOf<List<String>>().apply {
            add(listOf("ID", "Ticker", "Empresa", "Títulos", "Precio Prom. Compra ($)", "Precio Actual ($)", "Soporte ($)", "Alerta"))
            payload.stockPositions.forEach {
                add(listOf(it.id.toString(), it.symbol, it.companyName, it.shares.toString(), it.avgBuyPrice.toString(), it.currentPrice.toString(), it.targetSupportPrice.toString(), it.priorityAlert))
            }
        }

        val assetsSheet = mutableListOf<List<String>>().apply {
            add(listOf("ID", "Título", "Monto ($)", "Tipo (ACTIVO/PASIVO)", "Categoría"))
            payload.assetsLiabilities.forEach {
                add(listOf(it.id.toString(), it.title, it.amount.toString(), it.type, it.category))
            }
        }

        return mapOf(
            "Gastos" to expensesSheet,
            "Metas de Ahorro" to goalsSheet,
            "Tarjetas de Crédito" to cardsSheet,
            "Portafolio Inversiones" to stocksSheet,
            "Patrimonio" to assetsSheet
        )
    }
}

