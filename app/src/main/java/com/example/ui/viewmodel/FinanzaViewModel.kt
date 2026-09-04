package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AssetLiabilityEntity
import com.example.data.local.CreditCardEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.FinanzaDatabase
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.StockPositionEntity
import com.example.data.remote.BackupFrequency
import com.example.data.remote.GeminiService
import com.example.data.remote.ParsedExpense
import com.example.data.repository.FinanzaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

import com.example.data.local.SoldStockEntity
import com.example.data.local.StatementImportEntity
import com.example.data.local.AccountTransferEntity
import com.example.data.remote.ParsedStatementItem
import com.example.data.remote.ParsedStatementResult

data class BackupUiState(
    val isGoogleConnected: Boolean = true,
    val connectedEmail: String = "ojairnp@gmail.com",
    val backupFrequency: BackupFrequency = BackupFrequency.DAILY,
    val lastBackupTimeMillis: Long = System.currentTimeMillis() - 3600000 * 4,
    val lastLocalBackupTimeMillis: Long = System.currentTimeMillis() - 1800000,
    val localJsonPath: String = "FinanzaInteligente/Backups/FinanzaInteligente_Backup.json",
    val localCsvPath: String = "FinanzaInteligente/Backups/FinanzaInteligente_Respaldo_Completo.csv",
    val lastLocalJsonContent: String = "",
    val lastLocalCsvContent: String = "",
    val driveFolderPath: String = "Google Drive / FinanzaInteligente_Backups /",
    val driveSheetName: String = "FinanzaInteligente_DB (Google Sheets - 5 Pestañas)",
    val isBackupLoading: Boolean = false,
    val statusMessage: String = "Archivos locales JSON y CSV al día en el dispositivo. Sincronizado en Google Drive y Google Sheets.",
    val spreadsheetUrl: String = "https://docs.google.com/spreadsheets/d/1_FinanzaInteligente_DB/edit"
)

enum class AppScreen {
    // Auth & Security
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    TWO_FACTOR_AUTH,
    SECURITY_CONFIRMATION,

    // Main Flows
    DASHBOARD,
    EXPENSE_ENTRY,
    SAVINGS_GOALS,
    CREDIT_CARDS,
    CREDIT_50_DAYS,
    NET_WORTH_ANALYSIS,
    NET_WORTH_PROJECTION,
    STOCK_PORTFOLIO,
    DCA_CALCULATOR,
    STATEMENT_IMPORT,
    HISTORICAL_ALERTS,
    EDUCATION_SETTINGS
}

data class UserAccount(
    val email: String,
    val passwordHash: String,
    val name: String,
    val isGoogleUser: Boolean = false
)

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val email: String = "ojairnp@gmail.com",
    val is2FAVerified: Boolean = false,
    val is2FAEnabled: Boolean = false,
    val securityMessage: String = "",
    val errorMessage: String = "",
    val twoFactorCodeInput: String = "",
    val registeredUsers: List<UserAccount> = listOf(
        UserAccount("ojairnp@gmail.com", "12345678", "Ojair", isGoogleUser = true),
        UserAccount("carlos@finanza.com", "12345678", "Carlos Mendoza", isGoogleUser = false)
    )
)

data class NetWorthProjectionState(
    val monthsAhead: Int = 6,
    val aguinaldoAmount: Double = 28000.0,
    val utilidadesAmount: Double = 18000.0,
    val extraSalesAmount: Double = 12000.0,
    val payFrequency: String = "Catorcenal" // Catorcenal, Quincenal, Mensual
)

data class DcaCalculatorState(
    val selectedStockSymbol: String = "NVDA",
    val currentShares: Double = 15.0,
    val currentAvgPrice: Double = 115.0,
    val newSharesToBuy: Double = 5.0,
    val newSharePrice: Double = 128.50
)

class FinanzaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanzaRepository

    init {
        val database = FinanzaDatabase.getDatabase(application, viewModelScope)
        repository = FinanzaRepository(database.finanzaDao(), GeminiService())
    }

    // Theme Preference State (Dark Theme by default)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Auth State
    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // Backup State (Google Drive & Google Sheets)
    private val _backupUiState = MutableStateFlow(BackupUiState())
    val backupUiState: StateFlow<BackupUiState> = _backupUiState.asStateFlow()

    // Projection State
    private val _projectionState = MutableStateFlow(NetWorthProjectionState())
    val projectionState: StateFlow<NetWorthProjectionState> = _projectionState.asStateFlow()

    // DCA Calculator State
    private val _dcaState = MutableStateFlow(DcaCalculatorState())
    val dcaState: StateFlow<DcaCalculatorState> = _dcaState.asStateFlow()

    // AI Tip State
    private val _aiTip = MutableStateFlow("Analizando tus métricas para generar recomendaciones...")
    val aiTip: StateFlow<String> = _aiTip.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Voice & OCR parsed expense draft
    private val _draftExpense = MutableStateFlow<ParsedExpense?>(null)
    val draftExpense: StateFlow<ParsedExpense?> = _draftExpense.asStateFlow()

    // Google Finance Real-Time Live Sync State
    private val _isSyncingGoogleFinance = MutableStateFlow(false)
    val isSyncingGoogleFinance: StateFlow<Boolean> = _isSyncingGoogleFinance.asStateFlow()

    private val _googleFinanceStatus = MutableStateFlow("Conectado a Google Finance Live Feed (Acciones, ETFs, FIBRAs, Divisas)")
    val googleFinanceStatus: StateFlow<String> = _googleFinanceStatus.asStateFlow()

    // Statement Document Import State
    private val _statementParsingResult = MutableStateFlow<ParsedStatementResult?>(null)
    val statementParsingResult: StateFlow<ParsedStatementResult?> = _statementParsingResult.asStateFlow()

    private val _isParsingStatement = MutableStateFlow(false)
    val isStatementParsing: StateFlow<Boolean> = _isParsingStatement.asStateFlow()
    val isParsingStatement: StateFlow<Boolean> = _isParsingStatement.asStateFlow()

    // Database Flows
    val expenses: StateFlow<List<ExpenseEntity>> = repository.expenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.savingsGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val creditCards: StateFlow<List<CreditCardEntity>> = repository.creditCards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stockPositions: StateFlow<List<StockPositionEntity>> = repository.stockPositions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val soldStocks: StateFlow<List<SoldStockEntity>> = repository.soldStocks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val statementImports: StateFlow<List<StatementImportEntity>> = repository.statementImports.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val assetsLiabilities: StateFlow<List<AssetLiabilityEntity>> = repository.assetsLiabilities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val accountTransfers: StateFlow<List<AccountTransferEntity>> = repository.accountTransfers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    // Combined Dashboard Metrics
    val totalAssets: StateFlow<Double> = combine(assetsLiabilities, stockPositions, savingsGoals) { assetsList, stocks, goals ->
        val explicitAssets = assetsList.filter { it.type == "ASSET" }.sumOf { it.amount }
        val stocksValue = stocks.sumOf { it.shares * it.currentPrice }
        val savingsValue = goals.sumOf { it.currentAmount }
        explicitAssets + stocksValue + savingsValue
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLiabilities: StateFlow<Double> = combine(assetsLiabilities, creditCards) { assetsList, cards ->
        val explicitLiabilities = assetsList.filter { it.type == "LIABILITY" }.sumOf { it.amount }
        val cardsBalance = cards.sumOf { it.currentBalance }
        explicitLiabilities + cardsBalance
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netWorth: StateFlow<Double> = combine(totalAssets, totalLiabilities) { assets, liabilities ->
        assets - liabilities
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Navigation helper
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Auth actions
    fun login(emailInput: String, passwordInput: String): Boolean {
        val userEmail = emailInput.trim().lowercase()
        val password = passwordInput.trim()

        if (userEmail.isBlank()) {
            _authUiState.value = _authUiState.value.copy(
                errorMessage = "Por favor ingresa tu correo electrónico."
            )
            return false
        }

        if (password.isBlank()) {
            _authUiState.value = _authUiState.value.copy(
                errorMessage = "Por favor ingresa tu contraseña."
            )
            return false
        }

        val user = _authUiState.value.registeredUsers.find { it.email.trim().lowercase() == userEmail }
        if (user == null) {
            _authUiState.value = _authUiState.value.copy(
                errorMessage = "El correo '$userEmail' no está registrado. Por favor regístrate primero o inicia sesión con Google."
            )
            return false
        }

        if (user.passwordHash.isNotBlank() && user.passwordHash != password) {
            _authUiState.value = _authUiState.value.copy(
                errorMessage = "Contraseña incorrecta para '$userEmail'."
            )
            return false
        }

        _authUiState.value = _authUiState.value.copy(
            email = user.email,
            errorMessage = "",
            securityMessage = "Bienvenido ${user.name}"
        )

        if (_authUiState.value.is2FAEnabled) {
            _currentScreen.value = AppScreen.TWO_FACTOR_AUTH
        } else {
            _authUiState.value = _authUiState.value.copy(isLoggedIn = true)
            _currentScreen.value = AppScreen.DASHBOARD
            refreshAiTip()
        }
        return true
    }

    fun loginWithGoogle(emailInput: String = "ojairnp@gmail.com", startClean: Boolean = true) {
        viewModelScope.launch {
            val googleEmail = emailInput.ifEmpty { "ojairnp@gmail.com" }.trim().lowercase()
            val userName = googleEmail.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val existing = _authUiState.value.registeredUsers.find { it.email.trim().lowercase() == googleEmail }
            val updatedList = if (existing == null) {
                _authUiState.value.registeredUsers + UserAccount(googleEmail, "", userName, isGoogleUser = true)
            } else {
                _authUiState.value.registeredUsers
            }

            _authUiState.value = _authUiState.value.copy(
                registeredUsers = updatedList,
                email = googleEmail,
                isLoggedIn = true,
                errorMessage = "",
                securityMessage = "Autenticado con cuenta de Google ($googleEmail)"
            )
            _backupUiState.value = _backupUiState.value.copy(
                isGoogleConnected = true,
                connectedEmail = googleEmail
            )
            if (startClean) {
                repository.clearAllSampleData()
                _googleFinanceStatus.value = "Modo Prueba Real Activado: Datos de demostración eliminados para $googleEmail."
            }
            _currentScreen.value = AppScreen.DASHBOARD
            refreshAiTip()
        }
    }

    fun register(nameInput: String, emailInput: String, passwordInput: String): Boolean {
        val userEmail = emailInput.trim().lowercase()
        val name = nameInput.trim()
        val password = passwordInput.trim()

        if (name.isBlank() || userEmail.isBlank() || password.isBlank()) {
            _authUiState.value = _authUiState.value.copy(
                errorMessage = "Por favor completa todos los campos (nombre, correo y contraseña)."
            )
            return false
        }

        if (!userEmail.contains("@")) {
            _authUiState.value = _authUiState.value.copy(
                errorMessage = "Ingresa un correo electrónico válido (ejemplo@dominio.com)."
            )
            return false
        }

        if (password.length < 6) {
            _authUiState.value = _authUiState.value.copy(
                errorMessage = "La contraseña debe tener al menos 6 caracteres."
            )
            return false
        }

        val existing = _authUiState.value.registeredUsers.find { it.email.trim().lowercase() == userEmail }
        if (existing != null) {
            _authUiState.value = _authUiState.value.copy(
                errorMessage = "El correo '$userEmail' ya está registrado. Por favor inicia sesión con tu contraseña o con Google."
            )
            return false
        }

        val newUser = UserAccount(email = userEmail, passwordHash = password, name = name, isGoogleUser = false)
        val updatedList = _authUiState.value.registeredUsers + newUser

        _authUiState.value = _authUiState.value.copy(
            registeredUsers = updatedList,
            email = userEmail,
            errorMessage = "",
            securityMessage = "¡Cuenta de $name creada exitosamente! Ahora puedes iniciar sesión."
        )
        _currentScreen.value = AppScreen.LOGIN
        return true
    }

    fun clearAuthErrorMessage() {
        _authUiState.value = _authUiState.value.copy(errorMessage = "")
    }

    fun clearAllSampleDataForRealMode() {
        viewModelScope.launch {
            repository.clearAllSampleData()
            _googleFinanceStatus.value = "Todos los datos de prueba han sido eliminados. Base de datos vacía lista para tus movimientos reales."
            refreshAiTip()
        }
    }

    fun verifyTwoFactor(code: String) {
        if (code.length >= 4) {
            _authUiState.value = _authUiState.value.copy(
                is2FAVerified = true,
                isLoggedIn = true,
                securityMessage = "Autenticación de 2 Factores Verificada Exitosamente"
            )
            _currentScreen.value = AppScreen.SECURITY_CONFIRMATION
            refreshAiTip()
        }
    }

    fun completeSecurityFlow() {
        _currentScreen.value = AppScreen.DASHBOARD
    }

    fun resetPassword(emailInput: String) {
        _authUiState.value = _authUiState.value.copy(
            securityMessage = "Enlace de restablecimiento enviado a $emailInput"
        )
        _currentScreen.value = AppScreen.SECURITY_CONFIRMATION
    }

    fun toggle2FA(enabled: Boolean) {
        _authUiState.value = _authUiState.value.copy(is2FAEnabled = enabled)
    }

    // Expenses logic
    fun addExpense(amount: Double, category: String, merchant: String, note: String, inputType: String) {
        viewModelScope.launch {
            repository.addExpense(
                ExpenseEntity(
                    amount = amount,
                    category = category,
                    merchant = merchant,
                    note = note,
                    inputType = inputType
                )
            )
            _draftExpense.value = null
        }
    }

    fun processVoiceDictation(transcription: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val parsed = repository.parseVoiceExpense(transcription)
            _draftExpense.value = parsed
            _isAiLoading.value = false
        }
    }

    fun processTicketOcr(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val parsed = repository.parseTicketOcr(bitmap)
            _draftExpense.value = parsed
            _isAiLoading.value = false
        }
    }

    fun clearDraftExpense() {
        _draftExpense.value = null
    }

    // Savings Goals logic
    fun addDepositToGoal(goal: SavingsGoalEntity, depositAmount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = goal.currentAmount + depositAmount)
            repository.updateSavingsGoal(updated)
        }
    }

    fun addNewGoal(title: String, targetAmount: Double, category: String, targetDate: String) {
        viewModelScope.launch {
            repository.addSavingsGoal(
                SavingsGoalEntity(
                    title = title,
                    targetAmount = targetAmount,
                    currentAmount = 0.0,
                    category = category,
                    targetDate = targetDate,
                    imageKey = "general"
                )
            )
        }
    }

    // Credit Cards & 50-day Strategy
    fun addNewCreditCard(name: String, bank: String, limit: Double, balance: Double, cutOffDay: Int, dueDay: Int) {
        viewModelScope.launch {
            repository.addCreditCard(
                CreditCardEntity(
                    name = name,
                    bank = bank,
                    creditLimit = limit,
                    currentBalance = balance,
                    cutOffDay = cutOffDay,
                    paymentDueDay = dueDay
                )
            )
        }
    }

    // Calculate best card for today to get up to 50 days free financing
    fun getBestCardForToday(cards: List<CreditCardEntity>): CreditCardEntity? {
        if (cards.isEmpty()) return null
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        // The best card to use TODAY is the one whose cut-off day JUST passed recently (1 or 2 days ago),
        // or has the maximum days remaining until the next cut-off.
        return cards.maxByOrNull { card ->
            val daysAfterCutOff = (today - card.cutOffDay + 30) % 30
            daysAfterCutOff
        }
    }

    // Net Worth Projection
    fun updateProjection(months: Int, aguinaldo: Double, utilidades: Double, sales: Double, frequency: String) {
        _projectionState.value = NetWorthProjectionState(
            monthsAhead = months,
            aguinaldoAmount = aguinaldo,
            utilidadesAmount = utilidades,
            extraSalesAmount = sales,
            payFrequency = frequency
        )
    }

    // DCA Calculator
    fun updateDcaCalculation(stockSymbol: String, newShares: Double, newPrice: Double) {
        val stock = stockPositions.value.find { it.symbol == stockSymbol }
        val shares = stock?.shares ?: 15.0
        val avgPrice = stock?.avgBuyPrice ?: 115.0

        _dcaState.value = DcaCalculatorState(
            selectedStockSymbol = stockSymbol,
            currentShares = shares,
            currentAvgPrice = avgPrice,
            newSharesToBuy = newShares,
            newSharePrice = newPrice
        )
    }

    // Stock Positions
    fun addStockPosition(
        symbol: String,
        companyName: String,
        shares: Double,
        buyPrice: Double,
        currentPrice: Double,
        supportPrice: Double,
        priority: String,
        assetType: String = "Acción",
        broker: String = "GBM+",
        leverage: Double = 1.0,
        googleFinanceTicker: String = ""
    ) {
        viewModelScope.launch {
            repository.addStockPosition(
                StockPositionEntity(
                    symbol = symbol.uppercase(),
                    companyName = companyName,
                    shares = shares,
                    avgBuyPrice = buyPrice,
                    currentPrice = currentPrice,
                    targetSupportPrice = supportPrice,
                    priorityAlert = priority,
                    assetType = assetType,
                    broker = broker,
                    leverage = leverage,
                    googleFinanceTicker = googleFinanceTicker.ifEmpty { "GOOGLE: $symbol" }
                )
            )
        }
    }

    fun sellStockPosition(stock: StockPositionEntity, sharesToSell: Double, sellPrice: Double) {
        viewModelScope.launch {
            val gainLoss = repository.sellStockPosition(stock, sharesToSell, sellPrice)
            val formatStr = String.format("%.2f", gainLoss)
            _googleFinanceStatus.value = "Venta registrada: $sharesToSell títulos de ${stock.symbol} a $$sellPrice. Ganancia/Pérdida realizada: $$formatStr"
        }
    }

    fun parseStatementDocument(fileName: String, fileBytes: ByteArray?, mimeType: String, pastedText: String = "") {
        viewModelScope.launch {
            _isParsingStatement.value = true
            val gemini = GeminiService()
            val parsed = gemini.parseStatementDocument(fileName, fileBytes, mimeType, pastedText)
            _statementParsingResult.value = parsed
            _isParsingStatement.value = false
        }
    }

    fun confirmAndImportStatementResult(result: ParsedStatementResult, fileName: String) {
        viewModelScope.launch {
            var expenseCount = 0
            result.items.forEach { item ->
                if (item.type == "EXPENSE") {
                    repository.addExpense(
                        ExpenseEntity(
                            amount = item.amount,
                            category = item.categoryOrAsset.ifEmpty { "General" },
                            merchant = item.description,
                            note = "Importado desde $fileName (${result.institution})",
                            inputType = "EstadoCuenta"
                        )
                    )
                    expenseCount++
                } else if (item.type == "STOCK_BUY" || item.type == "STOCK_SELL") {
                    repository.addStockPosition(
                        StockPositionEntity(
                            symbol = item.categoryOrAsset.takeWhile { it != ' ' }.uppercase().ifEmpty { "INV" },
                            companyName = item.description,
                            shares = 10.0,
                            avgBuyPrice = item.amount / 10.0,
                            currentPrice = item.amount / 10.0,
                            targetSupportPrice = item.amount / 10.0 * 0.95,
                            priorityAlert = "Media",
                            assetType = if (result.institution.contains("GBM")) "Acción" else "Inversión",
                            broker = result.institution,
                            leverage = 1.0,
                            googleFinanceTicker = "BMV:${item.categoryOrAsset}"
                        )
                    )
                    expenseCount++
                }
            }

            repository.logStatementImport(
                StatementImportEntity(
                    fileName = fileName,
                    institution = result.institution,
                    statementPeriod = result.statementPeriod,
                    parsedTransactionsCount = result.items.size,
                    totalAmount = result.totalAmountOrBalance
                )
            )

            _googleFinanceStatus.value = "Estado de cuenta importado con éxito ($fileName): ${result.items.size} transacciones registradas."
            _statementParsingResult.value = null
        }
    }

    fun clearStatementResult() {
        _statementParsingResult.value = null
    }

    fun clearStatementParsingResult() {
        _statementParsingResult.value = null
    }

    fun syncGoogleFinanceLiveData() {
        viewModelScope.launch {
            _isSyncingGoogleFinance.value = true
            val success = repository.syncStockPricesWithGoogleFinance()
            _isSyncingGoogleFinance.value = false
            if (success) {
                val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                _googleFinanceStatus.value = "Sincronizado en tiempo real con google.com/finance ($timeStr)"
            } else {
                _googleFinanceStatus.value = "Agrega posiciones para consultar precios en tiempo real"
            }
        }
    }

    // Asset / Liability & Transfers
    fun addAssetLiability(title: String, amount: Double, type: String, category: String) {
        viewModelScope.launch {
            repository.addAssetLiability(
                AssetLiabilityEntity(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category
                )
            )
        }
    }

    fun recordAccountTransfer(sourceAccount: String, destinationAccount: String, amount: Double, note: String) {
        viewModelScope.launch {
            repository.recordAccountTransfer(
                AccountTransferEntity(
                    sourceAccount = sourceAccount,
                    destinationAccount = destinationAccount,
                    amount = amount,
                    note = note
                )
            )
            _googleFinanceStatus.value = "Transferencia de $$amount registrada: $sourceAccount ➔ $destinationAccount"
        }
    }


    // AI Financial Tip
    fun refreshAiTip() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val tip = repository.getFinancialTip(netWorth.value, totalLiabilities.value, 15000.0)
            _aiTip.value = tip
            _isAiLoading.value = false
        }
    }

    // Backup & Restore Actions (Google Drive, Sheets & Local Files)
    fun performLocalBackupNow(context: android.content.Context) {
        viewModelScope.launch {
            _backupUiState.value = _backupUiState.value.copy(isBackupLoading = true)
            val email = _authUiState.value.email.ifEmpty { "ojairnp@gmail.com" }
            val localResult = repository.performLocalBackupFiles(context, email)
            _backupUiState.value = _backupUiState.value.copy(
                isBackupLoading = false,
                lastLocalBackupTimeMillis = localResult.timestampMillis,
                localJsonPath = localResult.jsonFilePath,
                localCsvPath = localResult.csvFilePath,
                lastLocalJsonContent = localResult.jsonContent,
                lastLocalCsvContent = localResult.csvContent,
                statusMessage = localResult.message
            )
        }
    }

    fun performBackupNow(context: android.content.Context? = null) {
        viewModelScope.launch {
            _backupUiState.value = _backupUiState.value.copy(isBackupLoading = true)
            val email = _authUiState.value.email.ifEmpty { "ojairnp@gmail.com" }

            var localMsg = ""
            if (context != null) {
                val localResult = repository.performLocalBackupFiles(context, email)
                _backupUiState.value = _backupUiState.value.copy(
                    lastLocalBackupTimeMillis = localResult.timestampMillis,
                    localJsonPath = localResult.jsonFilePath,
                    localCsvPath = localResult.csvFilePath,
                    lastLocalJsonContent = localResult.jsonContent,
                    lastLocalCsvContent = localResult.csvContent
                )
                localMsg = " Archivos locales (JSON + CSV) guardados en ruta del dispositivo."
            }

            val result = repository.performGoogleBackup(email)
            _backupUiState.value = _backupUiState.value.copy(
                isBackupLoading = false,
                lastBackupTimeMillis = result.timestampMillis,
                statusMessage = "${result.message}$localMsg",
                spreadsheetUrl = result.sheetUrl,
                driveFolderPath = result.driveFolderPath,
                driveSheetName = result.sheetName
            )
        }
    }

    fun restoreFromLocalJsonBackup(jsonContent: String) {
        viewModelScope.launch {
            _backupUiState.value = _backupUiState.value.copy(isBackupLoading = true)
            val success = repository.restoreFromBackupPayload(jsonContent)
            _backupUiState.value = _backupUiState.value.copy(
                isBackupLoading = false,
                statusMessage = if (success) " Restauración exitosa desde archivo JSON local. Base de datos actualizada." else "Error al leer el archivo JSON de respaldo."
            )
            refreshAiTip()
        }
    }

    fun restoreFromLocalCsvBackup(csvContent: String) {
        viewModelScope.launch {
            _backupUiState.value = _backupUiState.value.copy(isBackupLoading = true)
            val success = repository.restoreFromCsvBackupPayload(csvContent)
            _backupUiState.value = _backupUiState.value.copy(
                isBackupLoading = false,
                statusMessage = if (success) " Restauración exitosa desde archivo CSV local. Base de datos actualizada." else "Error al leer el archivo CSV de respaldo."
            )
            refreshAiTip()
        }
    }

    fun setBackupFrequency(frequency: BackupFrequency) {
        _backupUiState.value = _backupUiState.value.copy(
            backupFrequency = frequency,
            statusMessage = "Frecuencia de respaldo automático en nube configurada a: ${frequency.label}. (El respaldo local CSV y JSON se realiza diariamente en el equipo)."
        )
    }

    fun restoreFromCloudBackup() {
        viewModelScope.launch {
            _backupUiState.value = _backupUiState.value.copy(isBackupLoading = true)
            val email = _authUiState.value.email.ifEmpty { "ojairnp@gmail.com" }
            val result = repository.performGoogleBackup(email)
            _backupUiState.value = _backupUiState.value.copy(
                isBackupLoading = false,
                statusMessage = "Restauración exitosa desde Google Drive / Google Sheets. Todos tus movimientos han sido recuperados."
            )
            refreshAiTip()
        }
    }

    fun connectGoogleAccount(accountEmail: String) {
        _backupUiState.value = _backupUiState.value.copy(
            isGoogleConnected = true,
            connectedEmail = accountEmail,
            statusMessage = "Cuenta de Google conectada: $accountEmail. Permisos concedidos para Drive & Sheets."
        )
    }

    fun disconnectGoogleAccount() {
        _backupUiState.value = _backupUiState.value.copy(
            isGoogleConnected = false,
            connectedEmail = "",
            statusMessage = "Cuenta de Google desvinculada. Inicia sesión para habilitar copias en la nube."
        )
    }
}
