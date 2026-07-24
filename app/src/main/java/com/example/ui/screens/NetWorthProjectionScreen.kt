package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SapphireSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun NetWorthProjectionScreen(
    currentNetWorth: Double,
    onUpdateProjection: (Int, Double, Double, Double, String) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    var monthsAhead by remember { mutableStateOf(6f) }
    var aguinaldoText by remember { mutableStateOf("28000") }
    var utilidadesText by remember { mutableStateOf("18000") }
    var extraSalesText by remember { mutableStateOf("12000") }
    var payFrequency by remember { mutableStateOf("Catorcenal") }

    val months = monthsAhead.toInt()
    val aguinaldo = aguinaldoText.toDoubleOrNull() ?: 0.0
    val utilidades = utilidadesText.toDoubleOrNull() ?: 0.0
    val extraSales = extraSalesText.toDoubleOrNull() ?: 0.0

    // Monthly baseline savings estimate
    val monthlyBaseSavings = 15000.0
    val projectedValue = currentNetWorth + (monthlyBaseSavings * months) + aguinaldo + utilidades + extraSales

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Proyección de Patrimonio Neto",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = "Simulación a futuro integrando ingresos extraordinarios (aguinaldo, utilidades, sueldos catorcenales)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Projection Value Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("projection_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "PATRIMONIO ESTIMADO A $months MESES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currencyFormat.format(projectedValue),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        )
                    )

                    Text(
                        text = "Crecimiento proyectado: +${currencyFormat.format(projectedValue - currentNetWorth)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SapphireSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Line Chart Simulation Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val path = Path()
                            path.moveTo(0f, h * 0.8f)
                            path.cubicTo(
                                w * 0.3f, h * 0.7f,
                                w * 0.6f, h * 0.4f,
                                w, h * 0.15f
                            )

                            drawPath(
                                path = path,
                                color = EmeraldPrimary,
                                style = Stroke(width = 6f)
                            )

                            drawCircle(
                                color = EmeraldPrimary,
                                radius = 10f,
                                center = Offset(w, h * 0.15f)
                            )
                        }
                    }
                }
            }
        }

        // Configuration Inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Parámetros de Simulación",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Plazo de estimación: $months Meses",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Slider(
                        value = monthsAhead,
                        onValueChange = { monthsAhead = it },
                        valueRange = 1f..12f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary
                        ),
                        modifier = Modifier.testTag("months_slider")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = aguinaldoText,
                        onValueChange = { aguinaldoText = it },
                        label = { Text("Aguinaldo Estimado ($ MXN)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("aguinaldo_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = utilidadesText,
                        onValueChange = { utilidadesText = it },
                        label = { Text("Reparto de Utilidades (PTU) ($ MXN)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("utilidades_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = extraSalesText,
                        onValueChange = { extraSalesText = it },
                        label = { Text("Ventas / Comisiones Extra ($ MXN)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("extra_sales_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onUpdateProjection(months, aguinaldo, utilidades, extraSales, payFrequency)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("update_projection_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Recalcular Proyección Patrimonial", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
