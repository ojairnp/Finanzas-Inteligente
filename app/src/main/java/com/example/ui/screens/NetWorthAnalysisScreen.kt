package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AssetLiabilityEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SapphireSecondary
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.History
import com.example.data.local.AccountTransferEntity

@Composable
fun NetWorthAnalysisScreen(
    totalAssets: Double,
    totalLiabilities: Double,
    netWorth: Double,
    items: List<AssetLiabilityEntity>,
    accountTransfers: List<AccountTransferEntity> = emptyList(),
    onAddAssetLiability: (String, Double, String, String) -> Unit,
    onRecordTransfer: (sourceAccount: String, destinationAccount: String, amount: Double, note: String) -> Unit = { _, _, _, _ -> }
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    var showAddDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }

    var titleInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("ASSET") } // "ASSET" or "LIABILITY"
    var categoryInput by remember { mutableStateOf("Efectivo") }

    // Transfer inputs
    var sourceAccInput by remember { mutableStateOf("Efectivo GBM+ (Venta Acciones)") }
    var destAccInput by remember { mutableStateOf("Nu Cuenta Rendimiento (15% a la vista)") }
    var transferAmtInput by remember { mutableStateOf("") }
    var transferNoteInput by remember { mutableStateOf("Traspaso para rendimiento diario") }

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
            item {
                Text(
                    text = "Análisis de Patrimonio Neto & Trazabilidad",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = "Desglose de Activos, Liquidez en Brokers/Bancos, Pasivos y Transferencias entre Cuentas",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Overview Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Balance General Patrimonial",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Activos (Incl. Efectivo Brokers)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    currencyFormat.format(totalAssets),
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    fontSize = 18.sp
                                )
                            }

                            Column {
                                Text("Total Pasivos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    currencyFormat.format(totalLiabilities),
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Patrimonio Neto Libre",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                currencyFormat.format(netWorth),
                                fontWeight = FontWeight.ExtraBold,
                                color = SapphireSecondary,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showTransferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("open_transfer_dialog_btn")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Registrar Transferencia / Mover Liquidez entre Cuentas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Transfer Traceability Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SapphireSecondary.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null, tint = SapphireSecondary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Trazabilidad y Conservación del Patrimonio", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SapphireSecondary)
                            Text(
                                "Al vender una acción o transferir dinero entre tu débito, broker (GBM+, Capital.com) o cuentas de rendimiento a la vista (Nu, Cetes), el dinero PERMANECE en tu patrimonio como efectivo disponible sin registrar un falso gasto.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Section Activos
            item {
                Text(
                    text = "🟢 Activos & Efectivo en Plataformas (${items.filter { it.type == "ASSET" }.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                )
            }

            val assetsList = items.filter { it.type == "ASSET" }
            if (assetsList.isEmpty()) {
                item { Text("No hay activos registrados.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(assetsList) { asset ->
                    AssetLiabilityRow(item = asset, currencyFormat = currencyFormat, isAsset = true)
                }
            }

            // Section Pasivos
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔴 Pasivos y Deudas Tarjetas (${items.filter { it.type == "LIABILITY" }.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                )
            }

            val liabilitiesList = items.filter { it.type == "LIABILITY" }
            if (liabilitiesList.isEmpty()) {
                item { Text("No hay pasivos registrados.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(liabilitiesList) { liability ->
                    AssetLiabilityRow(item = liability, currencyFormat = currencyFormat, isAsset = false)
                }
            }

            // Section Historial de Transferencias
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = SapphireSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Historial de Transferencias y Trazabilidad de Liquidez (${accountTransfers.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }

            if (accountTransfers.isEmpty()) {
                item { Text("Aún no has registrado transferencias entre cuentas.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(accountTransfers) { transfer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(transfer.sourceAccount, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SapphireSecondary)
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = SapphireSecondary, modifier = Modifier.padding(horizontal = 4.dp).size(16.dp))
                                    Text(transfer.destinationAccount, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                                }

                                Text(currencyFormat.format(transfer.amount), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            if (transfer.note.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(transfer.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_asset_fab"),
            containerColor = EmeraldPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar Registro")
        }
    }

    if (showTransferDialog) {
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = SapphireSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Transferencia Entre Cuentas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Registra traspasos de efectivo entre tu banco, brokers (GBM, Capital), pago de tarjetas o cuentas con rendimiento a la vista (Nu 15%, Cetes). No altera tu patrimonio neto total.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = sourceAccInput,
                        onValueChange = { sourceAccInput = it },
                        label = { Text("Cuenta Origen (Ej: GBM+ Efectivo, BBVA Débito)") },
                        modifier = Modifier.fillMaxWidth().testTag("transfer_source_input")
                    )

                    OutlinedTextField(
                        value = destAccInput,
                        onValueChange = { destAccInput = it },
                        label = { Text("Cuenta Destino (Ej: Nu Rendimiento 15%, Pago TC BBVA)") },
                        modifier = Modifier.fillMaxWidth().testTag("transfer_dest_input")
                    )

                    OutlinedTextField(
                        value = transferAmtInput,
                        onValueChange = { transferAmtInput = it },
                        label = { Text("Monto a Transferir ($ MXN)") },
                        modifier = Modifier.fillMaxWidth().testTag("transfer_amount_input")
                    )

                    OutlinedTextField(
                        value = transferNoteInput,
                        onValueChange = { transferNoteInput = it },
                        label = { Text("Nota o Motivo (Ej: Rendimiento a la vista, Pago TC)") },
                        modifier = Modifier.fillMaxWidth().testTag("transfer_note_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = transferAmtInput.toDoubleOrNull() ?: 0.0
                        if (sourceAccInput.isNotEmpty() && destAccInput.isNotEmpty() && amt > 0) {
                            onRecordTransfer(sourceAccInput, destAccInput, amt, transferNoteInput)
                        }
                        showTransferDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary),
                    modifier = Modifier.testTag("confirm_transfer_btn")
                ) {
                    Text("Registrar Transferencia")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AssetLiabilityRow(
    item: AssetLiabilityEntity,
    currencyFormat: NumberFormat,
    isAsset: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isAsset) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isAsset) EmeraldPrimary else ExpenseRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = currencyFormat.format(item.amount),
                fontWeight = FontWeight.Bold,
                color = if (isAsset) EmeraldPrimary else ExpenseRed,
                fontSize = 14.sp
            )
        }
    }
}

