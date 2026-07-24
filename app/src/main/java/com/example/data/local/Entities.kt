package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    val merchant: String,
    val note: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val inputType: String = "Manual" // "Manual", "Voz", "OCR"
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val category: String,
    val targetDate: String,
    val imageKey: String = "japan"
)

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bank: String,
    val creditLimit: Double,
    val currentBalance: Double,
    val cutOffDay: Int, // e.g. 15th of the month
    val paymentDueDay: Int, // e.g. 5th of next month
    val apr: Double = 24.0,
    val cardType: String = "Visa"
)

@Entity(tableName = "stock_positions")
data class StockPositionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val companyName: String,
    val shares: Double,
    val avgBuyPrice: Double,
    val currentPrice: Double,
    val targetSupportPrice: Double,
    val priorityAlert: String = "Media", // "Alta", "Media", "Baja"
    val assetType: String = "Acción", // "Acción", "ETF", "FIBRA", "Divisa / Cripto", "Fondo de Inversión"
    val broker: String = "GBM+", // "GBM+", "Capital.com", "Fondos BBVA", "Plata Inversión", "Interactive Brokers", "Bitso", "Actinver", "Otro"
    val leverage: Double = 1.0, // 1.0 (Sin apalancamiento), 2.0, 5.0, 10.0, 20.0
    val googleFinanceTicker: String = "" // e.g. "NASDAQ:NVDA", "BMV:FUNO11", "CURRENCY:USDMXN", "BITSO:BTCUSD"
)

@Entity(tableName = "sold_stock_positions")
data class SoldStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val companyName: String,
    val sharesSold: Double,
    val avgBuyPrice: Double,
    val sellPrice: Double,
    val realizedGainLoss: Double,
    val broker: String,
    val assetType: String,
    val leverage: Double = 1.0,
    val saleDateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "statement_imports")
data class StatementImportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val institution: String, // e.g. "GBM+", "BBVA", "Banamex", "Capital.com"
    val statementPeriod: String, // e.g. "Julio 2026"
    val parsedTransactionsCount: Int,
    val totalAmount: Double,
    val importDateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "asset_liabilities")
data class AssetLiabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "ASSET" or "LIABILITY"
    val category: String // "Efectivo", "Inversiones", "InventarioNegocio", "Tarjetas", "Prestamos", "Hipoteca"
)

@Entity(tableName = "account_transfers")
data class AccountTransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceAccount: String, // e.g. "Efectivo GBM+", "BBVA Débito"
    val destinationAccount: String, // e.g. "Nu Cuenta Rendimiento 15%", "BBVA TC (Pago)"
    val amount: Double,
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)

