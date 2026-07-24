package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SoldStockEntity
import com.example.data.local.StockPositionEntity
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.History
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SapphireSecondary
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPortfolioScreen(
    stocks: List<StockPositionEntity>,
    soldStocks: List<SoldStockEntity> = emptyList(),
    isSyncingGoogleFinance: Boolean = false,
    googleFinanceStatus: String = "Conectado con Google Finance Live",
    userEmail: String = "",
    onSyncGoogleFinance: () -> Unit = {},
    onAddStock: (
        symbol: String,
        companyName: String,
        shares: Double,
        buyPrice: Double,
        currentPrice: Double,
        supportPrice: Double,
        priority: String,
        assetType: String,
        broker: String,
        leverage: Double,
        ticker: String
    ) -> Unit,
    onSellStock: (stock: StockPositionEntity, sharesToSell: Double, sellPrice: Double) -> Unit = { _, _, _ -> },
    onNavigateToDca: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToStatementImport: () -> Unit = {}
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "US"))
    var showAddDialog by remember { mutableStateOf(false) }
    var stockToSell by remember { mutableStateOf<StockPositionEntity?>(null) }

    var activeTab by remember { mutableStateOf("Activas") } // "Activas" or "Vendidas"

    var selectedAssetFilter by remember { mutableStateOf("Todos") }
    var selectedBrokerFilter by remember { mutableStateOf("Todos") }

    // Filtered stocks list
    val filteredStocks = stocks.filter { stock ->
        val matchesAsset = selectedAssetFilter == "Todos" || stock.assetType.contains(selectedAssetFilter, ignoreCase = true)
        val matchesBroker = selectedBrokerFilter == "Todos" || stock.broker.contains(selectedBrokerFilter, ignoreCase = true)
        matchesAsset && matchesBroker
    }

    // Calculations incorporating leverage: gain = (currentPrice - avgBuyPrice) * shares * leverage
    val totalPortfolioValue = stocks.sumOf { it.shares * it.currentPrice * it.leverage }
    val totalInvested = stocks.sumOf { it.shares * it.avgBuyPrice }
    val totalGainLoss = stocks.sumOf { (it.currentPrice - it.avgBuyPrice) * it.shares * it.leverage }

    // Broker grouped summary
    val brokerGroups = stocks.groupBy { it.broker }

    // Form states for adding position
    var symbolText by remember { mutableStateOf("") }
    var companyText by remember { mutableStateOf("") }
    var sharesText by remember { mutableStateOf("") }
    var buyPriceText by remember { mutableStateOf("") }
    var currentPriceText by remember { mutableStateOf("") }
    var supportPriceText by remember { mutableStateOf("") }
    var priorityText by remember { mutableStateOf("Alta") }

    var assetTypeSelected by remember { mutableStateOf("Acción") }
    var brokerSelected by remember { mutableStateOf("GBM+") }
    var leverageSelected by remember { mutableStateOf(1.0) }

    val assetTypesList = listOf("Acción", "ETF", "FIBRA", "Divisa / Cripto", "Fondo de Inversión")
    val brokersList = listOf(
        "GBM+",
        "Capital.com",
        "Fondos BBVA",
        "Plata Inversión",
        "Interactive Brokers",
        "Bitso",
        "Actinver / Bursanet",
        "Finamex",
        "ARQ / Otro Broker"
    )
    val leverageOptionsList = listOf(1.0, 2.0, 5.0, 10.0, 20.0, 50.0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title
            item {
                Text(
                    text = "Portafolio e Inversiones Multibolsa",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = "Google Finance en tiempo real • Integración GBM, Capital.com (Apalancamiento), BBVA, Bitso, Plata y más",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // --- GOOGLE FINANCE REAL-TIME SYNC HEADER CARD ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Google Finance Live Ticker",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = EmeraldPrimary
                                    )
                                    Text(
                                        text = if (userEmail.isNotEmpty()) "Cuenta vinculada: $userEmail" else "Conexión Google Activa",
                                        fontSize = 11.sp,
                                        color = SapphireSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Button(
                                onClick = onSyncGoogleFinance,
                                enabled = !isSyncingGoogleFinance,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("sync_google_finance_btn")
                            ) {
                                if (isSyncingGoogleFinance) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Actualizando...", fontSize = 11.sp)
                                } else {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sincronizar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = googleFinanceStatus,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Portfolio Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "VALOR TOTAL PORTAFOLIO MULTIBOLSA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currencyFormat.format(totalPortfolioValue),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Ganancia/Pérdida Neto (incl. Apalancamiento): ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (totalGainLoss >= 0) "+" else ""}${currencyFormat.format(totalGainLoss)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalGainLoss >= 0) EmeraldPrimary else ExpenseRed
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateToDca,
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("nav_dca_calculator_btn")
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Punto Equilibrio", fontSize = 11.sp)
                            }

                            Button(
                                onClick = onNavigateToAlerts,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("nav_support_alerts_btn")
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Alertas Soporte", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onNavigateToStatementImport,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nav_import_statement_btn")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = SapphireSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Subir Estados de Cuenta PDF / Excel / Imagen (GBM, Bancos)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SapphireSecondary)
                        }
                    }
                }
            }

            // --- BROKER PROFITABILITY BREAKDOWN (GBM, Capital.com, Fondos BBVA, Bitso) ---
            item {
                Column {
                    Text(
                        text = "Rendimiento por Casa de Bolsa & Plataforma",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(brokerGroups.entries.toList()) { entry ->
                            val brokerName = entry.key
                            val brokerPositions = entry.value
                            val brokerMarketVal = brokerPositions.sumOf { it.shares * it.currentPrice * it.leverage }
                            val brokerGainLoss = brokerPositions.sumOf { (it.currentPrice - it.avgBuyPrice) * it.shares * it.leverage }

                            Card(
                                modifier = Modifier
                                    .width(180.dp)
                                    .clickable { selectedBrokerFilter = brokerName },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedBrokerFilter == brokerName) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = SapphireSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = brokerName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SapphireSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = currencyFormat.format(brokerMarketVal),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${if (brokerGainLoss >= 0) "+" else ""}${currencyFormat.format(brokerGainLoss)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (brokerGainLoss >= 0) EmeraldPrimary else ExpenseRed
                                        )
                                    }

                                    Text(
                                        text = "${brokerPositions.size} posición(es)",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Asset & Broker Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Filtrar por Tipo de Activo:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val filterOptions = listOf("Todos", "Acción", "ETF", "FIBRA", "Divisa", "Fondo")
                        items(filterOptions) { filter ->
                            FilterChip(
                                selected = selectedAssetFilter == filter,
                                onClick = { selectedAssetFilter = filter },
                                label = { Text(filter, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Tabs for Active vs Sold Positions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeTab == "Activas") EmeraldPrimary else Color.Transparent)
                            .clickable { activeTab = "Activas" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShowChart, contentDescription = null, tint = if (activeTab == "Activas") Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Posiciones Activas (${filteredStocks.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (activeTab == "Activas") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeTab == "Vendidas") SapphireSecondary else Color.Transparent)
                            .clickable { activeTab = "Vendidas" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = if (activeTab == "Vendidas") Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Acciones Vendidas (${soldStocks.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (activeTab == "Vendidas") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (activeTab == "Activas") {
                if (filteredStocks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No se encontraron posiciones activas con los filtros seleccionados.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(filteredStocks) { stock ->
                        StockItemRow(
                            stock = stock,
                            currencyFormat = currencyFormat,
                            onSellClick = { stockToSell = stock }
                        )
                    }
                }
            } else {
                if (soldStocks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Aún no tienes registro de acciones o títulos vendidos.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(soldStocks) { soldStock ->
                        SoldStockItemRow(soldStock = soldStock, currencyFormat = currencyFormat)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_stock_fab"),
            containerColor = EmeraldPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar Inversión")
        }
    }

    // --- ADD INVESTMENT POSITION DIALOG ---
    if (showAddDialog) {
        var brokerExpanded by remember { mutableStateOf(false) }
        var assetTypeExpanded by remember { mutableStateOf(false) }
        var leverageExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva Inversión / Posición", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(360.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = symbolText,
                            onValueChange = { symbolText = it },
                            label = { Text("Ticker / Símbolo (ej. NVDA, FUNO11, USD/MXN)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stock_symbol_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = companyText,
                            onValueChange = { companyText = it },
                            label = { Text("Nombre o Descripción (ej. NVIDIA, Fondo BBVA)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Broker Dropdown
                    item {
                        ExposedDropdownMenuBox(
                            expanded = brokerExpanded,
                            onExpandedChange = { brokerExpanded = !brokerExpanded }
                        ) {
                            OutlinedTextField(
                                value = brokerSelected,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Plataforma / Casa de Bolsa") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brokerExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = brokerExpanded,
                                onDismissRequest = { brokerExpanded = false }
                            ) {
                                brokersList.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b) },
                                        onClick = {
                                            brokerSelected = b
                                            brokerExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Asset Type Dropdown
                    item {
                        ExposedDropdownMenuBox(
                            expanded = assetTypeExpanded,
                            onExpandedChange = { assetTypeExpanded = !assetTypeExpanded }
                        ) {
                            OutlinedTextField(
                                value = assetTypeSelected,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Activo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetTypeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = assetTypeExpanded,
                                onDismissRequest = { assetTypeExpanded = false }
                            ) {
                                assetTypesList.forEach { a ->
                                    DropdownMenuItem(
                                        text = { Text(a) },
                                        onClick = {
                                            assetTypeSelected = a
                                            assetTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Leverage Dropdown
                    item {
                        ExposedDropdownMenuBox(
                            expanded = leverageExpanded,
                            onExpandedChange = { leverageExpanded = !leverageExpanded }
                        ) {
                            OutlinedTextField(
                                value = if (leverageSelected == 1.0) "1x (Sin Apalancamiento)" else "${leverageSelected.toInt()}x Apalancado (Capital.com / Forex)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Apalancamiento (Leverage Multiplier)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = leverageExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = leverageExpanded,
                                onDismissRequest = { leverageExpanded = false }
                            ) {
                                leverageOptionsList.forEach { lev ->
                                    DropdownMenuItem(
                                        text = { Text(if (lev == 1.0) "1x (Sin Apalancamiento)" else "${lev.toInt()}x Multiplicador") },
                                        onClick = {
                                            leverageSelected = lev
                                            leverageExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sharesText,
                                onValueChange = { sharesText = it },
                                label = { Text("Títulos/Unidades") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = buyPriceText,
                                onValueChange = { buyPriceText = it },
                                label = { Text("Precio Compra ($)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = currentPriceText,
                                onValueChange = { currentPriceText = it },
                                label = { Text("Precio Actual ($)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = supportPriceText,
                                onValueChange = { supportPriceText = it },
                                label = { Text("Punto Soporte ($)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shares = sharesText.toDoubleOrNull() ?: 1.0
                        val buyPrice = buyPriceText.toDoubleOrNull() ?: 100.0
                        val curPrice = currentPriceText.toDoubleOrNull() ?: buyPrice
                        val supPrice = supportPriceText.toDoubleOrNull() ?: (buyPrice * 0.9)

                        if (symbolText.isNotEmpty()) {
                            onAddStock(
                                symbolText,
                                companyText.ifEmpty { symbolText },
                                shares,
                                buyPrice,
                                curPrice,
                                supPrice,
                                priorityText,
                                assetTypeSelected,
                                brokerSelected,
                                leverageSelected,
                                "GOOGLE: $symbolText"
                            )
                        }
                        showAddDialog = false
                    },
                    modifier = Modifier.testTag("save_stock_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Guardar Inversión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- SELL STOCK DIALOG ---
    stockToSell?.let { targetStock ->
        var sellSharesText by remember { mutableStateOf(targetStock.shares.toString()) }
        var sellPriceText by remember { mutableStateOf(targetStock.currentPrice.toString()) }

        val sharesToSell = sellSharesText.toDoubleOrNull() ?: targetStock.shares
        val sellPrice = sellPriceText.toDoubleOrNull() ?: targetStock.currentPrice
        val estimatedGain = (sellPrice - targetStock.avgBuyPrice) * sharesToSell * targetStock.leverage

        AlertDialog(
            onDismissRequest = { stockToSell = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sell, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vender / Cerrar Posición (${targetStock.symbol})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Registra la venta para calcular la ganancia/pérdida realizada y moverla al Historial de Ventas (${targetStock.broker}).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = sellSharesText,
                        onValueChange = { sellSharesText = it },
                        label = { Text("Títulos a Vender (Máx: ${targetStock.shares})") },
                        modifier = Modifier.fillMaxWidth().testTag("sell_shares_input")
                    )

                    OutlinedTextField(
                        value = sellPriceText,
                        onValueChange = { sellPriceText = it },
                        label = { Text("Precio de Venta Actual ($)") },
                        modifier = Modifier.fillMaxWidth().testTag("sell_price_input")
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (estimatedGain >= 0) EmeraldPrimary.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text("Ganancia/Pérdida Realizada Estimada:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "${if (estimatedGain >= 0) "+" else ""}${currencyFormat.format(estimatedGain)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (estimatedGain >= 0) EmeraldPrimary else ExpenseRed
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSellStock(targetStock, sharesToSell, sellPrice)
                        stockToSell = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("confirm_sell_btn")
                ) {
                    Text("Confirmar Venta")
                }
            },
            dismissButton = {
                TextButton(onClick = { stockToSell = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun StockItemRow(
    stock: StockPositionEntity,
    currencyFormat: NumberFormat,
    onSellClick: () -> Unit = {}
) {
    val totalVal = stock.shares * stock.currentPrice * stock.leverage
    val totalCost = stock.shares * stock.avgBuyPrice
    val gain = (stock.currentPrice - stock.avgBuyPrice) * stock.shares * stock.leverage

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Badges row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Broker Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SapphireSecondary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stock.broker,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SapphireSecondary
                        )
                    }

                    // Asset Type Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stock.assetType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPrimary
                        )
                    }

                    // Leverage Badge if > 1.0
                    if (stock.leverage > 1.0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFF9800).copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${stock.leverage.toInt()}x Apalancado",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Google Finance Live",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stock.symbol,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${stock.shares} un.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = stock.companyName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Soporte: ${currencyFormat.format(stock.targetSupportPrice)} • Compra: ${currencyFormat.format(stock.avgBuyPrice)}",
                        fontSize = 11.sp,
                        color = SapphireSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormat.format(stock.currentPrice),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${if (gain >= 0) "+" else ""}${currencyFormat.format(gain)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (gain >= 0) EmeraldPrimary else ExpenseRed
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onSellClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.Sell, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vender", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                    }
                }
            }
        }
    }
}

@Composable
fun SoldStockItemRow(
    soldStock: SoldStockEntity,
    currencyFormat: NumberFormat
) {
    val isGain = soldStock.realizedGainLoss >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SapphireSecondary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(soldStock.broker, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SapphireSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isGain) EmeraldPrimary.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(if (isGain) "VENTA CON GANANCIA" else "VENTA EN PÉRDIDA", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = if (isGain) EmeraldPrimary else ExpenseRed)
                    }
                }

                val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(java.util.Date(soldStock.saleDateMillis))
                Text(dateStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${soldStock.symbol} (${soldStock.sharesSold} un.)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = soldStock.companyName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Compra: ${currencyFormat.format(soldStock.avgBuyPrice)} ➔ Venta: ${currencyFormat.format(soldStock.sellPrice)}",
                        fontSize = 11.sp,
                        color = SapphireSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Ganancia Realizada", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${if (isGain) "+" else ""}${currencyFormat.format(soldStock.realizedGainLoss)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (isGain) EmeraldPrimary else ExpenseRed
                    )
                }
            }
        }
    }
}
