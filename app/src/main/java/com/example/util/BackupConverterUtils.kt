package com.example.util

import com.example.data.local.AssetLiabilityEntity
import com.example.data.local.CreditCardEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.StockPositionEntity
import com.example.data.remote.BackupPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clase utilitaria para la conversión de datos financieros almacenados en Room
 * a formatos CSV y JSON para respaldos locales y sincronización con Google Drive / Sheets.
 */
object BackupConverterUtils {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val jsonAdapter = moshi.adapter(BackupPayload::class.java)

    /**
     * Convierte el objeto BackupPayload que contiene todas las entidades de Room a un String JSON estructurado.
     */
    fun toJson(payload: BackupPayload): String {
        return jsonAdapter.toJson(payload)
    }

    /**
     * Deserializa un String JSON a un objeto BackupPayload con todas las entidades de Room.
     */
    fun fromJson(jsonString: String): BackupPayload? {
        return try {
            jsonAdapter.fromJson(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Genera un archivo CSV unificado con secciones claras para todas las tablas de Room:
     * - Gastos
     * - Metas de Ahorro
     * - Tarjetas de Crédito
     * - Portafolio de Inversiones
     * - Activos y Pasivos (Patrimonio)
     */
    fun toCombinedCsv(payload: BackupPayload): String {
        val sb = StringBuilder()
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(payload.timestampMillis))
        
        sb.append("# RESPALDO FINANZA INTELIGENTE (CSV)\n")
        sb.append("# FECHA GENERACION: $dateStr\n")
        sb.append("# USUARIO: ${payload.userEmail}\n\n")

        // 1. SECCION GASTOS
        sb.append("--- SECCION GASTOS ---\n")
        sb.append(toExpenseCsv(payload.expenses))
        sb.append("\n")

        // 2. SECCION METAS
        sb.append("--- SECCION METAS_AHORRO ---\n")
        sb.append(toSavingsGoalCsv(payload.savingsGoals))
        sb.append("\n")

        // 3. SECCION TARJETAS
        sb.append("--- SECCION TARJETAS_CREDITO ---\n")
        sb.append(toCreditCardCsv(payload.creditCards))
        sb.append("\n")

        // 4. SECCION INVERSIONES
        sb.append("--- SECCION PORTAFOLIO_INVERSIONES ---\n")
        sb.append(toStockPositionCsv(payload.stockPositions))
        sb.append("\n")

        // 5. SECCION PATRIMONIO
        sb.append("--- SECCION PATRIMONIO ---\n")
        sb.append(toAssetLiabilityCsv(payload.assetsLiabilities))

        return sb.toString()
    }

    /**
     * Convierte la lista de gastos (ExpenseEntity) a CSV.
     */
    fun toExpenseCsv(expenses: List<ExpenseEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Monto,Categoria,Comercio,Nota,FechaMillis,TipoEntrada\n")
        expenses.forEach {
            val safeNote = escapeCsvField(it.note)
            val safeMerchant = escapeCsvField(it.merchant)
            val safeCategory = escapeCsvField(it.category)
            sb.append("${it.id},${it.amount},$safeCategory,$safeMerchant,$safeNote,${it.dateMillis},${it.inputType}\n")
        }
        return sb.toString()
    }

    /**
     * Convierte la lista de metas de ahorro (SavingsGoalEntity) a CSV.
     */
    fun toSavingsGoalCsv(goals: List<SavingsGoalEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Titulo,MontoObjetivo,AhorroActual,Categoria,FechaLimite\n")
        goals.forEach {
            val safeTitle = escapeCsvField(it.title)
            val safeCategory = escapeCsvField(it.category)
            sb.append("${it.id},$safeTitle,${it.targetAmount},${it.currentAmount},$safeCategory,${it.targetDate}\n")
        }
        return sb.toString()
    }

    /**
     * Convierte la lista de tarjetas de crédito (CreditCardEntity) a CSV.
     */
    fun toCreditCardCsv(cards: List<CreditCardEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Nombre,Banco,Limite,SaldoActual,DiaCorte,DiaPago,APR\n")
        cards.forEach {
            val safeName = escapeCsvField(it.name)
            val safeBank = escapeCsvField(it.bank)
            sb.append("${it.id},$safeName,$safeBank,${it.creditLimit},${it.currentBalance},${it.cutOffDay},${it.paymentDueDay},${it.apr}\n")
        }
        return sb.toString()
    }

    /**
     * Convierte la lista de posiciones bursátiles/inversiones (StockPositionEntity) a CSV.
     */
    fun toStockPositionCsv(stocks: List<StockPositionEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Symbol,Empresa,Titulos,PrecioCompra,PrecioActual,Soporte,Prioridad,Broker\n")
        stocks.forEach {
            val safeCompany = escapeCsvField(it.companyName)
            val safeBroker = escapeCsvField(it.broker)
            sb.append("${it.id},${it.symbol},$safeCompany,${it.shares},${it.avgBuyPrice},${it.currentPrice},${it.targetSupportPrice},${it.priorityAlert},$safeBroker\n")
        }
        return sb.toString()
    }

    /**
     * Convierte la lista de activos y pasivos (AssetLiabilityEntity) a CSV.
     */
    fun toAssetLiabilityCsv(assets: List<AssetLiabilityEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Titulo,Monto,Tipo,Categoria\n")
        assets.forEach {
            val safeTitle = escapeCsvField(it.title)
            val safeCategory = escapeCsvField(it.category)
            sb.append("${it.id},$safeTitle,${it.amount},${it.type},$safeCategory\n")
        }
        return sb.toString()
    }

    /**
     * Parsea un texto CSV unificado de respaldo de vuelta a un objeto BackupPayload.
     */
    fun fromCsv(csvContent: String): BackupPayload {
        val expenses = mutableListOf<ExpenseEntity>()
        val goals = mutableListOf<SavingsGoalEntity>()
        val cards = mutableListOf<CreditCardEntity>()
        val stocks = mutableListOf<StockPositionEntity>()
        val assets = mutableListOf<AssetLiabilityEntity>()

        var currentSection = ""

        csvContent.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("--- SECCION GASTOS ---")) currentSection = "GASTOS"
            else if (trimmed.startsWith("--- SECCION METAS_AHORRO ---")) currentSection = "METAS"
            else if (trimmed.startsWith("--- SECCION TARJETAS_CREDITO ---")) currentSection = "TARJETAS"
            else if (trimmed.startsWith("--- SECCION PORTAFOLIO_INVERSIONES ---")) currentSection = "INVERSIONES"
            else if (trimmed.startsWith("--- SECCION PATRIMONIO ---")) currentSection = "PATRIMONIO"
            else if (trimmed.isNotBlank() && !trimmed.startsWith("#") && !trimmed.startsWith("ID,")) {
                val cols = trimmed.split(",")
                try {
                    when (currentSection) {
                        "GASTOS" -> if (cols.size >= 7) {
                            expenses.add(
                                ExpenseEntity(
                                    id = cols[0].toLongOrNull() ?: 0L,
                                    amount = cols[1].toDoubleOrNull() ?: 0.0,
                                    category = unescapeCsvField(cols[2]),
                                    merchant = unescapeCsvField(cols[3]),
                                    note = unescapeCsvField(cols[4]),
                                    dateMillis = cols[5].toLongOrNull() ?: System.currentTimeMillis(),
                                    inputType = cols[6]
                                )
                            )
                        }
                        "METAS" -> if (cols.size >= 6) {
                            goals.add(
                                SavingsGoalEntity(
                                    id = cols[0].toLongOrNull() ?: 0L,
                                    title = unescapeCsvField(cols[1]),
                                    targetAmount = cols[2].toDoubleOrNull() ?: 0.0,
                                    currentAmount = cols[3].toDoubleOrNull() ?: 0.0,
                                    category = unescapeCsvField(cols[4]),
                                    targetDate = cols[5]
                                )
                            )
                        }
                        "TARJETAS" -> if (cols.size >= 8) {
                            cards.add(
                                CreditCardEntity(
                                    id = cols[0].toLongOrNull() ?: 0L,
                                    name = unescapeCsvField(cols[1]),
                                    bank = unescapeCsvField(cols[2]),
                                    creditLimit = cols[3].toDoubleOrNull() ?: 0.0,
                                    currentBalance = cols[4].toDoubleOrNull() ?: 0.0,
                                    cutOffDay = cols[5].toIntOrNull() ?: 15,
                                    paymentDueDay = cols[6].toIntOrNull() ?: 5,
                                    apr = cols[7].toDoubleOrNull() ?: 0.0
                                )
                            )
                        }
                        "INVERSIONES" -> if (cols.size >= 9) {
                            stocks.add(
                                StockPositionEntity(
                                    id = cols[0].toLongOrNull() ?: 0L,
                                    symbol = cols[1],
                                    companyName = unescapeCsvField(cols[2]),
                                    shares = cols[3].toDoubleOrNull() ?: 0.0,
                                    avgBuyPrice = cols[4].toDoubleOrNull() ?: 0.0,
                                    currentPrice = cols[5].toDoubleOrNull() ?: 0.0,
                                    targetSupportPrice = cols[6].toDoubleOrNull() ?: 0.0,
                                    priorityAlert = cols[7],
                                    broker = if (cols.size > 8) unescapeCsvField(cols[8]) else "GMB+"
                                )
                            )
                        }
                        "PATRIMONIO" -> if (cols.size >= 5) {
                            assets.add(
                                AssetLiabilityEntity(
                                    id = cols[0].toLongOrNull() ?: 0L,
                                    title = unescapeCsvField(cols[1]),
                                    amount = cols[2].toDoubleOrNull() ?: 0.0,
                                    type = cols[3],
                                    category = unescapeCsvField(cols[4])
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Manejar filas malformadas silenciosamente
                }
            }
        }

        return BackupPayload(
            expenses = expenses,
            savingsGoals = goals,
            creditCards = cards,
            stockPositions = stocks,
            assetsLiabilities = assets
        )
    }

    private fun escapeCsvField(field: String): String {
        return field.replace(",", " ").replace("\n", " ").trim()
    }

    private fun unescapeCsvField(field: String): String {
        return field.trim()
    }
}
