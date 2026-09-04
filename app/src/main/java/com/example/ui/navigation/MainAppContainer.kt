package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.Credit50DaysStrategyScreen
import com.example.ui.screens.CreditCardsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DcaBreakEvenCalculatorScreen
import com.example.ui.screens.ExpenseEntryScreen
import com.example.ui.screens.FinancialEducationScreen
import com.example.ui.screens.HistoricalSupportAlertsScreen
import com.example.ui.screens.NetWorthAnalysisScreen
import com.example.ui.screens.NetWorthProjectionScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.StatementImportScreen
import com.example.ui.screens.StockPortfolioScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.auth.SecurityConfirmationScreen
import com.example.ui.screens.auth.TwoFactorAuthScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SapphireSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FinanzaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: FinanzaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val authUiState by viewModel.authUiState.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val expenses by viewModel.expenses.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val creditCards by viewModel.creditCards.collectAsState()
    val stockPositions by viewModel.stockPositions.collectAsState()
    val assetsLiabilities by viewModel.assetsLiabilities.collectAsState()

    val totalAssets by viewModel.totalAssets.collectAsState()
    val totalLiabilities by viewModel.totalLiabilities.collectAsState()
    val netWorth by viewModel.netWorth.collectAsState()

    val aiTip by viewModel.aiTip.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val draftExpense by viewModel.draftExpense.collectAsState()
    val backupUiState by viewModel.backupUiState.collectAsState()
    val isSyncingGoogleFinance by viewModel.isSyncingGoogleFinance.collectAsState()
    val googleFinanceStatus by viewModel.googleFinanceStatus.collectAsState()

    val soldStocks by viewModel.soldStocks.collectAsState()
    val statementImports by viewModel.statementImports.collectAsState()
    val statementParsingResult by viewModel.statementParsingResult.collectAsState()
    val isStatementParsing by viewModel.isStatementParsing.collectAsState()
    val accountTransfers by viewModel.accountTransfers.collectAsState()


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Handle Authentication screens without topbar/bottombar scaffold wrapper
    when (currentScreen) {
        AppScreen.LOGIN -> {
            LoginScreen(
                errorMessage = authUiState.errorMessage,
                securityMessage = authUiState.securityMessage,
                onLoginClick = { email, password -> viewModel.login(email, password) },
                onGoogleLoginClick = { email, startClean -> viewModel.loginWithGoogle(email, startClean) },
                onRegisterNav = {
                    viewModel.clearAuthErrorMessage()
                    viewModel.navigateTo(AppScreen.REGISTER)
                },
                onForgotPasswordNav = {
                    viewModel.clearAuthErrorMessage()
                    viewModel.navigateTo(AppScreen.FORGOT_PASSWORD)
                }
            )
            return
        }
        AppScreen.REGISTER -> {
            RegisterScreen(
                errorMessage = authUiState.errorMessage,
                onRegisterSubmit = { name, email, password -> viewModel.register(name, email, password) },
                onGoogleRegisterClick = { email, startClean -> viewModel.loginWithGoogle(email, startClean) },
                onBackToLogin = {
                    viewModel.clearAuthErrorMessage()
                    viewModel.navigateTo(AppScreen.LOGIN)
                }
            )
            return
        }
        AppScreen.FORGOT_PASSWORD -> {
            ForgotPasswordScreen(
                onResetSubmit = { email -> viewModel.resetPassword(email) },
                onBackToLogin = { viewModel.navigateTo(AppScreen.LOGIN) }
            )
            return
        }
        AppScreen.TWO_FACTOR_AUTH -> {
            TwoFactorAuthScreen(
                email = authUiState.email,
                onVerifyCode = { code -> viewModel.verifyTwoFactor(code) }
            )
            return
        }
        AppScreen.SECURITY_CONFIRMATION -> {
            SecurityConfirmationScreen(
                message = authUiState.securityMessage,
                onContinueClick = { viewModel.completeSecurityFlow() }
            )
            return
        }
        else -> { /* Main App Flow */ }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "FinanzaInteligente",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    )
                    Text(
                        text = authUiState.email,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    NavigationDrawerItem(
                        label = { Text("Resumen Principal") },
                        selected = currentScreen == AppScreen.DASHBOARD,
                        onClick = {
                            viewModel.navigateTo(AppScreen.DASHBOARD)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                        modifier = Modifier.testTag("drawer_item_dashboard")
                    )

                    NavigationDrawerItem(
                        label = { Text("Registro Rápido IA (Voz / OCR)") },
                        selected = currentScreen == AppScreen.EXPENSE_ENTRY,
                        onClick = {
                            viewModel.navigateTo(AppScreen.EXPENSE_ENTRY)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }
                    )

                    NavigationDrawerItem(
                        label = { Text("Metas de Ahorro") },
                        selected = currentScreen == AppScreen.SAVINGS_GOALS,
                        onClick = {
                            viewModel.navigateTo(AppScreen.SAVINGS_GOALS)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Savings, contentDescription = null) }
                    )

                    NavigationDrawerItem(
                        label = { Text("Deudas y Créditos") },
                        selected = currentScreen == AppScreen.CREDIT_CARDS,
                        onClick = {
                            viewModel.navigateTo(AppScreen.CREDIT_CARDS)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = null) }
                    )

                    NavigationDrawerItem(
                        label = { Text("Estrategia 50 Días") },
                        selected = currentScreen == AppScreen.CREDIT_50_DAYS,
                        onClick = {
                            viewModel.navigateTo(AppScreen.CREDIT_50_DAYS)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
                    )

                    NavigationDrawerItem(
                        label = { Text("Análisis Patrimonio Neto") },
                        selected = currentScreen == AppScreen.NET_WORTH_ANALYSIS,
                        onClick = {
                            viewModel.navigateTo(AppScreen.NET_WORTH_ANALYSIS)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.PieChart, contentDescription = null) }
                    )

                    NavigationDrawerItem(
                        label = { Text("Proyección 6 Meses") },
                        selected = currentScreen == AppScreen.NET_WORTH_PROJECTION,
                        onClick = {
                            viewModel.navigateTo(AppScreen.NET_WORTH_PROJECTION)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.ShowChart, contentDescription = null) }
                    )

                    NavigationDrawerItem(
                        label = { Text("Portafolio Acciones & DCA") },
                        selected = currentScreen == AppScreen.STOCK_PORTFOLIO,
                        onClick = {
                            viewModel.navigateTo(AppScreen.STOCK_PORTFOLIO)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.ShowChart, contentDescription = null) }
                    )

                    NavigationDrawerItem(
                        label = { Text("Importar Estados de Cuenta PDF/Excel") },
                        selected = currentScreen == AppScreen.STATEMENT_IMPORT,
                        onClick = {
                            viewModel.navigateTo(AppScreen.STATEMENT_IMPORT)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.FileUpload, contentDescription = null) }
                    )

                    NavigationDrawerItem(
                        label = { Text("Educación & Configuración") },
                        selected = currentScreen == AppScreen.EDUCATION_SETTINGS,
                        onClick = {
                            viewModel.navigateTo(AppScreen.EDUCATION_SETTINGS)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (isDarkTheme) SapphireSecondary else EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isDarkTheme) "Modo Oscuro" else "Modo Claro",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.toggleDarkTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SapphireSecondary,
                                checkedTrackColor = SapphireSecondary.copy(alpha = 0.5f),
                                uncheckedThumbColor = EmeraldPrimary,
                                uncheckedTrackColor = EmeraldPrimary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("drawer_theme_toggle_switch")
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                AppScreen.DASHBOARD -> "FinanzaInteligente"
                                AppScreen.EXPENSE_ENTRY -> "Registro de Gastos"
                                AppScreen.SAVINGS_GOALS -> "Metas de Ahorro"
                                AppScreen.CREDIT_CARDS -> "Deudas y Créditos"
                                AppScreen.CREDIT_50_DAYS -> "Estrategia 50 Días"
                                AppScreen.NET_WORTH_ANALYSIS -> "Patrimonio Neto"
                                AppScreen.NET_WORTH_PROJECTION -> "Proyección Futura"
                                AppScreen.STOCK_PORTFOLIO -> "Portafolio Inversiones"
                                AppScreen.STATEMENT_IMPORT -> "Importar Estados de Cuenta"
                                AppScreen.DCA_CALCULATOR -> "Calculadora DCA"
                                AppScreen.HISTORICAL_ALERTS -> "Alertas de Soporte"
                                AppScreen.EDUCATION_SETTINGS -> "Ajustes & Educación"
                                else -> "FinanzaInteligente"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleDarkTheme(!isDarkTheme) },
                            modifier = Modifier.testTag("top_bar_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "Cambiar a Modo Claro" else "Cambiar a Modo Oscuro",
                                tint = if (isDarkTheme) Color(0xFFFBBF24) else EmeraldPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DASHBOARD,
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Inicio") },
                        label = { Text("Inicio") },
                        modifier = Modifier.testTag("nav_bottom_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.EXPENSE_ENTRY,
                        onClick = { viewModel.navigateTo(AppScreen.EXPENSE_ENTRY) },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Gastos") },
                        label = { Text("Gastos") },
                        modifier = Modifier.testTag("nav_bottom_expenses")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SAVINGS_GOALS,
                        onClick = { viewModel.navigateTo(AppScreen.SAVINGS_GOALS) },
                        icon = { Icon(Icons.Default.Savings, contentDescription = "Ahorros") },
                        label = { Text("Ahorro") },
                        modifier = Modifier.testTag("nav_bottom_savings")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.CREDIT_CARDS || currentScreen == AppScreen.CREDIT_50_DAYS,
                        onClick = { viewModel.navigateTo(AppScreen.CREDIT_CARDS) },
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = "Créditos") },
                        label = { Text("Crédito") },
                        modifier = Modifier.testTag("nav_bottom_credit")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.STOCK_PORTFOLIO,
                        onClick = { viewModel.navigateTo(AppScreen.STOCK_PORTFOLIO) },
                        icon = { Icon(Icons.Default.ShowChart, contentDescription = "Bolsa") },
                        label = { Text("Bolsa") },
                        modifier = Modifier.testTag("nav_bottom_stocks")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        totalAssets = totalAssets,
                        totalLiabilities = totalLiabilities,
                        netWorth = netWorth,
                        recentExpenses = expenses,
                        aiTip = aiTip,
                        isAiLoading = isAiLoading,
                        onNavigateTo = { screen -> viewModel.navigateTo(screen) },
                        onRefreshTip = { viewModel.refreshAiTip() },
                        onClearSampleData = { viewModel.clearAllSampleDataForRealMode() }
                    )

                    AppScreen.EXPENSE_ENTRY -> ExpenseEntryScreen(
                        draftExpense = draftExpense,
                        isAiLoading = isAiLoading,
                        allExpenses = expenses,
                        onVoiceDictate = { text -> viewModel.processVoiceDictation(text) },
                        onOcrScan = { bitmap -> viewModel.processTicketOcr(bitmap) },
                        onSaveExpense = { amt, cat, merchant, note, type ->
                            viewModel.addExpense(amt, cat, merchant, note, type)
                            viewModel.navigateTo(AppScreen.DASHBOARD)
                        },
                        onClearDraft = { viewModel.clearDraftExpense() }
                    )

                    AppScreen.SAVINGS_GOALS -> SavingsGoalsScreen(
                        goals = savingsGoals,
                        onAddDeposit = { goal, amt -> viewModel.addDepositToGoal(goal, amt) },
                        onAddNewGoal = { title, target, cat, date ->
                            viewModel.addNewGoal(title, target, cat, date)
                        }
                    )

                    AppScreen.CREDIT_CARDS -> CreditCardsScreen(
                        cards = creditCards,
                        onAddCard = { name, bank, limit, bal, cut, due ->
                            viewModel.addNewCreditCard(name, bank, limit, bal, cut, due)
                        },
                        onNavigateTo50Days = { viewModel.navigateTo(AppScreen.CREDIT_50_DAYS) }
                    )

                    AppScreen.CREDIT_50_DAYS -> Credit50DaysStrategyScreen(
                        cards = creditCards,
                        bestCard = viewModel.getBestCardForToday(creditCards)
                    )

                    AppScreen.NET_WORTH_ANALYSIS -> NetWorthAnalysisScreen(
                        totalAssets = totalAssets,
                        totalLiabilities = totalLiabilities,
                        netWorth = netWorth,
                        items = assetsLiabilities,
                        accountTransfers = accountTransfers,
                        onAddAssetLiability = { title, amt, type, cat ->
                            viewModel.addAssetLiability(title, amt, type, cat)
                        },
                        onRecordTransfer = { src, dest, amt, note ->
                            viewModel.recordAccountTransfer(src, dest, amt, note)
                        }
                    )


                    AppScreen.NET_WORTH_PROJECTION -> NetWorthProjectionScreen(
                        currentNetWorth = netWorth,
                        onUpdateProjection = { m, a, u, s, f ->
                            viewModel.updateProjection(m, a, u, s, f)
                        }
                    )

                    AppScreen.STOCK_PORTFOLIO -> StockPortfolioScreen(
                        stocks = stockPositions,
                        soldStocks = soldStocks,
                        isSyncingGoogleFinance = isSyncingGoogleFinance,
                        googleFinanceStatus = googleFinanceStatus,
                        userEmail = authUiState.email,
                        onSyncGoogleFinance = { viewModel.syncGoogleFinanceLiveData() },
                        onAddStock = { sym, comp, shares, buy, cur, sup, pri, asset, broker, lev, tick ->
                            viewModel.addStockPosition(sym, comp, shares, buy, cur, sup, pri, asset, broker, lev, tick)
                        },
                        onSellStock = { stock, sharesToSell, sellPrice ->
                            viewModel.sellStockPosition(stock, sharesToSell, sellPrice)
                        },
                        onNavigateToDca = { viewModel.navigateTo(AppScreen.DCA_CALCULATOR) },
                        onNavigateToAlerts = { viewModel.navigateTo(AppScreen.HISTORICAL_ALERTS) },
                        onNavigateToStatementImport = { viewModel.navigateTo(AppScreen.STATEMENT_IMPORT) }
                    )

                    AppScreen.STATEMENT_IMPORT -> StatementImportScreen(
                        statementImports = statementImports,
                        parsingResult = statementParsingResult,
                        isParsing = isStatementParsing,
                        onParseDocument = { file, bytes, mime, text ->
                            viewModel.parseStatementDocument(file, bytes, mime, text)
                        },
                        onConfirmImport = { result, file ->
                            viewModel.confirmAndImportStatementResult(result, file)
                        },
                        onClearResult = { viewModel.clearStatementParsingResult() },
                        onBack = { viewModel.navigateTo(AppScreen.STOCK_PORTFOLIO) }
                    )

                    AppScreen.DCA_CALCULATOR -> DcaBreakEvenCalculatorScreen(
                        onCalculateDca = { symbol, newShares, newPrice ->
                            viewModel.updateDcaCalculation(symbol, newShares, newPrice)
                        }
                    )

                    AppScreen.HISTORICAL_ALERTS -> HistoricalSupportAlertsScreen(
                        stocks = stockPositions
                    )

                    AppScreen.EDUCATION_SETTINGS -> FinancialEducationScreen(
                        userEmail = authUiState.email,
                        is2FAEnabled = authUiState.is2FAEnabled,
                        backupUiState = backupUiState,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { isDark -> viewModel.toggleDarkTheme(isDark) },
                        onToggle2FA = { enabled -> viewModel.toggle2FA(enabled) },
                        onPerformBackupNow = { ctx -> viewModel.performBackupNow(ctx) },
                        onPerformLocalBackupOnly = { ctx -> viewModel.performLocalBackupNow(ctx) },
                        onSetBackupFrequency = { freq -> viewModel.setBackupFrequency(freq) },
                        onRestoreFromBackup = { viewModel.restoreFromCloudBackup() },
                        onRestoreFromLocalJson = { json -> viewModel.restoreFromLocalJsonBackup(json) },
                        onRestoreFromLocalCsv = { csv -> viewModel.restoreFromLocalCsvBackup(csv) },
                        onClearSampleData = { viewModel.clearAllSampleDataForRealMode() },
                        onLogout = { viewModel.navigateTo(AppScreen.LOGIN) }
                    )

                    else -> { /* Fallback */ }
                }
            }
        }
    }
}
