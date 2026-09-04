package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.local.AccountTransferEntity
import com.example.data.local.AssetLiabilityEntity
import com.example.data.local.CreditCardEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.FinanzaDao
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.StockPositionEntity
import com.example.data.local.SoldStockEntity
import com.example.data.local.StatementImportEntity
import com.example.data.remote.BackupPayload
import com.example.data.remote.BackupResult
import com.example.data.remote.GeminiService
import com.example.data.remote.GoogleDriveSheetsService
import com.example.data.remote.GoogleFinanceService
import com.example.data.remote.ParsedExpense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FinanzaRepository(
    private val dao: FinanzaDao,
    private val geminiService: GeminiService,
    private val googleDriveSheetsService: GoogleDriveSheetsService = GoogleDriveSheetsService(),
    private val googleFinanceService: GoogleFinanceService = GoogleFinanceService()
) {
    val expenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val savingsGoals: Flow<List<SavingsGoalEntity>> = dao.getAllSavingsGoals()
    val creditCards: Flow<List<CreditCardEntity>> = dao.getAllCreditCards()
    val stockPositions: Flow<List<StockPositionEntity>> = dao.getAllStockPositions()
    val soldStocks: Flow<List<SoldStockEntity>> = dao.getAllSoldStocks()
    val statementImports: Flow<List<StatementImportEntity>> = dao.getAllStatementImports()
    val assetsLiabilities: Flow<List<AssetLiabilityEntity>> = dao.getAllAssetsLiabilities()
    val accountTransfers: Flow<List<AccountTransferEntity>> = dao.getAllAccountTransfers()

    suspend fun addExpense(expense: ExpenseEntity) = dao.insertExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)

    suspend fun addSavingsGoal(goal: SavingsGoalEntity) = dao.insertSavingsGoal(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) = dao.updateSavingsGoal(goal)

    suspend fun addCreditCard(card: CreditCardEntity) = dao.insertCreditCard(card)
    suspend fun updateCreditCard(card: CreditCardEntity) = dao.updateCreditCard(card)

    suspend fun addStockPosition(stock: StockPositionEntity) = dao.insertStockPosition(stock)
    suspend fun updateStockPosition(stock: StockPositionEntity) = dao.updateStockPosition(stock)

    suspend fun sellStockPosition(stock: StockPositionEntity, sharesToSell: Double, sellPrice: Double): Double {
        val actualSharesToSell = sharesToSell.coerceAtMost(stock.shares)
        val gainLoss = (sellPrice - stock.avgBuyPrice) * actualSharesToSell * stock.leverage
        val totalProceeds = actualSharesToSell * sellPrice

        val soldRecord = SoldStockEntity(
            symbol = stock.symbol,
            companyName = stock.companyName,
            sharesSold = actualSharesToSell,
            avgBuyPrice = stock.avgBuyPrice,
            sellPrice = sellPrice,
            realizedGainLoss = gainLoss,
            broker = stock.broker,
            assetType = stock.assetType,
            leverage = stock.leverage
        )

        dao.insertSoldStock(soldRecord)

        val remainingShares = stock.shares - actualSharesToSell
        if (remainingShares <= 0.0001) {
            dao.deleteStockPosition(stock)
        } else {
            dao.updateStockPosition(stock.copy(shares = remainingShares))
        }

        // Automatic deposit into Liquid Cash Asset for the broker platform
        val currentAssets = dao.getAllAssetsLiabilities().first()
        val existingCashAsset = currentAssets.find {
            it.type == "ASSET" && (it.title.contains(stock.broker, ignoreCase = true) || it.title.contains("Efectivo", ignoreCase = true))
        }

        if (existingCashAsset != null) {
            dao.insertAssetLiability(existingCashAsset.copy(amount = existingCashAsset.amount + totalProceeds))
        } else {
            dao.insertAssetLiability(
                AssetLiabilityEntity(
                    title = "Efectivo / Liquidez en ${stock.broker}",
                    amount = totalProceeds,
                    type = "ASSET",
                    category = "Efectivo"
                )
            )
        }

        // Log transfer for traceability
        dao.insertAccountTransfer(
            AccountTransferEntity(
                sourceAccount = "Venta Acción ${stock.symbol}",
                destinationAccount = "Efectivo Disponible (${stock.broker})",
                amount = totalProceeds,
                note = "Venta de $actualSharesToSell títulos a $$sellPrice (Permanecen en el patrimonio como dinero líquido)"
            )
        )

        return gainLoss
    }

    suspend fun recordAccountTransfer(transfer: AccountTransferEntity) {
        dao.insertAccountTransfer(transfer)

        // If target is a credit card payment, update credit card balance
        val currentCards = dao.getAllCreditCards().first()
        val targetCard = currentCards.find { transfer.destinationAccount.contains(it.name, true) || transfer.destinationAccount.contains(it.bank, true) }
        if (targetCard != null) {
            val updatedBalance = (targetCard.currentBalance - transfer.amount).coerceAtLeast(0.0)
            dao.updateCreditCard(targetCard.copy(currentBalance = updatedBalance))
        }

        // Update asset/liability accounts if names match
        val currentAssets = dao.getAllAssetsLiabilities().first()
        val sourceAsset = currentAssets.find { it.type == "ASSET" && transfer.sourceAccount.contains(it.title, true) }
        if (sourceAsset != null) {
            dao.insertAssetLiability(sourceAsset.copy(amount = (sourceAsset.amount - transfer.amount).coerceAtLeast(0.0)))
        }

        val destAsset = currentAssets.find { it.type == "ASSET" && transfer.destinationAccount.contains(it.title, true) }
        if (destAsset != null) {
            dao.insertAssetLiability(destAsset.copy(amount = destAsset.amount + transfer.amount))
        }
    }


    suspend fun logStatementImport(importItem: StatementImportEntity) = dao.insertStatementImport(importItem)

    suspend fun syncStockPricesWithGoogleFinance(): Boolean {
        val currentStocks = dao.getAllStockPositions().first()
        if (currentStocks.isEmpty()) return false
        val updatedStocks = googleFinanceService.syncAllPositionsWithGoogleFinance(currentStocks)
        dao.insertAllStockPositions(updatedStocks)
        return true
    }

    suspend fun addAssetLiability(item: AssetLiabilityEntity) = dao.insertAssetLiability(item)
    suspend fun deleteAssetLiability(item: AssetLiabilityEntity) = dao.deleteAssetLiability(item)

    suspend fun clearAllSampleData() {
        dao.clearExpenses()
        dao.clearSavingsGoals()
        dao.clearCreditCards()
        dao.clearStockPositions()
        dao.clearSoldStocks()
        dao.clearStatementImports()
        dao.clearAssetsLiabilities()
        dao.clearAccountTransfers()
    }

    // Backup & Restore via Google Drive & Sheets & Local CSV/JSON
    suspend fun performLocalBackupFiles(context: android.content.Context, userEmail: String): com.example.data.remote.LocalBackupResult {
        val currentExpenses = dao.getAllExpenses().first()
        val currentGoals = dao.getAllSavingsGoals().first()
        val currentCards = dao.getAllCreditCards().first()
        val currentStocks = dao.getAllStockPositions().first()
        val currentAssets = dao.getAllAssetsLiabilities().first()

        val payload = BackupPayload(
            userEmail = userEmail,
            expenses = currentExpenses,
            savingsGoals = currentGoals,
            creditCards = currentCards,
            stockPositions = currentStocks,
            assetsLiabilities = currentAssets
        )

        return googleDriveSheetsService.saveLocalBackupFiles(context, payload)
    }

    suspend fun performGoogleBackup(userEmail: String): BackupResult {
        val currentExpenses = dao.getAllExpenses().first()
        val currentGoals = dao.getAllSavingsGoals().first()
        val currentCards = dao.getAllCreditCards().first()
        val currentStocks = dao.getAllStockPositions().first()
        val currentAssets = dao.getAllAssetsLiabilities().first()

        val payload = BackupPayload(
            userEmail = userEmail,
            expenses = currentExpenses,
            savingsGoals = currentGoals,
            creditCards = currentCards,
            stockPositions = currentStocks,
            assetsLiabilities = currentAssets
        )

        return googleDriveSheetsService.performBackupToDriveAndSheets(payload)
    }

    suspend fun restoreFromBackupPayload(jsonString: String): Boolean {
        val payload = googleDriveSheetsService.parseJsonBackup(jsonString) ?: return false
        return restoreFromPayloadObject(payload)
    }

    suspend fun restoreFromCsvBackupPayload(csvString: String): Boolean {
        val payload = googleDriveSheetsService.parseCsvBackup(csvString)
        return restoreFromPayloadObject(payload)
    }

    private suspend fun restoreFromPayloadObject(payload: BackupPayload): Boolean {
        if (payload.expenses.isNotEmpty()) {
            dao.clearExpenses()
            dao.insertAllExpenses(payload.expenses)
        }
        if (payload.savingsGoals.isNotEmpty()) {
            dao.clearSavingsGoals()
            dao.insertAllSavingsGoals(payload.savingsGoals)
        }
        if (payload.creditCards.isNotEmpty()) {
            dao.clearCreditCards()
            dao.insertAllCreditCards(payload.creditCards)
        }
        if (payload.stockPositions.isNotEmpty()) {
            dao.clearStockPositions()
            dao.insertAllStockPositions(payload.stockPositions)
        }
        if (payload.assetsLiabilities.isNotEmpty()) {
            dao.clearAssetsLiabilities()
            dao.insertAllAssetsLiabilities(payload.assetsLiabilities)
        }
        return true
    }

    // Gemini AI integrations
    suspend fun parseVoiceExpense(speechText: String): ParsedExpense {
        return geminiService.parseVoiceExpense(speechText)
    }

    suspend fun parseTicketOcr(bitmap: Bitmap): ParsedExpense {
        return geminiService.parseTicketOcr(bitmap)
    }

    suspend fun getFinancialTip(netWorth: Double, debt: Double, savings: Double): String {
        return geminiService.generateFinancialTip(netWorth, debt, savings)
    }
}
