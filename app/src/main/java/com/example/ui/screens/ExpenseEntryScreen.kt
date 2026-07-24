package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ParsedExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SapphireSecondary

@Composable
fun ExpenseEntryScreen(
    draftExpense: ParsedExpense?,
    isAiLoading: Boolean,
    onVoiceDictate: (String) -> Unit,
    onOcrScan: (Bitmap) -> Unit,
    onSaveExpense: (Double, String, String, String, String) -> Unit,
    onClearDraft: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Voz, 1: OCR Ticket, 2: Manual

    // Voice simulation input
    var voiceText by remember { mutableStateOf("Gasté $420 pesos en gasolina en la estación Pemex") }
    var isListening by remember { mutableStateOf(false) }

    // Manual Form Inputs
    var amountText by remember { mutableStateOf(draftExpense?.amount?.toString() ?: "150.0") }
    var merchantText by remember { mutableStateOf(draftExpense?.merchant ?: "Starbucks") }
    var categoryText by remember { mutableStateOf(draftExpense?.category ?: "Restaurantes") }
    var noteText by remember { mutableStateOf(draftExpense?.note ?: "Café con el cliente") }

    // Synchronize draft expense updates
    if (draftExpense != null) {
        amountText = draftExpense.amount.toString()
        merchantText = draftExpense.merchant
        categoryText = draftExpense.category
        noteText = draftExpense.note
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Registro Rápido de Gastos",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = "Extrae y clasifica gastos automáticamente con Inteligencia Artificial",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Mode Switcher Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Dictado por Voz") },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null) },
                    modifier = Modifier.testTag("tab_voice")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Escaneo OCR") },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    modifier = Modifier.testTag("tab_ocr")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Manual") },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.testTag("tab_manual")
                )
            }
        }

        // 1. Dictado por Voz
        if (selectedTab == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Presiona el micrófono y dicta tu gasto",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Ejemplo: \"Ayer pagué 650 pesos de despensa en Soriana\"",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        val transition = rememberInfiniteTransition(label = "mic_pulse")
                        val scale by transition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = if (isListening) 1.25f else 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.2f)
                                )
                                .clickable {
                                    isListening = !isListening
                                    if (!isListening) {
                                        onVoiceDictate(voiceText)
                                    }
                                }
                                .testTag("voice_mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Dictar",
                                tint = if (isListening) MaterialTheme.colorScheme.onPrimary else EmeraldPrimary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = voiceText,
                            onValueChange = { voiceText = it },
                            label = { Text("Transcripción de Voz") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onVoiceDictate(voiceText) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("process_voice_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Procesando Voz con IA...")
                            } else {
                                Text("Analizar Dictado con IA")
                            }
                        }
                    }
                }
            }
        }

        // 2. Escaneo de Tickets (OCR)
        if (selectedTab == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Escaneo OCR de Ticket o Factura",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Toma una foto de tu ticket para extraer automáticamente el total y comercio",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    2.dp,
                                    SapphireSecondary,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = SapphireSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Visor de Cámara / Ticket OCR", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val dummyBitmap = createDummyTicketBitmap()
                                onOcrScan(dummyBitmap)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scan_ocr_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analizando Imagen de Ticket...")
                            } else {
                                Text("Capturar y Escanear Ticket")
                            }
                        }
                    }
                }
            }
        }

        // 3. Confirmación / Formulario Final
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Detalles de la Transacción",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Monto ($ MXN)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = merchantText,
                        onValueChange = { merchantText = it },
                        label = { Text("Comercio / Establecimiento") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_merchant_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = categoryText,
                        onValueChange = { categoryText = it },
                        label = { Text("Categoría") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_category_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Nota opcional") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val typeStr = when (selectedTab) {
                                0 -> "Voz"
                                1 -> "OCR"
                                else -> "Manual"
                            }
                            if (amt > 0) {
                                onSaveExpense(amt, categoryText, merchantText, noteText, typeStr)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_expense_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Gasto Automatizado", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Generate dummy ticket bitmap for OCR demonstration
fun createDummyTicketBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    canvas.drawColor(android.graphics.Color.WHITE)
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 24f

    canvas.drawText("SUPERMERCADO EXPRESS", 50f, 80f, paint)
    canvas.drawText("1x Despensa $890.50", 50f, 180f, paint)
    canvas.drawText("TOTAL: $890.50", 50f, 280f, paint)

    return bitmap
}
