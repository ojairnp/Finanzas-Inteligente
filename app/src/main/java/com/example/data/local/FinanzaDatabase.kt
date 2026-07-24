package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        SavingsGoalEntity::class,
        CreditCardEntity::class,
        StockPositionEntity::class,
        SoldStockEntity::class,
        StatementImportEntity::class,
        AssetLiabilityEntity::class,
        AccountTransferEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FinanzaDatabase : RoomDatabase() {
    abstract fun finanzaDao(): FinanzaDao

    companion object {
        @Volatile
        private var INSTANCE: FinanzaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FinanzaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanzaDatabase::class.java,
                    "finanza_inteligente_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(FinanzaDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class FinanzaDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.finanzaDao())
                }
            }
        }

        suspend fun populateInitialData(dao: FinanzaDao) {
            // Initial Expenses
            dao.insertExpense(ExpenseEntity(amount = 450.00, category = "Restaurantes", merchant = "Starbucks & Bistro", note = "Café y reunión de trabajo", inputType = "Manual"))
            dao.insertExpense(ExpenseEntity(amount = 1250.50, category = "Supermercado", merchant = "Walmart Supercenter", note = "Compra de despensa semanal", inputType = "OCR"))
            dao.insertExpense(ExpenseEntity(amount = 890.00, category = "Gasolina", merchant = "Gasolinera Pemex", note = "Tanque Lleno", inputType = "Voz"))
            dao.insertExpense(ExpenseEntity(amount = 320.00, category = "Servicios", merchant = "Netflix & Spotify", note = "Suscripciones mensuales", inputType = "Manual"))

            // Initial Savings Goals
            dao.insertSavingsGoal(SavingsGoalEntity(title = "Viaje a Japón 2027", targetAmount = 85000.0, currentAmount = 42500.0, category = "Viajes", targetDate = "15 Nov 2027", imageKey = "japan"))
            dao.insertSavingsGoal(SavingsGoalEntity(title = "Fondo de Emergencia (6 meses)", targetAmount = 120000.0, currentAmount = 95000.0, category = "Seguridad", targetDate = "31 Diciembre 2026", imageKey = "emergency"))
            dao.insertSavingsGoal(SavingsGoalEntity(title = "Enganche Casa/Depto", targetAmount = 250000.0, currentAmount = 110000.0, category = "Patrimonio", targetDate = "01 Junio 2028", imageKey = "home"))

            // Initial Credit Cards
            dao.insertCreditCard(CreditCardEntity(name = "Banamex Premier Gold", bank = "Citibanamex", creditLimit = 60000.0, currentBalance = 14200.0, cutOffDay = 12, paymentDueDay = 2, apr = 28.5, cardType = "Mastercard"))
            dao.insertCreditCard(CreditCardEntity(name = "BBVA Platinum Rewards", bank = "BBVA", creditLimit = 120000.0, currentBalance = 28500.0, cutOffDay = 25, paymentDueDay = 15, apr = 22.0, cardType = "Visa"))
            dao.insertCreditCard(CreditCardEntity(name = "American Express Gold", bank = "Amex", creditLimit = 80000.0, currentBalance = 8400.0, cutOffDay = 5, paymentDueDay = 25, apr = 30.0, cardType = "Amex"))

            // Initial Stock & Investment Positions across Brokers and Asset Classes
            dao.insertStockPosition(StockPositionEntity(symbol = "NVDA", companyName = "NVIDIA Corp.", shares = 15.0, avgBuyPrice = 115.0, currentPrice = 128.50, targetSupportPrice = 118.0, priorityAlert = "Alta", assetType = "Acción", broker = "GBM+", leverage = 1.0, googleFinanceTicker = "NASDAQ:NVDA"))
            dao.insertStockPosition(StockPositionEntity(symbol = "FUNO11", companyName = "Fibra Uno Real Estate", shares = 500.0, avgBuyPrice = 28.50, currentPrice = 31.20, targetSupportPrice = 29.0, priorityAlert = "Media", assetType = "FIBRA", broker = "GBM+", leverage = 1.0, googleFinanceTicker = "BMV:FUNO11"))
            dao.insertStockPosition(StockPositionEntity(symbol = "USD/MXN", companyName = "Dólar / Peso Mexicano", shares = 2000.0, avgBuyPrice = 19.80, currentPrice = 20.25, targetSupportPrice = 19.90, priorityAlert = "Alta", assetType = "Divisa / Cripto", broker = "Capital.com", leverage = 10.0, googleFinanceTicker = "CURRENCY:USDMXN"))
            dao.insertStockPosition(StockPositionEntity(symbol = "BBVA-BATER", companyName = "Fondo Renta Variable BBVA", shares = 1200.0, avgBuyPrice = 14.20, currentPrice = 15.60, targetSupportPrice = 14.50, priorityAlert = "Baja", assetType = "Fondo de Inversión", broker = "Fondos BBVA", leverage = 1.0, googleFinanceTicker = "BBVA:BATER"))
            dao.insertStockPosition(StockPositionEntity(symbol = "BTC/USD", companyName = "Bitcoin / US Dollar", shares = 0.25, avgBuyPrice = 61200.0, currentPrice = 66800.0, targetSupportPrice = 63000.0, priorityAlert = "Alta", assetType = "Divisa / Cripto", broker = "Bitso", leverage = 1.0, googleFinanceTicker = "BITSO:BTCUSD"))
            dao.insertStockPosition(StockPositionEntity(symbol = "QQQ", companyName = "Invesco QQQ Trust ETF", shares = 10.0, avgBuyPrice = 460.0, currentPrice = 485.40, targetSupportPrice = 470.0, priorityAlert = "Media", assetType = "ETF", broker = "Interactive Brokers", leverage = 1.0, googleFinanceTicker = "NASDAQ:QQQ"))

            // Initial Assets & Liabilities
            dao.insertAssetLiability(AssetLiabilityEntity(title = "Cuentas de Debito & Efectivo", amount = 145000.0, type = "ASSET", category = "Efectivo"))
            dao.insertAssetLiability(AssetLiabilityEntity(title = "Portafolio Inversiones Cetes/Acciones", amount = 320000.0, type = "ASSET", category = "Inversiones"))
            dao.insertAssetLiability(AssetLiabilityEntity(title = "Inventario de Negocio/Local", amount = 180000.0, type = "ASSET", category = "InventarioNegocio"))
            dao.insertAssetLiability(AssetLiabilityEntity(title = "Deuda Tarjetas de Crédito", amount = 51100.0, type = "LIABILITY", category = "Tarjetas"))
            dao.insertAssetLiability(AssetLiabilityEntity(title = "Préstamo Automotriz", amount = 95000.0, type = "LIABILITY", category = "Prestamos"))

            // Sample Sold Positions (Historial de Ventas Realizadas)
            dao.insertSoldStock(SoldStockEntity(symbol = "AAPL", companyName = "Apple Inc.", sharesSold = 10.0, avgBuyPrice = 180.0, sellPrice = 224.50, realizedGainLoss = 445.0, broker = "GBM+", assetType = "Acción", leverage = 1.0))
            dao.insertSoldStock(SoldStockEntity(symbol = "MSFT", companyName = "Microsoft Corp.", sharesSold = 5.0, avgBuyPrice = 410.0, sellPrice = 445.0, realizedGainLoss = 175.0, broker = "GBM+", assetType = "Acción", leverage = 1.0))

            // Sample Statement Import Log
            dao.insertStatementImport(StatementImportEntity(fileName = "Estado_Cuenta_GBM_Junio2026.pdf", institution = "GBM+ Casa de Bolsa", statementPeriod = "Junio 2026", parsedTransactionsCount = 8, totalAmount = 45200.0))
            dao.insertStatementImport(StatementImportEntity(fileName = "Estado_Cuenta_BBVA_TC.pdf", institution = "BBVA Bancomer", statementPeriod = "Junio 2026", parsedTransactionsCount = 14, totalAmount = 18450.0))

            // Sample Account Transfers & Traceability
            dao.insertAccountTransfer(AccountTransferEntity(sourceAccount = "Efectivo GBM+ (Venta AAPL)", destinationAccount = "Nu Cuenta Rendimiento (15% a la vista)", amount = 22450.0, note = "Transferencia de fondos líquidos para tasa de rendimiento diaria 15%"))
            dao.insertAccountTransfer(AccountTransferEntity(sourceAccount = "BBVA Débito Nómina", destinationAccount = "Pago BBVA Platinum TC", amount = 12500.0, note = "Pago de saldo para no generar intereses"))

        }
    }
}
