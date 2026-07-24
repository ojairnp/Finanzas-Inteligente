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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CreditCardEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SapphireSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreditCardsScreen(
    cards: List<CreditCardEntity>,
    onAddCard: (String, String, Double, Double, Int, Int) -> Unit,
    onNavigateTo50Days: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    var showAddDialog by remember { mutableStateOf(false) }

    var cardName by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var cutOffText by remember { mutableStateOf("15") }
    var dueText by remember { mutableStateOf("5") }

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
                    text = "Deudas y Créditos Centralizados",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = "Control de límites de crédito, fechas de corte y vencimientos de pago",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Strategy Banner Link
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("strategy_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SapphireSecondary.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💡 Estrategia de los 50 Días",
                                fontWeight = FontWeight.Bold,
                                color = SapphireSecondary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Descubre cuál tarjeta te otorga hasta 50 días de financiamiento 0% interés hoy.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = onNavigateTo50Days,
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("go_to_50_days_button")
                        ) {
                            Text("Ver Algoritmo", fontSize = 12.sp)
                        }
                    }
                }
            }

            items(cards) { card ->
                CreditCardItem(card = card, currencyFormat = currencyFormat)
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_credit_card_fab"),
            containerColor = SapphireSecondary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar Tarjeta")
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva Tarjeta / Crédito") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("Nombre de la Tarjeta") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_name_input")
                    )
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Banco Emisor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it },
                        label = { Text("Límite de Crédito ($ MXN)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_limit_input")
                    )
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = { Text("Saldo Utilizado Actualmente") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cutOffText,
                            onValueChange = { cutOffText = it },
                            label = { Text("Día Corte (1-31)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dueText,
                            onValueChange = { dueText = it },
                            label = { Text("Día Pago (1-31)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = limitText.toDoubleOrNull() ?: 50000.0
                        val balance = balanceText.toDoubleOrNull() ?: 0.0
                        val cutOff = cutOffText.toIntOrNull() ?: 15
                        val due = dueText.toIntOrNull() ?: 5

                        if (cardName.isNotEmpty()) {
                            onAddCard(cardName, bankName, limit, balance, cutOff, due)
                        }
                        showAddDialog = false
                    },
                    modifier = Modifier.testTag("save_credit_card_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary)
                ) {
                    Text("Guardar Tarjeta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CreditCardItem(
    card: CreditCardEntity,
    currencyFormat: NumberFormat
) {
    val usageRatio = (card.currentBalance / card.creditLimit).coerceIn(0.0, 1.0).toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SapphireSecondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = SapphireSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = card.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${card.bank} • ${card.cardType}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = currencyFormat.format(card.currentBalance),
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { usageRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (usageRatio > 0.7f) ExpenseRed else SapphireSecondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Límite: ${currencyFormat.format(card.creditLimit)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Día de Corte: ${card.cutOffDay} | Día Límite Pago: ${card.paymentDueDay}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
