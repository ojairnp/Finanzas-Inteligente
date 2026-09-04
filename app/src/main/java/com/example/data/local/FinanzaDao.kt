package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanzaDao {

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpenses(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // Savings Goals
    @Query("SELECT * FROM savings_goals")
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSavingsGoals(goals: List<SavingsGoalEntity>)

    @Query("DELETE FROM savings_goals")
    suspend fun clearSavingsGoals()

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)

    // Credit Cards / Debts
    @Query("SELECT * FROM credit_cards")
    fun getAllCreditCards(): Flow<List<CreditCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(card: CreditCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCreditCards(cards: List<CreditCardEntity>)

    @Query("DELETE FROM credit_cards")
    suspend fun clearCreditCards()

    @Update
    suspend fun updateCreditCard(card: CreditCardEntity)

    // Stocks
    @Query("SELECT * FROM stock_positions")
    fun getAllStockPositions(): Flow<List<StockPositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockPosition(stock: StockPositionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStockPositions(stocks: List<StockPositionEntity>)

    @Query("DELETE FROM stock_positions")
    suspend fun clearStockPositions()

    @Update
    suspend fun updateStockPosition(stock: StockPositionEntity)

    @Delete
    suspend fun deleteStockPosition(stock: StockPositionEntity)

    // Sold Stocks
    @Query("SELECT * FROM sold_stock_positions ORDER BY saleDateMillis DESC")
    fun getAllSoldStocks(): Flow<List<SoldStockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoldStock(soldStock: SoldStockEntity): Long

    @Query("DELETE FROM sold_stock_positions")
    suspend fun clearSoldStocks()

    // Statement Imports
    @Query("SELECT * FROM statement_imports ORDER BY importDateMillis DESC")
    fun getAllStatementImports(): Flow<List<StatementImportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatementImport(importItem: StatementImportEntity): Long

    @Query("DELETE FROM statement_imports")
    suspend fun clearStatementImports()

    // Assets & Liabilities
    @Query("SELECT * FROM asset_liabilities")
    fun getAllAssetsLiabilities(): Flow<List<AssetLiabilityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetLiability(item: AssetLiabilityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAssetsLiabilities(items: List<AssetLiabilityEntity>)

    @Query("DELETE FROM asset_liabilities")
    suspend fun clearAssetsLiabilities()

    @Delete
    suspend fun deleteAssetLiability(item: AssetLiabilityEntity)

    // Account Transfers & Traceability
    @Query("SELECT * FROM account_transfers ORDER BY dateMillis DESC")
    fun getAllAccountTransfers(): Flow<List<AccountTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountTransfer(transfer: AccountTransferEntity): Long

    @Query("DELETE FROM account_transfers")
    suspend fun clearAccountTransfers()

}
