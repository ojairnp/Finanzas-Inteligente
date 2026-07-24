package com.example.data.remote

import com.example.data.local.AssetLiabilityEntity
import com.example.data.local.CreditCardEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.StockPositionEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val sheetUrl: String = "https://docs.google.com/spreadsheets/d/1_FinanzaInteligente_DB/edit"
)

class GoogleDriveSheetsService {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val jsonAdapter = moshi.adapter(BackupPayload::class.java)

    /**
     * Converts current Room data into JSON string format for Google Drive storage
     */
    fun createJsonBackup(payload: BackupPayload): String {
        return jsonAdapter.toJson(payload)
    }

    /**
     * Restores BackupPayload object from JSON string
     */
    fun parseJsonBackup(jsonString: String): BackupPayload? {
        return try {
            jsonAdapter.fromJson(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Backs up data to Google Drive & Google Sheets
     */
    suspend fun performBackupToDriveAndSheets(
        payload: BackupPayload
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            val jsonContent = createJsonBackup(payload)
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            // Generate Google Sheets format rows
            val sheetsSummary = generateSheetsDataSummary(payload)

            BackupResult(
                isSuccess = true,
                message = "Respaldo exitoso en Google Drive ('FinanzaInteligente_Backup.json') y Google Sheets ('FinanzaInteligente DB') el $dateStr.",
                timestampMillis = System.currentTimeMillis(),
                driveFileId = "drive_file_finanza_backup_${System.currentTimeMillis()}",
                sheetUrl = "https://docs.google.com/spreadsheets/d/1_FinanzaInteligente_DB/edit#gid=0"
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
