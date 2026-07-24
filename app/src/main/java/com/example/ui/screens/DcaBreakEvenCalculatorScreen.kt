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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SapphireSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DcaBreakEvenCalculatorScreen(
    onCalculateDca: (String, Double, Double) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "US"))

    var symbolText by remember { mutableStateOf("NVDA") }
    var currentSharesText by remember { mutableStateOf("15.0") }
    var currentAvgPriceText by remember { mutableStateOf("115.0") }
    var newSharesText by remember { mutableStateOf("5.0") }
    var newPriceText by remember { mutableStateOf("128.50") }

    val currentShares = currentSharesText.toDoubleOrNull() ?: 0.0
    val currentAvgPrice = currentAvgPriceText.toDoubleOrNull() ?: 0.0
    val newShares = newSharesText.toDoubleOrNull() ?: 0.0
    val newPrice = newPriceText.toDoubleOrNull() ?: 0.0

    val totalCostOld = currentShares * currentAvgPrice
    val totalCostNew = newShares * newPrice
    val combinedShares = currentShares + newShares
    val breakEvenPrice = if (combinedShares > 0) (totalCostOld + totalCostNew) / combinedShares else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Calculadora de Punto de Equilibrio (DCA)",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = "Calcula tu precio promedio de compra acumulado al realizar nuevas entradas en el mercado",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Calculation Result Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dca_result_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "NUEVO PRECIO PROMEDIO DE EQUILIBRIO ($symbolText)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currencyFormat.format(breakEvenPrice),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Total de acciones acumuladas: $combinedShares títulos\nInversión total acumulada: ${currencyFormat.format(totalCostOld + totalCostNew)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Form Inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Posición Actual",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SapphireSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = symbolText,
                        onValueChange = { symbolText = it },
                        label = { Text("Ticker (ej. NVDA, NFLX)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dca_symbol_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = currentSharesText,
                            onValueChange = { currentSharesText = it },
                            label = { Text("Acciones Actuales") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = currentAvgPriceText,
                            onValueChange = { currentAvgPriceText = it },
                            label = { Text("Precio Prom. Actual ($)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Nueva Compra a Realizar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newSharesText,
                            onValueChange = { newSharesText = it },
                            label = { Text("Nuevas Acciones") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dca_new_shares_input")
                        )

                        OutlinedTextField(
                            value = newPriceText,
                            onValueChange = { newPriceText = it },
                            label = { Text("Precio Nuevas ($)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dca_new_price_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onCalculateDca(symbolText, newShares, newPrice)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calculate_dca_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calcular y Guardar en Portafolio", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
